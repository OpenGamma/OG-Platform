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

/**
 * 
 */
public class SemiLocalCubicSplineInterpolator1DTest {

  private static final SemiLocalCubicSplineInterpolator INTERP = new SemiLocalCubicSplineInterpolator();
  private static final SemiLocalCubicSplineInterpolator1D INTERP1D = new SemiLocalCubicSplineInterpolator1D();

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
      //      xValues[i] = i * i + i - 1.;
      xValues[i] = i + 1;
      yValues1[i] = 0.5 * xValues[i] * xValues[i] * xValues[i] - 1.5 * xValues[i] * xValues[i] + xValues[i] - 2.;
      yValues2[i] = Math.exp(0.1 * xValues[i] - 6.);
      yValues3[i] = (2. * xValues[i] * xValues[i] + xValues[i]) / (xValues[i] * xValues[i] + xValues[i] * xValues[i] * xValues[i] + 5. * xValues[i] + 2.);
      //      yValues1[i] = xValues[i];
      //      System.out.println(yValues1[i] + "\t" + yValues2[i] + "\t" + yValues3[i]);
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
        //        System.out.println(res1 + "\t" + INTERP1D.getNodeSensitivitiesForValue(dataBund1, xKeys[i])[j]);
        //        System.out.println(res2 + "\t" + INTERP1D.getNodeSensitivitiesForValue(dataBund2, xKeys[i])[j]);
        //        System.out.println(res3 + "\t" + INTERP1D.getNodeSensitivitiesForValue(dataBund3, xKeys[i])[j]);
        //        System.out.println(i + "\t" + j);
        //        INTERP1D.getNodeSensitivitiesForValue(dataBund1, xKeys[i]);
        assertEquals(res1, INTERP1D.getNodeSensitivitiesForValue(dataBund1, xKeys[i])[j], Math.max(Math.abs(yValues1[j]) * EPS, EPS));
        assertEquals(res2, INTERP1D.getNodeSensitivitiesForValue(dataBund2, xKeys[i])[j], Math.max(Math.abs(yValues2[j]) * EPS, EPS));
        assertEquals(res3, INTERP1D.getNodeSensitivitiesForValue(dataBund3, xKeys[i])[j], Math.max(Math.abs(yValues3[j]) * EPS, EPS));
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
   * Central finite difference approximation is not a good reference for this interpolation method
   * since first derivative value at s1=s2 \ne s3=s4 is not a limiting case of its general form
   */
  @Test
      (enabled = false)
      public void linearDataTest() {
    final double[] xValues = new double[] {1., 2., 3., 4., 5., 6., 7., 8. };
    //    final double[] yValues = new double[] {1., 3., 5., 7., 9., 11., 13., 15. };
    final double[] yValues = new double[] {1., 1., 1., 1., 1., 1., 1., 1. };
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

    for (int j = 1; j < nData; ++j) {
      yValuesUp[j] = yValues[j] * (1. + EPS * EPS);
      yValuesDw[j] = yValues[j] * (1. - EPS * EPS);
      Interpolator1DDataBundle dataBundUp = INTERP1D.getDataBundle(xValues, yValuesUp);
      Interpolator1DDataBundle dataBundDw = INTERP1D.getDataBundle(xValues, yValuesDw);
      for (int i = 0; i < 10 * nData; ++i) {
        double res0 = 0.5 * (INTERP1D.interpolate(dataBundUp, xKeys[i]) - INTERP1D.interpolate(dataBundDw, xKeys[i])) / EPS / EPS / yValues[j];
        double res1 = (INTERP1D.interpolate(dataBundUp, xKeys[i]) - INTERP1D.interpolate(dataBund, xKeys[i])) / EPS / EPS / yValues[j];
        double res2 = (INTERP1D.interpolate(dataBund, xKeys[i]) - INTERP1D.interpolate(dataBundDw, xKeys[i])) / EPS / EPS / yValues[j];
        System.out.println(res0 + "\t" + res1 + "\t" + res2 + "\t" + INTERP1D.getNodeSensitivitiesForValue(dataBund, xKeys[i])[j]);
        //        assertEquals(res0, INTERP1D.getNodeSensitivitiesForValue(dataBund, xKeys[i])[j], Math.max(Math.abs(yValues[j]) * EPS, EPS));
      }
      yValuesUp[j] = yValues[j];
      yValuesDw[j] = yValues[j];
      System.out.println("\n");
    }
  }
}
