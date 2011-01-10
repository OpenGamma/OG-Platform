/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.SingularValueDecomposition;
import org.apache.commons.math.linear.SingularValueDecompositionImpl;

import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.util.wrapper.CommonsMathWrapper;

/**
 * Wrapper for Commons implementation of SVD
 */
public class SVDecompositionCommons extends Decomposition<SVDecompositionResult> {

  @Override
  public SVDecompositionResult evaluate(final DoubleMatrix2D x) {
    Validate.notNull(x);
    final RealMatrix commonsMatrix = CommonsMathWrapper.wrap(x);
    final SingularValueDecomposition svd = new SingularValueDecompositionImpl(commonsMatrix);
    return new SVDecompositionCommonsResult(svd);
  }

}
