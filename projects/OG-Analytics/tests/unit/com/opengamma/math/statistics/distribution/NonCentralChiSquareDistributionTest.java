/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;

/**
 * 
 */
public class NonCentralChiSquareDistributionTest {
  private static final double DOF = 3;
  private static final double NON_CENTRALITY = 1.5;
  private static final NonCentralChiSquareDistribution DIST = new NonCentralChiSquareDistribution(DOF, NON_CENTRALITY);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeDOF() {
    new NonCentralChiSquareDistribution(-DOF, NON_CENTRALITY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeNonCentrality() {
    new NonCentralChiSquareDistribution(DOF, -NON_CENTRALITY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullX() {
    DIST.getCDF(null);
  }

  @Test(expected = NotImplementedException.class)
  public void testInverseCDF() {
    DIST.getInverseCDF(0.5);
  }

  @Test(expected = NotImplementedException.class)
  public void testPDF() {
    DIST.getPDF(0.5);
  }

  @Test(expected = NotImplementedException.class)
  public void testRandom() {
    DIST.nextRandom();
  }

  @Test
  public void test() {
    assertEquals(DIST.getDegrees(), DOF, 0);
    assertEquals(DIST.getNonCentrality(), NON_CENTRALITY, 0);
    assertEquals(DIST.getCDF(-100.), 0, 0);
    assertEquals(DIST.getCDF(0.), 0, 0);
    assertEquals(DIST.getCDF(5.), 0.649285, 1e-6);
  }

  /**
   * Numbers computed from R
   */
  @Test
  public void testLargeValues() {
    double x = 123;
    double dof = 6.4;
    double nonCent = 100.34;
    NonCentralChiSquareDistribution dist = new NonCentralChiSquareDistribution(dof, nonCent);
    assertEquals(0.7930769, dist.getCDF(x), 1e-6);

  }

  @Test
  public void testAgainstInfinateSum() {
    double dof = 2.65;
    double nonCentrality = 10.;
    NonCentralChiSquareDistribution dist = new NonCentralChiSquareDistribution(dof, nonCentrality);
    for (int i = 0; i < 100; i++) {
      double x = 1.0 * i;
      double cdf1 = dist.getCDF(x);
      double cdf2 = nonCenteredChiSquare(x, dof, nonCentrality);
      // System.out.println(x + "\t" + cdf1 + "\t" + cdf2);
      assertEquals(cdf1, cdf2, 1e-4);
    }

  }

  private static double nonCenteredChiSquare(final double z, final double k, final double lambda) {
    double sum = 0.0;
    double fact = 1;
    for (int i = 0; i < 100; i++) {
      ChiSquareDistribution chiSq = new ChiSquareDistribution(k + 2 * i);

      sum += Math.pow(lambda / 2.0, i) / fact * chiSq.getCDF(z);
      fact *= (i + 1);
    }
    return Math.exp(-lambda / 2) * sum;
  }

}
