/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.estimation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.ChiSquareDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * @author emcleod
 * 
 */
public class ChiSquareDistributionMomentEstimatorTest {
  private static final Function1D<Double[], ProbabilityDistribution<Double>> CALCULATOR = new ChiSquareDistributionMomentEstimator();

  @Test
  public void test() {
    final int n = 50000;
    final double k = 1.34;
    final ProbabilityDistribution<Double> p1 = new ChiSquareDistribution(k);
    final Double[] x = new Double[n];
    for (int i = 0; i < n; i++) {
      x[i] = p1.nextRandom();
    }
    final ChiSquareDistribution p2 = (ChiSquareDistribution) CALCULATOR.evaluate(x);
    assertEquals(p2.getDegreesOfFreedom(), k, 2.5e-2);
  }
}
