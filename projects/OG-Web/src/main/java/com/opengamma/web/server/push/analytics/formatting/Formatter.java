/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;

/**
 * @param <T> Type of object formatted by the formatter
 */
public interface Formatter<T> {

  public static final String FORMATTING_ERROR = "Formatting Error";

  enum FormatType {
    PRIMITIVE,
    DOUBLE,
    CURVE,
    SURFACE_DATA,
    UNPLOTTABLE_SURFACE_DATA,
    LABELLED_MATRIX_1D,
    LABELLED_MATRIX_2D,
    LABELLED_MATRIX_3D,
    TIME_SERIES,
    TENOR,
    UNKNOWN
  }

  Object formatForDisplay(T value, ValueSpecification valueSpec);

  Object formatForExpandedDisplay(T value, ValueSpecification valueSpec);

  Object formatForHistory(T history, ValueSpecification valueSpec);

  /**
   * If all values of type {@link T} can be formatted the same this method returns the common format type. If
   * different instances of {@link T} require different formatting this method should return {@link FormatType#UNKNOWN}.
   * @return The format type for {@link T} or {@link FormatType#UNKNOWN} if different instances require different 
   * formatting
   */
  FormatType getFormatForType();

  /**
   * Returns the format type for a value. If all values of a type can be formatted using the same formatter this
   * should always return the same type as {@link #getFormatForType()}. This method should never return
   * {@link FormatType#UNKNOWN}
   * @param value The value
   * @return The format type for the value
   */
  FormatType getFormatForValue(T value);
}
