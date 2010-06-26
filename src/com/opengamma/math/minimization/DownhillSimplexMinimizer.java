/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import com.opengamma.math.ConvergenceException;
import com.opengamma.math.function.FunctionND;

/**
 * 
 */
public class DownhillSimplexMinimizer extends MultidimensionalMinimizer {
  // TODO better delta
  private static final double DELTA = 0.35;
  private static final double EPS = 1e-12;
  private static final double OFFSET = 1e-15;
  private static final int MAX_FUNCTION_EVAL = 5000;

  @Override
  public Double[] minimize(final FunctionND<Double, Double> f, final Double[][] initialPoints) {
    checkInputs(f, initialPoints, 1);
    final int dim = f.getDimension();
    final int n = dim + 1;
    final Double[][] p = getInitialSimplex(initialPoints, dim, n);
    final Double[] y = getFunctionValues(f, n, p);
    Double[] pSum = getPSum(p, dim, n);
    final Double[] pMin = new Double[dim];
    int evaluationCount = 0;
    int iHigh, iLow;
    double diff, newY, tempY;
    while (evaluationCount < MAX_FUNCTION_EVAL) {
      final int[] indices = getIndices(y, n);
      iLow = indices[0];
      iHigh = indices[1];
      diff = 2 * Math.abs(y[iHigh] - y[iLow]) / (Math.abs(y[iHigh]) + Math.abs(y[iLow]) + OFFSET);
      if (diff < EPS) {
        for (int i = 0; i < dim; i++) {
          pMin[i] = p[iLow][i];
        }
        return pMin;
      }
      evaluationCount += 2;
      newY = getExtrapolatedValues(p, y, pSum, iHigh, -1., f, dim);
      if (newY <= y[iLow]) {
        newY = getExtrapolatedValues(p, y, pSum, iHigh, 2, f, dim);
      } else if (newY >= y[iHigh]) {
        tempY = y[iHigh];
        newY = getExtrapolatedValues(p, y, pSum, iHigh, 0.5, f, dim);
        if (newY > tempY) {
          for (int i = 0; i < n; i++) {
            if (i != iLow) {
              for (int j = 0; j < dim; j++) {
                pSum[j] = 0.5 * (p[i][j] + p[iLow][j]);
                p[i][j] = pSum[j];
              }
              y[i] = f.evaluate(pSum);
            }
          }
          evaluationCount += dim;
          pSum = getPSum(p, dim, n);
        }
      } else {
        --evaluationCount;
      }
    }
    throw new ConvergenceException("Could not converge in " + MAX_FUNCTION_EVAL + " function evaluations");
  }

  private Double[][] getInitialSimplex(final Double[][] initialPoints, final int dim, final int n) {
    final Double[][] p = new Double[n][dim];
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < dim; j++) {
        p[i][j] = initialPoints[0][j];
      }
      if (i != 0) {
        p[i][i - 1] += DELTA;
      }
    }
    return p;
  }

  private Double[] getPSum(final Double[][] p, final int ndim, final int mpts) {
    final Double[] psum = new Double[ndim];
    for (int j = 0; j < ndim; j++) {
      double sum = 0;
      for (int i = 0; i < mpts; i++) {
        sum += p[i][j];
      }
      psum[j] = sum;
    }
    return psum;
  }

  private double getExtrapolatedValues(final Double[][] p, final Double[] y, final Double[] pSum, final int iHigh, final double scaleFactor,
      final FunctionND<Double, Double> f, final int dim) {
    final Double[] newP = new Double[dim];
    final double scaleFactor1 = (1 - scaleFactor) / dim;
    final double scaleFactor2 = scaleFactor1 - scaleFactor;
    for (int j = 0; j < dim; j++) {
      newP[j] = pSum[j] * scaleFactor1 - p[iHigh][j] * scaleFactor2;
    }
    final double newY = f.evaluate(newP);
    if (newY < y[iHigh]) {
      y[iHigh] = newY;
      for (int j = 0; j < dim; j++) {
        pSum[j] += newP[j] - p[iHigh][j];
        p[iHigh][j] = newP[j];
      }
    }
    return newY;
  }

  private Double[] getFunctionValues(final FunctionND<Double, Double> f, final int n, final Double[][] p) {
    final Double[] y = new Double[n];
    for (int i = 0; i < n; i++) {
      y[i] = f.evaluate(p[i]);
    }
    return y;
  }

  private int[] getIndices(final Double[] y, final int n) {
    int iLow, iHigh, iNextHigh;
    iLow = 0;
    if (y[0] > y[1]) {
      iHigh = 0;
      iNextHigh = 1;
    } else {
      iHigh = 1;
      iNextHigh = 0;
    }
    for (int i = 0; i < n; i++) {
      if (y[i] <= y[iLow]) {
        iLow = i;
      }
      if (y[i] > y[iHigh]) {
        iNextHigh = iHigh;
        iHigh = i;
      } else if (y[i] > y[iNextHigh] && i != iHigh) {
        iNextHigh = i;
      }
    }
    return new int[] {iLow, iHigh};
  }
}
