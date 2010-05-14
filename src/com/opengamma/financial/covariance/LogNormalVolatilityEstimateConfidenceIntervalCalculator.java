/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import com.opengamma.math.statistics.ConfidenceInterval;
import com.opengamma.math.statistics.distribution.ChiSquareDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class LogNormalVolatilityEstimateConfidenceIntervalCalculator {
  private ProbabilityDistribution<Double> _chiSquare;

  public ConfidenceInterval getConfidenceInterval(final double volatility, final double confidenceLevel, final int n) {
    _chiSquare = new ChiSquareDistribution(n - 1);
    final double alpha = 1 - confidenceLevel;
    final double lower = volatility * Math.sqrt((n - 1) / _chiSquare.getInverseCDF(1 - alpha / 2));
    final double upper = volatility * Math.sqrt((n - 1) / _chiSquare.getInverseCDF(alpha / 2));
    return new ConfidenceInterval(volatility, lower, upper, confidenceLevel);
  }
}
