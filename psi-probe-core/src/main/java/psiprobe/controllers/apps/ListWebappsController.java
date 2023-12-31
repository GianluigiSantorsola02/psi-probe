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
import psiprobe.controllers.AbstractTomcatContainerController;
import psiprobe.model.Application;
import psiprobe.tools.ApplicationUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates the list of web application installed in the same "host" as the Probe.
 */
@Controller
public class ListWebappsController extends AbstractTomcatContainerController {

 @GetMapping(path = "/index.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
      HttpServletResponse response) throws Exception {

      List<Context> apps;
      ServletContext servletContext = null;
      try {
          apps = getContainerWrapper().getTomcatContainer().findContexts();
          servletContext = getServletContext();
          if (servletContext == null) {
              throw new IllegalStateException("No servlet context found");
          }

      } catch (NullPointerException ex) {
          throw new IllegalStateException("No container found for your server.", ex);
      }      List<Application> applications = new ArrayList<>(apps.size());
      boolean showResources = getContainerWrapper().getResourceResolver().supportsPrivateResources();
      for (Context appContext : apps) {
          // check if this is not the ROOT webapp
          if (appContext.getName() != null) {
              applications.add(ApplicationUtils.getApplication(appContext,
                      getContainerWrapper().getResourceResolver(), getContainerWrapper()));
          }
      }
      if (!applications.isEmpty() && !showResources) {
          request.setAttribute("no_resources", Boolean.TRUE);
      }
      return new ModelAndView(getViewName(), "apps", applications);
  }

  @Value("applications")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }
}
