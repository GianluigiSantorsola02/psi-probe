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

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import psiprobe.beans.JvmMemoryInfoAccessorBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * The Class BaseMemoryStatsController.
 */
public class BaseMemoryStatsController extends ParameterizableViewController {

  /** The jvm memory info accessor bean. */
  private final JvmMemoryInfoAccessorBean jvmMemoryInfoAccessorBean;

  /** The collection period. */
  private final ThreadLocal<Long> collectionPeriodThreadLocal = new ThreadLocal<>();

  // Other fields and methods...

  public long getCollectionPeriod() {
    Long period = collectionPeriodThreadLocal.get();
    return period != null ? period : 0L;
  }

  public void setCollectionPeriod(long collectionPeriod) {
    collectionPeriodThreadLocal.set(collectionPeriod);
  }

  public BaseMemoryStatsController(JvmMemoryInfoAccessorBean jvmMemoryInfoAccessorBean) {
    this.jvmMemoryInfoAccessorBean = jvmMemoryInfoAccessorBean;
  }

  /**
   * Gets the jvm memory info accessor bean.
   *
   * @return the jvm memory info accessor bean
   */
  public JvmMemoryInfoAccessorBean getJvmMemoryInfoAccessorBean() {
    return jvmMemoryInfoAccessorBean;
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    ModelAndView mv = new ModelAndView(Objects.requireNonNull(getViewName()));
    mv.addObject("pools", getJvmMemoryInfoAccessorBean().getPools());
    mv.addObject("collectionPeriod", getCollectionPeriod());
    return mv;
  }

}
