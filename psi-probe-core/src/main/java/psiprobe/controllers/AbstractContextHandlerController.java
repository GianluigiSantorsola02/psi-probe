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
package psiprobe.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import psiprobe.controllers.jsp.ViewServletSourceController;

/**
 * Base class for all controllers requiring "webapp" request parameter.
 */
public abstract class AbstractContextHandlerController extends AbstractTomcatContainerController {

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    String contextName = ServletRequestUtils.getStringParameter(request, "webapp", "");
    Context context;
      contextName = getContainerWrapper().getTomcatContainer().formatContextName(contextName);
      context = getContainerWrapper().getTomcatContainer().findContext(contextName);

      if (context != null || isContextOptional()) {
      return handleContext(contextName, context, request, response);
    }
    MessageSourceAccessor messageSourceAccessor = getMessageSourceAccessor();
    if (contextName != null && messageSourceAccessor != null) {
      request.setAttribute("errorMessage", messageSourceAccessor.getMessage("probe.src.contextDoesntExist", new Object[] {contextName}));
    }
    return new ModelAndView("errors/paramerror");
  }

  /**
   * Checks if is context optional.
   *
   * @return true, if is context optional
   */
  protected boolean isContextOptional() {
    return false;
  }

  /**
   * Handle context.
   *
   * @param contextName the context name
   * @param context the context
   * @param request the request
   * @param response the response
   *
   * @return the model and view
   *
   */


// ...

  public ModelAndView handleContext(String contextName, Context context,
                                    HttpServletRequest request, HttpServletResponse response) throws MyCustomException, ViewServletSourceController.FileProcessingException, Exception {
    // Your code logic here

    if (context == null) {
      throw new MyCustomException("Custom exception message");
    }

    return null;
  }
  public static class MyCustomException extends Exception {

    public MyCustomException(String message) {


      super(message);
    }
  }
}
