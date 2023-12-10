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

import org.springframework.stereotype.Controller;

import psiprobe.beans.stats.collectors.AppStatsCollectorBean;

/**
 * The Class ResetAppStatsController.
 */
@Controller
public class ResetAppStatsController extends AbstractNoSelfContextHandlerController {

  /** The stats' collector. */
  private final AppStatsCollectorBean statsCollector;

  public ResetAppStatsController(AppStatsCollectorBean statsCollector) {
    this.statsCollector = statsCollector;
  }

  @Override
  protected void executeAction(String contextName) {
    statsCollector.reset(contextName);
  }

}
