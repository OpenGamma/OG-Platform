/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.estimation;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.descriptive.MedianCalculator;
import com.opengamma.math.statistics.distribution.LaplaceDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class LaplaceDistributionMaximumLikelihoodEstimator extends DistributionParameterEstimator<Double> {
  private final Function1D<double[], Double> _median = new MedianCalculator();

  @Override
  public ProbabilityDistribution<Double> evaluate(final double[] x) {
    Validate.notNull(x, "x");
    ArgumentChecker.notEmpty(x, "x");
    final double median = _median.evaluate(x);
    final int n = x.length;
    double b = 0;
    for (int i = 0; i < n; i++) {
      b += Math.abs(x[i] - median);
    }
    return new LaplaceDistribution(median, b / n);
  }

}
