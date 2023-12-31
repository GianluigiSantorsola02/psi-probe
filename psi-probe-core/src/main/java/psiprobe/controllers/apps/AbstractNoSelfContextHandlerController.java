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

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceView;
import psiprobe.TomcatContainer;
import psiprobe.beans.ContainerListenerBean;
import psiprobe.controllers.AbstractContextHandlerController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base class preventing "destructive" actions to be executed on the Probe's context.
 */
public abstract class AbstractNoSelfContextHandlerController
    extends AbstractContextHandlerController {

  /** The Constant logger. */
  private static final Logger mylogger =
      LoggerFactory.getLogger(AbstractNoSelfContextHandlerController.class);

  /**
   * Sets the pass query string.
   *
   * @param passQueryString the new pass query string
   */
  public void setPassQueryString(boolean passQueryString) {
  }

  @Override
  public ModelAndView handleContext(String contextName, Context context,
                                    HttpServletRequest request, HttpServletResponse response) throws ContainerListenerBean.CustomException {

    try {
      handleContextAction(contextName, request);
      executeAction(contextName);
    } catch (ContainerListenerBean.CustomExceptionException | LifecycleException | TomcatContainer.StartException |
             TomcatContainer.StopException | InterruptedException e) {
      mylogger.error("Error during invocation", e);
      Thread.currentThread().interrupt(); // Re-interrupt the method
    }

    return new ModelAndView(new InternalResourceView(getViewName()));
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
