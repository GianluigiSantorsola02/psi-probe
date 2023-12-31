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

import java.lang.management.ManagementFactory;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * The Class AjaxUptimeController.
 */
@Controller
public class AjaxUptimeController extends ParameterizableViewController {

  @GetMapping(path = "/uptime.ajax")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  protected ModelAndView handleRequestInternal(@Nullable HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    long uptimeStartValue = ManagementFactory.getRuntimeMXBean().getStartTime();
    long uptime = System.currentTimeMillis() - uptimeStartValue;
    long uptimeDays = uptime / (1000 * 60 * 60 * 24);

    uptime = uptime % (1000 * 60 * 60 * 24);
    long uptimeHours = uptime / (1000 * 60 * 60);

    uptime = uptime % (1000 * 60 * 60);
    long uptimeMins = uptime / (1000 * 60);
    String  requestnull  = "request is null";
    String uptimehours = "uptimehours";
    if (request != null) {
      request.setAttribute(uptimehours, uptimeDays);
    } else {
      logger.error(requestnull);
    }
    if (request != null) {
        request.setAttribute(uptimehours, uptimeHours);
      }
     else {
      logger.error(requestnull);
    }
     if (request!=null) {
       request.setAttribute("uptime_mins", uptimeMins);
     } else {
       logger.error(requestnull);
     }

    return new ModelAndView(Objects.requireNonNull(getViewName()));
  }

  @Value("ajax/uptime")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
