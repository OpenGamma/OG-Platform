/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.analytics.math.linearalgebra;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.longdog.datacontainers.matrix.OGRealDenseMatrix;
import com.opengamma.longdog.nodes.SVD;

public class SVDecompositionOG extends Decomposition<SVDecompositionResult> {

  @Override
  public SVDecompositionResult evaluate(final DoubleMatrix2D x) {
    Validate.notNull(x);
    MatrixValidate.notNaNOrInfinite(x);
    SVD svd = new SVD(new OGRealDenseMatrix(x.asDoubleAoA()));
    return new SVDecompositionOGResult(svd);
  }

}
