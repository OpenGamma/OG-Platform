/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

/**
 *
 */
public enum DataType {
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
