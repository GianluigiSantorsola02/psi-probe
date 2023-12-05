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
package psiprobe.controllers.datasources;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import psiprobe.controllers.AbstractContextHandlerController;

import java.util.Objects;

/**
 * Resets datasource if the datasource supports it.
 */


@Controller
public class ResetDataSourceController extends AbstractContextHandlerController {

  /**
   * The Constant logger.
   */
  private static final Logger mylogger = LoggerFactory.getLogger(ResetDataSourceController.class);

  /**
   * The replace pattern.
   */
  private String replacePattern;

  /**
   * Sets the replace pattern.
   *
   * @param replacePattern the new replace pattern
   */
  @Value("^http(s)?://[a-zA-Z\\-\\.0-9]+(:[0-9]+)?")
  public void setReplacePattern(String replacePattern) {
    this.replacePattern = replacePattern;
  }

  @RequestMapping(path = "/app/resetds.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  public ModelAndView handleContext(String contextName, Context context,
                                    HttpServletRequest request, HttpServletResponse response) throws Exception {
    String resourceName = getResourceName(request);
    String redirectUrl = getRedirectUrl(request);

    resetResource(context, resourceName, request);
    handleResetErrors(request);

    mylogger.debug("Redirected to {}", redirectUrl);
    return new ModelAndView(new RedirectView(redirectUrl));
  }

  private String getResourceName(HttpServletRequest request) {
    String resourceName = null;
    if (request != null) {
      resourceName = ServletRequestUtils.getStringParameter(request, "resource", "");
    }
    return resourceName;
  }

  private String getRedirectUrl(HttpServletRequest request) {
    String referer = Objects.requireNonNull(request).getHeader("Referer");
    String redirectUrl;
    if (referer != null) {
      redirectUrl = referer.replaceAll(replacePattern, "");
    } else {
      redirectUrl = request.getContextPath() + getViewName();
    }
    return redirectUrl;
  }

  private void resetResource(Context context, String resourceName, HttpServletRequest request) {
    if (resourceName != null && context != null && getContainerWrapper() != null) {
      boolean reset = false;
      try {
        reset = getContainerWrapper().getResourceResolver().resetResource(context, resourceName,
                getContainerWrapper());
      } catch (NamingException e) {
        handleResetException(request, resourceName, e);
      }
      if (!reset) {
        handleResetFailure(request);
      }
    }
  }

  private void handleResetException(HttpServletRequest request, String resourceName, NamingException e) {
    String errorMessageString = "errorMessage";
    if (getMessageSourceAccessor() != null) {
      MessageSourceAccessor accessor = getMessageSourceAccessor();
      String message = accessor != null ? accessor.getMessage("probe.src.reset.datasource.notfound", new Object[] {resourceName}) : null;
      request.setAttribute(errorMessageString, message);
    } else {
      request.setAttribute(errorMessageString, "Default error message");
    }
    mylogger.trace("", e);
  }

  private void handleResetFailure(HttpServletRequest request) {
    String errorMessageString = "errorMessage";

    if (getMessageSourceAccessor() != null) {
      MessageSourceAccessor accessor = getMessageSourceAccessor();
      String message = accessor != null ? accessor.getMessage("probe.src.reset.datasource") : null;
      request.setAttribute(errorMessageString, message);
    } else {
      request.setAttribute(errorMessageString, "Default error message");
    }
  }

  private void handleResetErrors(HttpServletRequest request) {
    handleResetException(request);
    handleResetFailure(request);
  }

  private void handleResetException(HttpServletRequest request) {
    String errorMessageString = "errorMessage";

    if (getMessageSourceAccessor() != null) {
      MessageSourceAccessor accessor = getMessageSourceAccessor();
      String message = accessor != null ? accessor.getMessage("probe.src.reset.datasource") : null;
      request.setAttribute(errorMessageString, message);
    } else {
      request.setAttribute(errorMessageString, "Default error message");
    }
  }
}
