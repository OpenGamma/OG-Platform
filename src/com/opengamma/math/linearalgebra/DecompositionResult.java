/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import com.opengamma.math.matrix.DoubleMatrix1D;



/**
 * 
 */
public interface DecompositionResult {
  
  
  /**
   * Solve Ax = b where A is a (decomposed) matrix and b is some vector 
   * @param b
   * @return the vector x
   */
  DoubleMatrix1D Solve(DoubleMatrix1D b);

  
}
