/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.SingularValueDecompositionImpl;

import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.util.wrapper.CommonsMathWrapper;

/**
 * Wrapper for Commons implementation of SVD
 */
public class SVDecompositionCommons extends SVDecomposition {

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.math.function.Function1D#evaluate(java.lang.Object)
   */

  @Override
  public SVDecompositionResult evaluate(final DoubleMatrix2D x) {
    if (x == null) {
      throw new IllegalArgumentException("Passed a null to SingularValueDecomposition.evaluate");
    }
    final RealMatrix temp = CommonsMathWrapper.wrap(x);
    final org.apache.commons.math.linear.SingularValueDecomposition svd = new SingularValueDecompositionImpl(temp);

    return new SVDecompositionResultCommons(svd);
  }

}
