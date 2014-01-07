/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Test interpolateWithSensitivity method via PiecewisePolynomialInterpolator1D
 */
@Test(groups = TestGroup.UNIT)
public class NotAKnotCubicSplineInterpolator1DTest {

  //  private static final Random randObj = new Random();
  private static final CubicSplineInterpolator INTERP = new CubicSplineInterpolator();
  private static final NotAKnotCubicSplineInterpolator1D INTERP1D = new NotAKnotCubicSplineInterpolator1D();

  private static final double EPS = 1.e-7;

  /**
   * Recovery test on polynomial, rational, exponential functions, and node sensitivity test by finite difference method
   * Note that when conditioning number is large, both of the interpolation and node sensitivity produce a poor result
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
      xValues[i] = 0.1 * i * i + i - 8.;
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

    Interpolator1DDataBundle dataBund1 = INTERP1D.getDataBundle(xValues, yValues1);
    Interpolator1DDataBundle dataBund2 = INTERP1D.getDataBundle(xValues, yValues2);
    Interpolator1DDataBundle dataBund3 = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues3);
    for (int i = 0; i < 10 * nData; ++i) {
      assertEquals(resPrim1[i], INTERP1D.interpolate(dataBund1, xKeys[i]), 1.e-15);
      assertEquals(resPrim2[i], INTERP1D.interpolate(dataBund2, xKeys[i]), 1.e-15);
      assertEquals(resPrim3[i], INTERP1D.interpolate(dataBund3, xKeys[i]), 1.e-15);
    }

    for (int j = 0; j < nData; ++j) {
      yValues1Up[j] = yValues1[j] == 0. ? EPS : yValues1[j] * (1. + EPS);
      yValues2Up[j] = yValues2[j] == 0. ? EPS : yValues2[j] * (1. + EPS);
      yValues3Up[j] = yValues3[j] == 0. ? EPS : yValues3[j] * (1. + EPS);
      yValues1Dw[j] = yValues1[j] == 0. ? -EPS : yValues1[j] * (1. - EPS);
      yValues2Dw[j] = yValues2[j] == 0. ? -EPS : yValues2[j] * (1. - EPS);
      yValues3Dw[j] = yValues3[j] == 0. ? -EPS : yValues3[j] * (1. - EPS);
      Interpolator1DDataBundle dataBund1Up = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues1Up);
      Interpolator1DDataBundle dataBund2Up = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues2Up);
      Interpolator1DDataBundle dataBund3Up = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues3Up);
      Interpolator1DDataBundle dataBund1Dw = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues1Dw);
      Interpolator1DDataBundle dataBund2Dw = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues2Dw);
      Interpolator1DDataBundle dataBund3Dw = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues3Dw);
      for (int i = 0; i < 10 * nData; ++i) {
        final double ref1 = yValues1[j] == 0. ? EPS : yValues1[j] * EPS;
        final double ref2 = yValues2[j] == 0. ? EPS : yValues2[j] * EPS;
        final double ref3 = yValues3[j] == 0. ? EPS : yValues3[j] * EPS;
        double res1 = 0.5 * (INTERP1D.interpolate(dataBund1Up, xKeys[i]) - INTERP1D.interpolate(dataBund1Dw, xKeys[i])) / ref1;
        double res2 = 0.5 * (INTERP1D.interpolate(dataBund2Up, xKeys[i]) - INTERP1D.interpolate(dataBund2Dw, xKeys[i])) / ref2;
        double res3 = 0.5 * (INTERP1D.interpolate(dataBund3Up, xKeys[i]) - INTERP1D.interpolate(dataBund3Dw, xKeys[i])) / ref3;
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
   * 
   */
  @Test
  public void locallyFlatDataTest() {
    final double[] xValues = new double[] {1., 2., 3., 4., 5., 6., 7., 8. };
    final double[] yValues = new double[] {2., 4.0, 4., 7.0, 7., 6., 6., 9. };
    final int nData = xValues.length;
    double[] yValuesUp = Arrays.copyOf(yValues, nData);
    double[] yValuesDw = Arrays.copyOf(yValues, nData);
    final double[] xKeys = new double[10 * nData];
    final double xMin = xValues[0];
    final double xMax = xValues[nData - 1];
    for (int i = 0; i < 10 * nData; ++i) {
      xKeys[i] = xMin + (xMax - xMin) / (10 * nData - 1) * i;
    }

    Interpolator1DDataBundle dataBund = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues);

    for (int j = 0; j < nData; ++j) {
      yValuesUp[j] = yValues[j] * (1. + EPS);
      yValuesDw[j] = yValues[j] * (1. - EPS);
      Interpolator1DDataBundle dataBundUp = INTERP1D.getDataBundleFromSortedArrays(xValues, yValuesUp);
      Interpolator1DDataBundle dataBundDw = INTERP1D.getDataBundleFromSortedArrays(xValues, yValuesDw);
      for (int i = 0; i < 10 * nData; ++i) {
        double res0 = 0.5 * (INTERP1D.interpolate(dataBundUp, xKeys[i]) - INTERP1D.interpolate(dataBundDw, xKeys[i])) / EPS / yValues[j];
        assertEquals(res0, INTERP1D.getNodeSensitivitiesForValue(dataBund, xKeys[i])[j], Math.max(Math.abs(yValues[j]) * EPS, EPS));
      }
      yValuesUp[j] = yValues[j];
      yValuesDw[j] = yValues[j];
    }
  }

  /**
   * 
   */
  @Test
  public void flatDataTest() {
    final double[] xValues = new double[] {-12., -2.1, 3.9, 4.2, 5.9, 7.8, 11.3 };
    final double[] yValues = new double[] {4., 4.0, 4., 4.0, 4., 4.0, 4. };
    final int nData = xValues.length;
    double[] yValuesUp = Arrays.copyOf(yValues, nData);
    double[] yValuesDw = Arrays.copyOf(yValues, nData);
    final double[] xKeys = new double[10 * nData];
    final double xMin = xValues[0];
    final double xMax = xValues[nData - 1];
    for (int i = 0; i < 10 * nData; ++i) {
      xKeys[i] = xMin + (xMax - xMin) / (10 * nData - 1) * i;
    }

    Interpolator1DDataBundle dataBund = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues);

    for (int j = 0; j < nData; ++j) {
      yValuesUp[j] = yValues[j] * (1. + EPS);
      yValuesDw[j] = yValues[j] * (1. - EPS);
      Interpolator1DDataBundle dataBundUp = INTERP1D.getDataBundle(xValues, yValuesUp);
      Interpolator1DDataBundle dataBundDw = INTERP1D.getDataBundle(xValues, yValuesDw);
      for (int i = 0; i < 10 * nData; ++i) {
        double res0 = 0.5 * (INTERP1D.interpolate(dataBundUp, xKeys[i]) - INTERP1D.interpolate(dataBundDw, xKeys[i])) / EPS / yValues[j];
        assertEquals(res0, INTERP1D.getNodeSensitivitiesForValue(dataBund, xKeys[i])[j], Math.max(Math.abs(yValues[j]) * EPS, EPS));
      }
      yValuesUp[j] = yValues[j];
      yValuesDw[j] = yValues[j];
    }
  }

  /**
   * When the number of data points is small, the resulting interpolant is linear or quadratic 
   * where the size of the coefficient matrix and corresponding sensitivity matrix have smaller size
   */
  @Test
  public void LinearAndQuadraticTest() {
    final double[] xValuesForLin = new double[] {1., 2. };
    final double[] yValuesForLin1 = new double[] {3., 7. };
    final double[] yValuesForLin2 = new double[] {2, -6 };
    final double[] yValuesForLin1Up = new double[2];
    final double[] yValuesForLin2Up = new double[2];
    final double[] yValuesForLin1Dw = new double[2];
    final double[] yValuesForLin2Dw = new double[2];

    final double[] xValuesForQuad = new double[] {1., 2., 3. };
    final double[] yValuesForQuad1 = new double[] {1., 6., 5. };
    final double[] yValuesForQuad2 = new double[] {2., -2., -3. };
    final double[] yValuesForQuad1Up = new double[3];
    final double[] yValuesForQuad2Up = new double[3];
    final double[] yValuesForQuad1Dw = new double[3];
    final double[] yValuesForQuad2Dw = new double[3];

    final double[] xKeys = new double[] {-0.5, 6. / 5., 2.38, 1., 2., 3. };

    final int keyLength = xKeys.length;

    /**
     * Linear Interpolation
     */

    CubicSplineInterpolator interp = new CubicSplineInterpolator();

    Interpolator1DDataBundle dataBundLin1 = INTERP1D.getDataBundleFromSortedArrays(xValuesForLin, yValuesForLin1);
    Interpolator1DDataBundle dataBundLin2 = INTERP1D.getDataBundleFromSortedArrays(xValuesForLin, yValuesForLin2);

    for (int i = 0; i < keyLength; ++i) {
      final double val = interp.interpolate(xValuesForLin, yValuesForLin1, xKeys[i]);
      final double ref = val == 0. ? 1. : Math.abs(val);
      assertEquals(val, INTERP1D.interpolate(dataBundLin1, xKeys[i]), ref * 1.e-15);
    }
    for (int i = 0; i < keyLength; ++i) {
      final double val = interp.interpolate(xValuesForLin, yValuesForLin2, xKeys[i]);
      final double ref = val == 0. ? 1. : Math.abs(val);
      assertEquals(val, INTERP1D.interpolate(dataBundLin2, xKeys[i]), ref * 1.e-15);
    }

    for (int j = 0; j < 2; ++j) {
      yValuesForLin1Up[j] = yValuesForLin1[j] == 0. ? EPS : yValuesForLin1[j] * (1. + EPS);
      yValuesForLin2Up[j] = yValuesForLin2[j] == 0. ? EPS : yValuesForLin2[j] * (1. + EPS);
      yValuesForLin1Dw[j] = yValuesForLin1[j] == 0. ? EPS : yValuesForLin1[j] * (1. - EPS);
      yValuesForLin2Dw[j] = yValuesForLin2[j] == 0. ? EPS : yValuesForLin2[j] * (1. - EPS);
      Interpolator1DDataBundle dataBund1Up = INTERP1D.getDataBundleFromSortedArrays(xValuesForLin, yValuesForLin1Up);
      Interpolator1DDataBundle dataBund2Up = INTERP1D.getDataBundleFromSortedArrays(xValuesForLin, yValuesForLin2Up);
      Interpolator1DDataBundle dataBund1Dw = INTERP1D.getDataBundleFromSortedArrays(xValuesForLin, yValuesForLin1Dw);
      Interpolator1DDataBundle dataBund2Dw = INTERP1D.getDataBundleFromSortedArrays(xValuesForLin, yValuesForLin2Dw);
      for (int i = 0; i < keyLength; ++i) {
        final double ref1 = yValuesForLin1[j] == 0. ? EPS : yValuesForLin1[j] * EPS;
        final double ref2 = yValuesForLin2[j] == 0. ? EPS : yValuesForLin2[j] * EPS;
        double res1 = 0.5 * (INTERP1D.interpolate(dataBund1Up, xKeys[i]) - INTERP1D.interpolate(dataBund1Dw, xKeys[i])) / ref1;
        double res2 = 0.5 * (INTERP1D.interpolate(dataBund2Up, xKeys[i]) - INTERP1D.interpolate(dataBund2Dw, xKeys[i])) / ref2;
        assertEquals(res1, INTERP1D.getNodeSensitivitiesForValue(dataBundLin1, xKeys[i])[j], Math.max(Math.abs(yValuesForLin1[j]) * EPS, EPS));
        assertEquals(res2, INTERP1D.getNodeSensitivitiesForValue(dataBundLin2, xKeys[i])[j], Math.max(Math.abs(yValuesForLin2[j]) * EPS, EPS));
      }
      yValuesForLin1Up[j] = yValuesForLin1[j];
      yValuesForLin2Up[j] = yValuesForLin2[j];
      yValuesForLin1Dw[j] = yValuesForLin1[j];
      yValuesForLin2Dw[j] = yValuesForLin2[j];
    }

    /**
     * Quadratic Interpolation
     */

    Interpolator1DDataBundle dataBundQuad1 = INTERP1D.getDataBundleFromSortedArrays(xValuesForQuad, yValuesForQuad1);
    Interpolator1DDataBundle dataBundQuad2 = INTERP1D.getDataBundleFromSortedArrays(xValuesForQuad, yValuesForQuad2);

    for (int i = 0; i < keyLength; ++i) {
      final double val = interp.interpolate(xValuesForQuad, yValuesForQuad1, xKeys[i]);
      final double ref = val == 0. ? 1. : Math.abs(val);
      assertEquals(val, INTERP1D.interpolate(dataBundQuad1, xKeys[i]), ref * 1.e-15);
    }
    for (int i = 0; i < keyLength; ++i) {
      final double val = interp.interpolate(xValuesForQuad, yValuesForQuad2, xKeys[i]);
      final double ref = val == 0. ? 1. : Math.abs(val);
      assertEquals(val, INTERP1D.interpolate(dataBundQuad2, xKeys[i]), ref * 1.e-15);
    }

    for (int j = 0; j < 3; ++j) {
      yValuesForQuad1Up[j] = yValuesForQuad1[j] == 0. ? EPS : yValuesForQuad1[j] * (1. + EPS);
      yValuesForQuad2Up[j] = yValuesForQuad2[j] == 0. ? EPS : yValuesForQuad2[j] * (1. + EPS);
      yValuesForQuad1Dw[j] = yValuesForQuad1[j] == 0. ? EPS : yValuesForQuad1[j] * (1. - EPS);
      yValuesForQuad2Dw[j] = yValuesForQuad2[j] == 0. ? EPS : yValuesForQuad2[j] * (1. - EPS);
      Interpolator1DDataBundle dataBund1Up = INTERP1D.getDataBundleFromSortedArrays(xValuesForQuad, yValuesForQuad1Up);
      Interpolator1DDataBundle dataBund2Up = INTERP1D.getDataBundleFromSortedArrays(xValuesForQuad, yValuesForQuad2Up);
      Interpolator1DDataBundle dataBund1Dw = INTERP1D.getDataBundleFromSortedArrays(xValuesForQuad, yValuesForQuad1Dw);
      Interpolator1DDataBundle dataBund2Dw = INTERP1D.getDataBundleFromSortedArrays(xValuesForQuad, yValuesForQuad2Dw);
      for (int i = 0; i < keyLength; ++i) {
        final double ref1 = yValuesForQuad1[j] == 0. ? EPS : yValuesForQuad1[j] * EPS;
        final double ref2 = yValuesForQuad2[j] == 0. ? EPS : yValuesForQuad2[j] * EPS;
        double res1 = 0.5 * (INTERP1D.interpolate(dataBund1Up, xKeys[i]) - INTERP1D.interpolate(dataBund1Dw, xKeys[i])) / ref1;
        double res2 = 0.5 * (INTERP1D.interpolate(dataBund2Up, xKeys[i]) - INTERP1D.interpolate(dataBund2Dw, xKeys[i])) / ref2;
        assertEquals(res1, INTERP1D.getNodeSensitivitiesForValue(dataBundQuad1, xKeys[i])[j], Math.max(Math.abs(yValuesForQuad1[j]) * EPS, EPS));
        assertEquals(res2, INTERP1D.getNodeSensitivitiesForValue(dataBundQuad2, xKeys[i])[j], Math.max(Math.abs(yValuesForQuad2[j]) * EPS, EPS));
      }
      yValuesForQuad1Up[j] = yValuesForQuad1[j];
      yValuesForQuad2Up[j] = yValuesForQuad2[j];
      yValuesForQuad1Dw[j] = yValuesForQuad1[j];
      yValuesForQuad2Dw[j] = yValuesForQuad2[j];
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
