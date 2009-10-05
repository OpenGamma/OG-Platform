/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.regression;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author emcleod
 */
public class AdaptiveLeastSquaresRegression extends LeastSquaresRegression {
  private static final Logger s_Log = LoggerFactory.getLogger(AdaptiveLeastSquaresRegression.class);
  private final LeastSquaresRegression _regression;
  private final double _significanceLevel;

  public AdaptiveLeastSquaresRegression(final LeastSquaresRegression regression, final double significanceLevel) {
    _regression = regression;
    _significanceLevel = significanceLevel;
  }

  @Override
  public LeastSquaresRegressionResult regress(final Double[][] x, final Double[][] weights, final Double[] y, final boolean useIntercept) {
    final LeastSquaresRegressionResult result = _regression.regress(x, weights, y, useIntercept);
    if (areCoefficientsSignificant(result.getPValues()))
      return result;
    try {
      return getBestResult(result, x, weights, y, useIntercept);
    } catch (final RegressionException e) {
      s_Log.info("Could not find improvement on original regression; this is the result that has been returned");
      return result;
    }
  }

  private LeastSquaresRegressionResult getBestResult(final LeastSquaresRegressionResult result, final Double[][] x, final Double[][] w, final Double[] y, final boolean useIntercept) {
    final Double[] pStats = result.getPValues();
    final List<Integer> significantIndex = new ArrayList<Integer>();
    int i = 0;
    for (final Double p : pStats) {
      if (isCoefficientSignificant(p)) {
        significantIndex.add(i);
      }
      i++;
    }
    final int oldLength = pStats.length;
    final int newLength = significantIndex.size();
    if (newLength == 0) {
      s_Log.info("Could not find any significant regression coefficients");
      return result;
    }
    if (newLength == pStats.length)
      return result;
    final Double[][] newX = new Double[x.length][newLength];
    final Double[][] newW = new Double[x.length][newLength];
    int k;
    for (i = 0; i < x.length; i++) {
      for (int j = 0; j < oldLength; j++) {
        k = 0;
        if (significantIndex.contains(j)) {
          newX[i][k] = x[i][j];
          newW[i][k] = w[i][j];
          k++;
        }
      }
    }
    final LeastSquaresRegressionResult newResult = _regression.regress(newX, newW, y, useIntercept);
    if (result.getAdjustedRSquared() > newResult.getAdjustedRSquared())
      return result;
    return getBestResult(newResult, newX, newW, y, useIntercept);
  }

  private boolean areCoefficientsSignificant(final Double[] pStats) {
    for (final Double p : pStats) {
      if (!isCoefficientSignificant(p))
        return false;
    }
    return true;
  }

  private boolean isCoefficientSignificant(final Double pStat) {
    if (pStat > _significanceLevel)
      return false;
    return true;
  }
}
