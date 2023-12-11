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
package psiprobe.tools.logging.jdk;

import org.apache.commons.lang3.reflect.MethodUtils;
import psiprobe.tools.logging.DefaultAccessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The Class Jdk14ManagerAccessor.
 */
public class Jdk14ManagerAccessor extends DefaultAccessor {

  /**
   * Instantiates a new jdk14 manager accessor.
   *
   * @param cl the cl
   *
   * @throws ClassNotFoundException the class not found exception
   * @throws IllegalAccessException the illegal access exception
   * @throws InvocationTargetException the invocation target exception
   */
  public Jdk14ManagerAccessor(ClassLoader cl)
      throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {

    Class<?> clazz = cl.loadClass("java.util.logging.LogManager");
    Method getManager = MethodUtils.getAccessibleMethod(clazz, "getLogManager");
    Object manager = getManager.invoke(null);
    if (manager == null) {
      throw new NullPointerException(clazz.getName() + ".getLogManager() returned null");
    }
    setTarget(manager);
  }

  /**
   * Gets the root logger.
   *
   * @return the root logger
   */
  public Jdk14LoggerAccessor getRootLogger() {
    return getLogger("");
  }

  /**
   * Gets the logger.
   *
   * @param name the name
   *
   * @return the logger
   */
  public Jdk14LoggerAccessor getLogger(String name) {
    try {
      Object logger = MethodUtils.invokeMethod(getTarget(), "getLogger", name);
      if (logger == null) {
        throw new NullPointerException(
            getTarget().getClass().getName() + "#getLogger(\"" + name + "\") returned null");
      }
      Jdk14LoggerAccessor accessor = new Jdk14LoggerAccessor();
      accessor.setTarget(logger);
      accessor.setApplication(getApplication());
      return accessor;
    } catch (Exception e) {
      logger.error("Error occurred while getting logger");
    }
    return null;
  }

}
