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

import org.apache.catalina.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.RedirectView;
import psiprobe.beans.ContainerListenerBean;
import psiprobe.controllers.AbstractContextHandlerController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * Undeploys a web application.
 */
public abstract class BaseUndeployContextController extends AbstractContextHandlerController {

  /** The Constant logger. */
  private static final Logger log2 = LoggerFactory.getLogger(BaseUndeployContextController.class);

  /** The failure view name. */
  private static final ThreadLocal<String> threadLocalFailureViewName = new ThreadLocal<>();

  public void processUserRequest() {
    // Retrieve or set user-specific failureViewName
    String userFailureViewName = getUserFailureViewName();

    // Set the failureViewName for the current thread
    setThreadLocalFailureViewName(userFailureViewName);
    // Ensure to clean up the thread-local variable after the request is processed
    cleanupThreadLocal();
  }

  private String getUserFailureViewName() {
    // Retrieve or set user-specific failureViewName
    String userFailureViewName = threadLocalFailureViewName.get();
    if (userFailureViewName == null) {
      userFailureViewName = "defaultFailureViewName";  // Set default value if not yet initialized
      threadLocalFailureViewName.set(userFailureViewName);
    }
    return userFailureViewName;
  }

  private void setThreadLocalFailureViewName(String userFailureViewName) {
    threadLocalFailureViewName.set(userFailureViewName);
  }

  private void cleanupThreadLocal() {
    // Remove the thread-local variable after the request is processed
    threadLocalFailureViewName.remove();
  }


  @Override
  public ModelAndView handleContext(String contextName, Context context,
                                    HttpServletRequest request, HttpServletResponse response) throws ContainerListenerBean.CustomExceptionException {
    try {
      MessageSourceAccessor messageSourceAccessor = getMessageSourceAccessor();
      if (messageSourceAccessor != null && request.getContextPath().equals(contextName)) {
        throw new IllegalStateException(messageSourceAccessor.getMessage("probe.src.contextAction.cannotActOnSelf"));
      }
      getContainerWrapper().getTomcatContainer().remove(contextName);
      messageSourceAccessor = getMessageSourceAccessor();
      if (messageSourceAccessor != null) {
        String message = messageSourceAccessor.getMessage("probe.src.log.undeploy", Locale.forLanguageTag(contextName));        log2.info(message);
      }else {
        log2.info("Failed to get message source accessor. Undeploying {} context.", contextName);
      }
    } catch (Exception e) {
      request.setAttribute("errorMessage", e.getMessage());
      log2.error("Error during undeploy of '{}'", contextName, e);
        getUserFailureViewName();
        return new ModelAndView(new InternalResourceView(
                threadLocalFailureViewName.get()));
    }
    processUserRequest();
    return new ModelAndView(new RedirectView(request.getContextPath() + getViewName()));
  }


  @Value("/appsummary.htm")
  public void setFailureViewName(String failureViewName) {
  }
}
