/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public abstract class VectorRootFinder implements MultiDRootFinder<DoubleMatrix1D, DoubleMatrix1D, DoubleMatrix1D> {
  protected void checkInputs(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function, final DoubleMatrix1D x0) {
    if (function == null)
      throw new IllegalArgumentException("Function was null");
    if (x0 == null)
      throw new IllegalArgumentException("start position xo was null");
  }
}
