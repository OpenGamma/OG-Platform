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
 * Tests the {@link ArrayDepthConverter} class.
 */
@Test(groups = TestGroup.UNIT)
public class ArrayDepthConverterTest extends AbstractConverterTest {

  private final ArrayDepthConverter _converter = new ArrayDepthConverter();

  @Test
  public void testFromEmptyArray() {
    final JavaTypeInfo<String> stringType = JavaTypeInfo.builder(String.class).get();
    assertInvalidConversion(_converter, new String[0], stringType);
  }

  @Test
  public void testFromSingleElementArray() {
    final JavaTypeInfo<String> stringType = JavaTypeInfo.builder(String.class).get();
    assertValidConversion(_converter, new String[] {"Foo" }, stringType, "Foo");
  }

  @Test
  public void testFromMultiElementArray() {
    final JavaTypeInfo<String> stringType = JavaTypeInfo.builder(String.class).get();
    assertInvalidConversion(_converter, new String[] {"Foo", "Bar" }, stringType);
  }

  @Test
  public void testFromTwoDSingleElementArray() {
    final JavaTypeInfo<String> stringType = JavaTypeInfo.builder(String.class).get();
    final JavaTypeInfo<String[]> stringArrayType = JavaTypeInfo.builder(String[].class).get();
    assertValidConversion(_converter, new String[][] {new String[] {"Foo", "Bar" } }, stringArrayType, new String[] {"Foo", "Bar" });
    assertInvalidConversion(_converter, new String[][] {new String[] {"Foo", "Bar" } }, stringType);
  }

  @Test
  public void testFromTwoDMultiElementArray() {
    final JavaTypeInfo<String[]> stringArrayType = JavaTypeInfo.builder(String[].class).get();
    assertInvalidConversion(_converter, new String[][] {new String[] {"Foo", "Bar" }, new String[0] }, stringArrayType);
  }

  @Test
  public void testToSingleElementArray() {
    final JavaTypeInfo<String[]> stringArrayType = JavaTypeInfo.builder(String[].class).get();
    assertValidConversion(_converter, "Foo", stringArrayType, new String[] {"Foo" });
  }

  @Test
  public void testToTwoDSingleElementArray() {
    final JavaTypeInfo<String[][]> stringArrayType = JavaTypeInfo.builder(String[][].class).get();
    assertInvalidConversion(_converter, "Foo", stringArrayType);
    assertValidConversion(_converter, new String[] {"Foo", "Bar" }, stringArrayType, new String[][] {new String[] {"Foo", "Bar" } });
  }

}
