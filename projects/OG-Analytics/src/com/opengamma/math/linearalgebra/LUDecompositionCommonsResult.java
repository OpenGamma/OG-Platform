/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.linear.DecompositionSolver;
import org.apache.commons.math.linear.LUDecomposition;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.util.wrapper.CommonsMathWrapper;

/**
 * Wrapper for results of Commons implementation of LU Decomposition
 */
public class LUDecompositionCommonsResult implements LUDecompositionResult {
  private final double _determinant;
  private final DoubleMatrix2D _l;
  private final DoubleMatrix2D _p;
  private final int[] _pivot;
  private final DecompositionSolver _solver;
  private final DoubleMatrix2D _u;

  public LUDecompositionCommonsResult(final LUDecomposition lu) {
    if (lu.getL() == null) {
      throw new IllegalArgumentException("Matrix is singular; could not perform LU decomposition");
    }
    _determinant = lu.getDeterminant();
    _l = CommonsMathWrapper.unwrap(lu.getL());
    _p = CommonsMathWrapper.unwrap(lu.getP());
    _pivot = lu.getPivot();
    _solver = lu.getSolver();
    _u = CommonsMathWrapper.unwrap(lu.getU());
  }

  @Override
  public double getDeterminant() {
    return _determinant;
  }

  @Override
  public DoubleMatrix2D getL() {
    return _l;
  }

  @Override
  public DoubleMatrix2D getP() {
    return _p;
  }

  @Override
  public int[] getPivot() {
    return _pivot;
  }

  @Override
  public DoubleMatrix2D getU() {
    return _u;
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
