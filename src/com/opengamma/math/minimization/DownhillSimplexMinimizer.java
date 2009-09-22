package com.opengamma.math.minimization;

import com.opengamma.math.ConvergenceException;
import com.opengamma.math.function.Function;
import com.opengamma.util.CompareUtils;

/**
 * 
 * @author emcleod
 * 
 */

public class DownhillSimplexMinimizer implements MultidimensionalMinimizer<Double> {
  private final double _eps = 1e-9;
  private final int _maxIter = 100;

  @Override
  public Double[] minimize(Function<Double, Double> f, Double[] initialPoints) {
    int n = initialPoints.length;
    Double[][] simplex = getSimplex(initialPoints);
    Double[] sumX = getSumX(simplex);
    Double[] y = new Double[n + 1];
    for (int i = 0; i <= n; i++) {
      y[i] = f.evaluate(simplex[i]);
    }
    int iLow = 0;
    int iHigh, inHigh;
    double diff, newY, tempY;
    for (int ii = 0; ii < _maxIter; ii++) {
      iHigh = y[0] > y[1] ? 0 : 1;
      inHigh = iHigh == 1 ? 0 : 1;
      for (int i = 0; i <= n; i++) {
        if (y[i] <= y[iLow]) {
          iLow = i;
        }
        if (y[i] > y[iHigh]) {
          inHigh = iHigh;
          iHigh = i;
        } else if (y[i] > y[inHigh] && i != iHigh) {
          inHigh = i;
        }
      }
      System.out.println(ii + " " + y[iLow]);
      diff = 2 * Math.abs(y[iHigh] - y[iLow]) / (Math.abs(y[iHigh]) + Math.abs(y[iLow]) + _eps);
      if (diff < _eps) {
        return simplex[iLow];
      }
      newY = getExtrapolatedPoint(simplex, y, sumX, iHigh, -1.0, f);
      if (newY < y[iLow]) {
        newY = getExtrapolatedPoint(simplex, y, sumX, iHigh, 2.0, f);
      } else if (newY >= y[inHigh]) {
        tempY = y[iHigh];
        newY = getExtrapolatedPoint(simplex, y, sumX, iHigh, 0.5, f);
        if (newY >= tempY) {
          System.out.println("---------------------------------------" + iLow);
          for (int i = 0; i < n; i++) {
            if (i != iLow) {
              for (int j = 0; j < n - 1; j++) {
                simplex[i][j] = 0.5 * (simplex[i][j] + simplex[iLow][j]);
              }
              sumX = simplex[i];
              y[i] = f.evaluate(sumX);
            }
          }
          sumX = getSumX(simplex);
        }
      }
    }
    throw new ConvergenceException();
  }

  private Double getExtrapolatedPoint(Double[][] simplex, Double[] y, Double[] sumX, int iHigh, double factor, Function<Double, Double> f) {
    int n = simplex.length - 1;
    Double[] newX = new Double[n];
    double factor1 = (1 - factor) / n;
    double factor2 = factor1 - factor;
    for (int i = 0; i < n; i++) {
      newX[i] = sumX[i] * factor1 - simplex[iHigh][i] * factor2;
    }
    double newY = f.evaluate(newX);
    if (newY < y[iHigh]) {
      y[iHigh] = newY;
      for (int i = 0; i < n; i++) {
        sumX[i] += newX[i] - simplex[iHigh][i];
        simplex[iHigh][i] = newX[i];
      }
    }
    return newY;
  }

  private Double[][] getSimplex(Double[] initialPoints) {
    int n = initialPoints.length + 1;
    Double[][] result = new Double[n][n - 1];
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n - 1; j++) {
        result[i][j] = initialPoints[j];
        if (i > 0 && j == i - 1) {
          if (CompareUtils.closeEquals(result[i][j], 0)) {
            result[i][j] = 0.05;
          }
          result[i][j] *= 1.05;
        }
      }
    }
    return result;
  }

  private Double[] getSumX(Double[][] simplex) {
    Double[] result = new Double[simplex.length];
    double sum;
    for (int i = 0; i < simplex.length; i++) {
      sum = 0;
      for (int j = 0; j < simplex[0].length; j++) {
        sum += simplex[i][j];
      }
      result[i] = sum;
    }
    return result;
  }
}
