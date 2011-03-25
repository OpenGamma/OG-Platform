/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.List;

import com.opengamma.language.context.ContextTest;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.TypeConverter;

/* package */class AbstractConverterTest {

  private final SessionContext _sessionContext = ContextTest.createTestSessionContext();

  protected SessionContext getSessionContext() {
    return _sessionContext;
  }

  protected <T, J> void assertValidConversion(final TypeConverter converter, final T value,
      final JavaTypeInfo<J> target, final J expected) {
    assertEquals(true, converter.canConvert(getSessionContext(), value, target));
    J result = converter.convert(getSessionContext(), value, target);
    assertNotNull(value);
    assertEquals(expected, result);
  }

  protected <T, J> void assertInvalidConversion(final TypeConverter converter, final T value,
      final JavaTypeInfo<J> target) {
    assertEquals(false, converter.canConvert(getSessionContext(), value, target));
  }

  protected <J> void assertConversionCount(final int expected, final TypeConverter converter,
      final JavaTypeInfo<J> target) {
    final List<JavaTypeInfo<?>> conversions = converter.getConversionsTo(target);
    assertNotNull(conversions);
    assertEquals(expected, conversions.size());
  }

}
