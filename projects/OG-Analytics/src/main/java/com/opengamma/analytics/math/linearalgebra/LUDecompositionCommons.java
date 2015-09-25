/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import java.io.Serializable;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.linear.LUDecomposition;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.util.wrapper.CommonsMathWrapper;

/**
 * This class is a wrapper for the <a href="http://commons.apache.org/math/api-2.1/org/apache/commons/math/linear/LUDecompositionImpl.html">Commons Math library implementation</a> 
 * of LU decomposition.
 */
public class LUDecompositionCommons extends Decomposition<LUDecompositionResult> implements Serializable {

  /**
   * {@inheritDoc}
   */
  @Override
  public LUDecompositionResult evaluate(final DoubleMatrix2D x) {
    Validate.notNull(x);
    final RealMatrix temp = CommonsMathWrapper.wrap(x);
    final LUDecomposition lu = new LUDecompositionImpl(temp);
    return new LUDecompositionCommonsResult(lu);
  }

}
