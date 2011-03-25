/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Converts a value.
 */
public interface ValueConverter {

  <T> T convertValue(SessionContext sessionContext, Object value, JavaTypeInfo<T> type);

}
