/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import static org.junit.Assert.assertEquals;

/**
 * 
 * @author emcleod
 */
public class ProbabilityDistributionTest {
  private static final double EPS = 1e-5;

  public void testCDF(final double[] p, final double[] x, final ProbabilityDistribution<Double> dist) {
    for (int i = 0; i < p.length; i++) {
      assertEquals(dist.getCDF(x[i]), p[i], EPS);
    }
  }

  public void testPDF(final double[] z, final double[] x, final ProbabilityDistribution<Double> dist) {
    for (int i = 0; i < z.length; i++) {
      assertEquals(dist.getPDF(x[i]), z[i], EPS);
    }
  }

  public void testInverseCDF(final double[] x, final ProbabilityDistribution<Double> dist) {
    for (final double d : x) {
      assertEquals(dist.getInverseCDF(dist.getCDF(d)), d, EPS);
    }
    try {
      dist.getInverseCDF(3.4);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      dist.getInverseCDF(-0.2);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  private void fail() {
    // TODO Auto-generated method stub

  }
}
