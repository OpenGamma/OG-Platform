/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

public class StudentTDistributionTest extends ProbabilityDistributionTestCase {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final double[] X = new double[] { 0.32492, 0.270722, 0.717558, 1.372184, 1.36343, 1.770933, 2.13145, 2.55238, 2.80734, 3.6896 };
  private static final double[] DOF = new double[] { 1, 4, 6, 10, 11, 13, 15, 18, 23, 27 };
  private static final double[] P = new double[] { 0.6, 0.6, 0.75, 0.9, 0.9, 0.95, 0.975, 0.99, 0.995, 0.9995 };

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeDOF1() {
    new StudentTDistribution(-2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeDOF2() {
    new StudentTDistribution(-2, ENGINE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEngine() {
    new StudentTDistribution(2, null);
  }

  @Test
  public void test() {
    ProbabilityDistribution<Double> dist = new StudentTDistribution(1, ENGINE);
    testCDFWithNull(dist);
    testPDFWithNull(dist);
    testInverseCDF(X, dist);
    for (int i = 0; i < 10; i++) {
      dist = new StudentTDistribution(DOF[i], ENGINE);
      assertEquals(P[i], dist.getCDF(X[i]), EPS);
    }
  }

  @Test
  public void testNormal() {
    final ProbabilityDistribution<Double> highDOF = new StudentTDistribution(1000000, ENGINE);
    final ProbabilityDistribution<Double> normal = new NormalDistribution(0, 1, ENGINE);
    final double eps = 1e-4;
    double x;
    for (int i = 0; i < 100; i++) {
      x = RANDOM.nextDouble();
      assertEquals(highDOF.getCDF(x), normal.getCDF(x), eps);
      assertEquals(highDOF.getPDF(x), normal.getPDF(x), eps);
      assertEquals(highDOF.getInverseCDF(x), normal.getInverseCDF(x), eps);
    }
  }
}
