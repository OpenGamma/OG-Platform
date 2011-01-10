/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.estimation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class NormalDistributionMomentEstimatorTest {
  private static final Function1D<double[], ProbabilityDistribution<Double>> CALCULATOR = new NormalDistributionMomentEstimator();

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    CALCULATOR.evaluate((double[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty() {
    CALCULATOR.evaluate(new double[0]);
  }

  @Test
  public void test() {
    final int n = 500000;
    final double mu = 4.5;
    final double sigma = 0.86;
    final ProbabilityDistribution<Double> p1 = new NormalDistribution(mu, sigma, new MersenneTwister64(MersenneTwister64.DEFAULT_SEED));
    final double[] x = new double[n];
    for (int i = 0; i < n; i++) {
      x[i] = p1.nextRandom();
    }
    final NormalDistribution p2 = (NormalDistribution) CALCULATOR.evaluate(x);
    assertEquals(p2.getMean(), mu, 2.5e-2);
    assertEquals(p2.getStandardDeviation(), sigma, 2.5e-2);
  }

}
