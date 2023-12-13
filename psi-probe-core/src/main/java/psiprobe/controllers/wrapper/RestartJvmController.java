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
package psiprobe.controllers.wrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.tanukisoftware.wrapper.WrapperManager;
import psiprobe.PostParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * The Class RestartJvmController.
 */
@Controller
public class RestartJvmController extends PostParameterizableViewController {

  /** The Constant logger. */
  private static final Logger log9 = LoggerFactory.getLogger(RestartJvmController.class);

  @GetMapping(path = "/adm/restartvm.ajax")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    boolean done = false;
    try {
      Class.forName("org.tanukisoftware.wrapper.WrapperManager");
      log9.info("JVM is RESTARTED by {}", request.getRemoteAddr());
      WrapperManager.restartAndReturn();
      done = true;
    } catch (ClassNotFoundException e) {
      log9.info("WrapperManager not found. Do you have wrapper.jar in the classpath?");
      log9.trace("", e);
    }
    return new ModelAndView(Objects.requireNonNull(getViewName()), "done", done);
  }

  @Value("ajax/jvm_restarted")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
