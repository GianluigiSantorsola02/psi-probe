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
package psiprobe.controllers.cluster;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class ClusterMembersStatsController.
 */
@Controller
public class ClusterMembersStatsController extends BaseClusterStatsController {

    public ClusterMembersStatsController() {
        super();
    }

    @Override
    public void setLoadMembers(boolean loadMembers) {
        super.setThreadLocalLoadMembers(loadMembers);
    }

    @GetMapping(path = "/cluster/members.ajax")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Value("ajax/cluster/members")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
