/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Formatter.
 * 
 * @param <T> the type of object formatted by the formatter
 */
public interface TypeFormatter<T> {

  /**
   * Constant used for formatting errors.
   */
  String FORMATTING_ERROR = "Formatting Error";

  /**
   * Defines the types of format.
   */
  enum Format {
    CELL,
    EXPANDED,
    HISTORY,
  }

  Object formatCell(T value, ValueSpecification valueSpec, Object inlineKey);

  Object format(T value, ValueSpecification valueSpec, Format format, Object inlineKey);

  Class<T> getType();

  /**
   * If all values of type {@link T} can be formatted the same this method returns the common format type. If
   * different instances of {@link T} require different formatting this method should return {@link DataType#UNKNOWN}.
   * 
   * @return The format type for {@link T} or {@link DataType#UNKNOWN} if different instances require different 
   * formatting
   */
  DataType getDataType();

  /**
   * Returns the format type for a value. If all values of a type can be formatted using the same formatter this
   * should always return the same type as {@link #getDataType()}. This method should never return
   * {@link DataType#UNKNOWN}
   * 
   * @param value  the value
   * @return the format type for the value
   */
  DataType getDataTypeForValue(T value);

}
