/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtil;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtil;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.convert.AbstractConverterTest;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.util.tuple.Pair;

public class DefaultValueConverterTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultValueConverterTest.class);

  private final SessionContext _sessionContext = AbstractConverterTest.createTestSessionContext();
  private ValueConverter _valueConverter = new DefaultValueConverter();

  private <T> T convert(final Object value, final JavaTypeInfo<T> type) {
    return _valueConverter.convertValue(_sessionContext, value, type);
  }

  @Test
  public void testDirectAssignment() {
    // No converters needed
    final JavaTypeInfo<String> stringType = JavaTypeInfo.builder(String.class).get();
    final JavaTypeInfo<Integer> intType = JavaTypeInfo.builder(Integer.class).get();
    Integer i = convert((Integer) 42, intType);
    assertNotNull(i);
    assertEquals((Integer) 42, i);
    String s = convert("Foo", stringType);
    assertNotNull(s);
    assertEquals("Foo", s);
  }

  @Test
  public void testDirectConversion() {
    // One type converter needed
    final JavaTypeInfo<Data> dataType = JavaTypeInfo.builder(Data.class).get();
    final JavaTypeInfo<Value> valueType = JavaTypeInfo.builder(Value.class).get();
    final JavaTypeInfo<Integer> intType = JavaTypeInfo.builder(Integer.class).get();
    Data d = convert(ValueUtil.of(42), dataType);
    assertNotNull(d);
    assertEquals((Integer) 42, d.getSingle().getIntValue());
    Integer i = convert(ValueUtil.of(42), intType);
    assertNotNull(i);
    assertEquals((Integer) 42, i);
    Value v = convert(DataUtil.of(42), valueType);
    assertNotNull(v);
    assertEquals((Integer) 42, v.getIntValue());
    v = convert((Integer) 42, valueType);
    assertNotNull(v);
    assertEquals((Integer) 42, v.getIntValue());
  }

  @Test
  public void testChainConversion() {
    // Multiple type converters needed
    final JavaTypeInfo<Data> dataType = JavaTypeInfo.builder(Data.class).get();
    final JavaTypeInfo<Long> longType = JavaTypeInfo.builder(Long.class).get();
    Data d = convert((Short) (short) 42, dataType);
    assertNotNull(d);
    assertEquals((Integer) 42, d.getSingle().getIntValue());
    Long l = convert(DataUtil.of(42), longType);
    assertNotNull(l);
    assertEquals((Long) 42L, l);
  }

  private Pair<Double, Double> testChainConversionSpeedImpl() {
    _valueConverter = new DefaultValueConverter();
    long tCached = System.nanoTime();
    for (int i = 0; i < 1000; i++) {
      testChainConversion();
    }
    tCached = System.nanoTime() - tCached;
    long tUncached = System.nanoTime();
    for (int i = 0; i < 1000; i++) {
      _valueConverter = new DefaultValueConverter();
      testChainConversion();
    }
    tUncached = System.nanoTime() - tUncached;
    return Pair.of((double) tCached / 1e6, (double) tUncached / 1e6);
  }

  @Test(enabled = false)
  public void testChainConversionSpeed() {
    // Allow hotspot to have a go
    testChainConversionSpeedImpl();
    // Get real timings
    Pair<Double, Double> times = testChainConversionSpeedImpl();
    s_logger.info("Cached = {}ms, Uncached = {}ms", times.getFirst(), times.getSecond());
    assertTrue(times.getFirst() < times.getSecond());
  }

  @Test(expectedExceptions = InvalidConversionException.class)
  public void testFailedConversion() {
    final JavaTypeInfo<char[]> intArrayType = JavaTypeInfo.builder(char[].class).get();
    convert(DataUtil.of(42), intArrayType);
  }

  @Test
  public void testDoubleToData() {
    final double[][] matrix = new double[8][8];
    for (int i = 0; i < matrix.length; i++) {
      for (int j = 0; j < matrix[i].length; j++) {
        matrix[i][j] = (double) (i + 1) / (double) (j + 1);
      }
    }
    final JavaTypeInfo<Data> dataType = JavaTypeInfo.builder(Data.class).get();
    final Data data = convert(matrix, dataType);
    assertNotNull(data);
    assertNotNull(data.getMatrix());
    assertEquals(matrix.length, data.getMatrix().length);
    for (int i = 0; i < matrix.length; i++) {
      assertEquals(matrix[i].length, data.getMatrix()[i].length);
      for (int j = 0; j < matrix[i].length; j++) {
        assertEquals(matrix[i][j], data.getMatrix()[i][j].getDoubleValue(), 0);
      }
    }
  }

}
