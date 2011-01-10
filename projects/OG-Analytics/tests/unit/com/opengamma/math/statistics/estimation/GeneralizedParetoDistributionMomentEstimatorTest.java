/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.estimation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.math.statistics.distribution.GeneralizedParetoDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class GeneralizedParetoDistributionMomentEstimatorTest {
  private static final double MU = 6;
  private static final double SIGMA = 0.5;
  private static final double KSI = 0.1;
  private static final ProbabilityDistribution<Double> GPD = new GeneralizedParetoDistribution(MU, SIGMA, KSI, new MersenneTwister64(MersenneTwister64.DEFAULT_SEED));
  private static final int N = 100000;
  private static final double[] X = new double[N];
  private static final DistributionParameterEstimator<Double> ESTIMATOR = new GeneralizedParetoDistributionMomentEstimator();
  static {
    for (int i = 0; i < N; i++) {
      X[i] = GPD.nextRandom();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullArray() {
    ESTIMATOR.evaluate((double[]) null);
  }

  @Test
  public void test() {
    final double eps = 0.05;
    final GeneralizedParetoDistribution result = (GeneralizedParetoDistribution) ESTIMATOR.evaluate(X);
    assertEquals(MU, result.getMu(), MU * eps);
    assertEquals(SIGMA, result.getSigma(), SIGMA * eps);
    assertEquals(KSI, result.getKsi(), KSI * eps);
  }
}
