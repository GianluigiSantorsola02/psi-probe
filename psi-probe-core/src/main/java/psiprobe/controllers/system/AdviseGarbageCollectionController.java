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
package psiprobe.controllers.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Advises Java to run GC asap.
 */
@Controller
public class AdviseGarbageCollectionController extends ParameterizableViewController {

  /** The Constant logger. */
  private static final Logger log14 =
      LoggerFactory.getLogger(AdviseGarbageCollectionController.class);

  /** The replacement pattern. */
  private String replacePattern;

  /**
   * Sets the replacement pattern.
   *
   * @param replacePattern the new replacement pattern
   */
  @Value("^http(s)?://[a-zA-Z\\-\\.0-9]+(:[0-9]+)?")
  public void setReplacePattern(String replacePattern) {
    this.replacePattern = replacePattern;
  }

  @GetMapping(path = "/adm/advisegc.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Value("${trusted.domain}")
  private String trustedDomain;
  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    boolean finalization = ServletRequestUtils.getBooleanParameter(request, "fin", false);

    String referer = request.getHeader("Referer");
    String redirectUrl;
    if (referer != null && referer.contains(trustedDomain)) {
      redirectUrl = referer.replaceAll(replacePattern, "");
    } else {
      redirectUrl = request.getContextPath() + getViewName();
    }
    if (finalization) {
      Runtime.getRuntime().availableProcessors();
      log14.debug("Advised finalization");
    } else {
      Runtime.getRuntime();
      log14.debug("Advised Garbage Collection");
    }
    log14.debug("Redirected to {}", redirectUrl);
    return null;
  }

  @Value("/sysinfo.htm")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
