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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.util.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import psiprobe.TomcatContainer;
import psiprobe.model.ApplicationResource;

/**
 * This class wires support for Tomcat "privileged" context functionality into Spring. If
 * application context is privileged Tomcat would always call servlet.setWrapper method on each
 * request. ContainerWrapperBean wires the passed wrapper to the relevant Tomcat container adapter
 * class, which in turn helps the Probe to interpret the wrapper.
 */
public class ContainerWrapperBean {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(ContainerWrapperBean.class);

  /** The tomcat container. */
  private volatile TomcatContainer tomcatContainer;

  /** The lock. */
  private final Object lock = new Object();

  /** List of class names to adapt particular Tomcat implementation to TomcatContainer interface. */
  private final List<String> adapterClasses;

  /** The resource resolver. */
  private ResourceResolver resourceResolver;

  /** The force first adapter. */
  private final boolean forceFirstAdapter;
  private final Map<String, ResourceResolver> resourceResolvers;

  public ContainerWrapperBean(List<String> adapterClasses, boolean forceFirstAdapter, Map<String, ResourceResolver> resourceResolvers) {
    this.adapterClasses = adapterClasses;
    this.forceFirstAdapter = forceFirstAdapter;
    this.resourceResolvers = resourceResolvers;
  }


  /**
   * Sets the force first adapter. Setting this property to true will override the server polling
   * each adapter performs to test for compatibility. Instead, it will use the first one in the
   * adapterClasses list.
   *
   * @param wrapper the new force first adapter
   */

  @Value("false")

  public void setWrapper(Wrapper wrapper) {
    if (tomcatContainer == null) {
      if (lock.equals(wrapper)) {
        try {
          if (tomcatContainer == null) {
            initializeTomcatContainer(wrapper);
            return;
          }
        } catch (Exception e) {
          logger.error("", e);
        }
      }
    }

    unregisterContainerAdapter(wrapper);
  }
  private void initializeTomcatContainer(Wrapper wrapper) {
    String serverInfo = ServerInfo.getServerInfo();
    logger.info("Server info: {}", serverInfo);

    for (String className : adapterClasses) {
      try {
        Object obj = Class.forName(className).getDeclaredConstructor().newInstance();
        logger.debug("Testing container adapter: {}", className);

        if (obj instanceof TomcatContainer) {
          TomcatContainer container = (TomcatContainer) obj;

          if (forceFirstAdapter || container.canBoundTo(serverInfo)) {
            logger.info("Using {}", className);
            tomcatContainer = container;
            tomcatContainer.setWrapper(wrapper);
            break;
          }

          logger.debug("Cannot bind {} to {}", className, serverInfo);
        } else {
          logger.error("{} does not implement {}", className, TomcatContainer.class.getName());
        }
      } catch (Exception e) {
        logger.debug("", e);
        logger.info("Failed to load {}", className);
      }
    }

    if (tomcatContainer == null) {
      logger.error("No suitable container adapter found!");
    }
  }

  private void unregisterContainerAdapter(Wrapper wrapper) {
    try {
      if (tomcatContainer != null && wrapper == null) {
        logger.info("Unregistering container adapter");
        tomcatContainer.setWrapper(null);
      }
    } catch (Exception e) {
      logger.error("Could not unregister container adapter", e);
    }
  }
  /**
   * Gets the tomcat container.
   *
   * @return the tomcat container
   */
  public TomcatContainer getTomcatContainer() {
    return tomcatContainer;
  }

  /**
   * Gets the resource resolver.
   *
   * @return the resource resolver
   */
  public ResourceResolver getResourceResolver() {
    if (resourceResolver == null) {
      if (System.getProperty("jboss.server.name") != null) {
        resourceResolver = resourceResolvers.get("jboss");
        logger.info("Using JBOSS resource resolver");
      } else {
        resourceResolver = resourceResolvers.get("default");
        logger.info("Using DEFAULT resource resolver");
      }
    }
    return resourceResolver;
  }

  /**
   * Gets the data sources.
   *
   *
   */
  public static class DataSourceException extends Exception {
    public DataSourceException(String message) {
      super(message);
    }

  }
  public List<ApplicationResource> getDataSources() throws DataSourceException {
    try {
      List<ApplicationResource> resources = new ArrayList<>(getPrivateDataSources());
      resources.addAll(getGlobalDataSources());
      return resources;
    } catch (Exception e) {
      throw new DataSourceException("Error retrieving data sources: " + e.getMessage());
    }
  }

  /**
   * Gets the private data sources.
   *
   * @return the private data sources
   *
   * @throws DataSourceException the exception
   */

  public List<ApplicationResource> getPrivateDataSources() throws DataSourceException {
    try {
      List<ApplicationResource> resources = new ArrayList<>();
      if (tomcatContainer != null && getResourceResolver().supportsPrivateResources()) {
        for (Context app : getTomcatContainer().findContexts()) {
          List<ApplicationResource> appResources = getResourceResolver().getApplicationResources(app, this);
          // add only those resources that have data source info
          filterDataSources(appResources, resources);
        }
      }
      return resources;
    } catch (Exception e) {
      throw new DataSourceException("Error retrieving private data sources: " + e.getMessage());
    }
  }

  /**
   * Gets the global data sources.
   *
   * @return the global data sources
   *
   * @throws DataSourceException the exception
   */
  public List<ApplicationResource> getGlobalDataSources() throws DataSourceException {
    try {
      List<ApplicationResource> resources = new ArrayList<>();
      if (getResourceResolver().supportsGlobalResources()) {
        List<ApplicationResource> globalResources = getResourceResolver().getApplicationResources();
        // add only those resources that have data source info
        filterDataSources(globalResources, resources);
      }
      return resources;
    } catch (Exception e) {
      throw new DataSourceException("Error retrieving global data sources: " + e.getMessage());
    }
  }

  /**
   * Filter data sources.
   *
   * @param resources the resources
   * @param dataSources the data sources
   */
  protected void filterDataSources(List<ApplicationResource> resources,
      List<ApplicationResource> dataSources) {

    for (ApplicationResource res : resources) {
      if (res.getDataSourceInfo() != null) {
        dataSources.add(res);
      }
    }
  }

}
