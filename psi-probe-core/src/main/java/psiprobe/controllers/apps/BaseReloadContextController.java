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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Reloads application context.
 */
public class BaseReloadContextController extends AbstractNoSelfContextHandlerController {

  /** The Constant logger. */
  private static final Logger mylogger = LoggerFactory.getLogger(BaseReloadContextController.class);

  @Override
  protected void executeAction(String contextName) {
    Context context = getContainerWrapper().getTomcatContainer().findContext(contextName);
    if (context != null) {
      context.reload();

      // Logging action
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      // get username mylogger
      String name = auth.getName();
      MessageSourceAccessor messageSourceAccessor = getMessageSourceAccessor();
      if (messageSourceAccessor != null && mylogger.isInfoEnabled()) {
        mylogger.info(messageSourceAccessor.getMessage("probe.src.log.reload"), name, contextName);
      } else if (mylogger.isInfoEnabled()) {
        mylogger.info("Failed to get message source accessor. Reloading {} context.", contextName);
      }
    }}}