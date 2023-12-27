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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import psiprobe.jsp.Functions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * The Class RememberVisibilityController.
 */
@Controller
public class RememberVisibilityController extends AbstractController {

  @GetMapping(path = "/remember.ajax")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String cookieName = "cookieName";
    String state = ServletRequestUtils.getStringParameter(request, "state");
    if (state != null) {
      // Validate and sanitize the cookie name
      cookieName = sanitizeCookieName(cookieName);

      // Calculate the expiration date (e.g., 10 years from now)
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.YEAR, 10);
      Date expirationDate = calendar.getTime();

      // Format the expiration date in the desired format (e.g., RFC 1123)
      SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
      String expirationDateString = dateFormat.format(expirationDate);

      // Build the Set-Cookie header with the sanitized values and expiration date
      String sanitizedCookieName = sanitizeHeaderValue(cookieName);
      String sanitizedState = sanitizeHeaderValue(state);
      String setCookieHeader = sanitizedCookieName + "=" + sanitizedState + "; Expires=" + expirationDateString + "; Secure=true; HttpOnly=true";

    return new ModelAndView("remember", "setCookieHeader", setCookieHeader);
    }
    return null;
  }

  private String sanitizeHeaderValue(String cookieName) {
    return Functions.sanitizeHeaderValue(cookieName);
  }

  private String sanitizeCookieName(String cookieName) {
    return Functions.sanitizeCookieName(cookieName);

  }
}
