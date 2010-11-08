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

import com.opengamma.math.MathException;

/**
 * 
 */
public class AdaptiveLeastSquaresRegression extends LeastSquaresRegression {
  private static final Logger s_logger = LoggerFactory.getLogger(AdaptiveLeastSquaresRegression.class);
  private final LeastSquaresRegression _regression;
  private final double _significanceLevel;

  public AdaptiveLeastSquaresRegression(final LeastSquaresRegression regression, final double significanceLevel) {
    if (regression == null) {
      throw new IllegalArgumentException("Regression was null");
    }
    if (significanceLevel <= 0) {
      throw new IllegalArgumentException("Significance level must be greater than zero; have " + significanceLevel);
    }
    _regression = regression;
    _significanceLevel = significanceLevel;
  }

  @Override
  public LeastSquaresRegressionResult regress(final double[][] x, final double[][] weights, final double[] y, final boolean useIntercept) {
    final LeastSquaresRegressionResult result = _regression.regress(x, weights, y, useIntercept);
    if (areCoefficientsSignificant(result.getPValues())) {
      final List<Integer> tempIndex = new ArrayList<Integer>();
      for (int i = 0; i < result.getPValues().length; i++) {
        tempIndex.add(i);
      }
      return new NamedVariableLeastSquaresRegressionResult(getNames(tempIndex), result);
    }
    try {
      return getBestResult(result, x, weights, y, useIntercept);
    } catch (final MathException e) {
      s_logger.info("Could not find improvement on original regression; returning original");
      return result;
    }
  }

  private LeastSquaresRegressionResult getBestResult(final LeastSquaresRegressionResult result, final double[][] x, final double[][] w, final double[] y,
      final boolean useIntercept) {
    final double[] pValues = result.getPValues();
    final List<Integer> significantIndex = new ArrayList<Integer>();
    int i = 0;
    for (final double p : pValues) {
      if (isCoefficientSignificant(p)) {
        significantIndex.add(i);
      }
      i++;
    }
    final int oldLength = pValues.length;
    final int newLength = significantIndex.size();
    if (newLength == 0) {
      s_logger.info("Could not find any significant regression coefficients");
      final List<Integer> tempIndex = new ArrayList<Integer>();
      for (i = 0; i < pValues.length; i++) {
        tempIndex.add(i);
      }
      return new NamedVariableLeastSquaresRegressionResult(getNames(tempIndex), result);
    }
    if (newLength == pValues.length) {
      return new NamedVariableLeastSquaresRegressionResult(getNames(significantIndex), result);
    }
    final double[][] newX = new double[x.length][newLength];
    final double[][] newW = w == null ? null : new double[x.length][newLength];
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
      final List<String> names = getNames(significantIndex);
      return new NamedVariableLeastSquaresRegressionResult(names, newResult);
    }
    return getBestResult(newResult, newX, newW, y, useIntercept);
  }

  /**
   * @param significantIndex
   * @return
   */
  private List<String> getNames(final List<Integer> significantIndex) {
    final List<String> names = new ArrayList<String>();
    for (final Integer index : significantIndex) {
      names.add(index.toString());
    }
    return names;
  }

  private boolean areCoefficientsSignificant(final double[] pValue) {
    for (final double p : pValue) {
      if (!isCoefficientSignificant(p)) {
        return false;
      }
    }
    return true;
  }

  private boolean isCoefficientSignificant(final double pValue) {
    if (pValue > _significanceLevel) {
      return false;
    }
    return true;
  }
}
