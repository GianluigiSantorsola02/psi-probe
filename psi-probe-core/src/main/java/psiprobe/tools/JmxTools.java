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
package psiprobe.tools;

import javax.el.MethodNotFoundException;
import javax.management.*;
import javax.management.openmbean.CompositeData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class JmxTools.
 */
public final class JmxTools {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(JmxTools.class);

  /**
   * Prevent Instantiation.
   */
  private JmxTools() {
    // Prevent Instantiation
  }

  /**
   * Gets the attribute.
   *
   *
   */


   static String notFoundString = " not found for object ";
  public static Object getAttribute(MBeanServer mbeanServer, ObjectName objName, String attrName)
          throws AttributeNotFoundException {
    try {
      return mbeanServer.getAttribute(objName, attrName);
    } catch (javax.management.AttributeNotFoundException e) {
      logger.error("{} does not have '{}' attribute", objName, attrName);
      logger.trace("", e);
      throw new AttributeNotFoundException("1 Attribute '" + attrName + notFoundString + objName + "'");
    } catch (ReflectionException | InstanceNotFoundException | MBeanException e) {
      logger.error("", e);
      throw new AttributeNotFoundException("2 Attribute '" + attrName + "' not found for object '" + objName + "'");
    }
  }

  /**
   * Invoke.
   *
   * @param mbeanServer the mbean server
   * @param objName the obj name
   * @param method the method
   * @param o the o
   * @param s the s
   *
   * @return the object
   *
   */
  public static Object invoke(MBeanServer mbeanServer, ObjectName objName, String method,
                              Object[] o, String[] s) throws MethodNotFoundException {

    try {
      return mbeanServer.invoke(objName, method, o, s);

    } catch (Exception e) {
      logger.error("{} does not have '{}' attribute", objName, method);
      logger.trace("", e);
      throw new MethodNotFoundException("Method '" + method + notFoundString + objName + "'");
    }
  }
  /**
   * Gets the long attr.
   *
   * @param mbeanServer the mbean server
   * @param objName the obj name
   * @param attrName the attr name
   * @param defaultValue the default value
   *
   * @return the long attr
   */
  public static long getLongAttr(MBeanServer mbeanServer, ObjectName objName, String attrName,
      long defaultValue) {

    try {
      Object obj = mbeanServer.getAttribute(objName, attrName);
      return obj == null ? defaultValue : (Long) obj;
    } catch (Exception e) {
      logger.trace("", e);
      return defaultValue;
    }
  }

  /**
   * Gets the long attr.
   *
   * @param cds the cds
   * @param name the name
   *
   * @return the long attr
   */
  public static long getLongAttr(CompositeData cds, String name) {
    Object obj = cds.get(name);
    if (obj instanceof Long) {
      return (Long) obj;
    }
    return 0;
  }

  /**
   * Gets the long attr.
   *
   * @param mbeanServer the mbean server
   * @param objName the obj name
   * @param attrName the attr name
   *
   * @return the long attr
   *
   */
  public static long getLongAttr(MBeanServer mbeanServer, ObjectName objName, String attrName)
          throws AttributeNotFoundException {

    try {
      return (Long) mbeanServer.getAttribute(objName, attrName);
    } catch (Exception e) {
      logger.trace("", e);
      throw new AttributeNotFoundException("3 Attribute '" + attrName + notFoundString + objName + "'");
    }
  }

  /**
   * Gets the int attr.
   *
   * @param mbeanServer the mbean server
   * @param objName the obj name
   * @param attrName the attr name
   *
   * @return the int attr
   *
   */
  public static int getIntAttr(MBeanServer mbeanServer, ObjectName objName, String attrName)
          throws AttributeNotFoundException {

    try {
      return (Integer) mbeanServer.getAttribute(objName, attrName);
    } catch (Exception e) {
      logger.trace("", e);
      throw new AttributeNotFoundException("4 Attribute '" + attrName + notFoundString + objName + "'");
    }
  }

  /**
   * Gets the int attr.
   *
   * @param cds the cds
   * @param name the name
   * @param defaultValue the default value
   *
   * @return the int attr
   */
  public static int getIntAttr(CompositeData cds, String name, int defaultValue) {
    Object obj = cds.get(name);

    if (obj instanceof Integer) {
      return (Integer) obj;
    }
    return defaultValue;
  }

  /**
   * Gets the string attr.
   *
   * @param mbeanServer the mbean server
   * @param objName the obj name
   * @param attrName the attr name
   *
   * @return the string attr
   *
   */
  public static String getStringAttr(MBeanServer mbeanServer, ObjectName objName, String attrName)
          throws AttributeNotFoundException {

    try {
      Object obj = getAttribute(mbeanServer, objName, attrName);
      return obj == null ? null : obj.toString();
    } catch (Exception e) {
      logger.trace("", e);
      throw new AttributeNotFoundException("5 Attribute '" + attrName + notFoundString+ objName + "'");
    }
  }
  /**
   * Gets the string attr.
   *
   * @param cds the cds
   * @param name the name
   *
   * @return the string attr
   */
  public static String getStringAttr(CompositeData cds, String name) {
    Object obj = cds.get(name);
    return obj != null ? obj.toString() : null;
  }

  /**
   * Gets the boolean attr.
   *
   * @param cds the cds
   * @param name the name
   *
   * @return the boolean attr
   */
  public static boolean getBooleanAttr(CompositeData cds, String name) {
    Object obj = cds.get(name);
    return obj instanceof Boolean && (Boolean) obj;
  }

  /**
   * Checks for attribute.
   *
   * @param server the server
   * @param mbean the mbean
   * @param attrName the attr name
   *
   * @return true, if successful
   *
   */
  public static boolean hasAttribute(MBeanServer server, ObjectName mbean, String attrName)
          throws AttributeNotFoundException {

    try {
      MBeanInfo info = server.getMBeanInfo(mbean);
      MBeanAttributeInfo[] ai = info.getAttributes();
      for (MBeanAttributeInfo attribInfo : ai) {
        if (attribInfo.getName().equals(attrName)) {
          return true;
        }
      }
      return false;
    } catch (Exception e) {
      logger.trace("", e);
      throw new AttributeNotFoundException("6 Attribute '" + attrName + notFoundString + mbean + "'");
    }
  }

}
