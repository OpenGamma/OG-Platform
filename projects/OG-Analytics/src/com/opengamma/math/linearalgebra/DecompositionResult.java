/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * interface for holder of matrix decomposition results
 */
public interface DecompositionResult {

  /**
   * Solve Ax = b where A is a (decomposed) matrix and b is some vector 
   * @param b a vector 
   * @return the vector x
   */
  DoubleMatrix1D solve(final DoubleMatrix1D b);

  /**
   * Solve Ax = b where A is a (decomposed) matrix and b is some vector 
   * @param b vector as a double array
   * @return the vector x as a double array
   */
  double[] solve(final double[] b);

  /**
   * Solve Ax = b where A is a (decomposed) matrix and b is some matrix
   * @param b matrix 
   * @return the matrix x
   */
  DoubleMatrix2D solve(final DoubleMatrix2D b);

}
