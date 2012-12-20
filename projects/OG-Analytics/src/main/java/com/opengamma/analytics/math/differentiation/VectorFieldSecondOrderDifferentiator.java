/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.differentiation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class VectorFieldSecondOrderDifferentiator implements Differentiator<DoubleMatrix1D, DoubleMatrix1D, DoubleMatrix2D[]> {

  private static final double DEFAULT_EPS = 1e-4;
  private final double _eps;
  private final double _twoEps;
  private final double _epsSqr;

  private final VectorFieldFirstOrderDifferentiator _vectorFieldDiff;
  private final MatrixFieldFirstOrderDifferentiator _maxtrixFieldDiff;

  public VectorFieldSecondOrderDifferentiator() {
    _eps = DEFAULT_EPS;
    _twoEps = 2 * _eps;
    _epsSqr = _eps * _eps;
    _vectorFieldDiff = new VectorFieldFirstOrderDifferentiator(_eps);
    _maxtrixFieldDiff = new MatrixFieldFirstOrderDifferentiator(_eps);
  }

  /**
   * This computes the second derivative of a vector field, which is a rank 3 tensor field. The tensor is represented as an array of DoubleMatrix2D, where each matrix is
   * a Hessian (for the dependent variable y_i), so the indexing is H^i_{j,k} =\partial^2y_i/\partial x_j \partial x_k
   * @param function the function representing the vector field
   * @return A function representing the second derivative of the vector field (i.e. a rank 3 tensor field)
   */
  @Override
  public Function1D<DoubleMatrix1D, DoubleMatrix2D[]> differentiate(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function) {
    Validate.notNull(function);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFunc = _vectorFieldDiff.differentiate(function);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D[]> hFunc = _maxtrixFieldDiff.differentiate(jacFunc);
    return new Function1D<DoubleMatrix1D, DoubleMatrix2D[]>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix2D[] evaluate(final DoubleMatrix1D x) {
        DoubleMatrix2D[] gamma = hFunc.evaluate(x);
        return reshapeTensor(gamma);
      }
    };
  }

  @Override
  public Function1D<DoubleMatrix1D, DoubleMatrix2D[]> differentiate(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function, final Function1D<DoubleMatrix1D, Boolean> domain) {
    Validate.notNull(function);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFunc = _vectorFieldDiff.differentiate(function, domain);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D[]> hFunc = _maxtrixFieldDiff.differentiate(jacFunc, domain);
    return new Function1D<DoubleMatrix1D, DoubleMatrix2D[]>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix2D[] evaluate(final DoubleMatrix1D x) {

        DoubleMatrix2D[] gamma = hFunc.evaluate(x);
        return reshapeTensor(gamma);
      }
    };
  }

  /**
   * Gamma is in the  form gamma^i_{j,k} =\partial^2y_j/\partial x_i \partial x_k, where i is the index of the matrix in the stack (3rd index of the tensor),
   * and j,k are the individual matrix indices. We would like it in the form H^i_{j,k} =\partial^2y_i/\partial x_j \partial x_k, so that each matrix is a
   *Hessian (for the dependent variable y_i), hence the reshaping below.
   * @param gamma Rank 3 tensor
   * @return Reshaped rank 3 tensor
   */
  private DoubleMatrix2D[] reshapeTensor(final DoubleMatrix2D[] gamma) {
    final int m = gamma.length;
    final int n = gamma[0].getNumberOfRows();
    ArgumentChecker.isTrue(gamma[0].getNumberOfColumns() == m, "tenor wrong size. Seond index is {}, should be {}", gamma[0].getNumberOfColumns(), m);
    DoubleMatrix2D[] res = new DoubleMatrix2D[n];
    for (int i = 0; i < n; i++) {
      double[][] temp = new double[m][m];
      for (int j = 0; j < m; j++) {
        DoubleMatrix2D gammaJ = gamma[j];
        for (int k = j; k < m; k++) {
          temp[j][k] = gammaJ.getEntry(i, k);
        }
      }
      for (int j = 0; j < m; j++) {
        for (int k = 0; k < j; k++) {
          temp[j][k] = temp[k][j];
        }
      }
      res[i] = new DoubleMatrix2D(temp);
    }
    return res;
  }

  public Function1D<DoubleMatrix1D, DoubleMatrix2D[]> differentiateFull(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function) {
    return new Function1D<DoubleMatrix1D, DoubleMatrix2D[]>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix2D[] evaluate(final DoubleMatrix1D x) {
        Validate.notNull(x, "x");
        final DoubleMatrix1D y = function.evaluate(x);
        final int n = x.getNumberOfElements();
        final int m = y.getNumberOfElements();
        final double[] xData = x.getData();
        double oldValueJ, oldValueK;
        final double[][][] res = new double[m][n][n];
        int i, j, k;

        DoubleMatrix1D up, down, upup, updown, downup, downdown;
        for (j = 0; j < n; j++) {
          oldValueJ = xData[j];
          xData[j] += _eps;
          up = function.evaluate(x);
          xData[j] -= _twoEps;
          down = function.evaluate(x);
          for (i = 0; i < m; i++) {
            res[i][j][j] = (up.getEntry(i) + down.getEntry(i) - 2 * y.getEntry(i)) / _epsSqr;
          }
          for (k = j + 1; k < n; k++) {
            oldValueK = xData[k];
            xData[k] += _eps;
            downup = function.evaluate(x);
            xData[k] -= _twoEps;
            downdown = function.evaluate(x);
            xData[j] += _twoEps;
            updown = function.evaluate(x);
            xData[k] += _twoEps;
            upup = function.evaluate(x);
            xData[k] = oldValueK;
            for (i = 0; i < m; i++) {
              res[i][j][k] = (upup.getEntry(i) + downdown.getEntry(i) - updown.getEntry(i) - downup.getEntry(i)) / 4 / _epsSqr;
            }
          }
          xData[j] = oldValueJ;
        }
        DoubleMatrix2D[] mres = new DoubleMatrix2D[m];
        for (i = 0; i < m; i++) {
          for (j = 0; j < n; j++) {
            for (k = 0; k < j; k++) {
              res[i][j][k] = res[i][k][j];
            }
          }
          mres[i] = new DoubleMatrix2D(res[i]);
        }
        return mres;
      }
    };
  }

  public Function1D<DoubleMatrix1D, DoubleMatrix2D> differentiateNoCross(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function) {
    return new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
        Validate.notNull(x, "x");
        final DoubleMatrix1D y = function.evaluate(x);
        final int n = x.getNumberOfElements();
        final int m = y.getNumberOfElements();
        final double[] xData = x.getData();
        double oldValue;
        final double[][] res = new double[m][n];
        int i, j;

        DoubleMatrix1D up, down;
        for (j = 0; j < n; j++) {
          oldValue = xData[j];
          xData[j] += _eps;
          up = function.evaluate(x);
          xData[j] -= _twoEps;
          down = function.evaluate(x);
          for (i = 0; i < m; i++) {
            res[i][j] = (up.getEntry(i) + down.getEntry(i) - 2 * y.getEntry(i)) / _epsSqr;
          }
          xData[j] = oldValue;
        }
        return new DoubleMatrix2D(res);
      }
    };
  }

}
