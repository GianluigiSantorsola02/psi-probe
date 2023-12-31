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

import psiprobe.beans.ContainerListenerBean;
import psiprobe.model.Connector;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;

/**
 * The Class ConnectorStatsCollectorBean.
 */
public class ConnectorStatsCollectorBean extends AbstractStatsCollectorBean {

  /** The listener bean. */
  private final ContainerListenerBean listenerBean ;

  public ConnectorStatsCollectorBean() {
    listenerBean = new ContainerListenerBean( null );
  }


  @Override
  public void collect() throws ContainerListenerBean.CustomException, MalformedObjectNameException, InstanceNotFoundException, InterruptedException {
    for (Connector connector : listenerBean.getConnectors(false)) {
      String statName = "stat.connector." + connector.getProtocolHandler();
      buildDeltaStats(statName + ".requests", connector.getRequestCount());
      buildDeltaStats(statName + ".errors", connector.getErrorCount());
      buildDeltaStats(statName + ".sent", connector.getBytesSent());
      buildDeltaStats(statName + ".received", connector.getBytesReceived());
      buildDeltaStats(statName + ".proc_time", connector.getProcessingTime());
    }
  }

  /**
   * Reset.
   *
   * @param connectorName the connector name
   */
  public void reset(String connectorName) {
    String statName = "stat.connector." + connectorName;
    resetStats(statName + ".requests");
    resetStats(statName + ".errors");
    resetStats(statName + ".sent");
    resetStats(statName + ".received");
    resetStats(statName + ".proc_time");
  }

}
