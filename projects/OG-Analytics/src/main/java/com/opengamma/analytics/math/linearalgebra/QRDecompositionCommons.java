/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.linear.QRDecomposition;
import org.apache.commons.math.linear.QRDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.util.wrapper.CommonsMathWrapper;

/**
 * This class is a wrapper for the <a href="http://commons.apache.org/math/api-2.1/org/apache/commons/math/linear/QRDecompositionImpl.html">Commons Math library implementation</a> 
 * of QR decomposition.
 */
public class QRDecompositionCommons extends Decomposition<QRDecompositionResult> {

  /**
   * {@inheritDoc}
   */
  @Override
  public QRDecompositionResult evaluate(final DoubleMatrix2D x) {
    Validate.notNull(x);
    final RealMatrix temp = CommonsMathWrapper.wrap(x);
    final QRDecomposition qr = new QRDecompositionImpl(temp);
    return new QRDecompositionCommonsResult(qr);
  }

}
