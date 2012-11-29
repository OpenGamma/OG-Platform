/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;

/**
 * @param <T> Type of object formatted by the formatter
 */
/* package */ interface TypeFormatter<T> {

  public static final String FORMATTING_ERROR = "Formatting Error";

  enum Format {
    CELL,
    EXPANDED,
    HISTORY,
  }
  
  Object formatCell(T value, ValueSpecification valueSpec);
  
  Object format(T value, ValueSpecification valueSpec, Format format);

  Class<T> getType();
  
  /**
   * If all values of type {@link T} can be formatted the same this method returns the common format type. If
   * different instances of {@link T} require different formatting this method should return {@link DataType#UNKNOWN}.
   * @return The format type for {@link T} or {@link DataType#UNKNOWN} if different instances require different 
   * formatting
   */
  DataType getDataType();

  /**
   * Returns the format type for a value. If all values of a type can be formatted using the same formatter this
   * should always return the same type as {@link #getDataType()}. This method should never return
   * {@link DataType#UNKNOWN}
   * @param value The value
   * @return The format type for the value
   */
  DataType getDataTypeForValue(T value);
}
