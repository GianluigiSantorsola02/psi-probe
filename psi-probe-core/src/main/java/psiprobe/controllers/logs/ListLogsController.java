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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import psiprobe.beans.LogResolverBean;
import psiprobe.tools.logging.LogDestination;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

/**
 * The Class ListLogsController.
 */
@Controller
public class ListLogsController extends ParameterizableViewController {

  /** The error view. */
  private String errorView;

  /** The log resolver. */
  private static final ThreadLocal<LogResolverBean> threadLocalLogResolver = new ThreadLocal<>();

  public void processUserRequest() {
    // Retrieve or set user-specific logResolver
    LogResolverBean userLogResolver = getUserLogResolver();

    // Set the logResolver for the current thread
    setThreadLocalLogResolver(userLogResolver);

    // Ensure to clean up the thread-local variable after the request is processed
    cleanupThreadLocal();
  }

  private LogResolverBean getUserLogResolver() {
    // Retrieve or set user-specific logResolver
    LogResolverBean userLogResolver = threadLocalLogResolver.get();
    if (userLogResolver == null) {
      userLogResolver = new LogResolverBean();  // Create a new instance if not yet initialized
      threadLocalLogResolver.set(userLogResolver);
    }
    return userLogResolver;
  }

  private void setThreadLocalLogResolver(LogResolverBean userLogResolver) {
    threadLocalLogResolver.set(userLogResolver);
  }

  private void cleanupThreadLocal() {
    // Remove the thread-local variable after the request is processed
    threadLocalLogResolver.remove();
  }

  /**
   * Sets the error view.
   *
   * @param errorView the new error view
   */
  @Value("logs_notsupported")
  public void setErrorView(String errorView) {
    this.errorView = errorView;
  }

  @GetMapping(path = {"/logs", "/list.htm"})
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    boolean showAll = ServletRequestUtils.getBooleanParameter(request, "apps", false);
    List<LogDestination> uniqueList = getUserLogResolver().getLogDestinations(showAll);
    if (!uniqueList.isEmpty()) {
      return new ModelAndView(Objects.requireNonNull(getViewName())).addObject("logs", uniqueList);
    }
    processUserRequest();
    return new ModelAndView(errorView);
  }

  @Value("logs")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
