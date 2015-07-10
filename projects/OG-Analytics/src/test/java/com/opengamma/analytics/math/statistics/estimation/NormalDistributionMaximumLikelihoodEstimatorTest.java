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

import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NormalDistributionMaximumLikelihoodEstimatorTest {
  private static final DistributionParameterEstimator<Double> ESTIMATOR = new NormalDistributionMaximumLikelihoodEstimator();

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
    final int n = 500000;
    final double eps = 1e-2;
    final double mu = -1.3;
    final double sigma = 0.4;
    final ProbabilityDistribution<Double> p1 = new NormalDistribution(mu, sigma, new MersenneTwister64(MersenneTwister.DEFAULT_SEED));
    final double[] x = new double[n];
    for (int i = 0; i < n; i++) {
      x[i] = p1.nextRandom();
    }
    final NormalDistribution p2 = (NormalDistribution) ESTIMATOR.evaluate(x);
    assertEquals(p2.getMean(), mu, eps);
    assertEquals(p2.getStandardDeviation(), sigma, eps);
  }
}
