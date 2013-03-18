/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import org.testng.annotations.Test;

import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.test.AbstractConverterTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link PrimitiveArrayConverter} class.
 */
@Test(groups = TestGroup.UNIT)
public class PrimitiveArrayConverterTest extends AbstractConverterTest {

  private final PrimitiveArrayConverter _converter = new PrimitiveArrayConverter();
  
  @Test
  public void testBoolean () {
    final boolean[] a = new boolean[] { true, false };
    final Boolean[] b = new Boolean[] { true, false };
    assertValidConversion (_converter, a, JavaTypeInfo.builder(Boolean[].class).get (), b);
    assertValidConversion (_converter, b, JavaTypeInfo.builder(boolean[].class).get (), a);
  }
  
  @Test
  public void testByte() {
    final byte[] a = new byte[] {Byte.MIN_VALUE, 0, Byte.MAX_VALUE };
    final Byte[] b = new Byte[] {Byte.MIN_VALUE, 0, Byte.MAX_VALUE };
    assertValidConversion(_converter, a, JavaTypeInfo.builder(Byte[].class).get(), b);
    assertValidConversion(_converter, b, JavaTypeInfo.builder(byte[].class).get(), a);
  }

  @Test
  public void testCharacter() {
    final char[] a = new char[] {'A', 'B', 'C' };
    final Character[] b = new Character[] {'A', 'B', 'C' };
    assertValidConversion(_converter, a, JavaTypeInfo.builder(Character[].class).get(), b);
    assertValidConversion(_converter, b, JavaTypeInfo.builder(char[].class).get(), a);
  }
  
  @Test
  public void testDouble() {
    final double[] a = new double[] {Double.MIN_VALUE, 0, Double.MAX_VALUE };
    final Double[] b = new Double[] {Double.MIN_VALUE, 0d, Double.MAX_VALUE };
    assertValidConversion(_converter, a, JavaTypeInfo.builder(Double[].class).get(), b);
    assertValidConversion(_converter, b, JavaTypeInfo.builder(double[].class).get(), a);
  }

  @Test
  public void testFloat() {
    final float[] a = new float[] {Float.MIN_VALUE, 0, Float.MAX_VALUE };
    final Float[] b = new Float[] {Float.MIN_VALUE, 0f, Float.MAX_VALUE };
    assertValidConversion(_converter, a, JavaTypeInfo.builder(Float[].class).get(), b);
    assertValidConversion(_converter, b, JavaTypeInfo.builder(float[].class).get(), a);
  }

  @Test
  public void testInteger() {
    final int[] a = new int[] {Integer.MIN_VALUE, 0, Integer.MAX_VALUE };
    final Integer[] b = new Integer[] {Integer.MIN_VALUE, 0, Integer.MAX_VALUE };
    assertValidConversion(_converter, a, JavaTypeInfo.builder(Integer[].class).get(), b);
    assertValidConversion(_converter, b, JavaTypeInfo.builder(int[].class).get(), a);
  }

  @Test
  public void testLong() {
    final long[] a = new long[] {Long.MIN_VALUE, 0, Long.MAX_VALUE };
    final Long[] b = new Long[] {Long.MIN_VALUE, 0L, Long.MAX_VALUE };
    assertValidConversion(_converter, a, JavaTypeInfo.builder(Long[].class).get(), b);
    assertValidConversion(_converter, b, JavaTypeInfo.builder(long[].class).get(), a);
  }

  @Test
  public void testShort() {
    final short[] a = new short[] {Short.MIN_VALUE, 0, Short.MAX_VALUE };
    final Short[] b = new Short[] {Short.MIN_VALUE, 0, Short.MAX_VALUE };
    assertValidConversion(_converter, a, JavaTypeInfo.builder(Short[].class).get(), b);
    assertValidConversion(_converter, b, JavaTypeInfo.builder(short[].class).get(), a);
  }

}
