/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import org.testng.annotations.Test;

/**
 * 
 */
public class GammaDistributionTest extends ProbabilityDistributionTestCase {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeK1() {
    new GammaDistribution(-1, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeK2() {
    new GammaDistribution(-1, 1, ENGINE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeTheta1() {
    new GammaDistribution(1, -1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeTheta2() {
    new GammaDistribution(1, -1, ENGINE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEngine() {
    new GammaDistribution(1, 1, null);
  }

  @Test
  public void test() {
    final ProbabilityDistribution<Double> dist = new GammaDistribution(1, 0.5, ENGINE);
    testCDFWithNull(dist);
    testPDFWithNull(dist);
  }
}
