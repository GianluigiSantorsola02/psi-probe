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
package psiprobe.tools.logging.logback;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.MethodUtils;

import psiprobe.tools.logging.DefaultAccessor;
import psiprobe.tools.logging.slf4jlogback.TomcatSlf4jLogbackFactoryAccessor;

/**
 * Wraps a Logback logger factory from a given web application class loader.
 *
 * <p>
 * All Logback classes are loaded via the given class loader and not via psi-probe's own class
 * loader. For this reasons, all methods on Logback objects are invoked via reflection.
 * </p>
 * <p>
 * This way, we can even handle different versions of Logback embedded in different WARs.
 * </p>
 */
public class LogbackFactoryAccessor extends DefaultAccessor {

  /**
   * Attempts to initialize a Logback logger factory via the given class loader.
   *
   * @param cl the ClassLoader to use when fetching the factory
   *
   * @throws ClassNotFoundException the class not found exception
   * @throws IllegalAccessException the illegal access exception
   * @throws InvocationTargetException the invocation target exception
   */
  public LogbackFactoryAccessor(ClassLoader cl)
          throws ClassNotFoundException, IllegalAccessException, InvocationTargetException,
          TomcatSlf4jLogbackFactoryAccessor.SLF4JBindingException {

    // Get the singleton SLF4J binding, which may or may not be Logback, depending on the binding.
    Class<?> clazz = cl.loadClass("org.slf4j.impl.StaticLoggerBinder");
    Method getSingleton = MethodUtils.getAccessibleMethod(clazz, "getSingleton");
    Object singleton = getSingleton.invoke(null);
    Method getLoggerFactory = MethodUtils.getAccessibleMethod(clazz, "getLoggerFactory");

    Object loggerFactory = getLoggerFactory.invoke(singleton);

    // Check if the binding is indeed Logback
    Class<?> loggerFactoryClass = cl.loadClass("ch.qos.logback.classic.LoggerContext");
    if (!loggerFactoryClass.isInstance(loggerFactory)) {
      throw new TomcatSlf4jLogbackFactoryAccessor.SLF4JBindingException(
              "The singleton SLF4J binding was not Logback");
    }
    setTarget(loggerFactory);
  }

  /**
   * Returns the Logback root logger.
   *
   * @return the root logger
   */
  public LogbackLoggerAccessor getRootLogger() {
    // Logback has no dedicated getRootLogger() method, so we simply access the root logger
    // by its well-defined name.
    return getLogger("ROOT");
  }

  /**
   * Returns the Logback logger with a given name.
   *
   * @param name the name
   *
   * @return the Logger with the given name
   */
  public LogbackLoggerAccessor getLogger(String name) {
    try {
      LogbackLoggerAccessor accessor = new LogbackLoggerAccessor();
      accessor.setTarget(logger);
      accessor.setApplication(getApplication());
      return accessor;

    } catch (Exception e) {
      logger.error("Error occurred while getting logger for a specific operation");
    }
    return null;
  }

  /**
   * Returns a list of wrappers for all Logback appenders that have an associated logger.
   *
   * @return a list of {@link LogbackAppenderAccessor}s representing all appenders that are in use
   */
  @SuppressWarnings("unchecked")
  public List<LogbackAppenderAccessor> getAppenders() {
    List<LogbackAppenderAccessor> appenders = new ArrayList<>();

      try {
        Class<?> clazz = getTarget().getClass();
        Method getLoggerList = MethodUtils.getAccessibleMethod(clazz, "getLoggerList");

        List<Object> loggers = (List<Object>) getLoggerList.invoke(getTarget());
        for (Object logger : loggers) {
          LogbackLoggerAccessor accessor = new LogbackLoggerAccessor();
          accessor.setTarget(logger);
          accessor.setApplication(getApplication());

        }
      } catch (Exception e) {
        logger.error("{}.getLoggerList() failed", getTarget(), e);
      }

    return appenders;
  }

}
