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

import com.thoughtworks.xstream.XStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.Controller;
import psiprobe.model.TransportableModel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * The Class BeanToXmlController.
 */
@org.springframework.stereotype.Controller
public class BeanToXmlController extends AbstractController {

  /** The xml marker. */
  private String xmlMarker;

  /** The xstream. */
  private final XStream xstream;

  public BeanToXmlController(XStream xstream) {
    this.xstream = xstream;
  }

  /**
   * Sets the xml marker.
   *
   * @param xmlMarker the new xml marker
   */
  @Value(".oxml")
  public void setXmlMarker(String xmlMarker) {
    this.xmlMarker = xmlMarker;
  }

@GetMapping(path = "/*.oxml.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                               HttpServletResponse response) throws Exception {

    String path = request.getServletPath();
    String internalPath = path.replaceAll(xmlMarker, "");

    Controller controller = (Controller) Objects.requireNonNull(getApplicationContext()).getBean(internalPath);
    ModelAndView modelAndView = controller.handleRequest(request, response);
    if (modelAndView != null) {
      TransportableModel tm = new TransportableModel();
      tm.putAll(modelAndView.getModel());
      xstream.toXML(tm, response.getWriter());
    } else {
      // Handle the case when the model or the ModelAndView is null
      // Return an error response with an HTTP status of 500
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.getWriter().write("Error occurred while handling the request.");
      throw new HandleRequestException("Error occurred while handling the request.");
    }
    return null;
  }

  private static class HandleRequestException extends Exception {
    public HandleRequestException(String message) {
      super(message);
    }
  }
}
