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
package psiprobe.controllers.connectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import psiprobe.tools.TimeExpression;

/**
 * The Class ZoomChartController.
 */
@Controller
public class ZoomChartController extends ParameterizableViewController {

  /** The collection period. */
  private long collectionPeriod;

  /**
   * Gets the collection period.
   *
   * @return the collection period
   */
  public long getCollectionPeriod() {
    return collectionPeriod;
  }

  /**
   * Sets the collection period.
   *
   * @param collectionPeriod the new collection period
   */
  public void setCollectionPeriod(long collectionPeriod) {
    this.collectionPeriod = collectionPeriod;
  }

  /**
   * Sets the collection period using expression.
   *
   * @param collectionPeriod the new collection period expression
   */
  @Value("${psiprobe.beans.stats.collectors.connector.period}")
  public void setCollectionPeriod(String collectionPeriod) {
    this.collectionPeriod = TimeExpression.inSeconds(collectionPeriod);
  }

  @RequestMapping(path = "/zoomchart.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                               HttpServletResponse response) throws Exception {

    // Call the super method to get the ModelAndView object
    ModelAndView modelAndView = super.handleRequestInternal(request, response);

    // Check if the modelAndView is not null
    if (modelAndView != null) {
      // Add the "collectionPeriod" attribute to the modelAndView object
      modelAndView.addObject("collectionPeriod", getCollectionPeriod());

      // Return the updated modelAndView object
      return modelAndView;
    }

    // Add your error handling or logging code here
    System.err.println("Error: The super handleRequestInternal returned null!");
    // You can also throw an exception or perform any other appropriate action

    // Return null or provide an alternative action based on your requirements
    return null;
  }

  @Value("zoomreq")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
