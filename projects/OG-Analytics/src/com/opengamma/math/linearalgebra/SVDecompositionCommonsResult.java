/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.linear.DecompositionSolver;
import org.apache.commons.math.linear.SingularValueDecomposition;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.util.wrapper.CommonsMathWrapper;

/**
 * Wrapper for results of Commons implementation of SVD
 */
public class SVDecompositionCommonsResult implements SVDecompositionResult {
  private final double _condition;
  private final double _norm;
  private final int _rank;
  private final DoubleMatrix2D _s;
  private final double[] _singularValues;
  private final DoubleMatrix2D _u;
  private final DoubleMatrix2D _v;
  private final DoubleMatrix2D _uTranspose;
  private final DoubleMatrix2D _vTranspose;
  private final DecompositionSolver _solver;

  // TODO combine this and the colt result by feeding in the values for condition etc directly. This means that we will need an OG wrapper for the solver 
  // in the commons case or our own for the colt stuff
  public SVDecompositionCommonsResult(final SingularValueDecomposition svd) {
    Validate.notNull(svd);
    _condition = svd.getConditionNumber();
    _norm = svd.getNorm();
    _rank = svd.getRank();
    _s = CommonsMathWrapper.unwrap(svd.getS());
    _singularValues = svd.getSingularValues();
    _u = CommonsMathWrapper.unwrap(svd.getU());
    _uTranspose = CommonsMathWrapper.unwrap(svd.getUT());
    _v = CommonsMathWrapper.unwrap(svd.getV());
    _vTranspose = CommonsMathWrapper.unwrap(svd.getVT());
    _solver = svd.getSolver();
  }

  @Override
  public double getConditionNumber() {
    return _condition;
  }

  @Override
  public double getNorm() {
    return _norm;
  }

  @Override
  public int getRank() {
    return _rank;
  }

  @Override
  public DoubleMatrix2D getS() {
    return _s;
  }

  @Override
  public double[] getSingularValues() {
    return _singularValues;
  }

  @Override
  public DoubleMatrix2D getU() {
    return _u;
  }

  @Override
  public DoubleMatrix2D getUT() {
    return _uTranspose;
  }

  @Override
  public DoubleMatrix2D getV() {
    return _v;
  }

  @Override
  public DoubleMatrix2D getVT() {
    return _vTranspose;
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
