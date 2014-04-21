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

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NormalDistributionMomentEstimatorTest {
  private static final Function1D<double[], ProbabilityDistribution<Double>> CALCULATOR = new NormalDistributionMomentEstimator();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    CALCULATOR.evaluate((double[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmpty() {
    CALCULATOR.evaluate(new double[0]);
  }

  @Test
  public void test() {
    final int n = 500000;
    final double mu = 4.5;
    final double sigma = 0.86;
    final ProbabilityDistribution<Double> p1 = new NormalDistribution(mu, sigma, new MersenneTwister64(MersenneTwister.DEFAULT_SEED));
    final double[] x = new double[n];
    for (int i = 0; i < n; i++) {
      x[i] = p1.nextRandom();
    }
    final NormalDistribution p2 = (NormalDistribution) CALCULATOR.evaluate(x);
    assertEquals(p2.getMean(), mu, 2.5e-2);
    assertEquals(p2.getStandardDeviation(), sigma, 2.5e-2);
  }

}
