/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

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

  @Test
  public void testDirectConversion() {
    final JavaTypeInfo<Data> dataType = JavaTypeInfo.builder (Data.class).get ();
    final JavaTypeInfo<Value> valueType = JavaTypeInfo.builder (Value.class).get ();
    final JavaTypeInfo<Integer> intType = JavaTypeInfo.builder (Integer.class).get ();
    Data d = _valueConverter.convertValue(_sessionContext, ValueUtil.of(42), dataType);
    assertNotNull(d);
    Integer i = _valueConverter.convertValue(_sessionContext, ValueUtil.of(42), intType);
    assertNotNull(i);
    Value v = _valueConverter.convertValue(_sessionContext, DataUtil.of(42), valueType);
    assertNotNull(v);
    v = _valueConverter.convertValue(_sessionContext, (Integer) 42, valueType);
    assertNotNull(v);
  }

  @Test
  public void testChainConversion() {
    final JavaTypeInfo<Data> dataType = JavaTypeInfo.builder(Data.class).get();
    final JavaTypeInfo<Integer> intType = JavaTypeInfo.builder(Integer.class).get();
    Data d = _valueConverter.convertValue(_sessionContext, (Short) (short) 42, dataType);
    assertNotNull(d);
    Integer i = _valueConverter.convertValue(_sessionContext, DataUtil.of(42), intType);
    assertNotNull(i);
  }

  @Test(expectedExceptions = InvalidConversionException.class)
  public void testFailedConversion() {
    final JavaTypeInfo<int[]> intArrayType = JavaTypeInfo.builder(int[].class).get();
    _valueConverter.convertValue(_sessionContext, DataUtil.of(42), intArrayType);
  }

}
