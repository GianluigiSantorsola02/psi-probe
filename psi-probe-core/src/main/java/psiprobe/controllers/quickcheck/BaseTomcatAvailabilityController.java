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
package psiprobe.controllers.quickcheck;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.springframework.web.servlet.ModelAndView;

import psiprobe.controllers.AbstractTomcatContainerController;
import psiprobe.model.ApplicationResource;
import psiprobe.model.DataSourceInfo;
import psiprobe.model.TomcatTestReport;

/**
 * "Quick check" base controller.
 */
public class BaseTomcatAvailabilityController extends AbstractTomcatContainerController {


  @Override
  public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
    final long start = System.currentTimeMillis();
    TomcatTestReport tomcatTestReport = new TomcatTestReport();

    checkDatasourceStatus(tomcatTestReport);
    performMemoryTest(tomcatTestReport);
    performFileTest(tomcatTestReport);

    tomcatTestReport.setTestDuration(System.currentTimeMillis() - start);
    tomcatTestReport.setMaxServiceTime(0);

    return new ModelAndView(getViewName(), "testReport", tomcatTestReport);
  }

  private void checkDatasourceStatus(TomcatTestReport tomcatTestReport) throws NamingException {
    boolean allContextsAvailable = true;

    if (getContainerWrapper().getResourceResolver().supportsPrivateResources()) {
      for (Context appContext : getContainerWrapper().getTomcatContainer().findContexts()) {
        allContextsAvailable = allContextsAvailable && getContainerWrapper().getTomcatContainer().getAvailable(appContext);

        List<ApplicationResource> applicationResources = getContainerWrapper().getResourceResolver()
                .getApplicationResources(appContext, getContainerWrapper());

        updateDatasourceInfo(tomcatTestReport, applicationResources);
      }

      tomcatTestReport.setWebappAvailabilityTest(allContextsAvailable ? TomcatTestReport.TEST_PASSED : TomcatTestReport.TEST_FAILED);
    } else {
      List<ApplicationResource> resources = getContainerWrapper().getResourceResolver().getApplicationResources();
      updateDatasourceInfo(tomcatTestReport, resources);
    }

    tomcatTestReport.setDatasourceTest(TomcatTestReport.TEST_PASSED);
  }

  private void updateDatasourceInfo(TomcatTestReport tomcatTestReport, List<ApplicationResource> resources) {
    for (ApplicationResource resource : resources) {
      DataSourceInfo dsi = resource.getDataSourceInfo();
      if (dsi != null && dsi.getBusyScore() > tomcatTestReport.getDatasourceUsageScore()) {
        tomcatTestReport.setDatasourceUsageScore(dsi.getBusyScore());
        tomcatTestReport.setDataSourceName(resource.getName());
      }
    }
  }

  private void performMemoryTest(TomcatTestReport tomcatTestReport) {
    String word = "hello";
    int count = TomcatTestReport.DEFAULT_MEMORY_SIZE / word.length();

    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      for (; count > 0; count--) {
        bos.write(word.getBytes(StandardCharsets.UTF_8));
      }
      tomcatTestReport.setMemoryTest(TomcatTestReport.TEST_PASSED);
    } catch (IOException e) {
      tomcatTestReport.setMemoryTest(TomcatTestReport.TEST_FAILED);
      logger.trace("", e);
    }
  }

  private void performFileTest(TomcatTestReport tomcatTestReport) {
// Validate and sanitize the tmpDir path
    File tmpDir = getValidatedTmpDir();

// Ensure that the resolved canonical path is still under the system tmpdir directory
    try {
      File canonicalTmpDir = tmpDir.getCanonicalFile();
      File systemTmpDir = new File(System.getProperty("java.io.tmpdir")).getCanonicalFile();

      if (!canonicalTmpDir.toPath().startsWith(systemTmpDir.toPath())) {
        throw new ClassCastException("Potential directory traversal attempt");
      }
    } catch (IOException e) {
      // Handle the exception as needed
      e.printStackTrace();
    }
    int fileCount = tomcatTestReport.getDefaultFileCount();
    List<File> files = new ArrayList<>();
    List<OutputStream> fileStreams = new ArrayList<>();

    try {
      for (; fileCount > 0; fileCount--) {
        File file = new File(tmpDir, "tctest_" + fileCount);
        try (OutputStream fos = Files.newOutputStream(file.toPath())) {
          files.add(file);
          fileStreams.add(fos);
          fos.write("this is a test".getBytes(StandardCharsets.UTF_8));
        }
      }
      tomcatTestReport.setFileTest(TomcatTestReport.TEST_PASSED);
    } catch (IOException e) {
      tomcatTestReport.setFileTest(TomcatTestReport.TEST_FAILED);
      logger.trace("", e);
    } finally {
      closeFileStreams(fileStreams);
      deleteFiles(files);
    }
  }

  private File getValidatedTmpDir() {
    String tmpDirPath = System.getProperty("java.io.tmpdir");

    // Perform validation and sanitization on tmpDirPath as needed
    String sanitizedTmpDirPath = sanitizePath(tmpDirPath);

    return new File(sanitizedTmpDirPath);
  }

  private String sanitizePath(String path) {
    if (path == null || path.isEmpty()) {
      return "";
    }
    return path.replaceAll("[^A-Za-z0-9]", "_");
  }

  private void closeFileStreams(List<OutputStream> fileStreams) {
    for (OutputStream fileStream : fileStreams) {
      try {
        fileStream.close();
      } catch (IOException e) {
        logger.trace("", e);
      }
    }
  }

  private void deleteFiles(List<File> files) {
    for (File file : files) {
      try {
        Files.delete(file.toPath());
      } catch (IOException e) {
        logger.trace("", e);
      }
    }
  }

}
