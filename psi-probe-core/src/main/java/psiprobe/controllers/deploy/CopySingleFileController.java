/*
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */
package psiprobe.controllers.deploy;

import com.google.common.base.Strings;
import org.apache.catalina.Context;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileItemFactory;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceView;
import psiprobe.controllers.AbstractTomcatContainerController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Lets a user copy a single file to a deployed context.
 */
@Controller
public class CopySingleFileController extends AbstractTomcatContainerController {

  /** The Constant logger. */
  private static final Logger logg = LoggerFactory.getLogger(CopySingleFileController.class);

  @GetMapping(path = "/adm/deployfile.htm")
  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
    List<Context> apps = getContexts();
    List<Map<String, String>> applications = getApplications(apps);
    request.setAttribute("apps", applications);

    if (isMultipartContent(request)) {
      handleMultipartRequest(request);
    }

    return new ModelAndView(new InternalResourceView(getViewName()));
  }

  private List<Context> getContexts() {
    try {
      return getContainerWrapper().getTomcatContainer().findContexts();
    } catch (NullPointerException ex) {
      throw new IllegalStateException("No container found for your server: " +
              Objects.requireNonNull(getServletContext()).getServerInfo(), ex);
    }
  }

  private List<Map<String, String>> getApplications(List<Context> apps) {
    List<Map<String, String>> applications = new ArrayList<>();
    for (Context appContext : apps) {
      if (!Strings.isNullOrEmpty(appContext.getName())) {
        Map<String, String> app = new HashMap<>();
        app.put("value", appContext.getName());
        app.put("label", appContext.getName());
        applications.add(app);
      }
    }
    return applications;
  }

  private void processFileItems(List<FileItem> fileItems) throws Exception {
    String where = null;
    File tmpFile = null;

    for (FileItem fi : fileItems) {
      if (!fi.isFormField()) {
        if (fi.getName() != null && !fi.getName().isEmpty()) {
          // Validate and sanitize the file name
          String sanitizedFileName = sanitizeFileName(FilenameUtils.getName(fi.getName()));
          // Construct the safe temporary file path
          File tmpDir = new File(System.getProperty("java.io.tmpdir"));
          tmpFile = new File(tmpDir, sanitizedFileName);
          fi.write(tmpFile);
        }
      } else if ("where".equals(fi.getFieldName())) {
        where = fi.getString();
      }
    }

    if (tmpFile != null) {
      assert where != null;
      // Validate and sanitize the 'where' path
      String sanitizedPath = sanitizePath(where);
      File destinationFile = new File(sanitizedPath);

      // Ensure that the resolved canonical path is still under a safe directory
      File canonicalDestinationFile = destinationFile.getCanonicalFile();
      File safeDirectory = getSafeDirectory(); // Define a method to obtain a safe directory

      if (!canonicalDestinationFile.toPath().startsWith(safeDirectory.toPath())) {
        throw new DirectoryTraversalException("Potential directory traversal attempt");
      }

      // Perform the file copy operation
      FileUtils.copyFile(tmpFile, destinationFile);
    }
  }

  private String sanitizeFileName(String fileName) {
    if (fileName == null || fileName.isEmpty()) {
      return "";
    }
    return fileName.replaceAll("[^A-Za-z0-9]", "_");
  }

  private File getSafeDirectory() {
    // Customize this path according to your application's requirements
    return new File("/path/to/safe/directory");
  }

  private String sanitizePath(String path) {
    if (path == null || path.isEmpty()) {
      return "";
    }
    return path.replaceAll("[^A-Za-z0-9]", "_");
  }

  private void handleMultipartRequest(HttpServletRequest request) {
    File tmpFile = new File("");
    String contextName = null;
    String where = null;

    FileItemFactory factory =
            new DiskFileItemFactory(1048000, new File(System.getProperty("java.io.tmpdir")));
    ServletFileUpload upload = new ServletFileUpload(factory);
    upload.setSizeMax(-1);
    upload.setHeaderEncoding(StandardCharsets.UTF_8.name());

    try {
      List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request));
      processFileItems(fileItems);

    } catch (Exception e) {
      handleFileUploadException(e, request, tmpFile);
    }

    processUploadedFile(tmpFile, contextName, where, request);
  }


  private static final String ERROR_MESSAGE = "errorMessage";

  private void handleFileUploadException(Exception e, HttpServletRequest request, File tmpFile) {
    logg.error("Could not process file upload", e);
    request.setAttribute(ERROR_MESSAGE, Objects.requireNonNull(getMessageSourceAccessor())
            .getMessage("probe.src.deploy.file.uploadfailure", new Object[] {e.getMessage()}));
    if (tmpFile != null && tmpFile.exists() && !tmpFile.delete()) {
      logg.error("Unable to delete temp upload file");
    }
  }

  private void processDestFile(File destFile, File tmpFile, HttpServletRequest request) {
    if (destFile.exists()) {
      if (!destFile.getAbsolutePath().contains("..")) {
        try {
          FileUtils.copyFileToDirectory(tmpFile, destFile);

          request.setAttribute("successFile", Boolean.TRUE);
          Authentication auth = SecurityContextHolder.getContext().getAuthentication();
          String name = auth.getName();
          MessageSourceAccessor messageSourceAccessor = getMessageSourceAccessor();
          if (messageSourceAccessor != null) {
            String message = messageSourceAccessor.getMessage("probe.src.log.deploy", name);
            logg.info(message);
          }
        } catch (IOException e) {
          logger.error("An error occurred while copying the file to the destination directory", e);
        }
      } else {
        request.setAttribute(ERROR_MESSAGE, Objects.requireNonNull(getMessageSourceAccessor())
                .getMessage("probe.src.dest.inaccessible", new Object[] {destFile.getAbsolutePath()}));
      }
    }
  }

  private void processUploadedFile(File tmpFile, String contextName, String where,
                                   HttpServletRequest request) {
    String errMsg = null;

    try {
      if (!Strings.isNullOrEmpty(tmpFile.getName())) {
        contextName = getContainerWrapper().getTomcatContainer().formatContextName(contextName);

        String visibleContextName = "".equals(contextName) ? "/" : contextName;
        request.setAttribute("contextName", visibleContextName);

        if (getContainerWrapper().getTomcatContainer().findContext(contextName) != null) {
          File destFile = new File(getContainerWrapper().getTomcatContainer().getAppBase(),
                  contextName + where);

          if (destFile.exists()) {
            processDestFile(destFile, tmpFile, request);
          } else {
            errMsg = Objects.requireNonNull(getMessageSourceAccessor())
                    .getMessage("probe.src.deploy.file.notPath");
          }
        } else {
          errMsg = Objects.requireNonNull(getMessageSourceAccessor())
                  .getMessage("probe.src.deploy.file.notExists", new Object[] {visibleContextName});
        }
      } else {
        errMsg = Objects.requireNonNull(getMessageSourceAccessor())
                .getMessage("probe.src.deploy.file.notFile.failure");
      }
    } finally {
      if (errMsg != null) {
        request.setAttribute(ERROR_MESSAGE, errMsg);
      }
      if (!tmpFile.delete()) {
        logg.error("Unable to delete temp upload file");
      }
    }
  }

  private boolean isMultipartContent(HttpServletRequest request) {
    return FileUploadBase.isMultipartContent(new ServletRequestContext(request));
  }

  @Value("/adm/deploy.htm")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
