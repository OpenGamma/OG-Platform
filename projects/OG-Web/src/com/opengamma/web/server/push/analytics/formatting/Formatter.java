/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
public interface Formatter<T> {

  enum FormatType {
    PRIMITIVE,
    DOUBLE,
    CURVE,
    SURFACE_DATA,
    LABELLED_MATRIX_1D,
    LABELLED_MATRIX_2D,
    LABELLED_MATRIX_3D,
    TIME_SERIES,
    TENOR
  }

  Object formatForDisplay(T value, ValueSpecification valueSpec);

  Object formatForExpandedDisplay(T value, ValueSpecification valueSpec);

  Object formatForHistory(T history, ValueSpecification valueSpec);

  FormatType getFormatType();
}
