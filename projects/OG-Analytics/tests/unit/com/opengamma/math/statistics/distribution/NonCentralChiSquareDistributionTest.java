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
}
