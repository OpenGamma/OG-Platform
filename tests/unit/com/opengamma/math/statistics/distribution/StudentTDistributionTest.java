/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class StudentTDistributionTest extends ProbabilityDistributionTest {
  private static final double[] X = new double[] { 0.32492, 0.270722, 0.717558, 1.372184, 1.36343, 1.770933, 2.13145, 2.55238, 2.80734, 3.6896 };
  private static final double[] DOF = new double[] { 1, 4, 6, 10, 11, 13, 15, 18, 23, 27 };
  private static final double[] P = new double[] { 0.6, 0.6, 0.75, 0.9, 0.9, 0.95, 0.975, 0.99, 0.995, 0.9995 };

  @Test
  public void test() {
    try {
      new StudentTDistribution(-2);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      new StudentTDistribution(-2, ENGINE);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      new StudentTDistribution(2, null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    ProbabilityDistribution<Double> dist = new StudentTDistribution(1, ENGINE);
    testCDFWithNull(dist);
    testPDFWithNull(dist);
    try {
      dist.getInverseCDF(0.2);
      fail();
    } catch (final NotImplementedException e) {
      // Expected
    }
    for (int i = 0; i < 10; i++) {
      dist = new StudentTDistribution(DOF[i], ENGINE);
      assertEquals(P[i], dist.getCDF(X[i]), EPS);
    }
  }
}
