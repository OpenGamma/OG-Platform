/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.test.AbstractConverterTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ValueConverter} class.
 */
@Test(groups = TestGroup.UNIT)
public class ValueConverterTest extends AbstractConverterTest {

  private final ValueConverter _valueConverter = new ValueConverter();

  @Test
  public void testToValue() {
    final JavaTypeInfo<Value> target = JavaTypeInfo.builder(Value.class).get();
    assertEquals(true, _valueConverter.canConvertTo(target));
    assertValidConversion(_valueConverter, Boolean.TRUE, target, ValueUtils.of(true));
    assertValidConversion(_valueConverter, (Integer) 42, target, ValueUtils.of(42));
    assertValidConversion(_valueConverter, (Double) 3.14, target, ValueUtils.of(3.14));
    assertValidConversion(_valueConverter, "foo", target, ValueUtils.of("foo"));
    assertValidConversion(_valueConverter, FudgeContext.EMPTY_MESSAGE, target, ValueUtils.of(FudgeContext.EMPTY_MESSAGE));
    assertInvalidConversion(_valueConverter, DataUtils.of(42), target);
    assertConversionCount(5, _valueConverter, target);
  }

  @Test
  public void testToData() {
    final JavaTypeInfo<Data> target = JavaTypeInfo.builder(Data.class).get();
    assertEquals(false, _valueConverter.canConvertTo(target));
  }

  @Test
  public void testToBoolean() {
    final JavaTypeInfo<Boolean> target = JavaTypeInfo.builder(Boolean.class).get();
    assertEquals(true, _valueConverter.canConvertTo(target));
    assertValidConversion(_valueConverter, ValueUtils.of(true), target, Boolean.TRUE);
    assertConversionCount(1, _valueConverter, target);
  }

  @Test
  public void testToInteger() {
    final JavaTypeInfo<Integer> target = JavaTypeInfo.builder(Integer.class).get();
    assertEquals(true, _valueConverter.canConvertTo(target));
    assertValidConversion(_valueConverter, ValueUtils.of(42), target, (Integer) 42);
    assertConversionCount(1, _valueConverter, target);
  }

  @Test
  public void testToDouble() {
    final JavaTypeInfo<Double> target = JavaTypeInfo.builder(Double.class).get();
    assertEquals(true, _valueConverter.canConvertTo(target));
    assertValidConversion(_valueConverter, ValueUtils.of(3.14), target, (Double) 3.14);
    assertConversionCount(1, _valueConverter, target);
  }

  @Test
  public void testToString() {
    final JavaTypeInfo<String> target = JavaTypeInfo.builder(String.class).get();
    assertEquals(true, _valueConverter.canConvertTo(target));
    assertValidConversion(_valueConverter, ValueUtils.of("foo"), target, "foo");
    assertConversionCount(1, _valueConverter, target);
  }

  @Test
  public void testToMessage() {
    final JavaTypeInfo<FudgeMsg> target = JavaTypeInfo.builder(FudgeMsg.class).get();
    assertEquals(true, _valueConverter.canConvertTo(target));
    assertValidConversion(_valueConverter, ValueUtils.of(FudgeContext.EMPTY_MESSAGE), target,
        FudgeContext.EMPTY_MESSAGE);
    assertConversionCount(1, _valueConverter, target);
  }

}
