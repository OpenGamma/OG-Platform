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
    if (regression == null)
      throw new IllegalArgumentException("Regression was null");
    if (significanceLevel <= 0)
      throw new IllegalArgumentException("Significance level must be greater than zero; have " + significanceLevel);
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
      s_Log.info("Could not find improvement on original regression; returning original");
      return result;
    }
  }

  private LeastSquaresRegressionResult getBestResult(final LeastSquaresRegressionResult result, final Double[][] x, final Double[][] w, final Double[] y, final boolean useIntercept) {
    final Double[] pValues = result.getPValues();
    final List<Integer> significantIndex = new ArrayList<Integer>();
    int i = 0;
    for (final Double p : pValues) {
      if (isCoefficientSignificant(p)) {
        significantIndex.add(i);
      }
      i++;
    }
    final int oldLength = pValues.length;
    final int newLength = significantIndex.size();
    if (newLength == 0) {
      s_Log.info("Could not find any significant regression coefficients");
      return result;
    }
    if (newLength == pValues.length)
      return result;
    final Double[][] newX = new Double[x.length][newLength];
    final Double[][] newW = w == null ? null : new Double[x.length][newLength];
    int k;
    for (i = 0; i < x.length; i++) {
      k = 0;
      for (int j = 0; j < oldLength; j++) {
        if (significantIndex.contains(j)) {
          newX[i][k] = x[i][j];
          if (w != null) {
            newW[i][k] = w[i][j];
          }
          k++;
        }
      }
    }
    final LeastSquaresRegressionResult newResult = _regression.regress(newX, newW, y, useIntercept);
    if (areCoefficientsSignificant(newResult.getPValues())) {
      final List<String> names = new ArrayList<String>();
      for (final Integer index : significantIndex) {
        names.add(index.toString());
      }
      return new NamedVariableLeastSquaresRegressionResult(names, newResult);
    }
    return getBestResult(newResult, newX, newW, y, useIntercept);
  }

  private boolean areCoefficientsSignificant(final Double[] pValue) {
    for (final Double p : pValue) {
      if (!isCoefficientSignificant(p))
        return false;
    }
    return true;
  }

  private boolean isCoefficientSignificant(final Double pValue) {
    if (pValue > _significanceLevel)
      return false;
    return true;
  }
}
