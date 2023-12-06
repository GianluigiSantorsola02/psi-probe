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
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.RedirectView;

import psiprobe.TomcatContainer;
import psiprobe.beans.ContainerListenerBean;
import psiprobe.controllers.AbstractContextHandlerController;

/**
 * Base class preventing "destructive" actions to be executed on the Probe's context.
 */
public abstract class AbstractNoSelfContextHandlerController
    extends AbstractContextHandlerController {

  /** The Constant logger. */
  private static final Logger mylogger =
      LoggerFactory.getLogger(AbstractNoSelfContextHandlerController.class);

  /** The pass query string. */
  private boolean passQueryString;

  /**
   * Checks if is pass query string.
   *
   * @return true, if is pass query string
   */
  public boolean isPassQueryString() {
    return passQueryString;
  }

  /**
   * Sets the pass query string.
   *
   * @param passQueryString the new pass query string
   */
  public void setPassQueryString(boolean passQueryString) {
    this.passQueryString = passQueryString;
  }

  @Override
  public ModelAndView handleContext(String contextName, Context context,
                                    HttpServletRequest request, HttpServletResponse response) throws InterruptedException {

    try {
      handleContextAction(contextName, request);
      executeAction(contextName);
    } catch (ContainerListenerBean.CustomExceptionException | LifecycleException | TomcatContainer.StartException |
  TomcatContainer.StopException e) {
    request.setAttribute("errorMessage", e.getMessage());
    mylogger.error("Error during invocation", e);
    return new ModelAndView(new InternalResourceView(getViewName()));
  } catch (InterruptedException e) {
    request.setAttribute("errorMessage", e.getMessage());
    mylogger.error("Error during invocation", e);
    throw e;
  }

      return new ModelAndView(new RedirectView(request.getContextPath() + getViewName()
            + (isPassQueryString() ? "?" + request.getQueryString() : "")));
  }

  private void handleContextAction(String contextName, HttpServletRequest request) {
    try {
      if (request.getContextPath().equals(contextName)) {
        MessageSourceAccessor messageSourceAccessor = getMessageSourceAccessor();
        if (messageSourceAccessor == null) {
          throw new IllegalStateException("Failed to get message source accessor");
        }
        throw new IllegalStateException(messageSourceAccessor.getMessage("probe.src.contextAction.cannotActOnSelf"));
      }
    } catch (NullPointerException ex) {
      throw new IllegalStateException("Null context path or context name", ex);
    }
  }

  /**
   * Execute action.
   *
   * @param contextName the context name
   *
   */
  protected abstract void executeAction(String contextName) throws ContainerListenerBean.CustomExceptionException, LifecycleException, TomcatContainer.StartException, InterruptedException, TomcatContainer.StopException;

}
