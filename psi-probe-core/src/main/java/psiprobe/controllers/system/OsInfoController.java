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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import psiprobe.beans.RuntimeInfoAccessorBean;
import psiprobe.tools.TimeExpression;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Creates an instance of OsInfoController.
 */
@Controller
public class OsInfoController extends BaseSysInfoController {

  private static final RuntimeInfoAccessorBean runtimeInfoAccessor =  new RuntimeInfoAccessorBean() ;

  public OsInfoController() {
    super(runtimeInfoAccessor);
  }

  @GetMapping(path = "/adm/osinfo.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Value("osinfo")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

  /**
   * Sets the collection period by expression.
   *
   * @param collectionPeriod the new collection period by expression
   */
  @Value("${psiprobe.beans.stats.collectors.runtime.period}")
  public void setCollectionPeriod(String collectionPeriod) throws TimeExpression.NewCustomException {
    super.setCollectionPeriod(TimeExpression.inSeconds(collectionPeriod));
  }

}
