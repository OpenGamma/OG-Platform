/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.linear.CholeskyDecomposition;
import org.apache.commons.math.linear.CholeskyDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;

import com.opengamma.math.MathException;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.util.wrapper.CommonsMathWrapper;

/**
 * This class is a wrapper for the <a href="http://commons.apache.org/math/api-2.1/org/apache/commons/math/linear/CholeskyDecompositionImpl.html">Commons Math library implementation</a> 
 * of Cholesky decomposition.
 */
public class CholeskyDecompositionCommons extends Decomposition<CholeskyDecompositionResult> {

  /**
   * {@inheritDoc}
   */
  @Override
  public CholeskyDecompositionResult evaluate(final DoubleMatrix2D x) {
    Validate.notNull(x);
    final RealMatrix temp = CommonsMathWrapper.wrap(x);
    CholeskyDecomposition cholesky;
    try {
      cholesky = new CholeskyDecompositionImpl(temp);
    } catch (Exception e) {
      throw new MathException(e.toString());
    }
    return new CholeskyDecompositionCommonsResult(cholesky);
  }

}
