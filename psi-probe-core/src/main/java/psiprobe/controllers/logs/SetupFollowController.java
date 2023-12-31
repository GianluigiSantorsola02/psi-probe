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
package psiprobe.controllers.logs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import psiprobe.beans.ApplicationCreationException;
import psiprobe.beans.LogResolverBean;
import psiprobe.tools.logging.LogDestination;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * The Class SetupFollowController.
 */
@Controller
public class SetupFollowController extends AbstractLogHandlerController {

  private static final LogResolverBean logResolver = new LogResolverBean();

  public SetupFollowController() {
    super(logResolver);
  }

  @GetMapping(path = "/follow.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  protected ModelAndView handleLogFile(HttpServletRequest request, HttpServletResponse response,
      LogDestination logDest) throws ApplicationCreationException, IllegalAccessException, IOException {

    File logFile = logDest.getFile();
    List<LogDestination> sources = getLogResolver().getLogSources(logFile);
    return new ModelAndView(Objects.requireNonNull(getViewName())).addObject("log", logDest).addObject("sources", sources);
  }

  @Value("follow")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
