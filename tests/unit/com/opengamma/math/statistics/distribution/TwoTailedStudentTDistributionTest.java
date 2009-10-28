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
public class TwoTailedStudentTDistributionTest {

  @Test
  public void test() {
    try {
      new TwoTailedStudentTDistribution(-1);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final ProbabilityDistribution<Double> dist = new TwoTailedStudentTDistribution(1000);
    final ProbabilityDistribution<Double> normal = new NormalProbabilityDistribution(0, 1);
    try {
      dist.getCDF(-3.);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      dist.getInverseCDF(-0.3);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      dist.getInverseCDF(1.8);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }
}
