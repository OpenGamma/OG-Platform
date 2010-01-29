/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class TwoTailedStudentTDistributionTest {
  private static final ProbabilityDistribution<Double> T = new TwoTailedStudentTDistribution(5);
  private static final ProbabilityDistribution<Double> T1 = new StudentTDistribution(5);

  @Test(expected = IllegalArgumentException.class)
  public void testNegative() {
    new TwoTailedStudentTDistribution(-1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeX() {
    T.getCDF(-3.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLowP() {
    T.getInverseCDF(-0.3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighP() {
    T.getInverseCDF(1.5);
  }

  @Test
  public void test() {
    System.out.println(T.getInverseCDF(T.getCDF(0.5)));
    System.out.println();
  }
}
