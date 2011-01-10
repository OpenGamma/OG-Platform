/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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

    x = 455.038;
    dof = 12;
    nonCent = 444.44;

    dist = new NonCentralChiSquareDistribution(dof, nonCent);
    assertEquals(0.4961805, dist.getCDF(x), 1e-6);

    x = 999400;
    dof = 500;
    nonCent = 1000000;
    dist = new NonCentralChiSquareDistribution(dof, nonCent);
    assertEquals(0.2913029, dist.getCDF(x), 1e-6);

  }

}
