/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.test.AbstractConverterTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ArrayTypeConverter} class.
 */
@Test(groups = TestGroup.UNIT)
public class ArrayTypeConverterTest extends AbstractConverterTest {

  private final ArrayTypeConverter _converter = new ArrayTypeConverter();

  @Test
  public void testCanConvertTo() {
    assertEquals(true, _converter.canConvertTo(JavaTypeInfo.builder(String.class).arrayOf().get()));
    assertEquals(true, _converter.canConvertTo(JavaTypeInfo.builder(String.class).arrayOf().get()));
    assertEquals(false, _converter.canConvertTo(JavaTypeInfo.builder(String.class).get()));
    assertEquals(true, _converter.canConvertTo(JavaTypeInfo.builder(Integer.class).arrayOf().get()));
    assertEquals(false, _converter.canConvertTo(JavaTypeInfo.builder(int[].class).get()));
  }

  @Test
  public void testGetConversionsTo() {
    assertConversionCount(2, _converter, JavaTypeInfo.builder(String.class).arrayOf().get());
    assertConversionCount(2, _converter, JavaTypeInfo.builder(Integer.class).arrayOf().get());
    assertConversionCount(2, _converter, JavaTypeInfo.builder(Boolean.class).arrayOf().get());
    assertConversionCount(1, _converter, JavaTypeInfo.builder(Value.class).arrayOf().get());
    assertConversionCount(1, _converter, JavaTypeInfo.builder(Value.class).arrayOf().arrayOf().get());
  }

  @Test
  public void testConversion_1D() {
    final String[] a = new String[] {"42", "0", "100" };
    final Integer[] b = new Integer[] {42, 0, 100 };
    assertValidConversion(_converter, a, JavaTypeInfo.builder(Integer.class).arrayOf().get(), b);
    assertValidConversion(_converter, b, JavaTypeInfo.builder(String.class).arrayOf().get(), a);
  }

  @Test
  public void testConversion_2D() {
    final Value[][] a = new Value[][] {new Value[] {ValueUtils.of("1"), ValueUtils.of("2") }, new Value[] {ValueUtils.of("3"), ValueUtils.of("4") } };
    final Integer[][] b = new Integer[][] {new Integer[] {1, 2 }, new Integer[] {3, 4 } };
    assertValidConversion(_converter, a, JavaTypeInfo.builder(Integer.class).arrayOf().arrayOf().get(), b);
  }

  @Test
  public void testPrimitive() {
    final int[] a = new int[] {42, 0, 100 };
    final double[] b = new double[] {42, 0, 100 };
    assertValidConversion(_converter, a, JavaTypeInfo.builder(double[].class).get(), b);
    assertValidConversion(_converter, b, JavaTypeInfo.builder(int[].class).get(), a);
  }

}
