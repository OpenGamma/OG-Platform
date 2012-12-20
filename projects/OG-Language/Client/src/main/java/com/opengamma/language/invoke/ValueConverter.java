/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import com.opengamma.language.context.SessionContext;
import com.opengamma.language.convert.ValueConversionContext;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Converts a value.
 */
public abstract class ValueConverter {

  /**
   * Converts a value to a specified type if possible.
   * 
   * @param <T> the raw Java type to convert to
   * @param sessionContext the client session context
   * @param value the value to convert from
   * @param type the type to convert to
   * @return the converted value, null if that is the valid conversion, or the value of {@link JavaTypeInfo#failure}
   * for the type.
   * @throws InvalidConversionException if the conversion is not possible
   */
  public <T> T convertValue(final SessionContext sessionContext, final Object value, final JavaTypeInfo<T> type) {
    final ValueConversionContext conversionContext = new ValueConversionContext(sessionContext, this);
    convertValue(conversionContext, value, type);
    if (conversionContext.isFailed()) {
      throw new InvalidConversionException(value, type);
    }
    return conversionContext.<T>getResult();
  }

  /**
   * Converts a value to a specified type if possible. If the conversion is not possible, flags the failure
   * within the {@link ValueConversionContext} and returns an arbitrary value (e.g. null).
   * 
   * @param conversionContext the value conversion context into which the result, cost, and failure flags must be set if appropriate 
   * @param value the value to convert from
   * @param type the type to convert to
   */
  public abstract void convertValue(ValueConversionContext conversionContext, Object value, JavaTypeInfo<?> type);

}
