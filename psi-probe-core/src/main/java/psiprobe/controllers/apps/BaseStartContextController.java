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
 * Starts a web application.
 */
public class BaseStartContextController extends AbstractNoSelfContextHandlerController {

  /**
   * The Constant logger.
   */
  private static final Logger mylogger = LoggerFactory.getLogger(BaseStartContextController.class);

  @Override
  protected void executeAction(String contextName) throws LifecycleException, TomcatContainer.StartException, InterruptedException {
    getContainerWrapper().getTomcatContainer().start(contextName);

    // Logging action
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    // get username logger
    String name = auth.getName();
    MessageSourceAccessor messageSourceAccessor = getMessageSourceAccessor();
    boolean shouldLogReload = messageSourceAccessor != null && mylogger.isInfoEnabled();

    if (shouldLogReload) {
      mylogger.info("{} requested reload of {}", name, contextName);
    }
    else if (logger.isInfoEnabled()) {
      mylogger.info("Failed to get message source accessor. Starting {} context.", contextName);
    }

  }
}
