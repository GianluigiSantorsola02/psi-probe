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

import org.apache.catalina.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import psiprobe.model.ApplicationResource;
import psiprobe.model.DataSourceInfo;

import javax.management.*;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * An Adapter to convert information retrieved from JBoss JMX beans into internal resource model.
 */
public class JBossResourceResolverBean implements ResourceResolver {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(JBossResourceResolverBean.class);

  @Override
  public MBeanServer getMBeanServer() {
    for (MBeanServer server : MBeanServerFactory.findMBeanServer(null)) {
      if ("jboss".equals(server.getDefaultDomain())
          || "DefaultDomain".equals(server.getDefaultDomain())) {
        return server;
      }
    }
    return null;
  }

  @Override
  public boolean supportsPrivateResources() {
    return false;
  }

  @Override
  public boolean supportsGlobalResources() {
    return true;
  }

  @Override
  public boolean supportsDataSourceLookup() {
    return false;
  }

  @Override
  public List<ApplicationResource> getApplicationResources() {
    List<ApplicationResource> resources = new ArrayList<>();
    MBeanServer server = getMBeanServer();

    if (server != null) {
      try {
        Set<ObjectName> dsNames = server.queryNames(new ObjectName("jboss.jca:service=ManagedConnectionPool,*"), null);
        for (ObjectName managedConnectionPoolOName : dsNames) {
          ApplicationResource resource = createApplicationResource(server, managedConnectionPoolOName);
          resources.add(resource);
        }
      } catch (Exception e) {
        logger.error("There was an error querying JBoss JMX server:", e);
      }
    }

    return resources;
  }

  private ApplicationResource createApplicationResource(MBeanServer server, ObjectName managedConnectionPoolOName) throws InvalidCriteriaException, MalformedObjectNameException, ReflectionException, AttributeNotFoundException, InstanceNotFoundException, MBeanException {
    ApplicationResource resource = new ApplicationResource();
    resource.setName(managedConnectionPoolOName.getKeyProperty("name"));
    resource.setType("jboss");

    setAuthCriteria(server, managedConnectionPoolOName, resource);
    setDataSourceInfo(server, managedConnectionPoolOName, resource);

    return resource;
  }
  public static class InvalidCriteriaException extends Exception {
    public InvalidCriteriaException(String message) {
      super(message);
    }
  }

  private void setAuthCriteria(MBeanServer server, ObjectName managedConnectionPoolOName, ApplicationResource resource) throws InvalidCriteriaException {
    try {
      String criteria = (String) server.getAttribute(managedConnectionPoolOName, "Criteria");
      if ("ByApplication".equals(criteria)) {
        resource.setAuth("Application");
      } else if ("ByContainerAndApplication".equals(criteria)) {
        resource.setAuth("Both");
      } else {
        resource.setAuth("Container");
      }
    } catch (Exception e) {
      throw new InvalidCriteriaException("Error setting authentication criteria: " + e.getMessage());
    }
  }


  private void setDataSourceInfo(MBeanServer server, ObjectName managedConnectionPoolOName, ApplicationResource resource) throws MalformedObjectNameException, ReflectionException, AttributeNotFoundException, InstanceNotFoundException, MBeanException {
    DataSourceInfo dsInfo = new DataSourceInfo();
    dsInfo.setMaxConnections((Integer) server.getAttribute(managedConnectionPoolOName, "MaxSize"));
    dsInfo.setEstablishedConnections((Integer) server.getAttribute(managedConnectionPoolOName, "ConnectionCount"));
    dsInfo.setBusyConnections(((Long) server.getAttribute(managedConnectionPoolOName, "InUseConnectionCount")).intValue());

    ObjectName connectionFactoryOName = new ObjectName("jboss.jca:service=ManagedConnectionFactory,name=" + resource.getName());
    Element elm = (Element) server.getAttribute(connectionFactoryOName, "ManagedConnectionFactoryProperties");

    if (elm != null) {
      processManagedConnectionFactoryProperties(elm, dsInfo);
    }

    dsInfo.setResettable(true);
    resource.setDataSourceInfo(dsInfo);
  }

  private void processManagedConnectionFactoryProperties(Element elm, DataSourceInfo dsInfo) {
    NodeList nl = elm.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node node = nl.item(i);
      Node na = node.getAttributes().getNamedItem("name");
      if (na != null) {
        processManagedConnectionFactoryProperty(dsInfo, node, na);
      }
    }
  }

  private void processManagedConnectionFactoryProperty(DataSourceInfo dsInfo, Node node, Node na) {
    if ("ConnectionURL".equals(na.getNodeValue())) {
      dsInfo.setJdbcUrl(node.getFirstChild().getNodeValue());
    } else if ("UserName".equals(na.getNodeValue())) {
      dsInfo.setUsername(node.getFirstChild().getNodeValue());
    } else if ("JmsProviderAdapterJNDI".equals(na.getNodeValue())) {
      dsInfo.setJdbcUrl(node.getFirstChild().getNodeValue());
    } else if (node.getAttributes().getNamedItem("resource") != null) {
        node.getAttributes().getNamedItem("resource").getNodeValue();
      }
  }

  @Override
  public List<ApplicationResource> getApplicationResources(Context context,
      ContainerWrapperBean containerWrapper) {

    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean resetResource(Context context, String resourceName,
                               ContainerWrapperBean containerWrapper) throws NamingException {
    try {
      ObjectName poolOName =
              new ObjectName("jboss.jca:service=ManagedConnectionPool,name=" + resourceName);
      MBeanServer server = getMBeanServer();
      if (server != null) {
        resetResourceOnServer(poolOName, server, resourceName);
        return true;
      }
      return false;
    } catch (MalformedObjectNameException e) {
      logger.trace("", e);
      throw new NamingException(
              "Resource name: \"" + resourceName + "\" makes a malformed ObjectName");
    }
  }

  private void resetResourceOnServer(ObjectName poolOName, MBeanServer server, String resourceName) {
    try {
      server.invoke(poolOName, "stop", null, null);
      server.invoke(poolOName, "start", null, null);
    } catch (Exception e) {
      logger.error("Could not reset resource '{}'", resourceName, e);
    }
  }

  @Override
  public DataSource lookupDataSource(Context context, String resourceName,
      ContainerWrapperBean containerWrapper) {
    throw new UnsupportedOperationException(
        "This feature has not been implemented for JBoss server yet.");
  }

}
