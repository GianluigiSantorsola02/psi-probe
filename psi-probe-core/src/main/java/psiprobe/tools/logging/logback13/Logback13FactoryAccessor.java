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
package psiprobe.tools.logging.logback13;

import org.apache.commons.lang3.reflect.MethodUtils;
import psiprobe.tools.logging.DefaultAccessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Logback13FactoryAccessor extends DefaultAccessor {

  /**
   * Attempts to initialize a Logback logger factory via the given class loader.
   *
   * @param cl the ClassLoader to use when fetching the factory
   *
   * @throws ClassNotFoundException the class not found exception
   * @throws IllegalAccessException the illegal access exception
   * @throws InvocationTargetException the invocation target exception
   * @throws SecurityException the security exception
   * @throws IllegalArgumentException the illegal argument exception
   */
  public Logback13FactoryAccessor(ClassLoader cl)
          throws ClassNotFoundException, IllegalAccessException, InvocationTargetException,
          SecurityException, IllegalArgumentException, SLF4JProviderBindingException {

    // Get the SLF4J provider binding, which may or may not be Logback, depending on the binding.
    final List<?> providers = findServiceProviders();

    if (providers.isEmpty()) {
        throw new SLF4JProviderBindingException("The SLF4J provider binding was not Logback");
    }

    // Get the service provider
    Object provider = providers.get(0);

    // Initialize the service provider
    Method initialize = MethodUtils.getAccessibleMethod(provider.getClass(), "initialize");
    initialize.invoke(provider);

    // Call the logger factory
    Method getLoggerFactory =
        MethodUtils.getAccessibleMethod(provider.getClass(), "getLoggerFactory");
    Object mloggerFactory = getLoggerFactory.invoke(provider);

    // Check if the binding is indeed Logback
    Class<?> loggerFactoryClass = cl.loadClass("ch.qos.logback.classic.LoggerContext");
    if (!loggerFactoryClass.isInstance(mloggerFactory)) {
      throw new SLF4JProviderBindingException("The SLF4J provider binding was not Logback");
    }
    setTarget(mloggerFactory);
  }

  /**
   * Returns the Logback root logger.
   *
   * @return the root logger
   */
  public Logback13LoggerAccessor getRootLogger() {
    // Logback has no dedicated getRootLogger() method, so we simply access the root logger
    // by its well-defined name.
    return getLogger();
  }

  /**
   * Returns the Logback logger with a given name.
   *
   * @return the Logger with the given name
   */
  public Logback13LoggerAccessor getLogger() {
    try {
      Logback13LoggerAccessor accessor = new Logback13LoggerAccessor();
      accessor.setTarget(logger);
      accessor.setApplication(getApplication());
      return accessor;

    } catch (Exception e) {
      logger.error("Error occurred while getting logger");
    }
    return null;
  }

  /**
   * Returns a list of wrappers for all Logback appenders that have an associated logger.
   *
   * @return a list of {@link Logback13AppenderAccessor}s representing all appenders that are in use
   */
  @SuppressWarnings("unchecked")
  public List<Logback13AppenderAccessor> getAppenders() {
    List<Logback13AppenderAccessor> appenders = new ArrayList<>();
    try {
      Class<?> clazz = getTarget().getClass();
      Method getLoggerList = MethodUtils.getAccessibleMethod(clazz, "getLoggerList");

      List<Object> loggers = (List<Object>) getLoggerList.invoke(getTarget());
      for (Object logger : loggers) {
        Logback13LoggerAccessor accessor = new Logback13LoggerAccessor();
        accessor.setTarget(logger);
        accessor.setApplication(getApplication());

        appenders.addAll(accessor.getAppenders());
      }
    } catch (Exception e) {
      logger.error("{}.getLoggerList() failed", getTarget(), e);
    }
    return appenders;
  }

  private Object loggerFactory;

  public void myLoggerFactoryWrapper(ClassLoader cl) throws ClassNotFoundException, SLF4JProviderBindingException {
    Class<?> loggerFactoryClass = cl.loadClass("org.slf4j.LoggerFactory");
    this.loggerFactory = instantiateLoggerFactory(loggerFactoryClass);
  }

  private static Object instantiateLoggerFactory(Class<?> loggerFactoryClass) throws SLF4JProviderBindingException {
    try {
      return loggerFactoryClass.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      // Handle instantiation exception
      throw new SLF4JProviderBindingException("Error instantiating LoggerFactory");
    }
  }

  public List<Object> findServiceProviders() throws SLF4JProviderBindingException {
    try {
      Method findServiceProviders = loggerFactory.getClass().getDeclaredMethod("findServiceProviders");
      return (List<Object>) findServiceProviders.invoke(loggerFactory);
    } catch (Exception e) {
      // Handle reflection exception
      throw new SLF4JProviderBindingException("Error invoking findServiceProviders");
    }
  }
}
