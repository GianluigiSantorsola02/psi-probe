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
package psiprobe.controllers.logs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import psiprobe.beans.ApplicationCreationException;
import psiprobe.beans.LogResolverBean;
import psiprobe.tools.logging.LogDestination;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The Class AbstractLogHandlerController.
 */
public abstract class AbstractLogHandlerController extends ParameterizableViewController {

  /** The Constant logger. */
  private static final Logger log15 = LoggerFactory.getLogger(AbstractLogHandlerController.class);

  /** The log resolver. */
  private final LogResolverBean logResolver;

  protected AbstractLogHandlerController(LogResolverBean logResolver) {
    this.logResolver = logResolver;
  }

  /**
   * Gets the log resolver.
   *
   * @return the log resolver
   */
  public LogResolverBean getLogResolver() {
    return logResolver;
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                               HttpServletResponse response) throws Exception {

    String logType = ServletRequestUtils.getStringParameter(request, "logType");
    String webapp = ServletRequestUtils.getStringParameter(request, "webapp");
    boolean context = ServletRequestUtils.getBooleanParameter(request, "context", false);
    boolean root = ServletRequestUtils.getBooleanParameter(request, "root", false);
    String logName = ServletRequestUtils.getStringParameter(request, "logName");
    String logIndex = ServletRequestUtils.getStringParameter(request, "logIndex");

    LogDestination dest =
            logResolver.getLogDestination(logType, webapp, context, root, logName, logIndex);

    ModelAndView modelAndView = null;
    boolean logFound = false;
    if (dest != null) {
      if (dest.getFile() != null && dest.getFile().exists()) {
        modelAndView = handleLogFile(request, response, dest);
        logFound = true;
      } else {
        log15.error("Log file not found");
      }
    } else {
      log15.error("Log not found");
    }
    if (!logFound) {
      response.sendError(404);
    }
    return modelAndView;
  }
  /**
   * Handle log file.
   *
   * @param request the request
   * @param response the response
   * @param logDest the log dest
   *
   * @return the model and view
   *
   * @throws HandleLogFileException the exception
   */
  protected abstract ModelAndView handleLogFile(HttpServletRequest request,
      HttpServletResponse response, LogDestination logDest) throws HandleLogFileException, ServletRequestBindingException, IOException, ApplicationCreationException, IllegalAccessException;

}
