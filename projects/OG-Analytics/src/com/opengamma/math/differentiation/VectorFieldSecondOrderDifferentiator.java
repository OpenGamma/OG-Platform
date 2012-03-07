/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.differentiation;

import org.apache.commons.lang.Validate;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class VectorFieldSecondOrderDifferentiator implements Differentiator<DoubleMatrix1D, DoubleMatrix1D, DoubleMatrix2D> {

  private static final double DEFAULT_EPS = 1e-5;
  private final double _eps;
  private final double _twoEps;
  private final double _epsSqr;

  private final VectorFieldFirstOrderDifferentiator _vectorFieldDiff;
  private final MatrixFieldFirstOrderDifferentiator _maxtrixFieldDiff;

  public VectorFieldSecondOrderDifferentiator() {
    _eps = DEFAULT_EPS;
    _twoEps = 2 * _eps;
    _epsSqr = _eps * _eps;
    _vectorFieldDiff = new VectorFieldFirstOrderDifferentiator();
    _maxtrixFieldDiff = new MatrixFieldFirstOrderDifferentiator();
  }

  @Override
  public Function1D<DoubleMatrix1D, DoubleMatrix2D> differentiate(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function) {
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

  public Function1D<DoubleMatrix1D, DoubleMatrix2D[]> differentiateFull2(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function) {
    Validate.notNull(function);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFunc = _vectorFieldDiff.differentiate(function);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D[]> hFunc = _maxtrixFieldDiff.differentiate(jacFunc);
    return new Function1D<DoubleMatrix1D, DoubleMatrix2D[]>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix2D[] evaluate(final DoubleMatrix1D x) {
        //this is in the form gamma^i_{j,k} =\partial^2y_j/\partial x_i \partial x_k, where i is the index of the matrix in the stack (3rd index of the tensor),
        //and j,k are the individual matrix indices. We would like it in the form H^i_{j,k} =\partial^2y_i/\partial x_j \partial x_k, so that each matrix is a
        //Hessian (for the dependent variable y_i), hence the reshaping below.
        DoubleMatrix2D[] gamma = hFunc.evaluate(x);
        final int m = x.getNumberOfElements();
        final int n = gamma[0].getNumberOfRows();
        ArgumentChecker.isTrue(gamma.length == m, "tenor wrong size. Third index is {}, should be {}", gamma.length, m);
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
    };
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

  public Function1D<DoubleMatrix1D, DoubleMatrix2D[]> differentiateFullFromJacobian(final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobian) {
    return new Function1D<DoubleMatrix1D, DoubleMatrix2D[]>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix2D[] evaluate(final DoubleMatrix1D x) {
        DoubleMatrix2D jac = jacobian.evaluate(x);
        final int n = x.getNumberOfElements();
        final int m = jac.getNumberOfRows();
        final double[][][] res = new double[m][n][n];
        final double[] xData = x.getData();
        for (int k = 0; k < n; k++) {
          double oldValue = xData[k];
          xData[k] += _eps;
          DoubleMatrix2D up = jacobian.evaluate(x);
          xData[k] -= _twoEps;
          DoubleMatrix2D down = jacobian.evaluate(x);
          for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
              res[i][j][k] = (up.getEntry(i, j) - down.getEntry(i, j)) / _twoEps;
            }
          }
          xData[k] = oldValue;
        }
        DoubleMatrix2D[] mres = new DoubleMatrix2D[m];
        for (int i = 0; i < m; i++) {
          mres[i] = new DoubleMatrix2D(res[i]);
        }
        return mres;
      }
    };
  }

  public Function1D<DoubleMatrix1D, DoubleMatrix2D[]> differentiateFullFromJacobian(final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobian, final Function1D<DoubleMatrix1D, Boolean> domain) {
    Validate.notNull(jacobian);
    Validate.notNull(domain);

    final double[] wFwd = new double[] {-3., 4., -1. };
    final double[] wCent = new double[] {-1., 0., 1. };
    final double[] wBack = new double[] {1., -4., 3. };

    return new Function1D<DoubleMatrix1D, DoubleMatrix2D[]>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix2D[] evaluate(final DoubleMatrix1D x) {

        Validate.notNull(x, "x");
        ArgumentChecker.isTrue(domain.evaluate(x), "point {} is not in the function domain", x.toString());

        DoubleMatrix2D mid = jacobian.evaluate(x);

        final int n = x.getNumberOfElements();
        final int m = mid.getNumberOfRows();
        final double[][][] res = new double[m][n][n];
        final double[] xData = x.getData();
        DoubleMatrix2D[] y = new DoubleMatrix2D[3];
        double[] w;

        for (int k = 0; k < n; k++) {
          double oldValue = xData[k];
          xData[k] += _eps;

          if (!domain.evaluate(x)) {
            xData[k] = oldValue - _twoEps;
            if (!domain.evaluate(x)) {
              throw new MathException("cannot get derivative at point " + x.toString() + " in direction " + k);
            } else {
              y[2] = mid;
              y[0] = jacobian.evaluate(x);
              xData[k] = oldValue - _eps;
              y[1] = jacobian.evaluate(x);
              w = wBack;
            }
          } else {
            DoubleMatrix2D temp = jacobian.evaluate(x);
            xData[k] = oldValue - _eps;
            if (!domain.evaluate(x)) {
              y[0] = mid;
              y[1] = temp;
              xData[k] = oldValue + _twoEps;
              y[2] = jacobian.evaluate(x);
              w = wFwd;
            } else {
              y[2] = temp;
              xData[k] = oldValue - _eps;
              y[0] = jacobian.evaluate(x);
              y[1] = mid;
              w = wCent;
            }
          }

          for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
              double sum = 0.0;
              for (int l = 0; l < 3; l++) {
                if (w[l] != 0.0) {
                  sum += w[l] * y[l].getEntry(i, j);
                }
                res[i][j][k] = sum / _twoEps;
              }
            }
            xData[k] = oldValue;
          }
        }
        DoubleMatrix2D[] mres = new DoubleMatrix2D[m];
        for (int i = 0; i < m; i++) {
          mres[i] = new DoubleMatrix2D(res[i]);
        }
        return mres;
      }
    };
  }

}
