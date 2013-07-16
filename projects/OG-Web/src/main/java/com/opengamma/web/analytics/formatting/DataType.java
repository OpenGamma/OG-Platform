/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

/**
 * The type of data.
 */
public enum DataType {

  /**
   * Position.
   */
  POSITION,
  /**
   * Node.
   */
  NODE,
  /**
   * Fungible trade.
   */
  FUNGIBLE_TRADE,
  /**
   * OTC trade.
   */
  OTC_TRADE,
  /**
   * String.
   */
  STRING,
  /**
   * Double.
   */
  DOUBLE,
  /**
   * Curve.
   */
  CURVE,
  /**
   * Surface data.
   */
  SURFACE_DATA,
  /**
   * Vector.
   */
  VECTOR, // a single column of values without labels on the rows
  /**
   * Labeled matrix 1D.
   */
  LABELLED_MATRIX_1D, // a single column of values with a label for each row
  /**
   * Labeled matrix 2D.
   */
  LABELLED_MATRIX_2D,
  /**
   * Labeled matrix 3D.
   */
  LABELLED_MATRIX_3D,
  /**
   * Matrix 2D.
   */
  MATRIX_2D,
  /**
   * Time series.
   */
  TIME_SERIES,
  /**
   * Tenor.
   */
  TENOR,
  /**
   * Unknown.
   */
  UNKNOWN,

}
