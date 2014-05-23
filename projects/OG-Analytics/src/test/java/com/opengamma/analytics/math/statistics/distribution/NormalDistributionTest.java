/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.distribution;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NormalDistributionTest extends ProbabilityDistributionTestCase {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1, ENGINE);
  private static final double[] X = new double[] {0, 0.1, 0.4, 0.8, 1, 1.32, 1.78, 2, 2.36, 2.88, 3, 3.5, 4, 4.5, 5 };
  private static final double[] P = new double[] {0.50000, 0.53982, 0.65542, 0.78814, 0.84134, 0.90658, 0.96246, 0.97724, 0.99086, 0.99801, 0.99865, 0.99976, 0.99996, 0.99999, 0.99999 };
  private static final double[] Z = new double[] {0.39894, 0.39695, 0.36827, 0.28969, 0.24197, 0.16693, 0.08182, 0.05399, 0.02463, 0.00630, 4.43184e-3, 8.72682e-4, 1.3383e-4, 1.59837e-5, 1.48671e-6 };

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeSigmaDistribution() {
    new NormalDistribution(1, -0.4);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEngine() {
    new NormalDistribution(0, 1, null);
  }

  @Test
      (enabled = false)
      public void printTest() {
    for (int i = 0; i < 201; i++) {
      double x = -40 + 30 * i / 200.;
      double y = NORMAL.getCDF(x);
      System.out.println(x + "\t" + y);
    }
  }

  @Test
  public void test() {
    assertCDF(P, X, NORMAL);
    assertPDF(Z, X, NORMAL);
    assertInverseCDF(X, NORMAL);
  }

  @Test
  public void testRoundTrip() {
    for (int i = 0; i < 51; i++) {
      double x = -37.0 + 44 * i / 50.;
      double p = NORMAL.getCDF(x);
      double xStar = (p == 1.0 ? Double.POSITIVE_INFINITY : (p == 0.0 ? Double.NEGATIVE_INFINITY : NORMAL.getInverseCDF(p)));
      assertEquals(x, xStar, 1e-3);
    }
  }

  @Test
  public void testObject() {
    NormalDistribution other = new NormalDistribution(0, 1, ENGINE);
    assertEquals(NORMAL, other);
    assertEquals(NORMAL.hashCode(), other.hashCode());
    other = new NormalDistribution(0, 1);
    assertEquals(NORMAL, other);
    assertEquals(NORMAL.hashCode(), other.hashCode());
    other = new NormalDistribution(0.1, 1, ENGINE);
    assertFalse(NORMAL.equals(other));
    other = new NormalDistribution(0, 1.1, ENGINE);
    assertFalse(NORMAL.equals(other));
  }
}
