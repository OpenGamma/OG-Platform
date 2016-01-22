/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import java.io.Serializable;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.linear.DecompositionSolver;
import org.apache.commons.math.linear.LUDecomposition;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.util.wrapper.CommonsMathWrapper;

/**
 * Wrapper for results of the Commons implementation of LU decomposition ({@link LUDecompositionCommons})
 */
public class LUDecompositionCommonsResult implements LUDecompositionResult, Serializable {
  private final double _determinant;
  private final DoubleMatrix2D _l;
  private final DoubleMatrix2D _p;
  private final int[] _pivot;
  private final DecompositionSolver _solver;
  private final DoubleMatrix2D _u;

  /**
   * @param lu The result of the LU decomposition, not null. $\mathbf{L}$ cannot be singular.
   */
  public LUDecompositionCommonsResult(final LUDecomposition lu) {
    Validate.notNull(lu, "LU decomposition");
    Validate.notNull(lu.getL(), "Matrix is singular; could not perform LU decomposition");
    _determinant = lu.getDeterminant();
    _l = CommonsMathWrapper.unwrap(lu.getL());
    _p = CommonsMathWrapper.unwrap(lu.getP());
    _pivot = lu.getPivot();
    _solver = lu.getSolver();
    _u = CommonsMathWrapper.unwrap(lu.getU());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getDeterminant() {
    return _determinant;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleMatrix2D getL() {
    return _l;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleMatrix2D getP() {
    return _p;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int[] getPivot() {
    return _pivot;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleMatrix2D getU() {
    return _u;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleMatrix1D solve(final DoubleMatrix1D b) {
    Validate.notNull(b);
    return CommonsMathWrapper.unwrap(_solver.solve(CommonsMathWrapper.wrap(b)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[] solve(final double[] b) {
    Validate.notNull(b);
    return _solver.solve(b);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleMatrix2D solve(final DoubleMatrix2D b) {
    Validate.notNull(b);
    return CommonsMathWrapper.unwrap(_solver.solve(CommonsMathWrapper.wrap(b)));
  }

}
