/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import cern.colt.matrix.linalg.SingularValueDecomposition;

import com.opengamma.math.util.wrapper.ColtWrapper;

/**
 * 
 */
public class SingularValueDecompositionColt extends Decomposer {

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.math.function.Function1D#evaluate(java.lang.Object)
   */
  @Override
  public DecompositionResult evaluate(final com.opengamma.math.matrix.DoubleMatrix2D x) {
    if (x == null)
      throw new IllegalArgumentException("Passed a null to SingularValueDecomposition.evaluate");
    final cern.colt.matrix.DoubleMatrix2D temp = ColtWrapper.wrap(x);

    final cern.colt.matrix.linalg.SingularValueDecomposition svd = new SingularValueDecomposition(temp);

    return new SingularValueDecompositionColtResultHolder(svd);
  }

}
