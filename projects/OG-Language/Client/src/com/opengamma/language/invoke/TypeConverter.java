/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import java.util.List;

import com.opengamma.language.convert.ValueConversionContext;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Rule for converting data to a specific type.
 */
public interface TypeConverter {

  /**
   * Tests whether the type converter will attempt to produce a value of the given type.
   * 
   * @param targetType the desired type, not {@code null}
   * @return {@code true} if the type can be produced, {@code false} if it can never be produced
   */
  boolean canConvertTo(JavaTypeInfo<?> targetType);

  /**
   * Returns the set of types the converter will attempt to convert directly into the given type.
   * 
   * @param targetType the desired type, not {@code null}. This will only be invoked for types that return {@code true} from {@link #canConvertTo}.
   * @return the list of types it can convert to the target type, not {@code null} and not empty 
   */
  List<JavaTypeInfo<?>> getConversionsTo(JavaTypeInfo<?> targetType);

  /**
   * Converts a value to a specified type if possible. If the conversion is not possible indicates the
   * failure within the {@code conversionContext}.
   * 
   * @param conversionContext the value conversion context
   * @param value the value to convert from
   * @param type the type to convert to
   * for the type.
   */
  void convertValue(ValueConversionContext conversionContext, Object value, JavaTypeInfo<?> type);

}
