/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.fail;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.context.SessionContext;
import com.opengamma.language.convert.Converters;
import com.opengamma.language.convert.ValueConversionContext;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.AggregatingTypeConverterProvider;
import com.opengamma.language.invoke.DefaultValueConverter;
import com.opengamma.language.invoke.TypeConverter;
import com.opengamma.language.invoke.TypeConverterProvider;
import com.opengamma.language.invoke.TypeConverterProviderBean;

public class AbstractConverterTest {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractConverterTest.class);

  private final SessionContext _sessionContext;

  protected AbstractConverterTest() {
    final TestUtils testUtils = new TestUtils();
    testUtils.setTypeConverters(getTypeConverters());
    _sessionContext = testUtils.createSessionContext();
  }

  /**
   * Override this if {@link #useBean} is called from {@link #getTypeConverters}.
   * 
   * @param bean the bean to populate with individual type converters to test
   */
  protected void addTypeConvertersToBean(final TypeConverterProviderBean bean) {
    throw new UnsupportedOperationException();
  }

  /**
   * Creates a {@link TypeConverterProviderBean} and passes it to {@link #addTypeConvertersToBean} to be populated.
   * 
   * @return the populated type converter provider, not {@code null}
   */
  protected final TypeConverterProvider useBean() {
    final AggregatingTypeConverterProvider agg = new AggregatingTypeConverterProvider();
    final TypeConverterProviderBean bean = new TypeConverterProviderBean();
    addTypeConvertersToBean(bean);
    agg.addTypeConverterProvider(new Converters());
    agg.addTypeConverterProvider(bean);
    return agg;
  }

  /**
   * Returns the type converters to configure the context with. By default uses the OG-Language default converters. Override
   * this to use specific converters to test. A simple implementation calls {@link #useBean} and implements {@link #addTypeConvertersToBean}.
   * 
   * @return the type converter provider, not {@code null}
   */
  protected TypeConverterProvider getTypeConverters() {
    return new Converters();
  }

  protected SessionContext getSessionContext() {
    return _sessionContext;
  }

  protected <T, J> void assertValidConversion(final TypeConverter converter, final T value,
      final JavaTypeInfo<J> target, final J expected) {
    final ValueConversionContext context = new ValueConversionContext(getSessionContext(), new DefaultValueConverter());
    converter.convertValue(context, value, target);
    if (context.isFailed()) {
      s_logger.warn("Can't convert from {}/{} to {}, expected {}", new Object[] {value.getClass(), value, target, expected });
      fail();
    }
    assertNotNull(value);
    if (expected.getClass().isArray()) {
      if (expected instanceof boolean[]) {
        final boolean[] e = (boolean[]) expected;
        final boolean[] r = (boolean[]) context.getResult();
        assertEquals(e.length, r.length);
        for (int i = 0; i < e.length; i++) {
          assertEquals(e[i], r[i]);
        }
      } else if (expected instanceof byte[]) {
        assertArrayEquals((byte[]) expected, (byte[]) context.getResult());
      } else if (expected instanceof char[]) {
        assertArrayEquals((char[]) expected, (char[]) context.getResult());
      } else if (expected instanceof double[]) {
        assertArrayEquals((double[]) expected, (double[]) context.getResult(), 0d);
      } else if (expected instanceof float[]) {
        assertArrayEquals((float[]) expected, (float[]) context.getResult(), 0f);
      } else if (expected instanceof int[]) {
        assertArrayEquals((int[]) expected, (int[]) context.getResult());
      } else if (expected instanceof long[]) {
        assertArrayEquals((long[]) expected, (long[]) context.getResult());
      } else if (expected instanceof short[]) {
        assertArrayEquals((short[]) expected, (short[]) context.getResult());
      } else {
        assertArrayEquals((Object[]) expected, (Object[]) context.getResult());
      }
    } else {
      final Object result = context.getResult();
      if (!expected.equals(result)) {
        s_logger.warn("Bad conversion from {}/{} to {}, expected {}, got {}", new Object[] {value.getClass(), value, target, expected, result });
        fail();
      }
    }
  }

  protected <T, J> void assertInvalidConversion(final TypeConverter converter, final T value,
      final JavaTypeInfo<J> target) {
    final ValueConversionContext context = new ValueConversionContext(getSessionContext(), new DefaultValueConverter());
    converter.convertValue(context, value, target);
    if (!context.isFailed()) {
      final Object result = context.getResult();
      s_logger.warn("Shouldn't convert from {}/{} to {}, got {}", new Object[] {value.getClass(), value, target, result });
      fail();
    }
  }

  protected <J> void assertConversionCount(final int expected, final TypeConverter converter,
      final JavaTypeInfo<J> target) {
    final Map<JavaTypeInfo<?>, Integer> conversions = converter.getConversionsTo(target);
    assertNotNull(conversions);
    assertEquals(expected, conversions.size());
  }

}
