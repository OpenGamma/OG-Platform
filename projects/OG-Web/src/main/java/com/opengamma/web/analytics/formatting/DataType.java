/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

/**
 *
 */
public enum DataType {
  PRIMITIVE,
  DOUBLE,
  CURVE,
  SURFACE_DATA,
  VECTOR, // a single column of values without labels on the rows
  LABELLED_MATRIX_1D, // a single column of values with a label for each row
  LABELLED_MATRIX_2D,
  LABELLED_MATRIX_3D,
  TIME_SERIES,
  TENOR,
  UNKNOWN
}
