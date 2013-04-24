/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.Arrays;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ParallelArrayBinarySort;

/**
 * Cubic spline interpolation based on 
 * H. Akima, “A New Method of Interpolation and Smooth Curve Fitting Based on Local Procedures,” 
 * Journal of the Association for Computing Machinery, Vol 17, no 4, October 1970, 589-602
 */
public class SemiLocalCubicSplineInterpolator extends PiecewisePolynomialInterpolator {

  private final HermiteCoefficientsProvider _solver = new HermiteCoefficientsProvider();

  @Override
  public PiecewisePolynomialResult interpolate(final double[] xValues, final double[] yValues) {

    ArgumentChecker.notNull(xValues, "xValues");
    ArgumentChecker.notNull(yValues, "yValues");

    ArgumentChecker.isTrue(xValues.length == yValues.length, "(xValues length = yValues length) should be true");
    ArgumentChecker.isTrue(xValues.length > 2, "Data points should be >= 3");

    final int nDataPts = xValues.length;

    for (int i = 0; i < nDataPts; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(xValues[i]), "xValues containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(xValues[i]), "xValues containing Infinity");
      ArgumentChecker.isFalse(Double.isNaN(yValues[i]), "yValues containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(yValues[i]), "yValues containing Infinity");
    }

    for (int i = 0; i < nDataPts - 1; ++i) {
      for (int j = i + 1; j < nDataPts; ++j) {
        ArgumentChecker.isFalse(xValues[i] == xValues[j], "xValues should be distinct");
      }
    }

    double[] xValuesSrt = Arrays.copyOf(xValues, nDataPts);
    double[] yValuesSrt = Arrays.copyOf(yValues, nDataPts);
    ParallelArrayBinarySort.parallelBinarySort(xValuesSrt, yValuesSrt);

    final double[] intervals = _solver.intervalsCalculator(xValuesSrt);
    final double[] slopes = _solver.slopesCalculator(yValuesSrt, intervals);
    final double[] first = firstDerivativeCalculator(xValuesSrt, yValuesSrt);
    final double[][] coefs = _solver.solve(yValuesSrt, intervals, slopes, first);

    return new PiecewisePolynomialResult(new DoubleMatrix1D(xValuesSrt), new DoubleMatrix2D(coefs), 4, 1);
  }

  @Override
  public PiecewisePolynomialResult interpolate(final double[] xValues, final double[][] yValuesMatrix) {
    ArgumentChecker.notNull(xValues, "xValues");
    ArgumentChecker.notNull(yValuesMatrix, "yValuesMatrix");

    ArgumentChecker.isTrue(xValues.length == yValuesMatrix[0].length, "(xValues length = yValuesMatrix's row vector length) should be true");
    ArgumentChecker.isTrue(xValues.length > 2, "Data points should be >= 3");

    final int nDataPts = xValues.length;
    final int yValuesLen = yValuesMatrix[0].length;
    final int dim = yValuesMatrix.length;

    for (int i = 0; i < nDataPts; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(xValues[i]), "xValues containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(xValues[i]), "xValues containing Infinity");
    }
    for (int i = 0; i < yValuesLen; ++i) {
      for (int j = 0; j < dim; ++j) {
        ArgumentChecker.isFalse(Double.isNaN(yValuesMatrix[j][i]), "yValuesMatrix containing NaN");
        ArgumentChecker.isFalse(Double.isInfinite(yValuesMatrix[j][i]), "yValuesMatrix containing Infinity");
      }
    }
    for (int i = 0; i < nDataPts; ++i) {
      for (int j = i + 1; j < nDataPts; ++j) {
        ArgumentChecker.isFalse(xValues[i] == xValues[j], "xValues should be distinct");
      }
    }

    double[] xValuesSrt = new double[nDataPts];
    DoubleMatrix2D[] coefMatrix = new DoubleMatrix2D[dim];

    for (int i = 0; i < dim; ++i) {
      xValuesSrt = Arrays.copyOf(xValues, nDataPts);
      double[] yValuesSrt = Arrays.copyOf(yValuesMatrix[i], nDataPts);
      ParallelArrayBinarySort.parallelBinarySort(xValuesSrt, yValuesSrt);

      final double[] intervals = _solver.intervalsCalculator(xValuesSrt);
      final double[] slopes = _solver.slopesCalculator(yValuesSrt, intervals);
      final double[] first = firstDerivativeCalculator(xValuesSrt, yValuesSrt);

      coefMatrix[i] = new DoubleMatrix2D(_solver.solve(yValuesSrt, intervals, slopes, first));
    }

    final int nIntervals = coefMatrix[0].getNumberOfRows();
    final int nCoefs = coefMatrix[0].getNumberOfColumns();
    double[][] resMatrix = new double[dim * nIntervals][nCoefs];

    for (int i = 0; i < nIntervals; ++i) {
      for (int j = 0; j < dim; ++j) {
        resMatrix[dim * i + j] = coefMatrix[j].getRowVector(i).getData();
      }
    }

    for (int i = 0; i < (nIntervals * dim); ++i) {
      for (int j = 0; j < nCoefs; ++j) {
        ArgumentChecker.isFalse(Double.isNaN(resMatrix[i][j]), "Too large input");
        ArgumentChecker.isFalse(Double.isInfinite(resMatrix[i][j]), "Too large input");
      }
    }

    return new PiecewisePolynomialResult(new DoubleMatrix1D(xValuesSrt), new DoubleMatrix2D(resMatrix), nCoefs, dim);
  }

  private double[] firstDerivativeCalculator(final double[] xValues, final double[] yValues) {
    final int nData = xValues.length;
    double[] res = new double[nData];

    double[] extraPtsStart = extraPointsProvider(xValues[2], xValues[1], xValues[0], yValues[2], yValues[1], yValues[0]);
    double[] extraPtsEnd = extraPointsProvider(xValues[nData - 3], xValues[nData - 2], xValues[nData - 1], yValues[nData - 3], yValues[nData - 2], yValues[nData - 1]);

    double[] xValuesExt = new double[nData + 4];
    double[] yValuesExt = new double[nData + 4];

    xValuesExt[0] = extraPtsStart[2];
    xValuesExt[1] = extraPtsStart[0];
    xValuesExt[nData + 2] = extraPtsEnd[0];
    xValuesExt[nData + 3] = extraPtsEnd[2];
    yValuesExt[0] = extraPtsStart[3];
    yValuesExt[1] = extraPtsStart[1];
    yValuesExt[nData + 2] = extraPtsEnd[1];
    yValuesExt[nData + 3] = extraPtsEnd[3];
    for (int i = 0; i < nData; ++i) {
      xValuesExt[i + 2] = xValues[i];
      yValuesExt[i + 2] = yValues[i];
    }

    final double[] intervalsExt = _solver.intervalsCalculator(xValuesExt);
    final double[] slopesExt = _solver.slopesCalculator(yValuesExt, intervalsExt);

    for (int i = 0; i < nData; ++i) {
      if (Math.abs(slopesExt[i + 3] - slopesExt[i + 2]) == 0.) {
        if (Math.abs(slopesExt[i + 1] - slopesExt[i]) == 0.) {
          res[i] = 0.5 * (slopesExt[i + 1] + slopesExt[i + 2]);
        } else {
          res[i] = slopesExt[i + 2];
        }
      } else {
        if (Math.abs(slopesExt[i + 1] - slopesExt[i]) == 0.) {
          res[i] = slopesExt[i];
        } else {
          res[i] = (Math.abs(slopesExt[i + 3] - slopesExt[i + 2]) * slopesExt[i + 1] + Math.abs(slopesExt[i + 1] - slopesExt[i]) * slopesExt[i + 2]) /
              (Math.abs(slopesExt[i + 3] - slopesExt[i + 2]) + Math.abs(slopesExt[i + 1] - slopesExt[i]));
        }
      }
    }

    return res;
  }

  private double[] extraPointsProvider(final double x1, final double x2, final double x3, final double y1, final double y2, final double y3) {
    double[] res = new double[4];

    res[0] = (x3 - x1) + x2;
    res[1] = y3 + (res[0] - x3) * (2. * (y3 - y2) / (x3 - x2) - (y2 - y1) / (x2 - x1));
    res[2] = (x3 - x1) + x3;
    res[3] = res[1] + (res[2] - res[0]) * (2. * (res[1] - y3) / (res[0] - x3) - (y3 - y2) / (x3 - x2));

    return res;
  }

}
