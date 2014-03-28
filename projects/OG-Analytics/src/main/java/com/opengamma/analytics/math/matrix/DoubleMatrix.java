/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.matrix;

/**
 * Interface glue
 */
public interface DoubleMatrix extends Matrix<Double> {

  /**
   * Returns a copy of the underlying data as an array of arrays.
   * @return a copy of the underlying data as an array of arrays.
   */
  double[][] asDoubleAoA();

  /**
   * Returns a copy of the underlying data as a column major vector.
   * @return a copy of the underlying data as a column major vector.
   */
  double[] asDoubleArray();

}
