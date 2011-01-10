/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.linear.DecompositionSolver;
import org.apache.commons.math.linear.QRDecomposition;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.DoubleMatrixUtils;
import com.opengamma.math.util.wrapper.CommonsMathWrapper;

/**
 * Wrapper for results of Commons implementation of QR Decomposition
 */
public class QRDecompositionCommonsResult implements QRDecompositionResult {
  private final DoubleMatrix2D _h;
  private final DoubleMatrix2D _q;
  private final DoubleMatrix2D _r;
  private final DoubleMatrix2D _qTranspose;
  private final DecompositionSolver _solver;

  public QRDecompositionCommonsResult(final QRDecomposition qr) {
    Validate.notNull(qr);
    _h = CommonsMathWrapper.unwrap(qr.getH());
    _q = CommonsMathWrapper.unwrap(qr.getQ());
    _r = CommonsMathWrapper.unwrap(qr.getR());
    _qTranspose = DoubleMatrixUtils.getTranspose(_q);
    _solver = qr.getSolver();
  }

  @Override
  public DoubleMatrix2D getH() {
    return _h;
  }

  @Override
  public DoubleMatrix2D getQ() {
    return _q;
  }

  @Override
  public DoubleMatrix2D getQT() {
    return _qTranspose;
  }

  @Override
  public DoubleMatrix2D getR() {
    return _r;
  }

  @Override
  public DoubleMatrix1D solve(final DoubleMatrix1D b) {
    Validate.notNull(b);
    return CommonsMathWrapper.unwrap(_solver.solve(CommonsMathWrapper.wrap(b)));
  }

  @Override
  public double[] solve(final double[] b) {
    Validate.notNull(b);
    return _solver.solve(b);
  }

  @Override
  public DoubleMatrix2D solve(final DoubleMatrix2D b) {
    Validate.notNull(b);
    return CommonsMathWrapper.unwrap(_solver.solve(CommonsMathWrapper.wrap(b)));
  }

}
