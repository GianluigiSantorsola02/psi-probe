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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import psiprobe.beans.ContainerWrapperBean;
import psiprobe.model.ApplicationResource;
import psiprobe.model.DataSourceInfo;

import javax.inject.Inject;

/**
 * The Class DatasourceStatsCollectorBean.
 */
public class DatasourceStatsCollectorBean extends AbstractStatsCollectorBean {

  /** The Constant PREFIX_ESTABLISHED. */
  private static final String PREFIX_ESTABLISHED = "ds.est.";

  /** The Constant PREFIX_BUSY. */
  private static final String PREFIX_BUSY = "ds.busy.";

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(DatasourceStatsCollectorBean.class);

  /** The container wrapper. */
  private ContainerWrapperBean containerWrapper;

  @Inject
  public void cointanerWrapper(ContainerWrapperBean containerWrapper) {
    this.containerWrapper = containerWrapper;
  }

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

  @Override
  public void collect() throws InterruptedException, ContainerWrapperBean.DataSourceException {
    long currentTime = System.currentTimeMillis();
    if (containerWrapper == null) {
      logger.error("Cannot collect data source stats. Container wrapper is not set.");
    } else {
      for (ApplicationResource ds : getContainerWrapper().getDataSources()) {
        String appName = ds.getApplicationName();
        String name = (appName == null ? "" : appName) + '/' + ds.getName();
        DataSourceInfo dsi = ds.getDataSourceInfo();
        int numEstablished = dsi.getEstablishedConnections();
        int numBusy = dsi.getBusyConnections();
        logger.trace("Collecting stats for datasource: {}", name);
        buildAbsoluteStats(PREFIX_ESTABLISHED + name, numEstablished, currentTime);
        buildAbsoluteStats(PREFIX_BUSY + name, numBusy, currentTime);
      }
      logger.debug("datasource stats collected in {}ms", System.currentTimeMillis() - currentTime);
    }
  }


  public void reset() throws ResetException, ContainerWrapperBean.DataSourceException, ContainerWrapperNotSetException {
    if (containerWrapper == null) {
      logger.error("Cannot reset application stats. Container wrapper is not set.");
      throw new ContainerWrapperNotSetException("Container wrapper is not set.");
    } else {
      for (ApplicationResource ds : getContainerWrapper().getDataSources()) {
        reset(ds.getName());
      }
    }
  }

  public static class ResetException extends Exception {
    public ResetException(String message) {
      super(message);
    }
  }

  public void reset(String name) throws ResetException {
    try {
      resetStats(PREFIX_ESTABLISHED + name);
      resetStats(PREFIX_BUSY + name);
    } catch (Exception e) {
      throw new ResetException("Error occurred while resetting stats: " + e.getMessage());
    }
  }

  private static class ContainerWrapperNotSetException extends Exception {
    public ContainerWrapperNotSetException(String s) {
      super(s);
    }
  }
}
