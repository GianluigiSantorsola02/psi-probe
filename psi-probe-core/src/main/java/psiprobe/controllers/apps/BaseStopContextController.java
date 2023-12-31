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

import org.apache.catalina.LifecycleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import psiprobe.TomcatContainer;

/**
 * Stops a web application.
 */
public class BaseStopContextController extends AbstractNoSelfContextHandlerController {

  /** The Constant logger. */
  private static final Logger log13 = LoggerFactory.getLogger(BaseStopContextController.class);

  @Override
  protected void executeAction(String contextName) throws LifecycleException, TomcatContainer.StopException, InterruptedException {
    getContainerWrapper().getTomcatContainer().stop(contextName);

    // Logging action
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    // get username logger
    String name = auth.getName();
    MessageSourceAccessor messageSourceAccessor = getMessageSourceAccessor();
    if (messageSourceAccessor != null && log13.isInfoEnabled()) {
      log13.info(messageSourceAccessor.getMessage("probe.src.log.stop"), name, contextName);
    } else {
      if (log13.isInfoEnabled()) {
        log13.info("Failed to get message source accessor. Stopping {} context.", contextName);
      }
    }
  }
}
