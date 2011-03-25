/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import java.util.List;

import com.opengamma.language.context.SessionContext;
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
   * Returns the list of types the converter will attempt to convert directly into the given type.
   * 
   * @param targetType the desired type, not {@code null}. This will only be invoked for types that return {@code true} from {@link #canConvertTo}.
   * @return the list of types it can convert, not {@code null} 
   */
  List<JavaTypeInfo<?>> getConversionsTo(JavaTypeInfo<?> targetType);

  /**
   * Tests whether the type converter can convert the value to the the given type. This will only
   * be invoked for types that return {@code true} from {@link #canConvertTo}.
   * 
   * @param sessionContext the client session context, not {@code null}
   * @param fromValue the value to convert, not {@code null}
   * @param targetType the type to convert to, not {@code null}
   * @return {@code true} if the conversion is valid, {@code false} otherwise
   */
  boolean canConvert(SessionContext sessionContext, Object fromValue, JavaTypeInfo<?> targetType);

  /**
   * Converts a value to a given type. This will only be invoked for types and values that return {@code true}
   * from {@link #canConvert}.
   * 
   * @param <T> the raw type expected of the conversion
   * @param sessionContext the client session context, not {@code null}
   * @param fromValue the value to convert, not {@code null}
   * @param targetType the type to convert to, not {@code null}
   * @return the converted value, {@code null} if that is the valid conversion 
   */
  <T> T convert(SessionContext sessionContext, Object fromValue, JavaTypeInfo<T> targetType);

}
