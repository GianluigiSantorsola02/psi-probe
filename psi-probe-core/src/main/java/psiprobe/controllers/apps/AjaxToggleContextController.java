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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import psiprobe.TomcatContainer;
import psiprobe.controllers.AbstractContextHandlerController;

/**
 * Stops a web application.
 */
@Controller
public class AjaxToggleContextController extends AbstractContextHandlerController {

  /** The Constant logger. */
  private static final Logger log17 = LoggerFactory.getLogger(AjaxToggleContextController.class);

  @RequestMapping(path = "/app/toggle.ajax")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  public ModelAndView handleContext(String contextName, Context context,
                                    HttpServletRequest request, HttpServletResponse response) {

    if (shouldHandleContext(context, request, contextName)) {
      handleContextAction(contextName, context, request);
    }

    return new ModelAndView(getViewName(), "available",
            context != null && getContainerWrapper().getTomcatContainer().getAvailable(context));
  }

  private boolean shouldHandleContext(Context context, HttpServletRequest request, String contextName) {
    return context != null && !request.getContextPath().equals(contextName);
  }

  private void handleContextAction(String contextName, Context context, HttpServletRequest request) {
    try {
      // Logging action
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      // get username logger
      String name = auth.getName();
      boolean isStartAction = context.getState().isAvailable();
      String action = isStartAction ? "START" : "STOP";

      log17.info("{} requested {} of {}", request.getRemoteAddr(), action, contextName);

      if (isStartAction) {
        getContainerWrapper().getTomcatContainer().start(contextName);
      } else {
        getContainerWrapper().getTomcatContainer().stop(contextName);
      }

      MessageSourceAccessor messageSourceAccessor = getMessageSourceAccessor();
      if (messageSourceAccessor != null) {
        messageSourceAccessor.getMessage("probe.src.log." + action.toLowerCase(), name);
      } else {
        log17.error("Error: getMessageSourceAccessor() returned null!");
        // You can add additional error handling or logging code here
      }
    } catch (InterruptedException | LifecycleException | TomcatContainer.StartException | TomcatContainer.StopException e) {
      log17.error("Error during ajax request to START/STOP of '{}'", contextName, e);
      Thread.currentThread().interrupt();
    }
  }
  @Value("ajax/context_status")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
