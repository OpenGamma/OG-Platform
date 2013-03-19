/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.test.AbstractConverterTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link DataConverter} class.
 */
@Test(groups = TestGroup.UNIT)
public class DataConverterTest extends AbstractConverterTest {

  private final DataConverter _dataConverter = new DataConverter();

  private int _count = 0;

  private Value createValue() {
    return ValueUtils.of(++_count);
  }

  private Value[] createValue_1() {
    return new Value[] {createValue(), createValue()};
  }

  private Value[][] createValue_2() {
    return new Value[][] {createValue_1(), createValue_1()};
  }

  @Test
  public void testToData() {
    final JavaTypeInfo<Data> target = JavaTypeInfo.builder(Data.class).get();
    assertEquals(true, _dataConverter.canConvertTo(target));
    Data data = DataUtils.of(createValue());
    assertValidConversion(_dataConverter, data.getSingle(), target, data);
    data = DataUtils.of(createValue_1());
    assertValidConversion(_dataConverter, data.getLinear(), target, data);
    data = DataUtils.of(createValue_2());
    assertValidConversion(_dataConverter, data.getMatrix(), target, data);
    assertInvalidConversion(_dataConverter, "foo", target);
    assertConversionCount(3, _dataConverter, target);
  }

  @Test
  public void testToString() {
    final JavaTypeInfo<String> target = JavaTypeInfo.builder(String.class).get();
    assertEquals(false, _dataConverter.canConvertTo(target));
  }

  @Test
  public void testToValue() {
    final JavaTypeInfo<Value> target = JavaTypeInfo.builder(Value.class).get();
    assertEquals(true, _dataConverter.canConvertTo(target));
    final Data data = DataUtils.of(createValue());
    assertValidConversion(_dataConverter, data, target, data.getSingle());
    assertInvalidConversion(_dataConverter, DataUtils.of(createValue_1()), target);
    assertInvalidConversion(_dataConverter, DataUtils.of(createValue_2()), target);
    assertConversionCount(1, _dataConverter, target);
  }

  @Test
  public void testToValue_1() {
    final JavaTypeInfo<Value[]> target = JavaTypeInfo.builder(Value[].class).get();
    assertEquals(true, _dataConverter.canConvertTo(target));
    assertInvalidConversion(_dataConverter, DataUtils.of(createValue()), target);
    final Data data = DataUtils.of(createValue_1());
    assertValidConversion(_dataConverter, data, target, data.getLinear());
    assertInvalidConversion(_dataConverter, DataUtils.of(createValue_2()), target);
    assertConversionCount(1, _dataConverter, target);
  }

  @Test
  public void testToValue_2() {
    final JavaTypeInfo<Value[][]> target = JavaTypeInfo.builder(Value[][].class).get();
    assertEquals(true, _dataConverter.canConvertTo(target));
    assertInvalidConversion(_dataConverter, DataUtils.of(createValue()), target);
    assertInvalidConversion(_dataConverter, DataUtils.of(createValue_1()), target);
    final Data data = DataUtils.of(createValue_2());
    assertValidConversion(_dataConverter, data, target, data.getMatrix());
    assertConversionCount(1, _dataConverter, target);
  }

}
