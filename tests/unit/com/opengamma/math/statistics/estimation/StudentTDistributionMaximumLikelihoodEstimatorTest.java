/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.estimation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.math.statistics.distribution.StudentTDistribution;

/**
 * @author emcleod
 * 
 */
public class StudentTDistributionMaximumLikelihoodEstimatorTest {
  private static final DistributionMaximumLikelihoodEstimator<Double> ESTIMATOR = new StudentTDistributionMaximumLikelihoodEstimator();

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    ESTIMATOR.evaluate((Double[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty() {
    ESTIMATOR.evaluate(new Double[0]);
  }

  @Test
  public void test() {
    final int n = 20000;
    final double eps = 5e-2;
    final double nu = 5.4;
    final ProbabilityDistribution<Double> p1 = new StudentTDistribution(nu);
    final Double[] x = new Double[n];
    for (int i = 0; i < n; i++) {
      x[i] = p1.nextRandom();
    }
    final StudentTDistribution p2 = (StudentTDistribution) ESTIMATOR.evaluate(x);
    assertEquals(p2.getDegreesOfFreedom(), nu, eps);
  }
}
