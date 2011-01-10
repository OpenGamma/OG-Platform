/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

public class LaplaceDistributionTest extends ProbabilityDistributionTestCase {
  private static final double MU = 0.7;
  private static final double B = 0.5;
  private static final ProbabilityDistribution<Double> LAPLACE = new LaplaceDistribution(MU, B, ENGINE);
  private static final double[] DATA;
  private static final double EPS1 = 0.05;
  static {
    final int n = 500000;
    DATA = new double[n];
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
    assertEquals(mean, MU, EPS1);
    assertEquals(median, MU, EPS1);
    assertEquals(variance, 2 * B * B, EPS1);
    assertEquals(skew, 0, EPS1);
    assertEquals(kurtosis, 3, EPS1);
  }
}
