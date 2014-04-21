/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.estimation;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.analytics.math.statistics.distribution.LaplaceDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class LaplaceDistributionMaximumLikelihoodEstimatorTest {
  private static final DistributionParameterEstimator<Double> ESTIMATOR = new LaplaceDistributionMaximumLikelihoodEstimator();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    ESTIMATOR.evaluate((double[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmpty() {
    ESTIMATOR.evaluate(new double[0]);
  }

  @Test
  public void test() {
    final double mu = 0.367;
    final double b = 1.4;
    final ProbabilityDistribution<Double> distribution = new LaplaceDistribution(mu, b, new MersenneTwister64(MersenneTwister.DEFAULT_SEED));
    final int n = 500000;
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
