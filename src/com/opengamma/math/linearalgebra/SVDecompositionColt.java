/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import org.apache.commons.lang.Validate;

import cern.colt.matrix.linalg.SingularValueDecomposition;

import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.util.wrapper.ColtWrapper;

/**
 * Wrapper for Colt implementation of SVD
 */
public class SVDecompositionColt extends SVDecomposition {

  @Override
  public SVDecompositionResult evaluate(final DoubleMatrix2D x) {
    Validate.notNull(x);
    final cern.colt.matrix.DoubleMatrix2D coltMatrix = ColtWrapper.wrap(x);
    final SingularValueDecomposition svd = new SingularValueDecomposition(coltMatrix);
    return new SVDecompositionResultColt(svd);
  }

}
