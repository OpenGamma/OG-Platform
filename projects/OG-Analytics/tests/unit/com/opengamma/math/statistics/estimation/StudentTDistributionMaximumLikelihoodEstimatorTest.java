/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.estimation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.math.statistics.distribution.StudentTDistribution;

/**
 * 
 */
public class StudentTDistributionMaximumLikelihoodEstimatorTest {
  private static final DistributionParameterEstimator<Double> ESTIMATOR = new StudentTDistributionMaximumLikelihoodEstimator();

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
    final int n = 500000;
    final double eps = 5e-2;
    final double nu = 5.4;
    final ProbabilityDistribution<Double> p1 = new StudentTDistribution(nu, new MersenneTwister64(MersenneTwister64.DEFAULT_SEED));
    final double[] x = new double[n];
    for (int i = 0; i < n; i++) {
      x[i] = p1.nextRandom();
    }
    final StudentTDistribution p2 = (StudentTDistribution) ESTIMATOR.evaluate(x);
    assertEquals(p2.getDegreesOfFreedom(), nu, eps);
  }
}
