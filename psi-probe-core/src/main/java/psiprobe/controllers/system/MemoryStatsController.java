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
import psiprobe.beans.JvmMemoryInfoAccessorBean;
import psiprobe.tools.TimeExpression;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class MemoryStatsController.
 */
@Controller
public class MemoryStatsController extends BaseMemoryStatsController {

  private static final JvmMemoryInfoAccessorBean jvmMemoryInfoAccessorBean = new JvmMemoryInfoAccessorBean();

  public MemoryStatsController() {
    super(jvmMemoryInfoAccessorBean);
  }

  /**
   * Sets the collection period.
   *
   * @param collectionPeriod the new collection period
   */
  @Value("${psiprobe.beans.stats.collectors.memory.period}")
  public void setCollectionPeriod(String collectionPeriod) throws TimeExpression.NewCustomException {
    super.setCollectionPeriod(TimeExpression.inSeconds(collectionPeriod));
  }

  @GetMapping(path = "/memory.htm")
  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return super.handleRequest(request, response);
  }

  @Value("memory")
  @Override
  public void setViewName(String viewName) {
    super.setViewName(viewName);
  }

}
