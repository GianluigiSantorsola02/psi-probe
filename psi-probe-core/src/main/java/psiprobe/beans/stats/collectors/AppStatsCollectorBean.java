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
package psiprobe.beans.stats.collectors;

import org.apache.catalina.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.ServletContextAware;
import psiprobe.TomcatContainer;
import psiprobe.beans.ContainerWrapperBean;
import psiprobe.model.Application;
import psiprobe.tools.ApplicationUtils;

import javax.servlet.ServletContext;

/**
 * Collects application statistics.
 */
public class AppStatsCollectorBean extends AbstractStatsCollectorBean
    implements ServletContextAware {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(AppStatsCollectorBean.class);

  /** The container wrapper. */
  private ContainerWrapperBean containerWrapper;

  /** The servlet context. */
  private ServletContext servletContext;

  /** The self ignored. */
  private boolean selfIgnored;

  /**
   * Gets the container wrapper.
   *
   * @return the container wrapper
   */
  public ContainerWrapperBean getContainerWrapper() {
    return containerWrapper;
  }

  /**
   * Sets the container wrapper.
   *
   * @param containerWrapper the new container wrapper
   */
  public void setContainerWrapper(ContainerWrapperBean containerWrapper) {
    this.containerWrapper = containerWrapper;
  }

  /**
   * Checks if is self ignored.
   *
   * @return true, if is self ignored
   */
  public boolean isSelfIgnored() {
    return selfIgnored;
  }

  /**
   * Sets the self ignored.
   *
   * @param selfIgnored the new self ignored
   */
  @Value("${psiprobe.beans.stats.collectors.app.selfIgnored}")
  public void setSelfIgnored(boolean selfIgnored) {
    this.selfIgnored = selfIgnored;
  }

  /**
   * Gets the servlet context.
   *
   * @return the servlet context
   */
  protected ServletContext getServletContext() {
    return servletContext;
  }

  @Override
  public void setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  @Override
  public void collect() throws InterruptedException {
    long currentTime = System.currentTimeMillis();

    if (containerWrapper == null) {
      logger.error("Cannot collect application stats. Container wrapper is not set.");
    } else {
      TomcatContainer tomcatContainer = getContainerWrapper().getTomcatContainer();

      if (tomcatContainer != null) {
        collectContextStats(tomcatContainer, currentTime);
      }

      logger.debug("App stats collected in {}ms", System.currentTimeMillis() - currentTime);
    }
  }

  private void collectContextStats(TomcatContainer tomcatContainer, long currentTime) throws InterruptedException {
    long totalReqDelta = 0;
    long totalErrDelta = 0;
    long totalAvgProcTime = 0;
    int participatingAppCount = 0;

    for (Context ctx : tomcatContainer.findContexts()) {
      if (ctx != null && ctx.getName() != null) {
        collectApplicationStats(ctx, currentTime, totalReqDelta, totalErrDelta, totalAvgProcTime, participatingAppCount);
      }
    }

    buildTotalStats("total.requests", totalReqDelta, currentTime);
    buildTotalStats("total.errors", totalErrDelta, currentTime);
    buildTotalStats("total.avg_proc_time", 0, currentTime);
  }

  private void collectApplicationStats(Context ctx, long currentTime, long totalReqDelta, long totalErrDelta, long totalAvgProcTime, int participatingAppCount) throws InterruptedException {
    Application app = new Application();
    ApplicationUtils.collectApplicationServletStats(ctx, app);

    String appName = "".equals(ctx.getName()) ? "/" : ctx.getName();

    long reqDelta = buildDeltaStats("app.requests." + appName, app.getRequestCount(), currentTime);
    long errDelta = buildDeltaStats("app.errors." + appName, app.getErrorCount());
    long procTimeDelta = buildDeltaStats("app.proc_time." + appName, app.getProcessingTime(), currentTime);

    long avgProcTime = reqDelta == 0 ? 0 : procTimeDelta / reqDelta;
    buildAbsoluteStats("app.avg_proc_time." + appName, avgProcTime, currentTime);

    if (reqDelta > 0 && !excludeFromTotal(ctx)) {
      totalReqDelta += reqDelta;
      totalErrDelta += errDelta;
      totalAvgProcTime += avgProcTime;
      participatingAppCount++;
    }

  }

  private void buildTotalStats(String metricName, long value, long currentTime) throws InterruptedException {
    buildAbsoluteStats(metricName, value, currentTime);
  }

  /**
   * Exclude from total.
   *
   * @param ctx the ctx
   *
   * @return true, if successful
   */
  private boolean excludeFromTotal(Context ctx) {
    return isSelfIgnored() && getServletContext().equals(ctx.getServletContext());
  }

  /**
   * Reset.
   */
  public void reset() {
    if (containerWrapper == null) {
      logger.error("Cannot reset application stats. Container wrapper is not set.");
    } else {
      TomcatContainer tomcatContainer = getContainerWrapper().getTomcatContainer();
      if (tomcatContainer != null) {
        resetContextStats(tomcatContainer);
      }
    }

    resetStats("total.requests");
    resetStats("total.errors");
    resetStats("total.avg_proc_time");
  }

  private void resetContextStats(TomcatContainer tomcatContainer) {
    for (Context ctx : tomcatContainer.findContexts()) {
      if (ctx != null && ctx.getName() != null) {
        String appName = "".equals(ctx.getName()) ? "/" : ctx.getName();
        reset(appName);
      }
    }
  }

  @Override
  protected void resetStats(String metricName) {
    AppStatsCollectorBean statsCollection = new AppStatsCollectorBean();
    statsCollection.resetStats(metricName);
  }

  /**
   * Reset.
   *
   * @param appName the app name
   */
  public void reset(String appName) {
    resetStats("app.requests." + appName);
    resetStats("app.proc_time." + appName);
    resetStats("app.errors." + appName);
    resetStats("app.avg_proc_time." + appName);
  }

}
