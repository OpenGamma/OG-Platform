/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import org.apache.commons.lang.Validate;

import cern.colt.matrix.linalg.SingularValueDecomposition;

import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.util.wrapper.ColtMathWrapper;

/**
 * Wrapper for Colt implementation of SVD
 */
public class SVDecompositionColt extends Decomposition<SVDecompositionResult> {

  @Override
  public SVDecompositionResult evaluate(final DoubleMatrix2D x) {
    Validate.notNull(x);
    final cern.colt.matrix.DoubleMatrix2D coltMatrix = ColtMathWrapper.wrap(x);
    final SingularValueDecomposition svd = new SingularValueDecomposition(coltMatrix);
    return new SVDecompositionColtResult(svd);
  }

}
