/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.util.wrapper.CommonsMathWrapper;

/**
 * Wrapper for results of Commons implementation of QR Decomposition
 */
public class QRDecompositionResultCommons implements QRDecompositionResult {

  private final org.apache.commons.math.linear.QRDecomposition _qr;

  public QRDecompositionResultCommons(final org.apache.commons.math.linear.QRDecomposition qr) {
    _qr = qr;
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.linearalgebra.QRDecompositionResult#getH()
   */
  @Override
  public DoubleMatrix2D getH() {
    return CommonsMathWrapper.wrap(_qr.getH());
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.linearalgebra.QRDecompositionResult#getQ()
   */
  @Override
  public DoubleMatrix2D getQ() {
    return CommonsMathWrapper.wrap(_qr.getQ());
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.linearalgebra.QRDecompositionResult#getQT()
   */
  @Override
  public DoubleMatrix2D getQT() {
    return CommonsMathWrapper.wrap(_qr.getQT());
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.linearalgebra.QRDecompositionResult#getR()
   */
  @Override
  public DoubleMatrix2D getR() {
    return CommonsMathWrapper.wrap(_qr.getR());
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.linearalgebra.DecompositionResult#solve(com.opengamma.math.matrix.DoubleMatrix1D)
   */
  @Override
  public DoubleMatrix1D solve(DoubleMatrix1D b) {
    return CommonsMathWrapper.wrap(_qr.getSolver().solve(CommonsMathWrapper.wrap(b)));
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.linearalgebra.DecompositionResult#solve(double[])
   */
  @Override
  public double[] solve(double[] b) {
    return _qr.getSolver().solve(b);
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.linearalgebra.DecompositionResult#solve(com.opengamma.math.matrix.DoubleMatrix2D)
   */
  @Override
  public DoubleMatrix2D solve(DoubleMatrix2D b) {
    return CommonsMathWrapper.wrap(_qr.getSolver().solve(CommonsMathWrapper.wrap(b)));
  }

}
