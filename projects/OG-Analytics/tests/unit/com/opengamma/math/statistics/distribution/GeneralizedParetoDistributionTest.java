/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.descriptive.MeanCalculator;
import com.opengamma.math.statistics.descriptive.MedianCalculator;
import com.opengamma.math.statistics.descriptive.PopulationVarianceCalculator;

public class GeneralizedParetoDistributionTest extends ProbabilityDistributionTestCase {
  private static final double MU = 0.4;
  private static final double SIGMA = 1.4;
  private static final double KSI = 0.2;
  private static final ProbabilityDistribution<Double> DIST = new GeneralizedParetoDistribution(MU, SIGMA, KSI, ENGINE);
  private static final double LARGE_X = 1e20;

  @Test(expected = IllegalArgumentException.class)
  public void testBadSigma() {
    new GeneralizedParetoDistribution(MU, -SIGMA, KSI);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroKsi() {
    new GeneralizedParetoDistribution(MU, SIGMA, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullEngine() {
    new GeneralizedParetoDistribution(MU, SIGMA, KSI, null);
  }

  @Test
  public void testBadInputs() {
    testCDFWithNull(DIST);
    testPDFWithNull(DIST);
  }

  @Test
  public void testSupport() {
    ProbabilityDistribution<Double> dist = new GeneralizedParetoDistribution(MU, SIGMA, KSI, ENGINE);
    testLimit(dist, MU - EPS);
    assertEquals(dist.getCDF(MU + EPS), 0, EPS);
    assertEquals(dist.getCDF(LARGE_X), 1, EPS);
    dist = new GeneralizedParetoDistribution(MU, SIGMA, -KSI);
    final double limit = MU + SIGMA / KSI;
    testLimit(dist, MU - EPS);
    testLimit(dist, limit + EPS);
    assertEquals(dist.getCDF(MU + EPS), 0, EPS);
    assertEquals(dist.getCDF(limit - 1e-15), 1, EPS);
  }

  @Test
  public void testDistribution() {
    final Function1D<double[], Double> meanCalculator = new MeanCalculator();
    final Function1D<double[], Double> medianCalculator = new MedianCalculator();
    final Function1D<double[], Double> varianceCalculator = new PopulationVarianceCalculator();
    final int n = 1000000;
    final double eps = 0.1;
    final double[] data = new double[n];
    for (int i = 0; i < n; i++) {
      data[i] = DIST.nextRandom();
    }
    final double mean = MU + SIGMA / (1 - KSI);
    final double median = MU + SIGMA * (Math.pow(2, KSI) - 1) / KSI;
    final double variance = SIGMA * SIGMA / ((1 - KSI) * (1 - KSI) * (1 - 2 * KSI));
    assertEquals(meanCalculator.evaluate(data), mean, eps);
    assertEquals(medianCalculator.evaluate(data), median, eps);
    assertEquals(varianceCalculator.evaluate(data), variance, eps);
  }

  private void testLimit(final ProbabilityDistribution<Double> dist, final double limit) {
    try {
      dist.getCDF(limit);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      dist.getPDF(limit);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }
}
