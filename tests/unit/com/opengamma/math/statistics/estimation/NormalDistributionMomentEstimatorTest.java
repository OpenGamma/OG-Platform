/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.estimation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * @author emcleod
 * 
 */
public class NormalDistributionMomentEstimatorTest {
  private static final Function1D<Double[], ProbabilityDistribution<Double>> CALCULATOR = new NormalDistributionMomentEstimator();

  @Test
  public void test() {
    final int n = 50000;
    final double mu = 4.5;
    final double sigma = 0.86;
    final ProbabilityDistribution<Double> p1 = new NormalDistribution(mu, sigma);
    final Double[] x = new Double[n];
    for (int i = 0; i < n; i++) {
      x[i] = p1.nextRandom();
    }
    final NormalDistribution p2 = (NormalDistribution) CALCULATOR.evaluate(x);
    assertEquals(p2.getMean(), mu, 2.5e-2);
    assertEquals(p2.getStandardDeviation(), sigma, 2.5e-2);
  }

}
