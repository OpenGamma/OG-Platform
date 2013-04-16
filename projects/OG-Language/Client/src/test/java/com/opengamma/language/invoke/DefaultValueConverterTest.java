/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgFactory;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.convert.Converters;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.test.TestUtils;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

@Test(groups = TestGroup.UNIT)
public class DefaultValueConverterTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultValueConverterTest.class);

  private final SessionContext _sessionContext;

  private ValueConverter _valueConverter = new DefaultValueConverter();

  public DefaultValueConverterTest() {
    final TestUtils testUtils = new TestUtils();
    testUtils.setTypeConverters(new Converters());
    _sessionContext = testUtils.createSessionContext();
  }

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
    Data d = convert(ValueUtils.of(42), dataType);
    assertNotNull(d);
    assertEquals((Integer) 42, d.getSingle().getIntValue());
    Integer i = convert(ValueUtils.of(42), intType);
    assertNotNull(i);
    assertEquals((Integer) 42, i);
    Value v = convert(DataUtils.of(42), valueType);
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
    Long l = convert(DataUtils.of(42), longType);
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
    convert(DataUtils.of(42), intArrayType);
  }

  @Test
  public void testBoxingConverter() {
    convert(DataUtils.of(42.d), JavaTypeInfo.builder(Double.TYPE).get());
  }

  @Test(expectedExceptions = InvalidConversionException.class)
  public void testNullConverter_fail() {
    convert(new Data(), JavaTypeInfo.builder(String.class).get());
  }

  @Test
  public void testNullConverter_ok() {
    convert(new Data(), JavaTypeInfo.builder(String.class).allowNull().get());
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

  @Test
  public void testStringArray() {
    final String[] arr = new String[8];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = "str " + i;
    }
    final JavaTypeInfo<Data> dataType = JavaTypeInfo.builder(Data.class).get();
    final Data data = convert(arr, dataType);
    assertNotNull(data);
    assertNotNull(data.getLinear());
    assertEquals(arr.length, data.getLinear().length);
  }

  public static final class FudgeObject {

    private final String _str;

    private FudgeObject(final String str) {
      _str = str;
    }

    public MutableFudgeMsg toFudgeMsg(final FudgeMsgFactory factory) {
      final MutableFudgeMsg msg = factory.newMessage();
      msg.add("str", _str);
      return msg;
    }

  }

  @Test
  public void testFudgeConversion() {
    final JavaTypeInfo<Data> dataType = JavaTypeInfo.builder(Data.class).get();
    final FudgeObject object = new FudgeObject("Foo");
    final Data data = convert(object, dataType);
    assertNotNull(data);
    assertNotNull(data.getSingle());
    final FudgeSerializer serializer = new FudgeSerializer(FudgeContext.GLOBAL_DEFAULT);
    assertEquals(FudgeSerializer.addClassHeader(object.toFudgeMsg(serializer), object.getClass()), data.getSingle().getMessageValue());
  }

  @Test
  public void testWithDefault() {
    JavaTypeInfo<Boolean> dataType = JavaTypeInfo.builder(Boolean.class).defaultValue(true).get();
    Boolean v = convert(null, dataType);
    assertEquals(Boolean.TRUE, v);
    v = convert("TRUE", dataType);
    assertEquals(Boolean.TRUE, v);
    v = convert("FALSE", dataType);
    assertEquals(Boolean.FALSE, v);
    v = convert(new Data(), dataType);
    assertEquals(Boolean.TRUE, v);
    v = convert(DataUtils.of(new Value()), dataType);
    assertEquals(Boolean.TRUE, v);
    dataType = JavaTypeInfo.builder(Boolean.class).defaultValue(false).get();
    v = convert(null, dataType);
    assertEquals(Boolean.FALSE, v);
    v = convert("TRUE", dataType);
    assertEquals(Boolean.TRUE, v);
    v = convert("FALSE", dataType);
    assertEquals(Boolean.FALSE, v);
    v = convert(new Data(), dataType);
    assertEquals(Boolean.FALSE, v);
    v = convert(DataUtils.of(new Value()), dataType);
    assertEquals(Boolean.FALSE, v);
  }

}
