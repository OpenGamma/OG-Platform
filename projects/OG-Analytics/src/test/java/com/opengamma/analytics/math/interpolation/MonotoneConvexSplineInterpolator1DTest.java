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
public class MonotoneConvexSplineInterpolator1DTest {

  private static final MonotoneConvexSplineInterpolator INTERP = new MonotoneConvexSplineInterpolator();
  private static final MonotoneConvexSplineInterpolator1D INTERP1D = new MonotoneConvexSplineInterpolator1D();

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
      xValues[i] = i * i + i - 1.;
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
   * 
   */
  @Test
  public void zeroValuetest() {
    final int nData = 10;
    final double[] xValues = new double[nData];
    double[] yValues1 = new double[nData];
    double[] yValues2 = new double[nData];
    final double[] yValues3 = new double[nData];
    final double[] yValues1Up = new double[nData];
    final double[] yValues2Up = new double[nData];
    final double[] yValues3Up = new double[nData];
    final double[] yValues1Dw = new double[nData];
    final double[] yValues2Dw = new double[nData];
    final double[] yValues3Dw = new double[nData];
    final double[] xKeys = new double[10 * nData];

    yValues1 = new double[] {1.0, 0.0, 1.0, 2.0, 1.0, 3.0, 0.0, 0.0, 0.0, 3.0 };
    yValues2 = new double[] {-1.0, 0.0, -1.0, 2.0, -1.0, 3.0, 0.0, 0.0, 0.0, -3.0 };
    for (int i = 0; i < nData; ++i) {
      xValues[i] = i + 1;
      yValues3[i] = (-2. * xValues[i] * xValues[i] + xValues[i]) / (xValues[i] * xValues[i] + xValues[i] * xValues[i] * xValues[i] + 5. * xValues[i] + 2.);
      yValues1Up[i] = yValues1[i];
      yValues1Dw[i] = yValues1[i];
      yValues2Up[i] = yValues2[i];
      yValues2Dw[i] = yValues2[i];
      yValues3Up[i] = yValues3[i];
      yValues3Dw[i] = yValues3[i];
    }

    final double xMin = xValues[0];
    final double xMax = xValues[nData - 1];
    for (int i = 0; i < 10 * nData; ++i) {
      xKeys[i] = xMin + (xMax - xMin) / (10 * nData - 1) * i;
    }

    final MonotoneConvexSplineInterpolator[] bareInterp = new MonotoneConvexSplineInterpolator[] {INTERP };
    final MonotoneConvexSplineInterpolator1D[] wrappedInterp = new MonotoneConvexSplineInterpolator1D[] {INTERP1D };
    final int nMethods = bareInterp.length;

    for (int k = 0; k < nMethods; ++k) {
      final double[] resPrim1 = bareInterp[k].interpolate(xValues, yValues1, xKeys).getData();
      final double[] resPrim2 = bareInterp[k].interpolate(xValues, yValues2, xKeys).getData();
      final double[] resPrim3 = bareInterp[k].interpolate(xValues, yValues3, xKeys).getData();

      Interpolator1DDataBundle dataBund1 = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues1);
      Interpolator1DDataBundle dataBund2 = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues2);
      Interpolator1DDataBundle dataBund3 = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues3);
      for (int i = 0; i < 10 * nData; ++i) {
        final double ref1 = resPrim1[i];
        final double ref2 = resPrim2[i];
        final double ref3 = resPrim3[i];
        assertEquals(ref1, wrappedInterp[k].interpolate(dataBund1, xKeys[i]), 1.e-14 * Math.max(Math.abs(ref1), 1.));
        assertEquals(ref2, wrappedInterp[k].interpolate(dataBund2, xKeys[i]), 1.e-14 * Math.max(Math.abs(ref2), 1.));
        assertEquals(ref3, wrappedInterp[k].interpolate(dataBund3, xKeys[i]), 1.e-14 * Math.max(Math.abs(ref3), 1.));
      }

      for (int j = 0; j < nData; ++j) {
        final double den1 = Math.abs(yValues1[j]) == 0. ? EPS : yValues1[j] * EPS;
        final double den2 = Math.abs(yValues2[j]) == 0. ? EPS : yValues2[j] * EPS;
        final double den3 = Math.abs(yValues3[j]) == 0. ? EPS : yValues3[j] * EPS;
        yValues1Up[j] = Math.abs(yValues1[j]) == 0. ? EPS : yValues1[j] * (1. + EPS);
        yValues1Dw[j] = Math.abs(yValues1[j]) == 0. ? -EPS : yValues1[j] * (1. - EPS);
        yValues2Up[j] = Math.abs(yValues2[j]) == 0. ? EPS : yValues2[j] * (1. + EPS);
        yValues2Dw[j] = Math.abs(yValues2[j]) == 0. ? -EPS : yValues2[j] * (1. - EPS);
        yValues3Up[j] = Math.abs(yValues3[j]) == 0. ? EPS : yValues3[j] * (1. + EPS);
        yValues3Dw[j] = Math.abs(yValues3[j]) == 0. ? -EPS : yValues3[j] * (1. - EPS);
        Interpolator1DDataBundle dataBund1Up = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues1Up);
        Interpolator1DDataBundle dataBund2Up = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues2Up);
        Interpolator1DDataBundle dataBund3Up = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues3Up);
        Interpolator1DDataBundle dataBund1Dw = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues1Dw);
        Interpolator1DDataBundle dataBund2Dw = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues2Dw);
        Interpolator1DDataBundle dataBund3Dw = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues3Dw);
        for (int i = 0; i < 10 * nData; ++i) {
          double res1 = 0.5 * (wrappedInterp[k].interpolate(dataBund1Up, xKeys[i]) - wrappedInterp[k].interpolate(dataBund1Dw, xKeys[i])) / den1;
          double res2 = 0.5 * (wrappedInterp[k].interpolate(dataBund2Up, xKeys[i]) - wrappedInterp[k].interpolate(dataBund2Dw, xKeys[i])) / den2;
          double res3 = 0.5 * (wrappedInterp[k].interpolate(dataBund3Up, xKeys[i]) - wrappedInterp[k].interpolate(dataBund3Dw, xKeys[i])) / den3;
          assertEquals(res1, wrappedInterp[k].getNodeSensitivitiesForValue(dataBund1, xKeys[i])[j], Math.max(Math.abs(yValues1[j]) * EPS, EPS) * 10.);
          assertEquals(res2, wrappedInterp[k].getNodeSensitivitiesForValue(dataBund2, xKeys[i])[j], Math.max(Math.abs(yValues2[j]) * EPS, EPS) * 10.);
          assertEquals(res3, wrappedInterp[k].getNodeSensitivitiesForValue(dataBund3, xKeys[i])[j], Math.max(Math.abs(yValues3[j]) * EPS, EPS) * 10.);
        }
        yValues1Up[j] = yValues1[j];
        yValues1Dw[j] = yValues1[j];
        yValues2Up[j] = yValues2[j];
        yValues2Dw[j] = yValues2[j];
        yValues3Up[j] = yValues3[j];
        yValues3Dw[j] = yValues3[j];
      }
    }
  }

  /**
   * 
   */
  @Test
  public void linearDataTest() {
    final double[] xValues = new double[] {1., 2., 3., 4., 5., 6., 7., 8. };
    final double[][] yValues = new double[][] { {1., 3., 5., 7., 9., 11., 13., 15. }, {1., 1., 1., 1., 1., 1., 1., 1. }, {1., -1., -3., -5., -7., -9., -11., -13. } };
    final int nData = xValues.length;
    final int nDim = yValues.length;
    for (int k = 0; k < nDim; ++k) {
      double[] yValuesUp = Arrays.copyOf(yValues[k], nData);
      double[] yValuesDw = Arrays.copyOf(yValues[k], nData);
      final double[] xKeys = new double[10 * nData];
      final double xMin = xValues[0];
      final double xMax = xValues[nData - 1];
      for (int i = 0; i < 10 * nData; ++i) {
        xKeys[i] = xMin + (xMax - xMin) / (10 * nData - 1) * i;
      }

      Interpolator1DDataBundle dataBund = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues[k]);

      for (int j = 0; j < nData; ++j) {
        yValuesUp[j] = yValues[k][j] * (1. + EPS);
        yValuesDw[j] = yValues[k][j] * (1. - EPS);
        Interpolator1DDataBundle dataBundUp = INTERP1D.getDataBundle(xValues, yValuesUp);
        Interpolator1DDataBundle dataBundDw = INTERP1D.getDataBundle(xValues, yValuesDw);
        for (int i = 0; i < 10 * nData; ++i) {
          double res0 = 0.5 * (INTERP1D.interpolate(dataBundUp, xKeys[i]) - INTERP1D.interpolate(dataBundDw, xKeys[i])) / EPS / yValues[k][j];
          assertEquals(res0, INTERP1D.getNodeSensitivitiesForValue(dataBund, xKeys[i])[j], Math.max(Math.abs(yValues[k][j]) * EPS, EPS));
        }
        yValuesUp[j] = yValues[k][j];
        yValuesDw[j] = yValues[k][j];
      }
    }
  }

  /**
   * 
   */
  @Test
  public void monotoneDataTest() {
    final double[] xValues = new double[] {1., 2., 3., 4., 5., 6., 7., 8. };
    final double[][] yValues = new double[][] { {-1., 3.5, 5.0, 7.1, 9.9, 14., 33. / 2.1, 17. }, {11., 9.9, 9.8, 9.8, 9.8, -1.1, -1.1, -1.1 } };
    final int nData = xValues.length;
    final int nDim = yValues.length;
    for (int k = 0; k < nDim; ++k) {
      double[] yValuesUp = Arrays.copyOf(yValues[k], nData);
      double[] yValuesDw = Arrays.copyOf(yValues[k], nData);
      final double[] xKeys = new double[10 * nData];
      final double xMin = xValues[0];
      final double xMax = xValues[nData - 1];
      for (int i = 0; i < 10 * nData; ++i) {
        xKeys[i] = xMin + (xMax - xMin) / (10 * nData - 1) * i;
      }

      Interpolator1DDataBundle dataBund = INTERP1D.getDataBundleFromSortedArrays(xValues, yValues[k]);

      for (int j = 0; j < nData; ++j) {
        yValuesUp[j] = yValues[k][j] * (1. + EPS);
        yValuesDw[j] = yValues[k][j] * (1. - EPS);
        Interpolator1DDataBundle dataBundUp = INTERP1D.getDataBundle(xValues, yValuesUp);
        Interpolator1DDataBundle dataBundDw = INTERP1D.getDataBundle(xValues, yValuesDw);
        for (int i = 0; i < 10 * nData; ++i) {
          double res0 = 0.5 * (INTERP1D.interpolate(dataBundUp, xKeys[i]) - INTERP1D.interpolate(dataBundDw, xKeys[i])) / EPS / yValues[k][j];
          assertEquals(res0, INTERP1D.getNodeSensitivitiesForValue(dataBund, xKeys[i])[j], Math.max(Math.abs(yValues[k][j]) * EPS, EPS));
        }
        yValuesUp[j] = yValues[k][j];
        yValuesDw[j] = yValues[k][j];
      }
    }
  }
}
