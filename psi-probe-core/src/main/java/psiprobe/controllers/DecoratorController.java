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
package psiprobe.controllers;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import psiprobe.PostParameterizableViewController;
import psiprobe.Utils;

/**
 * The Class DecoratorController.
 */
@Controller
public class DecoratorController extends PostParameterizableViewController {

  /** The messages basename. */
  private String messagesBasename;

  /**
   * Gets the messages basename.
   *
   * @return the messages basename
   */
  public String getMessagesBasename() {
    return messagesBasename;
  }

  /**
   * Sets the messages basename.
   *
   * @param messagesBasename the new messages basename
   */
  @Value("/WEB-INF/messages")
  public void setMessagesBasename(String messagesBasename) {
    this.messagesBasename = messagesBasename;
  }

  @RequestMapping(path = "/decorator.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    try {
      request.setAttribute("hostname", InetAddress.getLocalHost().getHostName());
    } catch (UnknownHostException e) {
      request.setAttribute("hostname", "unknown");
      logger.trace("", e);
    }


    if (getApplicationContext() != null) {
      DataHandler properties = (DataHandler) getApplicationContext().getBean("version");
      Properties data = (Properties) getApplicationContext().getBean("version");
      if (data != null) {
        request.setAttribute("version", data.getProperty("probe.version"));
      } else {

        logger.error("Error: 'version' bean is null");
      }
    } else {
      logger.error("ApplicationContext is null. Cannot retrieve the 'version' bean");
      return null;
    }

    String lang = "en";
    if (getServletContext() != null) {
      String attributeName = "attributeName";
      Object attributeValue = getServletContext().getAttribute(attributeName);
    } else {
      logger.error("ServletContext is null. Cannot retrieve the servlet context");
    }

    if (getServletContext() != null) {
      for (String fileName : getMessageFileNamesForLocale(request.getLocale())) {
        if (getServletContext().getResource(fileName + ".properties") != null) {
          lang = fileName.substring(messagesBasename.length() + 1);
          break;
        }
      }
    } else {
      logger.error("ServletContext is null. Cannot retrieve the servlet context");
    }
    request.setAttribute("lang", lang);

    return super.handleRequestInternal(request, response);
  }

  /**
   * Gets the message file names for locale.
   *
   * @param locale the locale
   *
   * @return the message file names for locale
   */
  private List<String> getMessageFileNamesForLocale(Locale locale) {
    return Utils.getNamesForLocale(messagesBasename, locale);
  }

  @Value("decorators/probe")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
