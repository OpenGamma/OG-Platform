/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class GeneralizedExtremeValueDistributionTest extends ProbabilityDistributionTestCase {
  private static final double MU = 1.5;
  private static final double SIGMA = 0.6;
  private static final double KSI = 0.7;
  private static final ProbabilityDistribution<Double> DIST = new GeneralizedExtremeValueDistribution(MU, SIGMA, KSI);
  private static final double LARGE_X = 1e10;

  @Test(expected = IllegalArgumentException.class)
  public void testBadConstructor() {
    new GeneralizedExtremeValueDistribution(MU, -SIGMA, KSI);
  }

  @Test
  public void testBadInputs() {
    testCDFWithNull(DIST);
    testPDFWithNull(DIST);
  }

  @Test
  public void testSupport() {
    ProbabilityDistribution<Double> dist = new GeneralizedExtremeValueDistribution(MU, SIGMA, KSI);
    double limit = MU - SIGMA / KSI;
    testLimit(dist, limit - EPS);
    assertEquals(dist.getCDF(limit + EPS), 0, EPS);
    assertEquals(dist.getCDF(LARGE_X), 1, EPS);
    dist = new GeneralizedExtremeValueDistribution(MU, SIGMA, -KSI);
    limit = MU + SIGMA / KSI;
    testLimit(dist, limit + EPS);
    assertEquals(dist.getCDF(-LARGE_X), 0, EPS);
    assertEquals(dist.getCDF(limit - EPS), 1, EPS);
    dist = new GeneralizedExtremeValueDistribution(MU, SIGMA, 0);
    assertEquals(dist.getCDF(-LARGE_X), 0, EPS);
    assertEquals(dist.getCDF(LARGE_X), 1, EPS);
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
