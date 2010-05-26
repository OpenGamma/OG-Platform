/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import org.apache.commons.math.linear.QRDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;

import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.util.wrapper.CommonsMathWrapper;

/**
 * Wrapper for Commons implementation of QR Decomposition
 */
public class QRDecompositionCommons extends QRDecomposition {

  /* (non-Javadoc)
   * @see com.opengamma.math.function.Function1D#evaluate(java.lang.Object)
   */
  @Override
  public QRDecompositionResult evaluate(DoubleMatrix2D x) {
    if (x == null) {
      throw new IllegalArgumentException("Passed a null to QRDecomposer.evaluate");
    }
    final RealMatrix temp = CommonsMathWrapper.wrap(x);
    final org.apache.commons.math.linear.QRDecomposition qr = new QRDecompositionImpl(temp);

    return new QRDecompositionResultCommons(qr);
  }

}
