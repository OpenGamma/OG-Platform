/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import org.apache.commons.lang.Validate;

import cern.colt.matrix.linalg.SingularValueDecomposition;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.DoubleMatrixUtils;
import com.opengamma.analytics.math.util.wrapper.ColtMathWrapper;

/**
 * Wrapper for results of the Colt implementation of singular value decomposition {@link SVDecompositionColt}).
 */
public class SVDecompositionColtResult implements SVDecompositionResult {
  private final double _condition;
  private final double _norm;
  private final int _rank;
  private final DoubleMatrix2D _s;
  private final double[] _singularValues;
  private final DoubleMatrix2D _u;
  private final DoubleMatrix2D _v;
  private final DoubleMatrix2D _uTranspose;
  private final DoubleMatrix2D _vTranspose;

  /**
   * @param svd The result of the SV decomposition, not null
   */
  public SVDecompositionColtResult(final SingularValueDecomposition svd) {
    Validate.notNull(svd);
    _condition = svd.cond();
    _norm = svd.norm2();
    _rank = svd.rank();
    _s = ColtMathWrapper.wrap(svd.getS());
    _u = ColtMathWrapper.wrap(svd.getU());
    _uTranspose = DoubleMatrixUtils.getTranspose(_u);
    _v = ColtMathWrapper.wrap(svd.getV());
    _vTranspose = DoubleMatrixUtils.getTranspose(_v);
    _singularValues = svd.getSingularValues();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getConditionNumber() {
    return _condition;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getNorm() {
    return _norm;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getRank() {
    return _rank;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleMatrix2D getS() {
    return _s;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[] getSingularValues() {
    return _singularValues;
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
  public DoubleMatrix2D getUT() {
    return _uTranspose;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleMatrix2D getV() {
    return _v;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleMatrix2D getVT() {
    return _vTranspose;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleMatrix1D solve(final DoubleMatrix1D b) {
    Validate.notNull(b);
    return new DoubleMatrix1D(solve(b.getData()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[] solve(final double[] b) {
    Validate.notNull(b);
    final double[][] u = _u.getData();
    final double[][] v = _v.getData();
    final double[] w = _singularValues;
    final int m = u.length;
    final int n = u[0].length;
    int i, j;
    final double[] temp = new double[n];
    double sum;
    for (j = 0; j < n; j++) {
      sum = 0.0;
      // TODO change this to some threshold
      if (w[j] > 0.0) {
        for (i = 0; i < m; i++) {
          sum += u[i][j] * b[i];
        }
        sum /= w[j];
      }
      temp[j] = sum;
    }

    final double[] res = new double[n];
    for (i = 0; i < n; i++) {
      sum = 0.0;
      for (j = 0; j < n; j++) {
        sum += v[i][j] * temp[j];
      }
      res[i] = sum;
    }
    return res;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleMatrix2D solve(final com.opengamma.analytics.math.matrix.DoubleMatrix2D b) {
    Validate.notNull(b);
    final DoubleMatrix2D bt = DoubleMatrixUtils.getTranspose(b);
    final double[][] data = bt.getData();
    final int n = data.length;
    final double[][] res = new double[n][];
    for (int i = 0; i < n; i++) {
      res[i] = solve(data[i]);
    }
    final DoubleMatrix2D xTranspose = new DoubleMatrix2D(res);
    return DoubleMatrixUtils.getTranspose(xTranspose);
  }
}
