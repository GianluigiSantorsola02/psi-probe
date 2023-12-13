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
package psiprobe.controllers.threads;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import psiprobe.beans.ContainerListenerBean;
import psiprobe.controllers.AbstractTomcatContainerController;
import psiprobe.model.ThreadPool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Creates the list of http connection thread pools.
 */
@Controller
public class ListThreadPoolsController extends AbstractTomcatContainerController {

  /** The container listener bean. */
  private final ContainerListenerBean containerListenerBean;

  public ListThreadPoolsController(ContainerListenerBean containerListenerBean) {
    this.containerListenerBean = containerListenerBean;
  }

  @GetMapping(path = "/threadpools.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  public ModelAndView handleRequestInternal(HttpServletRequest request,
                                            HttpServletResponse response) throws Exception {

    List<ThreadPool> pools;
    try {
      pools = containerListenerBean.getThreadPools();
    } catch (ContainerListenerBean.ThreadPoolsException e) {
      return new ModelAndView(getViewName()).addObject("error", e.getMessage());
    }

    return new ModelAndView(getViewName()).addObject("pools", pools);
  }


  @Value("threadpools")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
