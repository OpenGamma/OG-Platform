/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.estimation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.statistics.distribution.LaplaceDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class LaplaceDistributionMaximumLikelihoodEstimatorTest {
  private static final DistributionParameterEstimator<Double> ESTIMATOR = new LaplaceDistributionMaximumLikelihoodEstimator();

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    ESTIMATOR.evaluate((double[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty() {
    ESTIMATOR.evaluate(new double[0]);
  }

  @Test
  public void test() {
    final double mu = 0.367;
    final double b = 1.4;
    final ProbabilityDistribution<Double> distribution = new LaplaceDistribution(mu, b);
    final int n = 100000;
    final double[] x = new double[n];
    for (int i = 0; i < n; i++) {
      x[i] = distribution.nextRandom();
    }
    final LaplaceDistribution result = (LaplaceDistribution) ESTIMATOR.evaluate(x);
    final double eps = 1e-2;
    assertEquals(1, result.getB() / b, eps);
    assertEquals(1, result.getMu() / mu, eps);
  }
}
