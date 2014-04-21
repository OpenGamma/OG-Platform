/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Test interpolateWithSensitivity method via PiecewisePolynomialInterpolator1D
 */
@Test(groups = TestGroup.UNIT)
public class NaturalSplineInterpolator1DTest {
  private static final NaturalSplineInterpolator INTERP = new NaturalSplineInterpolator();
  private static final NaturalSplineInterpolator1D INTERP1D = new NaturalSplineInterpolator1D();

  private static final double EPS = 1.e-7;

  /**
   * Recovery test on polynomial, rational, exponential functions, and node sensitivity test by finite difference method
   */
  @Test
  public void sampleFunctionTest() {
    final int nData = 10;
    final double[] xValues = new double[nData];
    final double[] yValues1 = new double[nData];
    final double[] yValues2 = new double[nData];
    final double[] yValues3 = new double[nData];
    final double[] yValues1Up = new double[nData];
    final double[] yValues2Up = new double[nData];
    final double[] yValues3Up = new double[nData];
    final double[] yValues1Dw = new double[nData];
    final double[] yValues2Dw = new double[nData];
    final double[] yValues3Dw = new double[nData];
    final double[] xKeys = new double[10 * nData];

    for (int i = 0; i < nData; ++i) {
      xValues[i] = i + 1;
      yValues1[i] = 0.5 * xValues[i] * xValues[i] * xValues[i] - 1.5 * xValues[i] * xValues[i] + xValues[i] - 2.;
      yValues2[i] = Math.exp(0.1 * xValues[i] - 6.);
      yValues3[i] = (2. * xValues[i] * xValues[i] + xValues[i]) / (xValues[i] * xValues[i] + xValues[i] * xValues[i] * xValues[i] + 5. * xValues[i] + 2.);
      yValues1Up[i] = yValues1[i];
      yValues2Up[i] = yValues2[i];
      yValues3Up[i] = yValues3[i];
      yValues1Dw[i] = yValues1[i];
      yValues2Dw[i] = yValues2[i];
      yValues3Dw[i] = yValues3[i];
    }

    final double xMin = xValues[0];
    final double xMax = xValues[nData - 1];
    for (int i = 0; i < 10 * nData; ++i) {
      xKeys[i] = xMin + (xMax - xMin) / (10 * nData - 1) * i;
    }

    final double[] resPrim1 = INTERP.interpolate(xValues, yValues1, xKeys).getData();
    final double[] resPrim2 = INTERP.interpolate(xValues, yValues2, xKeys).getData();
    final double[] resPrim3 = INTERP.interpolate(xValues, yValues3, xKeys).getData();

    Interpolator1DDataBundle dataBund1 = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues1);
    Interpolator1DDataBundle dataBund2 = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues2);
    Interpolator1DDataBundle dataBund3 = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues3);
    for (int i = 0; i < 10 * nData; ++i) {
      final double ref1 = resPrim1[i];
      final double ref2 = resPrim2[i];
      final double ref3 = resPrim3[i];
      assertEquals(ref1, INTERP1D.interpolate(dataBund1, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref1), 1.));
      assertEquals(ref2, INTERP1D.interpolate(dataBund2, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref2), 1.));
      assertEquals(ref3, INTERP1D.interpolate(dataBund3, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref3), 1.));
    }

    for (int j = 0; j < nData; ++j) {
      yValues1Up[j] = yValues1[j] * (1. + EPS);
      yValues2Up[j] = yValues2[j] * (1. + EPS);
      yValues3Up[j] = yValues3[j] * (1. + EPS);
      yValues1Dw[j] = yValues1[j] * (1. - EPS);
      yValues2Dw[j] = yValues2[j] * (1. - EPS);
      yValues3Dw[j] = yValues3[j] * (1. - EPS);
      Interpolator1DDataBundle dataBund1Up = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues1Up);
      Interpolator1DDataBundle dataBund2Up = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues2Up);
      Interpolator1DDataBundle dataBund3Up = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues3Up);
      Interpolator1DDataBundle dataBund1Dw = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues1Dw);
      Interpolator1DDataBundle dataBund2Dw = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues2Dw);
      Interpolator1DDataBundle dataBund3Dw = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues3Dw);
      for (int i = 0; i < 10 * nData; ++i) {
        double res1 = 0.5 * (INTERP1D.interpolate(dataBund1Up, xKeys[i]) - INTERP1D.interpolate(dataBund1Dw, xKeys[i])) / EPS / yValues1[j];
        double res2 = 0.5 * (INTERP1D.interpolate(dataBund2Up, xKeys[i]) - INTERP1D.interpolate(dataBund2Dw, xKeys[i])) / EPS / yValues2[j];
        double res3 = 0.5 * (INTERP1D.interpolate(dataBund3Up, xKeys[i]) - INTERP1D.interpolate(dataBund3Dw, xKeys[i])) / EPS / yValues3[j];
        assertEquals(res1, INTERP1D.getNodeSensitivitiesForValue(dataBund1, xKeys[i])[j], Math.max(Math.abs(yValues1[j]) * EPS, EPS) * 10.);
        assertEquals(res2, INTERP1D.getNodeSensitivitiesForValue(dataBund2, xKeys[i])[j], Math.max(Math.abs(yValues2[j]) * EPS, EPS) * 10.);
        assertEquals(res3, INTERP1D.getNodeSensitivitiesForValue(dataBund3, xKeys[i])[j], Math.max(Math.abs(yValues3[j]) * EPS, EPS) * 10.);
      }
      yValues1Up[j] = yValues1[j];
      yValues2Up[j] = yValues2[j];
      yValues3Up[j] = yValues3[j];
      yValues1Dw[j] = yValues1[j];
      yValues2Dw[j] = yValues2[j];
      yValues3Dw[j] = yValues3[j];
    }
  }

  /**
   * Bad conditioning data
   */
  @Test
  public void overShootTest() {
    final int nData = 10;
    final double[] xValues = new double[nData];
    final double[] yValues1 = new double[] {4., 3., 2., 1., 0.1, 0.1, 1., 2., 3., 4. };
    final double[] yValues2 = new double[] {-4., -3., -2., -1., -0.1, -0.1, -1., -2., -3., -4. };
    final double[] yValues3 = new double[] {1., 0.01, 0.5, 5., 0.1, 0.2, 0.5, 3., 2., 2. };
    final double[] yValues1Up = new double[nData];
    final double[] yValues2Up = new double[nData];
    final double[] yValues3Up = new double[nData];
    final double[] yValues1Dw = new double[nData];
    final double[] yValues2Dw = new double[nData];
    final double[] yValues3Dw = new double[nData];
    final double[] xKeys = new double[10 * nData];

    for (int i = 0; i < nData; ++i) {
      xValues[i] = i + 1;
      yValues1Up[i] = yValues1[i];
      yValues2Up[i] = yValues2[i];
      yValues3Up[i] = yValues3[i];
      yValues1Dw[i] = yValues1[i];
      yValues2Dw[i] = yValues2[i];
      yValues3Dw[i] = yValues3[i];
    }

    final double xMin = xValues[0];
    final double xMax = xValues[nData - 1];
    for (int i = 0; i < 10 * nData; ++i) {
      xKeys[i] = xMin + (xMax - xMin) / (10 * nData - 1) * i;
    }

    final double[] resPrim1 = INTERP.interpolate(xValues, yValues1, xKeys).getData();
    final double[] resPrim2 = INTERP.interpolate(xValues, yValues2, xKeys).getData();
    final double[] resPrim3 = INTERP.interpolate(xValues, yValues3, xKeys).getData();

    Interpolator1DDataBundle dataBund1 = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues1);
    Interpolator1DDataBundle dataBund2 = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues2);
    Interpolator1DDataBundle dataBund3 = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues3);
    for (int i = 0; i < 10 * nData; ++i) {
      final double ref1 = resPrim1[i];
      final double ref2 = resPrim2[i];
      final double ref3 = resPrim3[i];
      assertEquals(ref1, INTERP1D.interpolate(dataBund1, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref1), 1.));
      assertEquals(ref2, INTERP1D.interpolate(dataBund2, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref2), 1.));
      assertEquals(ref3, INTERP1D.interpolate(dataBund3, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref3), 1.));
    }

    for (int j = 0; j < nData; ++j) {
      yValues1Up[j] = yValues1[j] * (1. + EPS);
      yValues2Up[j] = yValues2[j] * (1. + EPS);
      yValues3Up[j] = yValues3[j] * (1. + EPS);
      yValues1Dw[j] = yValues1[j] * (1. - EPS);
      yValues2Dw[j] = yValues2[j] * (1. - EPS);
      yValues3Dw[j] = yValues3[j] * (1. - EPS);
      Interpolator1DDataBundle dataBund1Up = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues1Up);
      Interpolator1DDataBundle dataBund2Up = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues2Up);
      Interpolator1DDataBundle dataBund3Up = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues3Up);
      Interpolator1DDataBundle dataBund1Dw = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues1Dw);
      Interpolator1DDataBundle dataBund2Dw = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues2Dw);
      Interpolator1DDataBundle dataBund3Dw = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues3Dw);
      for (int i = 0; i < 10 * nData; ++i) {
        double res1 = 0.5 * (INTERP1D.interpolate(dataBund1Up, xKeys[i]) - INTERP1D.interpolate(dataBund1Dw, xKeys[i])) / EPS / yValues1[j];
        double res2 = 0.5 * (INTERP1D.interpolate(dataBund2Up, xKeys[i]) - INTERP1D.interpolate(dataBund2Dw, xKeys[i])) / EPS / yValues2[j];
        double res3 = 0.5 * (INTERP1D.interpolate(dataBund3Up, xKeys[i]) - INTERP1D.interpolate(dataBund3Dw, xKeys[i])) / EPS / yValues3[j];
        assertEquals(res1, INTERP1D.getNodeSensitivitiesForValue(dataBund1, xKeys[i])[j], Math.max(Math.abs(yValues1[j]) * EPS, EPS) * 10.);
        assertEquals(res2, INTERP1D.getNodeSensitivitiesForValue(dataBund2, xKeys[i])[j], Math.max(Math.abs(yValues2[j]) * EPS, EPS) * 10.);
        assertEquals(res3, INTERP1D.getNodeSensitivitiesForValue(dataBund3, xKeys[i])[j], Math.max(Math.abs(yValues3[j]) * EPS, EPS) * 10.);
      }
      yValues1Up[j] = yValues1[j];
      yValues2Up[j] = yValues2[j];
      yValues3Up[j] = yValues3[j];
      yValues1Dw[j] = yValues1[j];
      yValues2Dw[j] = yValues2[j];
      yValues3Dw[j] = yValues3[j];
    }
  }

  /**
   * Data points line on a straight line
   */
  @Test
  public void linearTest() {
    final int nData = 10;
    final double[] xValues = new double[nData];
    double[] yValues1 = new double[nData];
    double[] yValues2 = new double[nData];
    final double[] yValues1Up = new double[nData];
    final double[] yValues2Up = new double[nData];
    final double[] yValues1Dw = new double[nData];
    final double[] yValues2Dw = new double[nData];
    final double[] xKeys = new double[10 * nData];

    yValues1 = new double[] {3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0 };
    yValues2 = new double[] {-3.5, -2.0, -0.5, 1.0, 2.5, 4.0, 5.5, 7.0, 8.5, 10.0 };
    for (int i = 0; i < nData; ++i) {
      xValues[i] = i + 1;
      yValues1Up[i] = yValues1[i];
      yValues1Dw[i] = yValues1[i];
      yValues2Up[i] = yValues2[i];
      yValues2Dw[i] = yValues2[i];
    }

    final double xMin = xValues[0];
    final double xMax = xValues[nData - 1];
    for (int i = 0; i < 10 * nData; ++i) {
      xKeys[i] = xMin + (xMax - xMin) / (10 * nData - 1) * i;
    }

    final double[] resPrim1 = INTERP.interpolate(xValues, yValues1, xKeys).getData();
    final double[] resPrim2 = INTERP.interpolate(xValues, yValues2, xKeys).getData();

    Interpolator1DDataBundle dataBund1 = INTERP1D.getDataBundle(xValues, yValues1);
    Interpolator1DDataBundle dataBund2 = INTERP1D.getDataBundle(xValues, yValues2);
    for (int i = 0; i < 10 * nData; ++i) {
      final double ref1 = resPrim1[i];
      final double ref2 = resPrim2[i];
      assertEquals(ref1, INTERP1D.interpolate(dataBund1, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref1), 1.));
      assertEquals(ref2, INTERP1D.interpolate(dataBund2, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref2), 1.));
    }

    for (int j = 0; j < nData; ++j) {
      final double den1 = Math.abs(yValues1[j]) == 0. ? EPS : yValues1[j] * EPS;
      final double den2 = Math.abs(yValues2[j]) == 0. ? EPS : yValues2[j] * EPS;
      yValues1Up[j] = Math.abs(yValues1[j]) == 0. ? EPS : yValues1[j] * (1. + EPS);
      yValues1Dw[j] = Math.abs(yValues1[j]) == 0. ? -EPS : yValues1[j] * (1. - EPS);
      yValues2Up[j] = Math.abs(yValues2[j]) == 0. ? EPS : yValues2[j] * (1. + EPS);
      yValues2Dw[j] = Math.abs(yValues2[j]) == 0. ? -EPS : yValues2[j] * (1. - EPS);
      Interpolator1DDataBundle dataBund1Up = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues1Up);
      Interpolator1DDataBundle dataBund2Up = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues2Up);
      Interpolator1DDataBundle dataBund1Dw = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues1Dw);
      Interpolator1DDataBundle dataBund2Dw = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues2Dw);
      for (int i = 0; i < 10 * nData; ++i) {
        double res1 = 0.5 * (INTERP1D.interpolate(dataBund1Up, xKeys[i]) - INTERP1D.interpolate(dataBund1Dw, xKeys[i])) / den1;
        double res2 = 0.5 * (INTERP1D.interpolate(dataBund2Up, xKeys[i]) - INTERP1D.interpolate(dataBund2Dw, xKeys[i])) / den2;
        assertEquals(res1, INTERP1D.getNodeSensitivitiesForValue(dataBund1, xKeys[i])[j], Math.max(Math.abs(yValues1[j]) * EPS, EPS) * 10.);
        assertEquals(res2, INTERP1D.getNodeSensitivitiesForValue(dataBund2, xKeys[i])[j], Math.max(Math.abs(yValues2[j]) * EPS, EPS) * 10.);
      }
      yValues1Up[j] = yValues1[j];
      yValues1Dw[j] = yValues1[j];
      yValues2Up[j] = yValues2[j];
      yValues2Dw[j] = yValues2[j];
    }
  }

  /**
   * Endpoint condition is not relevant for constrained cubic spline
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void boundaryConditionTest() {
    final double[] xValues = new double[] {0., 1., 2., 3., };
    final double[] yValues = new double[] {0., 1., 2., 3., };

    INTERP1D.getDataBundle(xValues, yValues, 0., 0.);
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void boundaryConditionSortedTest() {
    final double[] xValues = new double[] {0., 1., 2., 3., };
    final double[] yValues = new double[] {0., 1., 2., 3., };

    INTERP1D.getDataBundleFromSortedArrays(xValues, yValues, 0., 0.);
  }
}
