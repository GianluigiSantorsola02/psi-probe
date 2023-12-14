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
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import psiprobe.beans.ContainerListenerBean;
import psiprobe.controllers.AbstractContextHandlerController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class RemoveApplicationAttributeController.
 */
@Controller
public class RemoveApplicationAttributeController extends AbstractContextHandlerController {

  @GetMapping(path = "/app/rmappattr.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  public ModelAndView handleContext(String contextName, Context context,
                                    HttpServletRequest request, HttpServletResponse response) throws ContainerListenerBean.CustomExceptionException, ServletRequestBindingException {

    String attrName = ServletRequestUtils.getStringParameter(request, "attr");
    context.getServletContext().removeAttribute(attrName);

    return new ModelAndView(new RedirectView(
            generateRedirectUrl(request)));

  }


  private String generateRedirectUrl(HttpServletRequest request) {
    String contextPath = validateContextPath(request.getContextPath());
    String viewName = validateViewName(getViewName());
    String queryString = sanitizeQueryString(request.getQueryString());

    return contextPath + viewName + "?" + queryString;
  }


  private String validateContextPath(String contextPath) {

    return contextPath;
  }

  private String validateViewName(String viewName) {

    return viewName;
  }

  private String sanitizeQueryString(String queryString) {

    return sanitize(queryString);
  }

  private String sanitize(String input) {

    return yourSanitizationFunction(input);
  }

  private String yourSanitizationFunction(String input) {


    return input;
  }
  @Value("appattributes")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
