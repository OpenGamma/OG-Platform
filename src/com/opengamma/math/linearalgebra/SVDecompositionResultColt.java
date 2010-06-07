/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.util.wrapper.ColtMathWrapper;

/**
 * Wrapper for results of Colt implementation of SVD
 */
public class SVDecompositionResultColt implements SVDecompositionResult {

  private final cern.colt.matrix.linalg.SingularValueDecomposition _svd;

  public SVDecompositionResultColt(final cern.colt.matrix.linalg.SingularValueDecomposition svd) {
    _svd = svd;
  }

  /*
   * (non-Javadoc)
   * 
   * @seecom.opengamma.math.linearalgebra.SingularValueDecompositionResult#
   * getConditionNumber()
   */
  @Override
  public double getConditionNumber() {
    return _svd.cond();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.linearalgebra.SingularValueDecompositionResult#getNorm()
   */
  @Override
  public double getNorm() {
    return _svd.norm2();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.linearalgebra.SingularValueDecompositionResult#getRank()
   */
  @Override
  public int getRank() {
    return _svd.rank();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.linearalgebra.SingularValueDecompositionResult#getS()
   */
  @Override
  public DoubleMatrix2D getS() {

    return ColtMathWrapper.wrap(_svd.getS());
  }

  /*
   * (non-Javadoc)
   * 
   * @seecom.opengamma.math.linearalgebra.SingularValueDecompositionResult#
   * getSingularValues()
   */
  @Override
  public double[] getSingularValues() {
    return _svd.getSingularValues();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.linearalgebra.SingularValueDecompositionResult#getU()
   */
  @Override
  public DoubleMatrix2D getU() {

    return ColtMathWrapper.wrap(_svd.getU());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.linearalgebra.SingularValueDecompositionResult#getUT()
   */
  @Override
  public DoubleMatrix2D getUT() {

    return ColtMathWrapper.wrap(_svd.getU()).getTranspose();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.linearalgebra.SingularValueDecompositionResult#getV()
   */
  @Override
  public DoubleMatrix2D getV() {

    return ColtMathWrapper.wrap(_svd.getV());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.linearalgebra.SingularValueDecompositionResult#getVT()
   */
  @Override
  public DoubleMatrix2D getVT() {

    return ColtMathWrapper.wrap(_svd.getV()).getTranspose();
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.linearalgebra.SingularValueDecompositionResult#Solve(com.opengamma.math.matrix.DoubleMatrix1D)
   */
  @Override
  public DoubleMatrix1D solve(DoubleMatrix1D b) {
    return new DoubleMatrix1D(solve(b.getData()));
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.linearalgebra.DecompositionResult#solve(double[])
   */
  @Override
  public double[] solve(final double[] b) {
    double[][] u = _svd.getU().toArray();
    double[][] v = _svd.getV().toArray();
    double[] w = _svd.getSingularValues();

    final int m = u.length;
    final int n = u[0].length;
    int i, j;

    final double[] temp = new double[n];
    double sum;
    for (j = 0; j < n; j++) {
      sum = 0.0;
      // TODO chance this to some threshold
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

  /* (non-Javadoc)
   * @see com.opengamma.math.linearalgebra.DecompositionResult#solve(com.opengamma.math.matrix.DoubleMatrix2D)
   */
  @Override
  public DoubleMatrix2D solve(DoubleMatrix2D b) {
    DoubleMatrix2D bt = b.getTranspose();
    double[][] data = bt.getData();
    int n = data.length;
    double[][] res = new double[n][];
    for (int i = 0; i < n; i++) {
      res[i] = solve(data[i]);
    }

    DoubleMatrix2D xt = new DoubleMatrix2D(res);
    return xt.getTranspose();
  }
}
