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
import com.opengamma.analytics.math.statistics.distribution.ChiSquareDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ChiSquareDistributionMomentEstimatorTest {
  private static final Function1D<double[], ProbabilityDistribution<Double>> CALCULATOR = new ChiSquareDistributionMomentEstimator();

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
    final double k = 1.34;
    final ProbabilityDistribution<Double> p1 = new ChiSquareDistribution(k, new MersenneTwister64(MersenneTwister.DEFAULT_SEED));
    final double[] x = new double[n];
    for (int i = 0; i < n; i++) {
      x[i] = p1.nextRandom();
    }
    final ChiSquareDistribution p2 = (ChiSquareDistribution) CALCULATOR.evaluate(x);
    assertEquals(p2.getDegreesOfFreedom(), k, 2.5e-2);
  }
}
