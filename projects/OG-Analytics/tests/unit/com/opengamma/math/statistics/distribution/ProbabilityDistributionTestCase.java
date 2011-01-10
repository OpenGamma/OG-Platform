/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

/**
 * 
 */
public class ProbabilityDistributionTestCase {
  protected static final double EPS = 1e-5;
  protected static final RandomEngine ENGINE = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);

  public void testCDF(final double[] p, final double[] x, final ProbabilityDistribution<Double> dist) {
    testCDFWithNull(dist);
    for (int i = 0; i < p.length; i++) {
      assertEquals(dist.getCDF(x[i]), p[i], EPS);
    }
  }

  public void testPDF(final double[] z, final double[] x, final ProbabilityDistribution<Double> dist) {
    testPDFWithNull(dist);
    for (int i = 0; i < z.length; i++) {
      assertEquals(dist.getPDF(x[i]), z[i], EPS);
    }
  }

  public void testInverseCDF(final double[] x, final ProbabilityDistribution<Double> dist) {
    testInverseCDFWithNull(dist);
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

  public void testInverseCDFWithNull(final ProbabilityDistribution<Double> dist) {
    try {
      dist.getInverseCDF(null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  public void testPDFWithNull(final ProbabilityDistribution<Double> dist) {
    try {
      dist.getPDF(null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  public void testCDFWithNull(final ProbabilityDistribution<Double> dist) {
    try {
      dist.getCDF(null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }
}
