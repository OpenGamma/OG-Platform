/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.test.AbstractConverterTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link EnumConverter} class.
 */
@Test(groups = TestGroup.UNIT)
public class EnumConverterTest extends AbstractConverterTest {

  private final EnumConverter _converter = new EnumConverter();

  public static enum TestEnum {
    FOO,
    BAR,
    FOO_BAR;
  }

  public void testToString() {
    final JavaTypeInfo<String> target = JavaTypeInfo.builder(String.class).get();
    assertEquals(_converter.canConvertTo(target), true);
    assertValidConversion(_converter, TestEnum.FOO, target, "FOO");
    assertConversionCount(1, _converter, target);
  }

  public void testFromString() {
    final JavaTypeInfo<TestEnum> target = JavaTypeInfo.builder(TestEnum.class).get();
    assertEquals(_converter.canConvertTo(target), true);
    assertValidConversion(_converter, "FOO", target, TestEnum.FOO);
    assertInvalidConversion(_converter, "42", target);
    assertConversionCount(1, _converter, target);
  }

  public void testMixedCase() {
    final JavaTypeInfo<TestEnum> target = JavaTypeInfo.builder(TestEnum.class).get();
    assertEquals(_converter.canConvertTo(target), true);
    assertValidConversion(_converter, "Foo", target, TestEnum.FOO);
  }

  public void testWithSpaces() {
    final JavaTypeInfo<TestEnum> target = JavaTypeInfo.builder(TestEnum.class).get();
    assertEquals(_converter.canConvertTo(target), true);
    assertValidConversion(_converter, "Foo Bar", target, TestEnum.FOO_BAR);
  }

}
