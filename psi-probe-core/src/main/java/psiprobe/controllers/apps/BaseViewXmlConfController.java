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
package psiprobe.controllers.apps;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.web.servlet.ModelAndView;

import psiprobe.Utils;
import psiprobe.controllers.AbstractContextHandlerController;

/**
 * Displays a deployment descriptor (web.xml) or a context descriptor (context.xml) of a web
 * application
 */
public abstract class BaseViewXmlConfController extends AbstractContextHandlerController {

  /** The Constant logger. */
  private static final Logger theLogger = LoggerFactory.getLogger(BaseViewXmlConfController.class);

  /** The Constant TARGET_WEB_XML. */
  private static final String TARGET_WEB_XML = "web.xml";

  /** The Constant TARGET_CONTEXT_XML. */
  private static final String TARGET_CONTEXT_XML = "context.xml";

  /** Type of a file to be displayed. */
  private String displayTarget;

  /** Url that will be used in the view to download the file. */
  private String downloadUrl;

  /**
   * Gets the display target.
   *
   * @return the display target
   */
  public String getDisplayTarget() {
    return displayTarget;
  }

  /**
   * Sets the display target.
   *
   * @param displayTarget the new display target
   */
  public void setDisplayTarget(String displayTarget) {
    this.displayTarget = displayTarget;
  }

  /**
   * Sets the download url.
   *
   * @param downloadUrl the new download url
   */
  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
  }

  @Override
  public ModelAndView handleContext(String contextName, Context context,
                                    HttpServletRequest request, HttpServletResponse response) throws DisplayTargetException, UnknownDisplayTargetException, IOException {

    if (displayTarget == null) {
      throw new DisplayTargetException("Display target is not set for " + getClass().getName());
    }

    ModelAndView mv = new ModelAndView(getViewName());
    File xmlFile;

    if (TARGET_WEB_XML.equals(displayTarget)) {
      xmlFile = handleWebXmlDisplayTarget(context, mv);
    } else if (TARGET_CONTEXT_XML.equals(displayTarget)) {
      xmlFile = handleContextXmlDisplayTarget(context, mv);
    } else {
      throw new UnknownDisplayTargetException("Unknown display target " + getDisplayTarget());
    }

    mv.addObject("displayTarget", displayTarget);
    mv.addObject("downloadUrl", downloadUrl);

    if (xmlFile != null) {
      handleXmlFile(xmlFile, mv, contextName);
    } else {
      theLogger.debug("Cannot determine path to {} file of {} application.", getDisplayTarget(), contextName);
    }

    return mv;
  }

  private File handleWebXmlDisplayTarget(Context context, ModelAndView mv) {
    ServletContext sctx = context.getServletContext();
    String xmlPath = sctx.getRealPath("/WEB-INF/web.xml");
    File xmlFile = new File(xmlPath);
    mv.addObject("fileDesc", 104);

    MessageSourceAccessor accessor = getMessageSourceAccessor();
    if (accessor != null) {
      String message = accessor.getMessage("probe.src.app.viewxmlconf.webxml.desc");
      mv.addObject("message", message);
    } else {
      theLogger.debug("MessageSourceAccessor is null");
    }

    return xmlFile;
  }

  private File handleContextXmlDisplayTarget(Context context, ModelAndView mv) {
    File xmlFile = getContainerWrapper().getTomcatContainer().getConfigFile(context);
    String message = "";
    MessageSourceAccessor accessor = getMessageSourceAccessor();
    if (accessor != null) {
      message = accessor.getMessage("probe.src.app.viewxmlconf.contextxml.desc");
    }
    mv.addObject("fileDesc", message);

    return xmlFile;
  }

  private void handleXmlFile(File xmlFile, ModelAndView mv, String contextName) throws IOException {
    mv.addObject("fileName", xmlFile.getName());
    if (xmlFile.exists()) {
      try (InputStream fis = Files.newInputStream(xmlFile.toPath())) {
        String encoding = Charset.defaultCharset().displayName();
        mv.addObject("content", Utils.highlightStream(TARGET_WEB_XML, fis, "xml", encoding == null ? "ISO-8859-1" : encoding));
      }
    } else {
      theLogger.debug("File {} of {} application does not exists.", xmlFile.getPath(), contextName);
    }
  }

  @Value("context.xml")
  public abstract void setDownloadTarget(String downloadTarget);

  protected void setExtendedInfo(boolean extendedInfo) {

  }

  protected void setCollectionPeriod(long l) {

  }

  public static class DisplayTargetException extends Throwable {
    public DisplayTargetException(String s) {
      super(s);
    }
  }

  public static class UnknownDisplayTargetException extends Throwable {
    public UnknownDisplayTargetException(String s) {
      super(s);
    }
  }
}
