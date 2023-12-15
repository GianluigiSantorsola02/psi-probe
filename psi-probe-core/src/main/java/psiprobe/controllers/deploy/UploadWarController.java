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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceView;
import psiprobe.controllers.AbstractTomcatContainerController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;


/**
 * Uploads and installs web application from a .WAR.
 */
@Controller
public class UploadWarController extends AbstractTomcatContainerController {

  /** The Constant logger. */
  private static final Logger log4 = LoggerFactory.getLogger(UploadWarController.class);

  @GetMapping(path = "/adm/war.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
    if (FileUploadBase.isMultipartContent(new ServletRequestContext(request))) {
      handleMultipartRequest(request);
    }
    return new ModelAndView(new InternalResourceView(getViewName()));
  }

  private void handleMultipartRequest(HttpServletRequest request) {
    File tmpWar = null;

    try {
      FileItemFactory factory = new DiskFileItemFactory(1048000, new File(System.getProperty("java.io.tmpdir")));
      ServletFileUpload upload = new ServletFileUpload(factory);
      upload.setSizeMax(-1);
      upload.setHeaderEncoding(StandardCharsets.UTF_8.name());

      List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request));
      for (FileItem fi : fileItems) {
        String fileName = fi.getName();
        if (fileName != null) {
          String cleanFileName = FilenameUtils.getName(fileName).replaceAll("[^A-Za-z0-9\\-]", "_");
          File uploadedFile = new File("/path/to/upload/directory/" + cleanFileName);
          if (uploadedFile.getCanonicalPath().startsWith("/path/to/upload/directory/")) {
            fi.write(uploadedFile);
          } else {
            throw new   DirectoryTraversalException("Directory traversal attempt: " + uploadedFile.getCanonicalPath());
          }
        }
      }
    } catch (Exception e) {
      handleFileUploadException(e, request, tmpWar);
    } finally {
      cleanupAfterFileUpload(tmpWar, request);
    }
  }

  private void processFileItem(FileItem fi) {
    if (!fi.isFormField()) {
      handleWarFileItem(fi);
    } else {
      handleFormField(fi);
    }
  }

  private void handleWarFileItem(FileItem fi) {
    if (fi.getName() != null && !fi.getName().isEmpty()) {
      File tmpWar = new File(System.getProperty("java.io.tmpdir"), FilenameUtils.getName(fi.getName()));
      writeToFile(fi, tmpWar);
      processFileItem(fi);
    }
  }

  private void handleFormField(FileItem fi) {
    if ("context".equals(fi.getFieldName())) {
      fi.getString();
    } else if ("update".equals(fi.getFieldName()) && "yes".equals(fi.getString())) {
      fi.getFieldName();
    } else if ("compile".equals(fi.getFieldName()) && "yes".equals(fi.getString())) {
      fi.getFieldName();
    } else if ("discard".equals(fi.getFieldName()) && "yes".equals(fi.getString())) {
      fi.getFieldName();
    }
  }

  private void writeToFile(FileItem fi, File tmpWar) {
    try {
      fi.write(tmpWar);
    } catch (Exception e) {
      log4.error("Could not write to temporary war file", e);
    }
  }

  private void handleFileUploadException(Exception e, HttpServletRequest request, File tmpWar) {
    log4.error("Could not process file upload", e);
    String errorMessage = Objects.requireNonNull(getMessageSourceAccessor())
            .getMessage("probe.src.deploy.war.uploadfailure", new Object[]{e.getMessage()});
    request.setAttribute(ERROR_MESSAGE_KEY, errorMessage);

    if (tmpWar != null && tmpWar.exists() && !tmpWar.delete()) {
      log4.error("Unable to delete temp war file");
    }
  }

  private void cleanupAfterFileUpload(File tmpWar, HttpServletRequest request) {
    if (tmpWar != null && tmpWar.exists() && !tmpWar.delete()) {
      logger.error("Unable to delete temp war file");
    }

    String errMsg = request.getAttribute(ERROR_MESSAGE_KEY) != null ? (String) request.getAttribute(ERROR_MESSAGE_KEY) : null;
    if (errMsg != null) {
      request.setAttribute(ERROR_MESSAGE_KEY, errMsg);
    }
  }

// Other methods like deployWar, resetStats, etc. can be extracted for further readability.

  @Value("/adm/deploy.htm")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

  private static final String ERROR_MESSAGE_KEY = "errorMessage";
}
