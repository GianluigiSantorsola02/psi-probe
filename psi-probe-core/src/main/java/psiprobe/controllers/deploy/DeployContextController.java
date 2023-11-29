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
package psiprobe.controllers.deploy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceView;

import psiprobe.controllers.AbstractTomcatContainerController;

/**
 * Forces Tomcat to install a pre-configured context name.
 */
@Controller
public class DeployContextController extends AbstractTomcatContainerController {

  @RequestMapping(path = "/adm/deploycontext.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  public ModelAndView handleRequestInternal(HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    String contextName = ServletRequestUtils.getStringParameter(request, "context", null);

    if (contextName != null) {
      try {
        MessageSourceAccessor messageSourceAccessor = getMessageSourceAccessor();
        if (getContainerWrapper().getTomcatContainer().installContext(contextName) && messageSourceAccessor != null) {
          request.setAttribute("successMessage", messageSourceAccessor.getMessage("probe.src.deploy.context.success", new Object[] {contextName}));
        }          // Logging action
          Authentication auth = SecurityContextHolder.getContext().getAuthentication();
          // get username logger
          String name = auth.getName();
          messageSourceAccessor = getMessageSourceAccessor();
        if (messageSourceAccessor != null) {
          logger.info(messageSourceAccessor.getMessage("probe.src.log.deploycontext"), name, contextName);
        }
         else {
            messageSourceAccessor = getMessageSourceAccessor();
          if (messageSourceAccessor != null) {
            request.setAttribute("errorMessage", messageSourceAccessor.getMessage("probe.src.deploy.context.failure", new Object[] {contextName}));
          } else {
            request.setAttribute("errorMessage", "Error: Failed to get message source accessor.");
          }        }
      } catch (Exception e) {
        request.setAttribute("errorMessage", e.getMessage());
        logger.trace("", e);
      }
    }

    return new ModelAndView(new InternalResourceView(getViewName()));
  }

  @Value("/adm/deploy.htm")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
