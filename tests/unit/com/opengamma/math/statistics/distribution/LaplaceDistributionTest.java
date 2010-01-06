/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.statistics.descriptive.MeanCalculator;
import com.opengamma.math.statistics.descriptive.MedianCalculator;
import com.opengamma.math.statistics.descriptive.SampleFisherKurtosisCalculator;
import com.opengamma.math.statistics.descriptive.SampleSkewnessCalculator;
import com.opengamma.math.statistics.descriptive.SampleVarianceCalculator;

/**
 * @author emcleod
 * 
 */
public class LaplaceDistributionTest extends ProbabilityDistributionTest {
  private static final double MU = 0.7;
  private static final double B = 5;
  private static final ProbabilityDistribution<Double> LAPLACE = new LaplaceDistribution(MU, B);
  private static final Double[] DATA;
  private static final double EPS = 0.05;
  static {
    final int n = 500000;
    DATA = new Double[n];
    for (int i = 0; i < n; i++) {
      DATA[i] = LAPLACE.nextRandom();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeBDistribution() {
    new LaplaceDistribution(1, -0.4);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullEngine() {
    new LaplaceDistribution(0, 1, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInverseCDFWithLow() {
    LAPLACE.getInverseCDF(-0.45);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInverseCDFWithHigh() {
    LAPLACE.getInverseCDF(6.7);
  }

  @Test
  public void test() {
    testCDFWithNull(LAPLACE);
    testPDFWithNull(LAPLACE);
    testInverseCDFWithNull(LAPLACE);
    final double mean = new MeanCalculator().evaluate(DATA);
    final double median = new MedianCalculator().evaluate(DATA);
    final double variance = new SampleVarianceCalculator().evaluate(DATA);
    final double skew = new SampleSkewnessCalculator().evaluate(DATA);
    final double kurtosis = new SampleFisherKurtosisCalculator().evaluate(DATA);
    assertEquals(mean, MU, EPS);
    assertEquals(median, MU, EPS);
    assertEquals(variance, 2 * B * B, EPS);
    assertEquals(skew, 0, EPS);
    assertEquals(kurtosis, 3, EPS);
  }
}
