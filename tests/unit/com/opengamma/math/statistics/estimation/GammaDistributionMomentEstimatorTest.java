/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.estimation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.GammaDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * @author emcleod
 * 
 */
public class GammaDistributionMomentEstimatorTest {
  private static final Function1D<Double[], ProbabilityDistribution<Double>> CALCULATOR = new GammaDistributionMomentEstimator();

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    CALCULATOR.evaluate((Double[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty() {
    CALCULATOR.evaluate(new Double[0]);
  }

  @Test
  public void test() {
    final int n = 50000;
    final double k = 0.97;
    final double theta = 0.46;
    final ProbabilityDistribution<Double> p1 = new GammaDistribution(k, theta);
    final Double[] x = new Double[n];
    for (int i = 0; i < n; i++) {
      x[i] = p1.nextRandom();
    }
    final GammaDistribution p2 = (GammaDistribution) CALCULATOR.evaluate(x);
    final double eps = 0.025;
    assertEquals(p2.getK(), k, eps);
    assertEquals(p2.getTheta(), theta, eps);
  }

}
