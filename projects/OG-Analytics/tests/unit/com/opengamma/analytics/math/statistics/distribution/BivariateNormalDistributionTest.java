/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.distribution;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Random;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.statistics.distribution.BivariateNormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 *
 */
public class BivariateNormalDistributionTest {
  private static final ProbabilityDistribution<double[]> DIST = new BivariateNormalDistribution();
  private static final double EPS = 1e-8;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCDF() {
    DIST.getCDF(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInsufficientLengthCDF() {
    DIST.getCDF(new double[] {2, 1});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testExcessiveLengthCDF() {
    DIST.getCDF(new double[] {2, 1, 4, 5});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighCorrelation() {
    DIST.getCDF(new double[] {1., 1., 3.});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowCorrelation() {
    DIST.getCDF(new double[] {1., 1., -3.});
  }

  @Test
  public void test() {
    assertEquals(DIST.getCDF(new double[] {Double.POSITIVE_INFINITY, Math.random(), Math.random()}), 1, 0);
    assertEquals(DIST.getCDF(new double[] {Math.random(), Double.POSITIVE_INFINITY, Math.random()}), 1, 0);
    assertEquals(DIST.getCDF(new double[] {Double.NEGATIVE_INFINITY, Math.random(), Math.random()}), 0, 0);
    assertEquals(DIST.getCDF(new double[] {Math.random(), Double.NEGATIVE_INFINITY, Math.random()}), 0, 0);
    assertEquals(DIST.getCDF(new double[] {0.0, 0.0, 0.0}), 0.25, EPS);
    assertEquals(DIST.getCDF(new double[] {0.0, 0.0, -0.5}), 1. / 6, EPS);
    assertEquals(DIST.getCDF(new double[] {0.0, 0.0, 0.5}), 1. / 3, EPS);

    assertEquals(DIST.getCDF(new double[] {0.0, -0.5, 0.0}), 0.1542687694, EPS);
    assertEquals(DIST.getCDF(new double[] {0.0, -0.5, -0.5}), 0.0816597607, EPS);
    assertEquals(DIST.getCDF(new double[] {0.0, -0.5, 0.5}), 0.2268777781, EPS);

    assertEquals(DIST.getCDF(new double[] {0.0, 0.5, 0.0}), 0.3457312306, EPS);
    assertEquals(DIST.getCDF(new double[] {0.0, 0.5, -0.5}), 0.2731222219, EPS);
    assertEquals(DIST.getCDF(new double[] {0.0, 0.5, 0.5}), 0.4183402393, EPS);

    assertEquals(DIST.getCDF(new double[] {-0.5, 0.0, 0.0}), 0.1542687694, EPS);
    assertEquals(DIST.getCDF(new double[] {-0.5, 0.0, -0.5}), 0.0816597607, EPS);
    assertEquals(DIST.getCDF(new double[] {-0.5, 0.0, 0.5}), 0.2268777781, EPS);

    assertEquals(DIST.getCDF(new double[] {-0.5, -0.5, 0.0}), 0.0951954128, EPS);
    assertEquals(DIST.getCDF(new double[] {-0.5, -0.5, -0.5}), 0.0362981865, EPS);
    assertEquals(DIST.getCDF(new double[] {-0.5, -0.5, 0.5}), 0.1633195213, EPS);

    assertEquals(DIST.getCDF(new double[] {-0.5, 0.5, 0.0}), 0.2133421259, EPS);
    assertEquals(DIST.getCDF(new double[] {-0.5, 0.5, -0.5}), 0.1452180174, EPS);
    assertEquals(DIST.getCDF(new double[] {-0.5, 0.5, 0.5}), 0.2722393522, EPS);

    assertEquals(DIST.getCDF(new double[] {0.5, 0.0, 0.0}), 0.3457312306, EPS);
    assertEquals(DIST.getCDF(new double[] {0.5, 0.0, -0.5}), 0.2731222219, EPS);
    assertEquals(DIST.getCDF(new double[] {0.5, 0.0, 0.5}), 0.4183402393, EPS);

    assertEquals(DIST.getCDF(new double[] {0.5, -0.5, 0.0}), 0.2133421259, EPS);
    assertEquals(DIST.getCDF(new double[] {0.5, -0.5, -0.5}), 0.1452180174, EPS);
    assertEquals(DIST.getCDF(new double[] {0.5, -0.5, 0.5}), 0.2722393522, EPS);

    assertEquals(DIST.getCDF(new double[] {0.5, 0.5, 0.0}), 0.4781203354, EPS);
    assertEquals(DIST.getCDF(new double[] {0.5, 0.5, -0.5}), 0.4192231090, EPS);
    assertEquals(DIST.getCDF(new double[] {0.0, -1.0, -1.0}), 0.00000000, EPS);
  }

  @Test
  public void testZeroZeroAnalyticCases() {

    final double MAX_ABS_ERROR_DW2 = 9.0E-6;
    checkAtZeroZero(0.0, 1.0/4.0, MAX_ABS_ERROR_DW2);
    checkAtZeroZero(1.0/2.0, 1.0/3.0, MAX_ABS_ERROR_DW2);
    checkAtZeroZero(-1.0/2.0, 1.0/6.0, MAX_ABS_ERROR_DW2);
    checkAtZeroZero(1.0/Math.sqrt(2.0), 3.0/8.0, MAX_ABS_ERROR_DW2);
    checkAtZeroZero(-1.0/Math.sqrt(2.0), 1.0/8.0, MAX_ABS_ERROR_DW2);
    checkAtZeroZero(Math.sqrt(3.0)/2.0, 5.0/12.0, MAX_ABS_ERROR_DW2);
    checkAtZeroZero(-Math.sqrt(3.0)/2.0, 1.0/12.0, MAX_ABS_ERROR_DW2);
    checkAtZeroZero(1.0, 1.0/2.0, MAX_ABS_ERROR_DW2);
    checkAtZeroZero(-1.0, 0.0, MAX_ABS_ERROR_DW2);

    Random rng = new Random(42);
    for(int i = 0;i<50000; ++i) {
      final double rho = 2.0 * rng.nextDouble() - 1.0;
      if (rho==-1.0) continue;
      if (rho==1.0) continue;//not possible?
      final double expectedM = 0.25 + Math.asin(rho)/(2.0*Math.PI);
      checkAtZeroZero(rho, expectedM, MAX_ABS_ERROR_DW2);
    }
  }

  @Test
  public void testAtSpecificNonZeroZeroAnalyticCases() {

    final double MAX_ABS_ERROR_DW2 = 9.0E-7;

    checkAt(0.5, 0.5, 0.95, 6.469071953667896E-01, MAX_ABS_ERROR_DW2);
    checkAt(0.5, 0.5, -0.95, 3.829520842043984E-01, MAX_ABS_ERROR_DW2);
    checkAt(0.5, 0.5, 0.7, 5.805266392700936E-01, MAX_ABS_ERROR_DW2);
    checkAt(0.5, 0.5, -0.7, 3.98076964063486E-01, MAX_ABS_ERROR_DW2);
    checkAt(0.5, 0.5, 0.2, 5.036399310969482E-01, MAX_ABS_ERROR_DW2);
    checkAt(0.5, 0.5, -0.2, 4.538723806509604E-01, MAX_ABS_ERROR_DW2);
    checkAt(0.5, 0.5, 0.0, 4.781203353511161E-01, MAX_ABS_ERROR_DW2);
    checkAt(-0.5, 0.5, 0.95, 3.085103770696148E-01, MAX_ABS_ERROR_DW2);
    checkAt(-0.5, 0.5, -0.95, 4.455526590722349E-02, MAX_ABS_ERROR_DW2);
    checkAt(-0.5, 0.5, 0.7, 2.933854972105271E-01, MAX_ABS_ERROR_DW2);
    checkAt(-0.5, 0.5, -0.7, 1.109358220039195E-01, MAX_ABS_ERROR_DW2);
    checkAt(-0.5, 0.5, 0.2, 2.375900806230527E-01, MAX_ABS_ERROR_DW2);
    checkAt(-0.5, 0.5, -0.2, 1.878225301770649E-01, MAX_ABS_ERROR_DW2);
    checkAt(10.1, -10.0, 0.93, 7.619853024160583E-24, MAX_ABS_ERROR_DW2);
    checkAt(2.0, -2.0, 1.0, 2.275013194817922E-02, MAX_ABS_ERROR_DW2);

  }

  private void checkAtZeroZero(final double rho, final double expectedM, final double maxAbsError) {

    double actualM = DIST.getCDF(new double[] {0.0, 0.0, rho});
    double absErr = Math.abs(actualM-expectedM);
    if (absErr>maxAbsError) {
      fail("Failed on ZeroZero at rho: " + rho + " expected " + expectedM + " actual " + actualM);
    }
  }

  private void checkAt(final double a, final double b, final double rho, final double expectedM, final double maxAbsError) {

    double actualM = DIST.getCDF(new double[] {a, b, rho});
    double absErr = Math.abs(actualM-expectedM);
    if (absErr>maxAbsError) {
      fail("Failed on at a=" + a + " b=" + b + " rho: " + rho + " expected " + expectedM + " actual " + actualM);
    }
  }
}
