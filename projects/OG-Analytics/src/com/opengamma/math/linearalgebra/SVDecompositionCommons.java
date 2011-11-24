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
 * This class is a wrapper for the <a href="http://commons.apache.org/math/api-2.1/org/apache/commons/math/linear/SingularValueDecompositionImpl.html">Commons Math library implementation</a>
 * of singular value decomposition.
 */
public class SVDecompositionCommons extends Decomposition<SVDecompositionResult> {

  /**
   * {@inheritDoc}
   */
  @Override
  public SVDecompositionResult evaluate(final DoubleMatrix2D x) {
    Validate.notNull(x);
    MatrixValidate.noNaN(x);
    final RealMatrix commonsMatrix = CommonsMathWrapper.wrap(x);
    final SingularValueDecomposition svd = new SingularValueDecompositionImpl(commonsMatrix);
    return new SVDecompositionCommonsResult(svd);
  }



}
