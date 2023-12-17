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
package psiprobe.controllers.cluster;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.ModelAndView;
import psiprobe.TomcatContainer;
import psiprobe.beans.ClusterWrapperBean;
import psiprobe.controllers.AbstractTomcatContainerController;
import psiprobe.model.jmx.Cluster;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class BaseClusterStatsController.
 */
public abstract class BaseClusterStatsController extends AbstractTomcatContainerController {

  /** The load members. */
  private static final ThreadLocal<Boolean> threadLocalLoadMembers = new ThreadLocal<>();

  public void processUserRequest() {
    // Retrieve or set user-specific loadMembers flag
    boolean userLoadMembers = getUserLoadMembers();

    // Set the loadMembers flag for the current thread
    setThreadLocalLoadMembers(userLoadMembers);

    // Ensure to clean up the thread-local variable after the request is processed
    cleanupThreadLocal();
  }

  private boolean getUserLoadMembers() {
    // Retrieve or set user-specific loadMembers flag
    Boolean userLoadMembers = threadLocalLoadMembers.get();
    if (userLoadMembers == null) {
      userLoadMembers = true;  // Set default value if not yet initialized
      threadLocalLoadMembers.set(userLoadMembers);
    }
    return userLoadMembers;
  }

  void setThreadLocalLoadMembers(boolean userLoadMembers) {
    threadLocalLoadMembers.set(userLoadMembers);
  }

  private void cleanupThreadLocal() {
    // Remove the thread-local variable after the request is processed
    threadLocalLoadMembers.remove();
  }


  /** The collection period. */
  private long collectionPeriod;

  public BaseClusterStatsController() {
    super();
  }


  /**
   * Gets the cluster wrapper.
   *
   * @return the cluster wrapper
   */
  public ClusterWrapperBean getClusterWrapper() {
      return new ClusterWrapperBean();
  }

    /**
   * Gets the collection period.
   *
   * @return the collection period
   */
  public long getCollectionPeriod() {
    return collectionPeriod;
  }

  /**
   * Sets the collection period.
   *
   * @param collectionPeriod the new collection period
   */
  public void setCollectionPeriod(long collectionPeriod) {
    this.collectionPeriod = collectionPeriod;
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    TomcatContainer container = getContainerWrapper().getTomcatContainer();
    Cluster cluster = getClusterWrapper().getCluster(container.getName(), container.getHostName(),
        getUserLoadMembers());
    processUserRequest();
    return new ModelAndView(getViewName()).addObject("cluster", cluster)
        .addObject("collectionPeriod", getCollectionPeriod());
  }

  @Value("false")
  public abstract void setLoadMembers(boolean loadMembers);
}
