/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;

import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.util.wrapper.CommonsMathWrapper;

/**
 * Wrapper for Commons implementation of LU Decomposition
 */
public class LUDecompositionCommons extends Decomposition<LUDecompositionResult> {

  /* (non-Javadoc)
   * @see com.opengamma.math.function.Function1D#evaluate(java.lang.Object)
   */
  @Override
  public LUDecompositionResult evaluate(final DoubleMatrix2D x) {
    if (x == null) {
      throw new IllegalArgumentException("Passed a null to LowerUpperDecomposition.evaluate");
    }
    final RealMatrix temp = CommonsMathWrapper.wrap(x);
    final org.apache.commons.math.linear.LUDecomposition lu = new LUDecompositionImpl(temp);

    return new LUDecompositionResultCommons(lu);
  }

}
