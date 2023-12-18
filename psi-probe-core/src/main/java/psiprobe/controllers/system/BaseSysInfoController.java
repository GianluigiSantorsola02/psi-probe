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
import org.springframework.web.servlet.ModelAndView;
import psiprobe.beans.RuntimeInfoAccessorBean;
import psiprobe.controllers.AbstractTomcatContainerController;
import psiprobe.model.SystemInformation;
import psiprobe.tools.SecurityUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates an instance of SystemInformation.
 */
public abstract class BaseSysInfoController extends AbstractTomcatContainerController {

  /** The filter out keys. */
  private static final ThreadLocal<List<String>> threadLocalFilterOutKeys = new ThreadLocal<>();

  public void processUserRequest() {
    // Retrieve or create user-specific filterOutKeys list
    List<String> userFilterOutKeys = getUserFilterOutKeys();

    // Set the filterOutKeys for the current thread
    setThreadLocalFilterOutKeys(userFilterOutKeys);

    // Ensure to clean up the thread-local variable after the request is processed
    cleanupThreadLocal();
  }


  private List<String> getUserFilterOutKeys() {
    // Retrieve or create user-specific filterOutKeys list
    List<String> userFilterOutKeys = threadLocalFilterOutKeys.get();
    if (userFilterOutKeys == null) {
      userFilterOutKeys = new ArrayList<>();
      threadLocalFilterOutKeys.set(userFilterOutKeys);
    }
    return userFilterOutKeys;
  }

  private void setThreadLocalFilterOutKeys(List<String> userFilterOutKeys) {
    threadLocalFilterOutKeys.set(userFilterOutKeys);
  }

  private void cleanupThreadLocal() {
    // Remove the thread-local variable after the request is processed
    threadLocalFilterOutKeys.remove();
  }

  /** The runtime info accessor. */
  private static final ThreadLocal<RuntimeInfoAccessorBean> threadLocalRuntimeInfoAccessor = new ThreadLocal<>();

  public void processUserRequest1() {
    // Retrieve or set user-specific runtimeInfoAccessor
    RuntimeInfoAccessorBean userRuntimeInfoAccessor = getUserRuntimeInfoAccessor();

    // Set the runtimeInfoAccessor for the current thread
    setThreadLocalRuntimeInfoAccessor(userRuntimeInfoAccessor);

    // Ensure to clean up the thread-local variable after the request is processed
    cleanupThreadLocal1();
  }

  private RuntimeInfoAccessorBean getUserRuntimeInfoAccessor() {
    // Retrieve or set user-specific runtimeInfoAccessor
    RuntimeInfoAccessorBean userRuntimeInfoAccessor = threadLocalRuntimeInfoAccessor.get();
    if (userRuntimeInfoAccessor == null) {
      userRuntimeInfoAccessor = new RuntimeInfoAccessorBean();  // Create a new instance if not yet initialized
      threadLocalRuntimeInfoAccessor.set(userRuntimeInfoAccessor);
    }
    return userRuntimeInfoAccessor;
  }

  private void setThreadLocalRuntimeInfoAccessor(RuntimeInfoAccessorBean userRuntimeInfoAccessor) {
    threadLocalRuntimeInfoAccessor.set(userRuntimeInfoAccessor);
  }

  private void cleanupThreadLocal1() {
    // Remove the thread-local variable after the request is processed
    threadLocalRuntimeInfoAccessor.remove();
  }

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


  public BaseSysInfoController(RuntimeInfoAccessorBean runtimeInfoAccessor) {
    this.setThreadLocalRuntimeInfoAccessor(runtimeInfoAccessor);
  }


  /**
   * Gets the runtime info accessor.
   *
   * @return the runtime info accessor
   */
  public RuntimeInfoAccessorBean getRuntimeInfoAccessor() {
    return getUserRuntimeInfoAccessor();
  }

  /**
   * Gets the collection period.
   *
   * @return the collection period
   */
  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    SystemInformation systemInformation = new SystemInformation();
    systemInformation
        .setAppBase(getContainerWrapper().getTomcatContainer().getAppBase().getAbsolutePath());
    systemInformation.setConfigBase(getContainerWrapper().getTomcatContainer().getConfigBase());

    Map<String, String> sysProps = new HashMap<>();
    for (String propertyName : System.getProperties().stringPropertyNames()) {
      String propertyValue = System.getProperty(propertyName);
      sysProps.put(propertyName, propertyValue);
    }

    if (!SecurityUtils.hasAttributeValueRole(getServletContext())) {
      for (String key : threadLocalFilterOutKeys.get()) {
        sysProps.remove(key);
      }
    }

    processUserRequest();
    processUserRequest1();
    systemInformation.setSystemProperties(sysProps);

    ModelAndView mv = new ModelAndView(getViewName());
    mv.addObject("systemInformation", systemInformation);
    mv.addObject("runtime", getRuntimeInfoAccessor().getRuntimeInformation());
    mv.addObject("collectionPeriod", getCollectionPeriod());
    return mv;
  }

  @Value("jmxremote.password")
  public void setFilterOutKeys(List<String> filterOutKeys) {
  }
}
