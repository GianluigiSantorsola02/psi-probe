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
package psiprobe.controllers.connectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;
import psiprobe.beans.stats.collectors.ConnectorStatsCollectorBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class ResetConnectorStatsController.
 */
@Controller
public class ResetConnectorStatsController extends ParameterizableViewController {

  /** The collector bean. */
  private static final ThreadLocal<ConnectorStatsCollectorBean> threadLocalCollectorBean = new ThreadLocal<>();

  public void processUserRequest() {
    // Retrieve or set user-specific collectorBean
    ConnectorStatsCollectorBean userCollectorBean = getUserCollectorBean();

    // Set the collectorBean for the current thread
    setThreadLocalCollectorBean(userCollectorBean);

    // Ensure to clean up the thread-local variable after the request is processed
    cleanupThreadLocal();
  }

  private ConnectorStatsCollectorBean getUserCollectorBean() {
    // Retrieve or set user-specific collectorBean
    ConnectorStatsCollectorBean userCollectorBean = threadLocalCollectorBean.get();
    if (userCollectorBean == null) {
      userCollectorBean = new ConnectorStatsCollectorBean();  // Create a new instance if not yet initialized
      threadLocalCollectorBean.set(userCollectorBean);
    }
    return userCollectorBean;
  }

  private void setThreadLocalCollectorBean(ConnectorStatsCollectorBean userCollectorBean) {
    threadLocalCollectorBean.set(userCollectorBean);
  }

  private void cleanupThreadLocal() {
    // Remove the thread-local variable after the request is processed
    threadLocalCollectorBean.remove();
  }


  @GetMapping(path = "/app/connectorReset.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    String connectorName = ServletRequestUtils.getRequiredStringParameter(request, "cn");
    getUserCollectorBean().reset(connectorName);
    processUserRequest();
    return new ModelAndView(new RedirectView(request.getContextPath() + getViewName()));
  }

  @Value("/connectors.htm")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
