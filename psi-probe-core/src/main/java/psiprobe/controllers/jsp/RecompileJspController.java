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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import psiprobe.controllers.AbstractContextHandlerController;
import psiprobe.model.jsp.Summary;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Class RecompileJspController.
 */
@Controller
public class RecompileJspController extends AbstractContextHandlerController {

  /** The Constant logger. */
  private static final Logger mylogger = LoggerFactory.getLogger(RecompileJspController.class);

  @GetMapping(path = "/app/recompile.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  public ModelAndView handleContext(String contextName, Context context,
                                    HttpServletRequest request, HttpServletResponse response) {

    HttpSession session = request.getSession(false);
    Summary summary = (Summary) session.getAttribute(DisplayJspController.SUMMARY_ATTRIBUTE);

    if (summary != null && "post".equalsIgnoreCase(request.getMethod())) {
      List<String> names = new ArrayList<>();
      for (String name : Collections.list(request.getParameterNames())) {
        if ("on".equals(request.getParameter(name))) {
          names.add(name);
        }
      }
      getContainerWrapper().getTomcatContainer().recompileJsps(context, summary, names);
      request.getSession(false).setAttribute(DisplayJspController.SUMMARY_ATTRIBUTE, summary);
    } else if (summary != null && contextName.equals(summary.getName())) {
      String name = null;
      name = ServletRequestUtils.getStringParameter(request, "source", "");

        List<String> names = new ArrayList<>();
        names.add(name);
        getContainerWrapper().getTomcatContainer().recompileJsps(context, summary, names);
        request.getSession(false).setAttribute(DisplayJspController.SUMMARY_ATTRIBUTE, summary);
    }
    return new ModelAndView(new RedirectView(request.getContextPath()
        + ServletRequestUtils.getStringParameter(request, "view", getViewName()) + "?"
        + request.getQueryString()));
  }

  @Value("/app/jsp.htm")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
