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
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import psiprobe.beans.ContainerListenerBean;
import psiprobe.controllers.AbstractContextHandlerController;
import psiprobe.model.jsp.Summary;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * The Class DisplayJspController.
 */
@Controller
public class DisplayJspController extends AbstractContextHandlerController {

  /** The Constant SUMMARY_ATTRIBUTE. */
  public static final String SUMMARY_ATTRIBUTE = "jsp.summary";

  @GetMapping(path = "/app/jsp.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  public ModelAndView handleContext(String contextName, Context context,
                                    HttpServletRequest request, HttpServletResponse response) throws ContainerListenerBean.CustomExceptionException {
    boolean compile = ServletRequestUtils.getBooleanParameter(request, "compile", false);

    HttpSession session = request.getSession(false);
    Summary summary = (Summary) session.getAttribute(SUMMARY_ATTRIBUTE);
    if (summary == null || !contextName.equals(summary.getName())) {
      summary = new Summary();
      summary.setName(contextName);
    }
    getContainerWrapper().getTomcatContainer().listContextJsps(context, summary, compile);

    request.getSession(false).setAttribute(SUMMARY_ATTRIBUTE, summary);

    if (compile) {
      return new ModelAndView(new RedirectView(
            constructRedirectUrl(request.getRequestURI(), contextName)));
    }
    return new ModelAndView(getViewName(), "summary", summary);
  }

  private String constructRedirectUrl(String requestUri, String contextName) {
    String encodedContextName;
    try {
      encodedContextName = URLEncoder.encode(contextName, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      encodedContextName = contextName;
    }
      return requestUri + "?webapp=" + (encodedContextName.isEmpty() ? "/" : encodedContextName);
  }
  @Value("showjsps")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
