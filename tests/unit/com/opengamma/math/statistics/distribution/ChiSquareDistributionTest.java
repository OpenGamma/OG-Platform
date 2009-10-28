/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class ChiSquareDistributionTest extends ProbabilityDistributionTest {
  private static final double[] X = new double[] { 1.9, 5.8, 9.0, 15.5, 39 };
  private static final double[] DOF = new double[] { 3, 6, 7, 16, 28 };
  private static final double[] Q = new double[] { 0.59342, 0.44596, 0.25266, 0.48837, 0.08092 };

  @Test
  public void test() {
    try {
      new ChiSquareDistribution(-2);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      new ChiSquareDistribution(-2, ENGINE);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      new ChiSquareDistribution(2, null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    ProbabilityDistribution<Double> dist = new ChiSquareDistribution(1, ENGINE);
    testCDFWithNull(dist);
    testPDFWithNull(dist);
    testInverseCDFWithNull(dist);
    for (int i = 0; i < 5; i++) {
      dist = new ChiSquareDistribution(DOF[i], ENGINE);
      assertEquals(1 - dist.getCDF(X[i]), Q[i], EPS);
      assertEquals(dist.getInverseCDF(dist.getCDF(X[i])), X[i], EPS);
    }
  }
}
