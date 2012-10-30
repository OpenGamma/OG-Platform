/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import com.opengamma.language.Data;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Default implementation of {@link ResultConverter}.
 */
public class DefaultResultConverter implements ResultConverter {

  private static final JavaTypeInfo<Data> DATA = JavaTypeInfo.builder(Data.class).get();

  @Override
  public Data convertResult(final SessionContext sessionContext, final Object result) {
    final ValueConverter converter = sessionContext.getGlobalContext().getValueConverter();
    return converter.convertValue(sessionContext, result, DATA);
  }

}
