/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.linearalgebra.Decomposer;
import com.opengamma.math.linearalgebra.SingularValueDecomposition;
import com.opengamma.math.linearalgebra.SingularValueDecompositionResult;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class NewtonVectorRootFinder extends VectorRootFinder {

  private static final double DEF_TOL = 1e-7;
  private static final int MAX_STEPS = 100;
  private static final double EPS = 1e-8;
  private final double _relTol, _absTol;
  private final int _maxSteps;
  private final Decomposer _SVD;

  public NewtonVectorRootFinder(final double atol, final double rtol, final int maxSteps) {
    _absTol = atol;
    _relTol = rtol;
    _maxSteps = maxSteps;
    _SVD = new SingularValueDecomposition();
  }

  public NewtonVectorRootFinder(final double atol, final double rtol) {
    this(atol, rtol, MAX_STEPS);
  }

  public NewtonVectorRootFinder(final double tol, final int maxSteps) {
    this(tol, tol, maxSteps);
  }

  public NewtonVectorRootFinder(final double tol) {
    this(tol, tol);
  }

  public NewtonVectorRootFinder(final int maxSteps) {
    this(DEF_TOL, DEF_TOL, maxSteps);
  }

  public NewtonVectorRootFinder() {
    this(DEF_TOL, DEF_TOL, MAX_STEPS);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.rootfinding.MultiDRootFinder#getRoot(com.opengamma.math
   * .function.Function1D)
   */
  @Override
  public DoubleMatrix1D getRoot(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function,
      final DoubleMatrix1D startPosition) {

    DoubleMatrix1D x1 = startPosition;
    DoubleMatrix1D x2 = getNextPosition(function, x1);
    int count = 0;
    while (!closeEnough(x1, x2)) {
      x1 = x2;
      x2 = getNextPosition(function, x1);
      count++;
      if (count > _maxSteps)
        throw new RootNotFoundException("Failed to converge");
    }

    return x2;
  }

  private boolean closeEnough(final DoubleMatrix1D x, final DoubleMatrix1D y) {
    final double[] xd = x.getDataAsPrimitiveArray();
    final double[] yd = y.getDataAsPrimitiveArray();
    final int n = xd.length;
    if (yd.length != n)
      throw new IllegalArgumentException("Different length inputs");
    double diff, scale;
    for (int i = 0; i < n; i++) {
      diff = Math.abs(xd[i] - yd[i]);
      scale = Math.max(Math.abs(xd[i]), Math.abs(yd[i]));
      if (diff > _absTol + scale * _relTol)
        return false;
    }
    return true;
  }

  private DoubleMatrix1D getNextPosition(final Function1D<DoubleMatrix1D, DoubleMatrix1D> f, final DoubleMatrix1D x) {
    final DoubleMatrix2D h = getFirstDerivativeMatrix(f, x);
    final SingularValueDecompositionResult svd = (SingularValueDecompositionResult) _SVD.evaluate(h);

    final double[] b = f.evaluate(x).getDataAsPrimitiveArray();
    final double[][] u = svd.getU().getDataAsPrimitiveArray();
    final double[][] v = svd.getV().getDataAsPrimitiveArray();
    final double[] w = svd.getSingularValues();
    final int m = u.length;
    final int n = u[0].length;
    int i, j;
    if (m != b.length)
      throw new IndexOutOfBoundsException("size of matrix U does not match position vector");

    final double[] temp = new double[n];
    double sum;
    for (j = 0; j < n; j++) {
      sum = 0.0;
      // TODO chance this to some threshold
      if (w[j] > 0.0) {
        for (i = 0; i < m; i++)
          sum += u[i][j] * b[i];
        sum /= w[j];
      }
      temp[j] = sum;
    }

    final double[] res = new double[n];
    for (i = 0; i < n; i++) {
      sum = 0.0;
      for (j = 0; j < n; j++)
        sum += v[i][j] * temp[j];
      res[i] = x.getDataAsPrimitiveArray()[i] - sum;

    }
    return new DoubleMatrix1D(res);
  }

  private DoubleMatrix2D getFirstDerivativeMatrix(final Function1D<DoubleMatrix1D, DoubleMatrix1D> f,
      final DoubleMatrix1D x) {
    final double[] pos = x.getDataAsPrimitiveArray();
    final int m = pos.length;

    pos[0] += EPS;
    double[] yp = f.evaluate(new DoubleMatrix1D(pos)).getDataAsPrimitiveArray();
    pos[0] -= 2 * EPS;
    double[] ym = f.evaluate(new DoubleMatrix1D(pos)).getDataAsPrimitiveArray();
    final int n = yp.length;
    final double[][] res = new double[n][m];
    for (int i = 0; i < n; i++)
      res[i][0] = (yp[i] - ym[i]) / 2 / EPS;

    for (int j = 1; j < m; j++) {
      pos[j] += EPS;
      yp = f.evaluate(new DoubleMatrix1D(pos)).getDataAsPrimitiveArray();
      pos[j] -= 2 * EPS;
      ym = f.evaluate(new DoubleMatrix1D(pos)).getDataAsPrimitiveArray();
      for (int i = 0; i < n; i++)
        res[i][j] = (yp[i] - ym[i]) / 2 / EPS;
    }

    return new DoubleMatrix2D(res);

  }

}
