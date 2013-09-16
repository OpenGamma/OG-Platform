/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.covariance;

import com.opengamma.analytics.math.statistics.ConfidenceInterval;
import com.opengamma.analytics.math.statistics.distribution.ChiSquareDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 *
 * Calculates the confidence interval for a volatility estimate, where the
 * percentage changes in the price are assumed to be normally distributed.
 * <p>
 * The estimate is given by:
 * $$
 * \begin{eqnarray*}
 * P\left[\hat{\sigma}\sqrt{\frac{n-1}{\chi^2(n-1; \frac{\alpha}{2})}} \leq \sigma \leq \hat{\sigma}\sqrt{\frac{n-1}{\chi^2(n-1; 1 - \frac{\alpha}{2})}}\right] = 1 - \alpha
 * \end{eqnarray*}
 * $$
 * where $\chi^2(n-1; \frac{\alpha}{2})$ is the value of the $\chi^2$
 * distribution with $n-1$ degrees of freedom and a confidence level of
 * $1 - * \alpha$.
 */
public class LogNormalVolatilityEstimateConfidenceIntervalCalculator {
  /** The chi-squared probability distribution */
  private ProbabilityDistribution<Double> _chiSquare;

  /**
   *
   * @param volatility The volatility estimate
   * @param confidenceLevel The confidence level for the interval
   * @param n Degrees of freedom
   * @return The confidence interval
   */
  public ConfidenceInterval getConfidenceInterval(final double volatility, final double confidenceLevel, final int n) {
    _chiSquare = new ChiSquareDistribution(n - 1);
    final double alpha = 1 - confidenceLevel;
    final double lower = volatility * Math.sqrt((n - 1) / _chiSquare.getInverseCDF(1 - alpha / 2));
    final double upper = volatility * Math.sqrt((n - 1) / _chiSquare.getInverseCDF(alpha / 2));
    return new ConfidenceInterval(volatility, lower, upper, confidenceLevel);
  }
}
