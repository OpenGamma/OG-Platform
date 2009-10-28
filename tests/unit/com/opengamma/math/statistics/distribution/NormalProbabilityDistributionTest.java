/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class NormalProbabilityDistributionTest extends ProbabilityDistributionTest {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalProbabilityDistribution(0, 1);
  private static final double[] X = new double[] { 0, 0.1, 0.4, 0.8, 1, 1.32, 1.78, 2, 2.36, 2.88, 3, 3.5, 4, 4.5, 5 };
  private static final double[] P = new double[] { 0.50000, 0.53982, 0.65542, 0.78814, 0.84134, 0.90658, 0.96246, 0.97724, 0.99086, 0.99801, 0.99865, 0.99976, 0.99996, 0.99999,
      0.99999 };
  private static final double[] Z = new double[] { 0.39894, 0.39695, 0.36827, 0.28969, 0.24197, 0.16693, 0.08182, 0.05399, 0.02463, 0.00630, 4.43184e-3, 8.72682e-4, 1.3383e-4,
      1.59837e-5, 1.48671e-6 };

  @Test
  public void test() {
    try {
      new NormalProbabilityDistribution(1, -0.4);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      new NormalProbabilityDistribution(1, 0.4, null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      new NormalProbabilityDistribution(1, -0.4, ENGINE);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    testCDF(P, X, NORMAL);
    testPDF(Z, X, NORMAL);
    testInverseCDF(X, NORMAL);
  }
}
