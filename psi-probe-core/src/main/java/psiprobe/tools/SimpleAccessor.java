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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * The Class SimpleAccessor.
 */
public class SimpleAccessor implements Accessor {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(SimpleAccessor.class);

  @Override
  public Object get(Object obj, Field field) throws IllegalAccessException {
    boolean accessible = pre(field);
    try {
      return get0(obj, field);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      logger.trace("", e);
      return null;
    } finally {
      post(field, accessible);
    }
  }

  /**
   * Gets the 0.
   *
   * @param obj the obj
   * @param field the field
   *
   * @return the 0
   *
   * @throws IllegalAccessException the illegal access exception
   */
  protected Object get0(Object obj, Field field) throws IllegalAccessException {

    if (field.isAccessible()) {
      return field.get(obj);
    }
    return null;
  }

  /**
   * Pre.
   *
   * @param field the field
   *
   * @return true, if successful
   */
  private boolean pre(Field field) {
    boolean accessible = Modifier.isPublic(field.getModifiers());
    if (!accessible) {
      try {
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.set(AccessibleObject.class, field);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.PRIVATE & ~Modifier.PROTECTED);
      } catch (NoSuchFieldException | IllegalAccessException ex) {
        logger.trace("", ex);
      }
    }
    return accessible;
  }

  /**
   * Post.
   *
   * @param field the field
   * @param value the value
   */
  private void post(Field field, boolean value) {
    if (!value) {
      try {
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.set(AccessibleObject.class, field);
        int modifiers = field.getModifiers() & ~Modifier.PRIVATE & ~Modifier.PROTECTED;
        modifiersField.setInt(field, modifiers);
      } catch (NoSuchFieldException | IllegalAccessException ex) {
        logger.trace("", ex);
      }
    }
  }

}
