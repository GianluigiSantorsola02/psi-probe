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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import psiprobe.beans.ContainerListenerBean;
import psiprobe.controllers.AbstractContextHandlerController;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Creates a list of resources for a particular web application.
 */
@Controller
public class ListApplicationResourcesController extends AbstractContextHandlerController {

  @GetMapping(path = "/resources.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  public ModelAndView handleContext(String contextName, Context context,
                                    HttpServletRequest request, HttpServletResponse response) throws  ContainerListenerBean.CustomException {

    try {
      return new ModelAndView(getViewName(), "resources", getContainerWrapper().getResourceResolver()
          .getApplicationResources(context, getContainerWrapper()));
    } catch (NamingException ignored) {
      return null;
    }
  }

  @Value("resources")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
