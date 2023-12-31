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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * The Class Instruments.
 */
public class Instruments {

  /** The Constant SIZE_VOID. */
  public static final long SIZE_VOID = 0;

  /** The Constant SIZE_BOOLEAN. */
  public static final long SIZE_BOOLEAN = 1;

  /** The Constant SIZE_BYTE. */
  public static final long SIZE_BYTE = 1;

  /** The Constant SIZE_CHAR. */
  public static final long SIZE_CHAR = 2;

  /** The Constant SIZE_SHORT. */
  public static final long SIZE_SHORT = 2;

  /** The Constant SIZE_INT. */
  public static final long SIZE_INT = 4;

  /** The Constant SIZE_LONG. */
  public static final long SIZE_LONG = 8;

  /** The Constant SIZE_FLOAT. */
  public static final long SIZE_FLOAT = 4;

  /** The Constant SIZE_DOUBLE. */
  public static final long SIZE_DOUBLE = 8;

  /** The Constant SIZE_OBJECT. */
  public static final long SIZE_OBJECT = 8;

  /** The Constant SIZE_REFERENCE. */
  public static final long SIZE_REFERENCE;

  /** The Constant ACCESSOR. */
  private static final Accessor ACCESSOR = new SimpleAccessor();

  /** The Constant IGNORE_NIO. */
  private static final boolean IGNORE_NIO;

  static {
    String ignoreNioProp = System.getProperty("psiprobe.intruments.ignoreNIO");
    String os64bitProp = System.getProperty("sun.arch.data.model");
    IGNORE_NIO = ignoreNioProp == null || "true".equalsIgnoreCase(ignoreNioProp);
    SIZE_REFERENCE = "64".equals(os64bitProp) ? 8 : 4;
  }

  /** The next queue. */
  private final List<Object> nextQueue = new LinkedList<>();

  /** The class loader. */
  private ClassLoader classLoader;

  /**
   * Size of.
   *
   * @param obj the obj
   *
   * @return the long
   */
  public static long sizeOf(Object obj) throws IllegalAccessException {
    return new Instruments().internalSizeOf(obj);
  }

  /**
   * Size of.
   *
   * @param obj the obj
   * @param cl the cl
   *
   * @return the long
   */
  public static long sizeOf(Object obj, ClassLoader cl) throws IllegalAccessException {
    Instruments instruments = new Instruments();
    instruments.classLoader = cl;
    return instruments.internalSizeOf(obj);
  }

  /**
   * Internal size of.
   *
   * @param root the root
   *
   * @return the long
   */
  private long internalSizeOf(Object root) throws IllegalAccessException {
    long size = 0;
    Queue<Object> queue = new LinkedList<>();
    Set<ObjectWrapper> processedObjects = new HashSet<>(2048);

    queue.add(root);

    while (!queue.isEmpty()) {
      Object obj = queue.poll();

      if (isInitialized() && obj != null
              && (classLoader == obj.getClass().getClassLoader())
              && (!IGNORE_NIO || !obj.getClass().getName().startsWith("java.nio."))) {
        ObjectWrapper ow = new ObjectWrapper(obj);
        if (!processedObjects.contains(ow)) {
          processedObjects.add(ow);

          if (obj.getClass().isArray()) {
            size += sizeOfArray(obj);
          } else if (obj.getClass().isPrimitive()) {
            size += sizeOfPrimitive(obj.getClass());
          } else {
            size += sizeOfObject(obj);
              queue.addAll(Objects.requireNonNull(getObjectFields()));
          }
        }
      }
    }

    return size;
  }

  private Collection<?> getObjectFields() {
    return nextQueue;
  }

  /**
   * Size of object.
   *
   * @param obj the obj
   *
   * @return the long
   */
  private long sizeOfObject(Object obj) throws IllegalAccessException {
    long size = SIZE_OBJECT;
    Class<?> clazz = obj.getClass();

    while (clazz != null) {
      size += getSizeOfFields(obj, clazz.getDeclaredFields());
      clazz = clazz.getSuperclass();
    }

    return size;
  }

  private long getSizeOfFields(Object obj, Field[] fields) throws IllegalAccessException {
    long size = 0;

    for (Field field : fields) {
      if (!Modifier.isStatic(field.getModifiers())) {
        if (field.getType().isPrimitive()) {
          size += sizeOfPrimitive(field.getType());
        } else {
          Object val = ACCESSOR.get(obj, field);
          if (field.getType().isArray()) {
            size += sizeOfArray(val);
          } else {
            size += SIZE_REFERENCE;
            nextQueue.add(val);
          }
        }
      }
    }

    return size;
  }

  /**
   * Size of array.
   *
   * @param obj the obj
   *
   * @return the long
   */
  private long sizeOfArray(Object obj) {
    if (obj != null) {
      Class<?> ct = obj.getClass().getComponentType();
      if (ct.isPrimitive()) {
        return Array.getLength(obj) * sizeOfPrimitive(ct);
      }
      for (int i = 0; i < Array.getLength(obj); i++) {
        nextQueue.add(Array.get(obj, i));
      }
    }
    return 0;
  }

  /**
   * Size of primitive.
   *
   * @param <T> the generic type
   * @param type the type
   *
   * @return the long
   */
  private static <T> long sizeOfPrimitive(Class<T> type) {
    if (type == Boolean.TYPE) {
      return SIZE_BOOLEAN;
    }
    if (type == Byte.TYPE) {
      return SIZE_BYTE;
    }
    if (type == Character.TYPE) {
      return SIZE_CHAR;
    }
    if (type == Short.TYPE) {
      return SIZE_SHORT;
    }
    if (type == Integer.TYPE) {
      return SIZE_INT;
    }
    if (type == Long.TYPE) {
      return SIZE_LONG;
    }
    if (type == Float.TYPE) {
      return SIZE_FLOAT;
    }
    if (type == Double.TYPE) {
      return SIZE_DOUBLE;
    }
    if (type == Void.TYPE) {
      return SIZE_VOID;
    }
    return SIZE_REFERENCE;
  }

  /**
   * Checks if is initialized.
   *
   * @return true, if is initialized
   */
  public static boolean isInitialized() {
    return ACCESSOR != null;
  }

  /**
   * Gets the field.
   *
   * @param obj the obj
   * @param name the name
   *
   * @return the field
   */
  public static Object getField(Object obj, String name) throws IllegalAccessException {
    if (isInitialized()) {
      Field field = findField(obj.getClass(), name);
      if (field != null) {
        return ACCESSOR.get(obj, field);
      }
    }
    return null;
  }

  /**
   * Find field.
   *
   * @param <T> the generic type
   * @param clazz the clazz
   * @param name the name
   *
   * @return the field
   */
  public static <T> Field findField(Class<T> clazz, String name) {
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      if (name.equals(field.getName())) {
        return field;
      }
    }
    Class<?> superClass = clazz.getSuperclass();
    if (superClass != null) {
      return findField(superClass, name);
    }
    return null;
  }

}
