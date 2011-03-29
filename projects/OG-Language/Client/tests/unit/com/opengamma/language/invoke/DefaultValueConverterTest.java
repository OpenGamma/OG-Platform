/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.Test;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtil;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtil;
import com.opengamma.language.context.ContextTest;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.JavaTypeInfo;

public class DefaultValueConverterTest {

  private final SessionContext _sessionContext = ContextTest.createTestSessionContext();
  private final ValueConverter _valueConverter = new DefaultValueConverter();

  private <T> T convert(final Object value, final JavaTypeInfo<T> type) {
    return _valueConverter.convertValue(_sessionContext, value, type);
  }

  @Test
  public void testDirectConversion() {
    final JavaTypeInfo<Data> dataType = JavaTypeInfo.builder (Data.class).get ();
    final JavaTypeInfo<Value> valueType = JavaTypeInfo.builder (Value.class).get ();
    final JavaTypeInfo<Integer> intType = JavaTypeInfo.builder (Integer.class).get ();
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
    final JavaTypeInfo<Data> dataType = JavaTypeInfo.builder(Data.class).get();
    final JavaTypeInfo<Long> longType = JavaTypeInfo.builder(Long.class).get();
    Data d = convert((Short) (short) 42, dataType);
    assertNotNull(d);
    assertEquals((Integer) 42, d.getSingle().getIntValue());
    /*Long l = convert(DataUtil.of(42), longType);
    assertNotNull(l);
    assertEquals((Long) 42L, l);*/
  }

  @Test(expectedExceptions = InvalidConversionException.class)
  public void testFailedConversion() {
    final JavaTypeInfo<int[]> intArrayType = JavaTypeInfo.builder(int[].class).get();
    convert(DataUtil.of(42), intArrayType);
  }

}
