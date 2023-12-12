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
package psiprobe;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import oracle.ucp.UniversalConnectionPoolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import psiprobe.beans.stats.collectors.AppStatsCollectorBean;
import psiprobe.beans.stats.collectors.ClusterStatsCollectorBean;
import psiprobe.beans.stats.collectors.ConnectorStatsCollectorBean;
import psiprobe.beans.stats.collectors.DatasourceStatsCollectorBean;
import psiprobe.beans.stats.collectors.JvmMemoryStatsCollectorBean;
import psiprobe.beans.stats.collectors.RuntimeStatsCollectorBean;
import psiprobe.beans.stats.listeners.MemoryPoolMailingListener;
import psiprobe.beans.stats.listeners.StatsCollectionListener;
import psiprobe.beans.stats.providers.ConnectorSeriesProvider;
import psiprobe.beans.stats.providers.MultipleSeriesProvider;
import psiprobe.beans.stats.providers.StandardSeriesProvider;
import psiprobe.model.stats.StatsCollection;
import psiprobe.tools.Mailer;

/**
 * The Class ProbeConfigStats.
 */
@Configuration
public class ProbeConfigStats {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(ProbeConfigStats.class);

  /**
   * Gets the stats collection.
   *
   * @return the stats collection
   */
  @Bean(name = "statsCollection")
  public StatsCollection getStatsCollection() throws UniversalConnectionPoolException {
    logger.debug("Instantiated statsCollection");
    String storagePath = "";
    XStream xstream = new XStream();
    return new StatsCollection(xstream, storagePath);
  }

  /**
   * Gets the connector stats collector bean.
   *
   * @return the connector stats collector bean
   */
  @Bean(name = "connectorStatsCollector")
  public ConnectorStatsCollectorBean getConnectorStatsCollectorBean() {
    logger.debug("Instantiated connectorStatsCollector");
    return new ConnectorStatsCollectorBean();
  }

  /**
   * Gets the cluster stats collector bean.
   *
   * @return the cluster stats collector bean
   */
  @Bean(name = "clusterStatsCollector")
  public ClusterStatsCollectorBean getClusterStatsCollectorBean() {
    logger.debug("Instantiated clusterStatsCollector");
    return new ClusterStatsCollectorBean();
  }

  /**
   * Gets the runtime stats collector bean.
   *
   * @return the runtime stats collector bean
   */
  @Bean(name = "runtimeStatsCollector")
  public RuntimeStatsCollectorBean getRuntimeStatsCollectorBean() {
    logger.debug("Instantiated runtimeStatsCollector");
    return new RuntimeStatsCollectorBean();
  }

  /**
   * Gets the app stats collector bean.
   *
   * @return the app stats collector bean
   */
  @Bean(name = "appStatsCollector")
  public AppStatsCollectorBean getAppStatsCollectorBean() {
    logger.debug("Instantiated appStatsCollector");
    return new AppStatsCollectorBean();
  }

  /**
   * Gets the jvm memory stats collector bean.
   *
   * @return the jvm memory stats collector bean
   */
  @Bean(name = "memoryStatsCollector")
  public JvmMemoryStatsCollectorBean getJvmMemoryStatsCollectorBean() {
    logger.debug("Instantiated memoryStatsCollector");
    return new JvmMemoryStatsCollectorBean();
  }

  /**
   * Gets the datasource stats collector bean.
   *
   * @return the datasource stats collector bean
   */
  @Bean(name = "datasourceStatsCollector")
  public DatasourceStatsCollectorBean getDatasourceStatsCollectorBean() {
    logger.debug("Instantiated datasourceStatsCollector");
    return new DatasourceStatsCollectorBean();
  }

  /**
   * Gets the memory pool mailing listener.
   *
   * @return the memory pool mailing listener
   */
  @Bean(name = "listeners")
  public List<StatsCollectionListener> getMemoryPoolMailingListener() {
    logger.debug("Instantiated listeners");
    List<StatsCollectionListener> list = new ArrayList<>();
    Mailer mailer = new Mailer();
    list.add(new MemoryPoolMailingListener(mailer));
    return list;
  }

  /**
   * Gets the connector series provider.
   *
   * @return the connector series provider
   */
  @Bean(name = "rcn")
  public ConnectorSeriesProvider getConnectorSeriesProvider() {
    logger.debug("Instantiated rcn");
    return new ConnectorSeriesProvider();
  }

  /**
   * Gets the cl traffic.
   *
   * @return the cl traffic
   */
  @Bean(name = "clTraffic")
  public StandardSeriesProvider getClTraffic() {
    logger.debug("Instantiated clTraffic");
    List<String> list = new ArrayList<>();
    list.add("cluster.sent");
    list.add("cluster.received");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the cl request.
   *
   * @return the cl request
   */
  @Bean(name = "clRequest")
  public StandardSeriesProvider getClRequest() {
    logger.debug("Instantiated clRequest");
    List<String> list = new ArrayList<>();
    list.add("cluster.req.sent");
    list.add("cluster.req.received");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the connector.
   *
   * @return the connector
   */
  @Bean(name = "connector")
  public StandardSeriesProvider getConnector() {
    logger.debug("Instantiated connector");
    List<String> list = new ArrayList<>();
    list.add("stat.connector.{0}.requests");
    list.add("stat.connector.{0}.errors");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the traffic.
   *
   * @return the traffic
   */
  @Bean(name = "traffic")
  public StandardSeriesProvider getTraffic() {
    logger.debug("Instantiated traffic");
    List<String> list = new ArrayList<>();
    list.add("stat.connector.{0}.sent");
    list.add("stat.connector.{0}.received");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the connector proc time.
   *
   * @return the connector proc time
   */
  @Bean(name = "connectorProcTime")
  public StandardSeriesProvider getConnectorProcTime() {
    logger.debug("Instantiated connectorProcTime");
    List<String> list = new ArrayList<>();
    list.add("stat.connector.{0}.proc_time");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the memory usage.
   *
   * @return the memory usage
   */
  @Bean(name = "memoryUsage")
  public StandardSeriesProvider getMemoryUsage() {
    logger.debug("Instantiated memoryUsage");
    List<String> list = new ArrayList<>();
    list.add("memory.pool.{0}");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the os memory.
   *
   * @return the os memory
   */
  @Bean(name = "osMemory")
  public StandardSeriesProvider getOsMemory() {
    logger.debug("Instantiated osMemory");
    List<String> list = new ArrayList<>();
    list.add("os.memory.physical");
    list.add("os.memory.committed");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the swap usage.
   *
   * @return the swap usage
   */
  @Bean(name = "swapUsage")
  public StandardSeriesProvider getSwapUsage() {
    logger.debug("Instantiated swapUsage");
    List<String> list = new ArrayList<>();
    list.add("os.memory.swap");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the cpu usage.
   *
   * @return the cpu usage
   */
  @Bean(name = "cpuUsage")
  public StandardSeriesProvider getCpuUsage() {
    logger.debug("Instantiated cpuUsage");
    List<String> list = new ArrayList<>();
    list.add("os.cpu");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the fd usage.
   *
   * @return the fd usage
   */
  @Bean(name = "fdUsage")
  public StandardSeriesProvider getFdUsage() {
    logger.debug("Instantiated fdUsage");
    List<String> list = new ArrayList<>();
    list.add("os.fd.open");
    list.add("os.fd.max");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the app req.
   *
   * @return the app req
   */
  @Bean(name = "appReq")
  public StandardSeriesProvider getAppReq() {
    logger.debug("Instantiated appReq");
    List<String> list = new ArrayList<>();
    list.add("app.requests.{0}");
    list.add("app.errors.{0}");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the app avg proc time.
   *
   * @return the app avg proc time
   */
  @Bean(name = "appAvgProcTime")
  public StandardSeriesProvider getAppAvgProcTime() {
    logger.debug("Instantiated appAvgProcTime");
    List<String> list = new ArrayList<>();
    list.add("app.avgProcTime.{0}");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the total avg proc time.
   *
   * @return the total avg proc time
   */
  @Bean(name = "totalAvgProcTime")
  public StandardSeriesProvider getTotalAvgProcTime() {
    logger.debug("Instantiated totalAvgProcTime");
    List<String> list = new ArrayList<>();
    list.add("total.avgProcTime");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the total req.
   *
   * @return the total req
   */
  @Bean(name = "totalReq")
  public StandardSeriesProvider getTotalReq() {
    logger.debug("Instantiated totalReq");
    List<String> list = new ArrayList<>();
    list.add("total.requests");
    list.add("total.errors");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the datasource usage.
   *
   * @return the datasource usage
   */
  @Bean(name = "datasourceUsage")
  public StandardSeriesProvider getDatasourceUsage() {
    logger.debug("Instantiated datasourceUsage");
    List<String> list = new ArrayList<>();
    list.add("ds.est.{0}");
    list.add("ds.busy.{0}");

    StandardSeriesProvider provider = new StandardSeriesProvider();
    provider.setStatNames(list);
    return provider;
  }

  /**
   * Gets the all app avg proc time.
   *
   * @return the all app avg proc time
   */
  @Bean(name = "allAppAvgProcTime")
  public MultipleSeriesProvider getAllAppAvgProcTime() {
    logger.debug("Instantiated allAppAvgProcTime");
    MultipleSeriesProvider provider = new MultipleSeriesProvider();
    provider.setMovingAvgFrame(10);
    provider.setStatNamePrefix("app.avgProcTime.");
    provider.setTop(4);
    return provider;
  }

  /**
   * Gets the all app req.
   *
   * @return the all app req
   */
  @Bean(name = "allAppReq")
  public MultipleSeriesProvider getAllAppReq() {
    logger.debug("Instantiated allAppReq");
    MultipleSeriesProvider provider = new MultipleSeriesProvider();
    provider.setMovingAvgFrame(10);
    provider.setStatNamePrefix("app.requests.");
    provider.setTop(4);
    return provider;
  }

}
