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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The Class InstrumentsTests.
 */
class InstrumentsTests {

  /**
   * Test object.
   */
  @Test
  void testObject() throws IllegalAccessException {
    Object o = new Object();
    long objectSize = Instruments.sizeOf(o);
    Assertions.assertEquals(Instruments.SIZE_OBJECT, objectSize);
  }

  /**
   * Test boolean.
   */
  @Test
  void testBoolean() throws IllegalAccessException {
    boolean b = false;
    long booleanSize = Instruments.sizeOf(b) - Instruments.SIZE_OBJECT;
    Assertions.assertEquals(Instruments.SIZE_BOOLEAN, booleanSize);
  }

  /**
   * Test byte.
   */
  @Test
  void testByte() throws IllegalAccessException {
    byte b = 0x00;
    long byteSize = Instruments.sizeOf(b) - Instruments.SIZE_OBJECT;
    Assertions.assertEquals(Instruments.SIZE_BYTE, byteSize);
  }

  /**
   * Test char.
   */
  @Test
  void testChar() throws IllegalAccessException {
    char c = '\0';
    long charSize = Instruments.sizeOf(c) - Instruments.SIZE_OBJECT;
    Assertions.assertEquals(Instruments.SIZE_CHAR, charSize);
  }

  /**
   * Test short.
   */
  @Test
  void testShort() throws IllegalAccessException {
    short s = 0;
    long shortSize = Instruments.sizeOf(s) - Instruments.SIZE_OBJECT;
    Assertions.assertEquals(Instruments.SIZE_SHORT, shortSize);
  }

  /**
   * Test int.
   */
  @Test
  void testInt() throws IllegalAccessException {
    int i = 0;
    long intSize = Instruments.sizeOf(i) - Instruments.SIZE_OBJECT;
    Assertions.assertEquals(Instruments.SIZE_INT, intSize);
  }

  /**
   * Test long.
   */
  @Test
  void testLong() throws IllegalAccessException {
    long l = 0;
    long longSize = Instruments.sizeOf(l) - Instruments.SIZE_OBJECT;
    Assertions.assertEquals(Instruments.SIZE_LONG, longSize);
  }

  /**
   * Test float.
   */
  @Test
  void testFloat() throws IllegalAccessException {
    float f = 0.0F;
    long floatSize = Instruments.sizeOf(f) - Instruments.SIZE_OBJECT;
    Assertions.assertEquals(Instruments.SIZE_FLOAT, floatSize);
  }

}
