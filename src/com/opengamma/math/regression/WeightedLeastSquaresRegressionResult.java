/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.regression;

/**
 * 
 * @author emcleod
 */
public class WeightedLeastSquaresRegressionResult extends LeastSquaresRegressionResult {

  public WeightedLeastSquaresRegressionResult(final LeastSquaresRegressionResult result) {
    super(result);
  }

  public WeightedLeastSquaresRegressionResult(final Double[] betas, final Double[] residuals, final Double meanSquareError, final Double[] standardErrorOfBeta,
      final Double rSquared, final Double rSquaredAdjusted, final Double[] tStats, final Double[] pValues, final boolean hasIntercept) {
    super(betas, residuals, meanSquareError, standardErrorOfBeta, rSquared, rSquaredAdjusted, tStats, pValues, hasIntercept);
  }

  public Double getWeightedPredictedValue(final Double[] x, final Double[] w) {
    if (x == null)
      throw new IllegalArgumentException("Variable array was null");
    if (w == null)
      throw new IllegalArgumentException("Weight array was null");
    final Double[] betas = getBetas();
    if (hasIntercept() && x.length != betas.length - 1 || x.length != betas.length)
      throw new IllegalArgumentException("Number of variables did not match number used in regression");
    if (x.length != w.length)
      throw new IllegalArgumentException("Number of weights did not match number of variables");
    double sum = 0;
    for (int i = 0; i < (hasIntercept() ? x.length + 1 : x.length); i++) {
      if (hasIntercept()) {
        if (i == 0) {
          sum += betas[0];
        } else {
          sum += betas[i] * x[i - 1] * w[i - 1];
        }
      } else {
        sum += betas[i] * x[i] * w[i];
      }
    }
    return sum;
  }
}
