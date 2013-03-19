/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static org.testng.AssertJUnit.fail;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.test.AbstractConverterTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link PrimitiveConverter} class.
 */
@Test(groups = TestGroup.UNIT)
public class PrimitiveConverterTest extends AbstractConverterTest {

  private static final Logger s_logger = LoggerFactory.getLogger(PrimitiveConverterTest.class);

  private final PrimitiveConverter _converter = new PrimitiveConverter();

  private final Object[][] _values = new Object[][] {
      new Object[] {false, null, (byte) 0, 'F', 0.0d, 0.0f, 0, 0L, (short) 0, "false" },
      new Object[] {true, null, (byte) 1, 'T', 1.0d, 1.0f, 1, 1L, (short) 1, "true" },
      new Object[] {(byte) 0, false, null, null, 0.0d, 0.0f, 0, 0L, (short) 0, "0" },
      new Object[] {Byte.MIN_VALUE, true, null, null, (double) Byte.MIN_VALUE, (float) Byte.MIN_VALUE, (int) Byte.MIN_VALUE, (long) Byte.MIN_VALUE, (short) Byte.MIN_VALUE,
          Byte.toString(Byte.MIN_VALUE) },
      new Object[] {Byte.MAX_VALUE, true, null, null, (double) Byte.MAX_VALUE, (float) Byte.MAX_VALUE, (int) Byte.MAX_VALUE, (long) Byte.MAX_VALUE, (short) Byte.MAX_VALUE,
          Byte.toString(Byte.MAX_VALUE) },
      new Object[] {'\0', null, null, null, null, null, null, null, null, "\0" },
      new Object[] {'T', true, null, null, null, null, null, null, null, "T" },
      new Object[] {'F', false, null, null, null, null, null, null, null, "F" },
      new Object[] {'t', true, null, null, null, null, null, null, null, "t" },
      new Object[] {'f', false, null, null, null, null, null, null, null, "f" },
      new Object[] {0.0d, false, (byte) 0, null, null, 0.0f, 0, 0L, (short) 0, "0.0" },
      new Object[] {Double.MIN_VALUE, true, (byte) 0, null, null, 0.0f, 0, 0L, (short) 0, Double.toString(Double.MIN_VALUE) },
      new Object[] {Double.MAX_VALUE, true, null, null, null, (float) Double.MAX_VALUE, null, null, null, Double.toString(Double.MAX_VALUE) },
      new Object[] {Double.NaN, null, null, null, null, Float.NaN, null, null, null, "NaN" },
      new Object[] {Double.NEGATIVE_INFINITY, null, null, null, null, Float.NEGATIVE_INFINITY, null, null, null, "-Infinity" },
      new Object[] {Double.POSITIVE_INFINITY, null, null, null, null, Float.POSITIVE_INFINITY, null, null, null, "Infinity" },
      new Object[] {0.0f, false, (byte) 0, null, 0.0d, null, 0, 0L, (short) 0, "0.0" },
      new Object[] {Float.MIN_VALUE, true, (byte) 0, null, (double) Float.MIN_VALUE, null, 0, 0L, (short) 0, Float.toString(Float.MIN_VALUE) },
      new Object[] {Float.MAX_VALUE, true, null, null, (double) Float.MAX_VALUE, null, null, null, null, Float.toString(Float.MAX_VALUE) },
      new Object[] {Float.NaN, null, null, null, Double.NaN, null, null, null, null, "NaN" },
      new Object[] {Float.NEGATIVE_INFINITY, null, null, null, Double.NEGATIVE_INFINITY, null, null, null, null, "-Infinity" },
      new Object[] {Float.POSITIVE_INFINITY, null, null, null, Double.POSITIVE_INFINITY, null, null, null, null, "Infinity" },
      new Object[] {0, false, (byte) 0, null, 0.0d, 0.0f, null, 0L, (short) 0, "0" },
      new Object[] {Integer.MIN_VALUE, true, null, null, (double) Integer.MIN_VALUE, (float) Integer.MIN_VALUE, null, (long) Integer.MIN_VALUE, null, Integer.toString(Integer.MIN_VALUE) },
      new Object[] {Integer.MAX_VALUE, true, null, null, (double) Integer.MAX_VALUE, (float) Integer.MAX_VALUE, null, (long) Integer.MAX_VALUE, null, Integer.toString(Integer.MAX_VALUE) },
      new Object[] {0L, false, (byte) 0, null, 0.0d, 0.0f, 0, null, (short) 0, "0" },
      new Object[] {Long.MIN_VALUE, true, null, null, (double) Long.MIN_VALUE, (float) Long.MIN_VALUE, null, null, null, Long.toString(Long.MIN_VALUE) },
      new Object[] {Long.MAX_VALUE, true, null, null, (double) Long.MAX_VALUE, (float) Long.MAX_VALUE, null, null, null, Long.toString(Long.MAX_VALUE) },
      new Object[] {(short) 0, false, (byte) 0, null, 0.0d, 0.0f, 0, 0L, null, "0" },
      new Object[] {Short.MIN_VALUE, true, null, null, (double) Short.MIN_VALUE, (float) Short.MIN_VALUE, (int) Short.MIN_VALUE, (long) Short.MIN_VALUE, null,
          Short.toString(Short.MIN_VALUE) },
      new Object[] {Short.MAX_VALUE, true, null, null, (double) Short.MAX_VALUE, (float) Short.MAX_VALUE, (int) Short.MAX_VALUE, (long) Short.MAX_VALUE, null,
          Short.toString(Short.MAX_VALUE) },
      new Object[] {"", null, null, null, null, null, null, null, null, null },
      new Object[] {"T", true, null, 'T', null, null, null, null, null, null },
      new Object[] {"True", true, null, null, null, null, null, null, null, null },
      new Object[] {"F", false, null, 'F', null, null, null, null, null, null },
      new Object[] {"False", false, null, null, null, null, null, null, null, null },
      new Object[] {"0", null, (byte) 0, '0', 0.0d, 0.0f, 0, 0L, (short) 0, null },
      new Object[] {"42", null, (byte) 42, null, 42.0d, 42.0f, 42, 42L, (short) 42, null },
      new Object[] {"3.14", null, null, null, 3.14d, 3.14f, null, null, null, null } };

  @SuppressWarnings("unchecked")
  private <T> void assertConversions(final JavaTypeInfo<T> type, final int targetType) {
    Set<Class<?>> conversions = new HashSet<Class<?>>();
    for (Object[] values : _values) {
      if (values.length != 10) {
        s_logger.warn("Bad length {}", (Object) values);
        fail();
      }
      final Object convertFrom = values[0];
      if (convertFrom.getClass() == type.getRawClass()) {
        for (int i = 1; i < values.length; i++) {
          if (values[i] != null) {
            conversions.add(values[i].getClass());
          }
        }
        continue;
      }
      if (values[targetType] == null) {
        assertInvalidConversion(_converter, convertFrom, type);
      } else {
        assertValidConversion(_converter, convertFrom, type, (T) values[targetType]);
      }
    }
    s_logger.info("Type {} should be convertible to {} types", type, conversions);
    assertConversionCount(conversions.size(), _converter, type);
  }

  @Test
  public void testToBoolean() {
    final JavaTypeInfo<Boolean> type = JavaTypeInfo.builder(Boolean.class).get();
    assertConversions(type, 1);
  }

  @Test
  public void testToByte() {
    final JavaTypeInfo<Byte> type = JavaTypeInfo.builder(Byte.class).get();
    assertConversions(type, 2);
  }

  @Test
  public void testToCharacter() {
    final JavaTypeInfo<Character> type = JavaTypeInfo.builder(Character.class).get();
    assertConversions(type, 3);
  }

  @Test
  public void testToDouble() {
    final JavaTypeInfo<Double> type = JavaTypeInfo.builder(Double.class).get();
    assertConversions(type, 4);
  }

  @Test
  public void testToFloat() {
    final JavaTypeInfo<Float> type = JavaTypeInfo.builder(Float.class).get();
    assertConversions(type, 5);
  }

  @Test
  public void testToInteger() {
    final JavaTypeInfo<Integer> type = JavaTypeInfo.builder(Integer.class).get();
    assertConversions(type, 6);
  }

  @Test
  public void testToLong() {
    final JavaTypeInfo<Long> type = JavaTypeInfo.builder(Long.class).get();
    assertConversions(type, 7);
  }

  @Test
  public void testToShort() {
    final JavaTypeInfo<Short> type = JavaTypeInfo.builder(Short.class).get();
    assertConversions(type, 8);
  }

  @Test
  public void testToString() {
    final JavaTypeInfo<String> type = JavaTypeInfo.builder(String.class).get();
    assertConversions(type, 9);
  }

}
