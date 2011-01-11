/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class ChiSquareDistributionTest extends ProbabilityDistributionTestCase {
  private static final double[] X = new double[] {1.9, 5.8, 9.0, 15.5, 39};
  private static final double[] DOF = new double[] {3, 6, 7, 16, 28};
  private static final double[] Q = new double[] {0.59342, 0.44596, 0.25266, 0.48837, 0.08092};

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeDOF1() {
    new ChiSquareDistribution(-2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeDOF2() {
    new ChiSquareDistribution(-2, ENGINE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullEngine() {
    new ChiSquareDistribution(2, null);
  }

  @Test
  public void test() {
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
