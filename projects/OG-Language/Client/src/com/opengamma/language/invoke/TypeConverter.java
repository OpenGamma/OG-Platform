/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import java.util.Map;

import com.opengamma.language.convert.ValueConversionContext;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Rule for converting data to a specific type.
 */
public interface TypeConverter {

  /**
   * Tests whether the type converter will attempt to produce a value of the given type.
   * <p>
   * This answers the question 'can something be converted into the given type?'.
   * 
   * @param targetType  the desired type, not null
   * @return true if the type can be produced, false if it can never be produced
   */
  boolean canConvertTo(JavaTypeInfo<?> targetType);

  /**
   * Returns the set of types the converter will attempt to convert directly into the given type.
   * <p>
   * This answers the question 'which types can be converted into the given type?'.
   * 
   * @param targetType  the desired type, not null. This will only be invoked for types that return true from {@link #canConvertTo}.
   * @return the types it can convert to the target type with the conversion cost, not empty, not null
   */
  Map<JavaTypeInfo<?>, Integer> getConversionsTo(JavaTypeInfo<?> targetType);

  /**
   * Converts a value to a specified type if possible.
   * <p>
   * If the conversion is not possible indicates the failure within the {@code conversionContext}.
   * 
   * @param conversionContext  the value conversion context
   * @param value  the value to convert from
   * @param type  the type to convert to for the type
   */
  void convertValue(ValueConversionContext conversionContext, Object value, JavaTypeInfo<?> type);

  /**
   * Returns a "key" to identify the converter.
   * <p>
   * Only one converter with any given key is allowed in a chain. This is enforced by the
   * aggregator so that a language binding can override conversions already provided.
   * 
   * @return the converter key, not null
   */
  String getTypeConverterKey();

}
