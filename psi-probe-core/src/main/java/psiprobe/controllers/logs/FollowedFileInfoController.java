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
import psiprobe.beans.LogResolverBean;
import psiprobe.tools.logging.LogDestination;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * The Class FollowedFileInfoController.
 */
@Controller
public class FollowedFileInfoController extends AbstractLogHandlerController {

    private static final LogResolverBean logResolver = new LogResolverBean();

    public FollowedFileInfoController() {
        super(logResolver);
    }

    @GetMapping(path = "/ff_info.ajax")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  protected ModelAndView handleLogFile(HttpServletRequest request, HttpServletResponse response,
      LogDestination logDest) {
    return new ModelAndView(Objects.requireNonNull(getViewName())).addObject("log", logDest);
  }

  @Value("ajax/followed_file_info")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
