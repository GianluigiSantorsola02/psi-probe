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
package psiprobe.beans;

import java.lang.management.ManagementFactory;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import psiprobe.model.jmx.AsyncClusterSender;
import psiprobe.model.jmx.Cluster;
import psiprobe.model.jmx.ClusterSender;
import psiprobe.model.jmx.PooledClusterSender;
import psiprobe.model.jmx.SyncClusterSender;
import psiprobe.tools.JmxTools;

/**
 * The Class ClusterWrapperBean.
 */
public class ClusterWrapperBean {

  /**
   * Gets the cluster.
   *
   * @param serverName the server name
   * @param hostName the host name
   * @param loadMembers the load members
   *
   * @return the cluster
   *
   * @throws Exception the exception
   */
  public Cluster getCluster(String serverName, String hostName, boolean loadMembers)
          throws getClusterException, MalformedObjectNameException {

    Cluster cluster = null;

    MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
    ObjectName membershipOName =
            new ObjectName(serverName + ":type=ClusterMembership,host=" + hostName);
    ObjectName receiverOName =
            new ObjectName(serverName + ":type=ClusterReceiver,host=" + hostName);
    ObjectName senderOName = new ObjectName(serverName + ":type=ClusterSender,host=" + hostName);

    Set<ObjectInstance> clusters = getClusters(mbeanServer, hostName);
    Set<ObjectInstance> membership = getMembership(mbeanServer, membershipOName);

    if (clusters != null && !clusters.isEmpty() && membership != null && !membership.isEmpty()) {
      ObjectName clusterOName = clusters.iterator().next().getObjectName();
      cluster = createCluster(mbeanServer, clusterOName);

      populateClusterWithMembershipInfo(mbeanServer, membershipOName, cluster);
      populateClusterWithReceiverInfo(mbeanServer, receiverOName, cluster);
      populateClusterWithSenderInfo(mbeanServer, senderOName, cluster);

      if (loadMembers) {
        ObjectName[] senders = getSenderObjectNames(mbeanServer, senderOName);
        populateClusterWithSenders(mbeanServer, senders, cluster);
      }
    }

    return cluster;
  }

  private void populateClusterWithSenders(MBeanServer mbeanServer, ObjectName[] senders, Cluster cluster) {

    for (ObjectName senderOName : senders) {
      ClusterSender sender = getClusterSender(cluster);
      sender.setResend(getSenders(mbeanServer, senderOName));
    }
  }

  private boolean getSenders(MBeanServer mbeanServer, ObjectName senderOName) {
    return false;
  }

  private ObjectName[] getSenderObjectNames(MBeanServer mbeanServer, ObjectName senderOName) {
    return null;
  }

  private void populateClusterWithSenderInfo(MBeanServer mbeanServer, ObjectName senderOName, Cluster cluster) {

  }

  private void populateClusterWithReceiverInfo(MBeanServer mbeanServer, ObjectName receiverOName, Cluster cluster) {
  }

  private void populateClusterWithMembershipInfo(MBeanServer mbeanServer, ObjectName membershipOName, Cluster cluster) {
  }

  private Cluster createCluster(MBeanServer mbeanServer, ObjectName clusterOName) {
    return null;
  }

  private Set<ObjectInstance> getMembership(MBeanServer mbeanServer, ObjectName membershipOName) {
    return null;
  }

  private Set<ObjectInstance> getClusters(MBeanServer mbeanServer, String hostName) {
    return null;
  }

  private static ClusterSender getClusterSender(Cluster cluster) {
    ClusterSender sender;

    if ("pooled".equals(cluster.getSenderReplicationMode())) {
      sender = new PooledClusterSender();
    } else if ("synchronous".equals(cluster.getSenderReplicationMode())) {
      sender = new SyncClusterSender();
    } else if ("asynchronous".equals(cluster.getSenderReplicationMode())
        || "fastasyncqueue".equals(cluster.getSenderReplicationMode())) {
      sender = new AsyncClusterSender();
    } else {
      sender = new ClusterSender();
    }
    return sender;
  }

  private class getClusterException extends Exception {

    public getClusterException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
