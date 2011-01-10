/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import com.opengamma.math.statistics.ConfidenceInterval;
import com.opengamma.math.statistics.distribution.ChiSquareDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 * Calculates the confidence interval for a volatility estimate, where the percentage changes in the price are assumed to be normally distributed.
 * The estimate is given by:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{eqnarray*}
 * P\\left[\\hat{\\sigma}\\sqrt{\\frac{n-1}{\\chi^2(n-1; \\frac{\\alpha}{2})}} \\leq \\sigma \\leq \\hat{\\sigma}\\sqrt{\\frac{n-1}{\\chi^2(n-1; 1 - \\frac{\\alpha}{2})}}\\right] = 1 - \\alpha
 * \\end{eqnarray*}}
 * where {@latex.inline %preamble{\\usepackage{amsmath}} $\\hat{\\sigma}$} is the volatility estimate and {@latex.inline %preamble{\\usepackage{amsmath}} $\\chi^2(n-1; \\frac{\\alpha}{2})$} 
 * is the value of the {@latex.inline $\\chi^2$} distribution with {@latex.inline $n-1$} degrees of freedom and a confidence level of {@latex.inline $1 - \\alpha$}.
 */
public class LogNormalVolatilityEstimateConfidenceIntervalCalculator {
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
