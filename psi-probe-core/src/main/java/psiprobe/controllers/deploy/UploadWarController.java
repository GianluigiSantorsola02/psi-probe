package psiprobe.controllers.deploy;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

/**
 * Uploads and installs web application from a .WAR.
 */
@Controller
public class UploadWarController extends AbstractTomcatContainerController {

  private static final Logger log4 = LoggerFactory.getLogger(UploadWarController.class);

  @GetMapping(path = "/adm/war.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
      FileItemFactory factory =
              new DiskFileItemFactory(1048000, new File(System.getProperty("java.io.tmpdir")));
      ServletFileUpload upload = new ServletFileUpload(factory);
      upload.setSizeMax(-1);
      upload.setHeaderEncoding(StandardCharsets.UTF_8.name());

      List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(request));
      for (FileItem fi : fileItems) {
        String fileName = fi.getName();
        if (fileName != null) {
          String cleanFileName = FilenameUtils.getName(fileName).replaceAll("[^A-Za-z0-9\\-]", "_");
          File uploadedFile = new File(getSafeTempDir(), cleanFileName);

          // Validate the file path to prevent directory traversal
          if (isValidFilePath(uploadedFile)) {
            fi.write(uploadedFile);
            handleWarFileItem(fi);
          } else {
            throw new DirectoryTraversalException(
                    "Directory traversal attempt: " + uploadedFile.getCanonicalPath());
          }
        }
      }
    } catch (Exception e) {
      handleFileUploadException(e, request, tmpWar);
    } finally {
      cleanupAfterFileUpload(tmpWar, request);
    }
  }

  private boolean isValidFilePath(File file) throws Exception {
    // Ensure that the canonical path of the file is under the safe directory
    String safeTempDir = getSafeTempDir();
    return file.getCanonicalPath().startsWith(safeTempDir);
  }

  private void handleWarFileItem(FileItem fi) {
    if (!fi.isFormField()) {
      String cleanFileName = FilenameUtils.getName(fi.getName()).replaceAll("[^A-Za-z0-9\\-]", "_");
      File tmpWar = new File(getSafeTempDir(), cleanFileName);

      try {
        // Ensure that the canonical path of the temporary file is under the safe directory
        if (tmpWar.getCanonicalPath().startsWith(getSafeTempDir())) {
          writeToFile(fi, tmpWar);
        } else {
          throw new DirectoryTraversalException(
                  "Directory traversal attempt: " + tmpWar.getCanonicalPath());
        }
      } catch (Exception e) {
        log4.error("Error handling war file item", e);
        // Handle the exception appropriately, e.g., log or throw a custom exception
      }
    }
  }

  // Helper method to get the safe temporary directory
  private String getSafeTempDir() {
    // Customize this path according to your application's requirements
    String path = "/path/to/safe/temporary/directory/";
    return path;
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

    String errMsg = request.getAttribute(ERROR_MESSAGE_KEY) != null
            ? (String) request.getAttribute(ERROR_MESSAGE_KEY)
            : null;
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
