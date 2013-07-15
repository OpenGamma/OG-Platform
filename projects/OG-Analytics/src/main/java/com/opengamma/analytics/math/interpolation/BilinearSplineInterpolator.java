/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 *  Given a set of data (x0Values_i, x1Values_j, yValues_{ij}), derive the piecewise bicubic function, f(x0,x1) = sum_{i=0}^{3} sum_{j=0}^{3} coefMat_{ij} (x0-x0Values_i)^{3-i} (x1-x1Values_j)^{3-j},
 *  for the region x0Values_i < x0 < x0Values_{i+1}, x1Values_j < x1 < x1Values_{j+1}  such that f(x0Values_a, x1Values_b) = yValues_{ab} where a={i,i+1}, b={j,j+1}. 
 */
public class BilinearSplineInterpolator extends PiecewisePolynomialInterpolator2D {
  private static final double ERROR = 1.e-13;

  @Override
  public PiecewisePolynomialResult2D interpolate(final double[] x0Values, final double[] x1Values, final double[][] yValues) {

    ArgumentChecker.notNull(x0Values, "x0Values");
    ArgumentChecker.notNull(x1Values, "x1Values");
    ArgumentChecker.notNull(yValues, "yValues");

    final int nData0 = x0Values.length;
    final int nData1 = x1Values.length;

    ArgumentChecker.isTrue(nData0 == yValues.length, "x0Values length = yValues number of rows");
    ArgumentChecker.isTrue(nData1 == yValues[0].length, "x1Values length = yValues number of columns");
    ArgumentChecker.isTrue(nData0 > 1, "Data points along x0 direction should be more than 1");
    ArgumentChecker.isTrue(nData1 > 1, "Data points along x1 direction should be more than 1");

    for (int i = 0; i < nData0; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(x0Values[i]), "x0Values containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(x0Values[i]), "x0Values containing Infinity");
    }
    for (int i = 0; i < nData1; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(x1Values[i]), "x1Values containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(x1Values[i]), "x1Values containing Infinity");
    }
    for (int i = 0; i < nData0; ++i) {
      for (int j = 0; j < nData1; ++j) {
        ArgumentChecker.isFalse(Double.isNaN(yValues[i][j]), "yValues containing NaN");
        ArgumentChecker.isFalse(Double.isInfinite(yValues[i][j]), "yValues containing Infinity");
      }
    }

    for (int i = 0; i < nData0; ++i) {
      for (int j = i + 1; j < nData0; ++j) {
        ArgumentChecker.isFalse(x0Values[i] == x0Values[j], "x0Values should be distinct");
      }
    }
    for (int i = 0; i < nData1; ++i) {
      for (int j = i + 1; j < nData1; ++j) {
        ArgumentChecker.isFalse(x1Values[i] == x1Values[j], "x1Values should be distinct");
      }
    }

    final int order = 2;

    DoubleMatrix2D[][] coefMat = new DoubleMatrix2D[nData0 - 1][nData1 - 1];
    for (int i = 0; i < nData0 - 1; ++i) {
      for (int j = 0; j < nData1 - 1; ++j) {
        final double interval0 = x0Values[i + 1] - x0Values[i];
        final double interval1 = x1Values[j + 1] - x1Values[j];
        double ref = 0.;
        double[][] coefMatTmp = new double[order][order];
        coefMatTmp[1][1] = yValues[i][j];
        coefMatTmp[0][1] = (-yValues[i][j] + yValues[i + 1][j]) / interval0;
        coefMatTmp[1][0] = (-yValues[i][j] + yValues[i][j + 1]) / interval1;
        coefMatTmp[0][0] = (yValues[i][j] - yValues[i + 1][j] - yValues[i][j + 1] + yValues[i + 1][j + 1]) / interval0 / interval1;
        for (int k = 0; k < order; ++k) {
          for (int l = 0; l < order; ++l) {
            ArgumentChecker.isFalse(Double.isNaN(coefMatTmp[k][l]), "Too large/small input");
            ArgumentChecker.isFalse(Double.isInfinite(coefMatTmp[k][l]), "Too large/small input");
            ref += coefMatTmp[k][l] * Math.pow(interval0, 1 - k) * Math.pow(interval1, 1 - l);
          }
        }
        final double bound = Math.max(Math.abs(ref) + Math.abs(yValues[i + 1][j + 1]), 0.1);
        ArgumentChecker.isTrue(Math.abs(ref - yValues[i + 1][j + 1]) < ERROR * bound, "Input is too large/small or data points are too close");
        coefMat[i][j] = new DoubleMatrix2D(coefMatTmp);
        coefMat[i][j] = new DoubleMatrix2D(coefMatTmp);
      }
    }

    return new PiecewisePolynomialResult2D(new DoubleMatrix1D(x0Values), new DoubleMatrix1D(x1Values), coefMat, new int[] {order, order });
  }
}
