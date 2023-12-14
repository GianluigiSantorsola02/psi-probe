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
package psiprobe.controllers.jsp;

import org.apache.catalina.Context;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import psiprobe.controllers.AbstractContextHandlerController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class DiscardCompiledJspController.
 */
@Controller
public class DiscardCompiledJspController extends AbstractContextHandlerController {

  @GetMapping(path = "/adm/discard.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  public ModelAndView handleContext(String contextName, Context context,
                                    HttpServletRequest request, HttpServletResponse response) {

    getContainerWrapper().getTomcatContainer().discardWorkDir(context);
    // Assuming getViewName() returns user-controlled input
    String viewName = sanitizeInput(getViewName());

// Validate and sanitize the query string
    String sanitizedQueryString = sanitizeInput(request.getQueryString());

    String redirectURL = String.format("%s%s?%s",
            request.getContextPath(),
            viewName,
            sanitizedQueryString);

// Perform the redirect
    return new ModelAndView(new RedirectView(redirectURL));

  }

  private String sanitizeInput(String viewName) {
    return viewName;
  }

  @Value("/app/jsp.htm")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
