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
public class MonotonicityPreservingCubicSplineInterpolator1DTest {

  private static final MonotonicityPreservingCubicSplineInterpolator INTERP_NAT = new MonotonicityPreservingCubicSplineInterpolator(new NaturalSplineInterpolator());
  private static final MonotonicityPreservingCubicSplineInterpolator1D INTERP1D_NAT = new MonotonicityPreservingCubicSplineInterpolator1D();
  private static final MonotonicityPreservingCubicSplineInterpolator INTERP_NAK = new MonotonicityPreservingCubicSplineInterpolator(new CubicSplineInterpolator());
  private static final MonotonicityPreservingCubicSplineInterpolator1D INTERP1D_NAK = new MonotonicityPreservingCubicSplineInterpolator1D(new CubicSplineInterpolator());
  private static final MonotonicityPreservingCubicSplineInterpolator INTERP_AKIMA = new MonotonicityPreservingCubicSplineInterpolator(new SemiLocalCubicSplineInterpolator());
  private static final MonotonicityPreservingCubicSplineInterpolator1D INTERP1D_AKIMA = new MonotonicityPreservingCubicSplineInterpolator1D(new SemiLocalCubicSplineInterpolator());
  private static final MonotonicityPreservingCubicSplineInterpolator INTERP_CONSTRAINED = new MonotonicityPreservingCubicSplineInterpolator(new ConstrainedCubicSplineInterpolator());
  private static final MonotonicityPreservingCubicSplineInterpolator1D INTERP1D_CONSTRAINED = new MonotonicityPreservingCubicSplineInterpolator1D(new ConstrainedCubicSplineInterpolator());

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
    final double[] yValues1Clamped = new double[nData + 2];
    final double[] yValues2Clamped = new double[nData + 2];
    final double[] yValues3Clamped = new double[nData + 2];
    Arrays.fill(yValues1Clamped, 0.);
    Arrays.fill(yValues2Clamped, 0.);
    Arrays.fill(yValues3Clamped, 0.);
    final double[] yValues1Up = new double[nData];
    final double[] yValues2Up = new double[nData];
    final double[] yValues1Dw = new double[nData];
    final double[] yValues2Dw = new double[nData];
    final double[] yValues3Up = new double[nData];
    final double[] yValues3Dw = new double[nData];
    final double[] yValues4Up = new double[nData];
    final double[] yValues4Dw = new double[nData];
    final double[] yValues5Up = new double[nData];
    final double[] yValues5Dw = new double[nData];
    final double[] xKeys = new double[10 * nData];

    final double[] yValues4 = new double[] {1.0, 2.0, 2.0, 2.0, 1.0, 3.0, 1.0, 3.0, 1.0, 4.0 };
    final double[] yValues5 = new double[] {1.0, 2.0, 1.0, 2.0, 1.0, 3.0, 1.0, 3.0, 1.0, 3.0 };
    for (int i = 0; i < nData; ++i) {
      xValues[i] = i + 1;
      yValues1[i] = 0.5 * xValues[i] * xValues[i] * xValues[i] - 1.5 * xValues[i] * xValues[i] + xValues[i] - 2.;
      yValues2[i] = Math.exp(0.1 * xValues[i] - 6.);
      yValues3[i] = (2. * xValues[i] * xValues[i] + xValues[i]) / (xValues[i] * xValues[i] + xValues[i] * xValues[i] * xValues[i] + 5. * xValues[i] + 2.);
      yValues1Clamped[i + 1] = yValues1[i];
      yValues2Clamped[i + 1] = yValues2[i];
      yValues3Clamped[i + 1] = yValues3[i];
      yValues1Up[i] = yValues1[i];
      yValues2Up[i] = yValues2[i];
      yValues1Dw[i] = yValues1[i];
      yValues2Dw[i] = yValues2[i];
      yValues3Up[i] = yValues3[i];
      yValues3Dw[i] = yValues3[i];
      yValues4Up[i] = yValues4[i];
      yValues4Dw[i] = yValues4[i];
      yValues5Up[i] = yValues5[i];
      yValues5Dw[i] = yValues5[i];
    }

    final double xMin = xValues[0];
    final double xMax = xValues[nData - 1];
    for (int i = 0; i < 10 * nData; ++i) {
      xKeys[i] = xMin + (xMax - xMin) / (10 * nData - 1) * i;
    }

    final MonotonicityPreservingCubicSplineInterpolator[] bareInterp = new MonotonicityPreservingCubicSplineInterpolator[] {INTERP_NAT, INTERP_NAK, INTERP_AKIMA };
    final MonotonicityPreservingCubicSplineInterpolator1D[] wrappedInterp = new MonotonicityPreservingCubicSplineInterpolator1D[] {INTERP1D_NAT, INTERP1D_NAK, INTERP1D_AKIMA };
    final int nMethods = bareInterp.length;

    for (int k = 0; k < nMethods; ++k) {
      final double[] resPrim1 = bareInterp[k].interpolate(xValues, yValues1, xKeys).getData();
      final double[] resPrim2 = bareInterp[k].interpolate(xValues, yValues2, xKeys).getData();
      final double[] resPrim3 = bareInterp[k].interpolate(xValues, yValues3, xKeys).getData();
      final double[] resPrim4 = bareInterp[k].interpolate(xValues, yValues4, xKeys).getData();
      final double[] resPrim5 = bareInterp[k].interpolate(xValues, yValues5, xKeys).getData();

      Interpolator1DDataBundle dataBund1 = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues1);
      Interpolator1DDataBundle dataBund2 = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues2);
      Interpolator1DDataBundle dataBund3 = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues3);
      Interpolator1DDataBundle dataBund4 = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues4);
      Interpolator1DDataBundle dataBund5 = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues5);
      for (int i = 0; i < 10 * nData; ++i) {
        final double ref1 = resPrim1[i];
        final double ref2 = resPrim2[i];
        final double ref3 = resPrim3[i];
        final double ref4 = resPrim4[i];
        final double ref5 = resPrim5[i];
        assertEquals(ref1, wrappedInterp[k].interpolate(dataBund1, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref1), 1.));
        assertEquals(ref2, wrappedInterp[k].interpolate(dataBund2, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref2), 1.));
        assertEquals(ref3, wrappedInterp[k].interpolate(dataBund3, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref3), 1.));
        assertEquals(ref4, wrappedInterp[k].interpolate(dataBund4, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref4), 1.));
        assertEquals(ref5, wrappedInterp[k].interpolate(dataBund5, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref5), 1.));
      }

      for (int j = 0; j < nData; ++j) {
        //      for (int j = 1; j < nData; ++j) {
        yValues1Up[j] = yValues1[j] * (1. + EPS);
        yValues2Up[j] = yValues2[j] * (1. + EPS);
        yValues3Up[j] = yValues3[j] * (1. + EPS);
        yValues1Dw[j] = yValues1[j] * (1. - EPS);
        yValues2Dw[j] = yValues2[j] * (1. - EPS);
        yValues3Up[j] = yValues3[j] * (1. + EPS);
        yValues3Dw[j] = yValues3[j] * (1. - EPS);
        yValues4Up[j] = yValues4[j] * (1. + EPS);
        yValues4Dw[j] = yValues4[j] * (1. - EPS);
        yValues5Up[j] = yValues5[j] * (1. + EPS);
        yValues5Dw[j] = yValues5[j] * (1. - EPS);
        Interpolator1DDataBundle dataBund1Up = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues1Up);
        Interpolator1DDataBundle dataBund2Up = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues2Up);
        Interpolator1DDataBundle dataBund1Dw = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues1Dw);
        Interpolator1DDataBundle dataBund2Dw = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues2Dw);
        Interpolator1DDataBundle dataBund3Up = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues3Up);
        Interpolator1DDataBundle dataBund3Dw = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues3Dw);
        Interpolator1DDataBundle dataBund4Up = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues4Up);
        Interpolator1DDataBundle dataBund4Dw = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues4Dw);
        Interpolator1DDataBundle dataBund5Up = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues5Up);
        Interpolator1DDataBundle dataBund5Dw = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues5Dw);
        for (int i = 0; i < 10 * nData; ++i) {
          double res1 = 0.5 * (wrappedInterp[k].interpolate(dataBund1Up, xKeys[i]) - wrappedInterp[k].interpolate(dataBund1Dw, xKeys[i])) / EPS / yValues1[j];
          double res2 = 0.5 * (wrappedInterp[k].interpolate(dataBund2Up, xKeys[i]) - wrappedInterp[k].interpolate(dataBund2Dw, xKeys[i])) / EPS / yValues2[j];
          double res3 = 0.5 * (wrappedInterp[k].interpolate(dataBund3Up, xKeys[i]) - wrappedInterp[k].interpolate(dataBund3Dw, xKeys[i])) / EPS / yValues3[j];
          double res4 = 0.5 * (wrappedInterp[k].interpolate(dataBund4Up, xKeys[i]) - wrappedInterp[k].interpolate(dataBund4Dw, xKeys[i])) / EPS / yValues4[j];
          double res5 = 0.5 * (wrappedInterp[k].interpolate(dataBund5Up, xKeys[i]) - wrappedInterp[k].interpolate(dataBund5Dw, xKeys[i])) / EPS / yValues5[j];
          assertEquals(res1, wrappedInterp[k].getNodeSensitivitiesForValue(dataBund1, xKeys[i])[j], Math.max(Math.abs(yValues1[j]) * EPS, EPS) * 10.);
          assertEquals(res2, wrappedInterp[k].getNodeSensitivitiesForValue(dataBund2, xKeys[i])[j], Math.max(Math.abs(yValues2[j]) * EPS, EPS) * 10.);
          assertEquals(res3, wrappedInterp[k].getNodeSensitivitiesForValue(dataBund3, xKeys[i])[j], Math.max(Math.abs(yValues3[j]) * EPS, EPS) * 10.);
          assertEquals(res4, wrappedInterp[k].getNodeSensitivitiesForValue(dataBund4, xKeys[i])[j], Math.max(Math.abs(yValues4[j]) * EPS, EPS) * 10.);
          assertEquals(res5, wrappedInterp[k].getNodeSensitivitiesForValue(dataBund5, xKeys[i])[j], Math.max(Math.abs(yValues5[j]) * EPS, EPS) * 10.);
        }
        yValues1Up[j] = yValues1[j];
        yValues2Up[j] = yValues2[j];
        yValues1Dw[j] = yValues1[j];
        yValues2Dw[j] = yValues2[j];
        yValues3Up[j] = yValues3[j];
        yValues3Dw[j] = yValues3[j];
        yValues4Up[j] = yValues4[j];
        yValues4Dw[j] = yValues4[j];
        yValues5Up[j] = yValues5[j];
        yValues5Dw[j] = yValues5[j];
      }
    }

  }

  /**
   * Primary interpolator is cubic spline with clamped endpoint condition
   */
  @Test
  public void clampedTest() {
    final int nData = 10;
    final double[] xValues = new double[nData];
    final double[] yValues1 = new double[nData];
    final double[] yValues2 = new double[nData];
    final double[] yValues3 = new double[nData];
    final double[] yValues1Clamped = new double[nData + 2];
    final double[] yValues2Clamped = new double[nData + 2];
    final double[] yValues3Clamped = new double[nData + 2];
    Arrays.fill(yValues1Clamped, 0.);
    Arrays.fill(yValues2Clamped, 0.);
    Arrays.fill(yValues3Clamped, 0.);
    final double[] yValues1Up = new double[nData];
    final double[] yValues2Up = new double[nData];
    final double[] yValues1Dw = new double[nData];
    final double[] yValues2Dw = new double[nData];
    final double[] yValues3Up = new double[nData];
    final double[] yValues3Dw = new double[nData];
    final double[] yValues4Up = new double[nData];
    final double[] yValues4Dw = new double[nData];
    final double[] yValues5Up = new double[nData];
    final double[] yValues5Dw = new double[nData];
    final double[] xKeys = new double[10 * nData];

    final double[] yValues4 = new double[] {1.0, 2.0, 2.0, 2.0, 1.0, 3.0, 1.0, 3.0, 1.0, 4.0 };
    final double[] yValues5 = new double[] {1.0, 2.0, 1.0, 2.0, 1.0, 3.0, 1.0, 3.0, 1.0, 3.0 };
    final double[] yValues4Clamped = new double[] {0., 1.0, 2.0, 2.0, 2.0, 1.0, 3.0, 1.0, 3.0, 1.0, 4.0, 0. };
    final double[] yValues5Clamped = new double[] {0., 1.0, 2.0, 1.0, 2.0, 1.0, 3.0, 1.0, 3.0, 1.0, 3.0, 0. };
    for (int i = 0; i < nData; ++i) {
      xValues[i] = i + 1;
      yValues1[i] = 0.5 * xValues[i] * xValues[i] * xValues[i] - 1.5 * xValues[i] * xValues[i] + xValues[i] - 2.;
      yValues2[i] = Math.exp(0.1 * xValues[i] - 6.);
      yValues3[i] = (2. * xValues[i] * xValues[i] + xValues[i]) / (xValues[i] * xValues[i] + xValues[i] * xValues[i] * xValues[i] + 5. * xValues[i] + 2.);
      yValues1Clamped[i + 1] = yValues1[i];
      yValues2Clamped[i + 1] = yValues2[i];
      yValues3Clamped[i + 1] = yValues3[i];
      yValues1Up[i] = yValues1[i];
      yValues2Up[i] = yValues2[i];
      yValues1Dw[i] = yValues1[i];
      yValues2Dw[i] = yValues2[i];
      yValues3Up[i] = yValues3[i];
      yValues3Dw[i] = yValues3[i];
      yValues4Up[i] = yValues4[i];
      yValues4Dw[i] = yValues4[i];
      yValues5Up[i] = yValues5[i];
      yValues5Dw[i] = yValues5[i];
    }

    final double xMin = xValues[0];
    final double xMax = xValues[nData - 1];
    for (int i = 0; i < 10 * nData; ++i) {
      xKeys[i] = xMin + (xMax - xMin) / (10 * nData - 1) * i;
    }

    final double[] bdConds = new double[] {-1., -0.1, 0., 1. / 3., 0.9 };
    final int nConds = bdConds.length;
    final MonotonicityPreservingCubicSplineInterpolator bare = new MonotonicityPreservingCubicSplineInterpolator(new CubicSplineInterpolator());
    final MonotonicityPreservingCubicSplineInterpolator1D wrap = new MonotonicityPreservingCubicSplineInterpolator1D(new CubicSplineInterpolator());
    for (int l = 0; l < nConds; ++l) {
      for (int m = 0; m < nConds; ++m) {
        yValues1Clamped[0] = bdConds[l];
        yValues1Clamped[nData + 1] = bdConds[m];
        yValues2Clamped[0] = bdConds[l];
        yValues2Clamped[nData + 1] = bdConds[m];
        yValues3Clamped[0] = bdConds[l];
        yValues3Clamped[nData + 1] = bdConds[m];
        yValues4Clamped[0] = bdConds[l];
        yValues4Clamped[nData + 1] = bdConds[m];
        yValues5Clamped[0] = bdConds[l];
        yValues5Clamped[nData + 1] = bdConds[m];

        final double[] resPrim1 = bare.interpolate(xValues, yValues1Clamped, xKeys).getData();
        final double[] resPrim2 = bare.interpolate(xValues, yValues2Clamped, xKeys).getData();
        final double[] resPrim3 = bare.interpolate(xValues, yValues3Clamped, xKeys).getData();
        final double[] resPrim4 = bare.interpolate(xValues, yValues4Clamped, xKeys).getData();
        final double[] resPrim5 = bare.interpolate(xValues, yValues5Clamped, xKeys).getData();

        Interpolator1DDataBundle dataBund1 = wrap.getDataBundleFromSortedArrays(xValues, yValues1, bdConds[l], bdConds[m]);
        Interpolator1DDataBundle dataBund2 = wrap.getDataBundleFromSortedArrays(xValues, yValues2, bdConds[l], bdConds[m]);
        Interpolator1DDataBundle dataBund3 = wrap.getDataBundleFromSortedArrays(xValues, yValues3, bdConds[l], bdConds[m]);
        Interpolator1DDataBundle dataBund4 = wrap.getDataBundleFromSortedArrays(xValues, yValues4, bdConds[l], bdConds[m]);
        Interpolator1DDataBundle dataBund5 = wrap.getDataBundleFromSortedArrays(xValues, yValues5, bdConds[l], bdConds[m]);
        for (int i = 0; i < 10 * nData; ++i) {
          final double ref1 = resPrim1[i];
          final double ref2 = resPrim2[i];
          final double ref3 = resPrim3[i];
          final double ref4 = resPrim4[i];
          final double ref5 = resPrim5[i];
          assertEquals(ref1, wrap.interpolate(dataBund1, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref1), 1.));
          assertEquals(ref2, wrap.interpolate(dataBund2, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref2), 1.));
          assertEquals(ref3, wrap.interpolate(dataBund3, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref3), 1.));
          assertEquals(ref4, wrap.interpolate(dataBund4, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref4), 1.));
          assertEquals(ref5, wrap.interpolate(dataBund5, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref5), 1.));
        }

        for (int j = 0; j < nData; ++j) {
          yValues1Up[j] = yValues1[j] * (1. + EPS);
          yValues2Up[j] = yValues2[j] * (1. + EPS);
          yValues3Up[j] = yValues3[j] * (1. + EPS);
          yValues1Dw[j] = yValues1[j] * (1. - EPS);
          yValues2Dw[j] = yValues2[j] * (1. - EPS);
          yValues3Up[j] = yValues3[j] * (1. + EPS);
          yValues3Dw[j] = yValues3[j] * (1. - EPS);
          yValues4Up[j] = yValues4[j] * (1. + EPS);
          yValues4Dw[j] = yValues4[j] * (1. - EPS);
          yValues5Up[j] = yValues5[j] * (1. + EPS);
          yValues5Dw[j] = yValues5[j] * (1. - EPS);
          Interpolator1DDataBundle dataBund1Up = wrap.getDataBundleFromSortedArrays(xValues, yValues1Up, bdConds[l], bdConds[m]);
          Interpolator1DDataBundle dataBund2Up = wrap.getDataBundleFromSortedArrays(xValues, yValues2Up, bdConds[l], bdConds[m]);
          Interpolator1DDataBundle dataBund1Dw = wrap.getDataBundleFromSortedArrays(xValues, yValues1Dw, bdConds[l], bdConds[m]);
          Interpolator1DDataBundle dataBund2Dw = wrap.getDataBundleFromSortedArrays(xValues, yValues2Dw, bdConds[l], bdConds[m]);
          Interpolator1DDataBundle dataBund3Up = wrap.getDataBundleFromSortedArrays(xValues, yValues3Up, bdConds[l], bdConds[m]);
          Interpolator1DDataBundle dataBund3Dw = wrap.getDataBundleFromSortedArrays(xValues, yValues3Dw, bdConds[l], bdConds[m]);
          Interpolator1DDataBundle dataBund4Up = wrap.getDataBundleFromSortedArrays(xValues, yValues4Up, bdConds[l], bdConds[m]);
          Interpolator1DDataBundle dataBund4Dw = wrap.getDataBundleFromSortedArrays(xValues, yValues4Dw, bdConds[l], bdConds[m]);
          Interpolator1DDataBundle dataBund5Up = wrap.getDataBundleFromSortedArrays(xValues, yValues5Up, bdConds[l], bdConds[m]);
          Interpolator1DDataBundle dataBund5Dw = wrap.getDataBundleFromSortedArrays(xValues, yValues5Dw, bdConds[l], bdConds[m]);
          for (int i = 0; i < 10 * nData; ++i) {
            double res1 = 0.5 * (wrap.interpolate(dataBund1Up, xKeys[i]) - wrap.interpolate(dataBund1Dw, xKeys[i])) / EPS / yValues1[j];
            double res2 = 0.5 * (wrap.interpolate(dataBund2Up, xKeys[i]) - wrap.interpolate(dataBund2Dw, xKeys[i])) / EPS / yValues2[j];
            double res3 = 0.5 * (wrap.interpolate(dataBund3Up, xKeys[i]) - wrap.interpolate(dataBund3Dw, xKeys[i])) / EPS / yValues3[j];
            double res4 = 0.5 * (wrap.interpolate(dataBund4Up, xKeys[i]) - wrap.interpolate(dataBund4Dw, xKeys[i])) / EPS / yValues4[j];
            double res5 = 0.5 * (wrap.interpolate(dataBund5Up, xKeys[i]) - wrap.interpolate(dataBund5Dw, xKeys[i])) / EPS / yValues5[j];
            assertEquals(res1, wrap.getNodeSensitivitiesForValue(dataBund1, xKeys[i])[j], Math.max(Math.abs(yValues1[j]) * EPS, EPS) * 10.);
            assertEquals(res2, wrap.getNodeSensitivitiesForValue(dataBund2, xKeys[i])[j], Math.max(Math.abs(yValues2[j]) * EPS, EPS) * 10.);
            assertEquals(res3, wrap.getNodeSensitivitiesForValue(dataBund3, xKeys[i])[j], Math.max(Math.abs(yValues3[j]) * EPS, EPS) * 10.);
            assertEquals(res4, wrap.getNodeSensitivitiesForValue(dataBund4, xKeys[i])[j], Math.max(Math.abs(yValues4[j]) * EPS, EPS) * 10.);
            assertEquals(res5, wrap.getNodeSensitivitiesForValue(dataBund5, xKeys[i])[j], Math.max(Math.abs(yValues5[j]) * EPS, EPS) * 10.);
          }
          yValues1Up[j] = yValues1[j];
          yValues2Up[j] = yValues2[j];
          yValues1Dw[j] = yValues1[j];
          yValues2Dw[j] = yValues2[j];
          yValues3Up[j] = yValues3[j];
          yValues3Dw[j] = yValues3[j];
          yValues4Up[j] = yValues4[j];
          yValues4Dw[j] = yValues4[j];
          yValues5Up[j] = yValues5[j];
          yValues5Dw[j] = yValues5[j];
        }
      }
    }
  }

  /**
   * Data contain zero yValues
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

    final MonotonicityPreservingCubicSplineInterpolator[] bareInterp = new MonotonicityPreservingCubicSplineInterpolator[] {INTERP_NAT, INTERP_NAK, INTERP_AKIMA, INTERP_CONSTRAINED };
    final MonotonicityPreservingCubicSplineInterpolator1D[] wrappedInterp = new MonotonicityPreservingCubicSplineInterpolator1D[] {INTERP1D_NAT, INTERP1D_NAK, INTERP1D_AKIMA, INTERP1D_CONSTRAINED };
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
        assertEquals(ref1, wrappedInterp[k].interpolate(dataBund1, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref1), 1.));
        assertEquals(ref2, wrappedInterp[k].interpolate(dataBund2, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref2), 1.));
        assertEquals(ref3, wrappedInterp[k].interpolate(dataBund3, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref3), 1.));
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
   * Data are monotone
   */
  @Test
  public void MonotoneTest() {
    final int nData = 10;
    final double[] xValues = new double[nData];
    double[] yValues1 = new double[nData];
    double[] yValues2 = new double[nData];
    double[] yValues3 = new double[nData];
    final double[] yValues1Up = new double[nData];
    final double[] yValues2Up = new double[nData];
    final double[] yValues3Up = new double[nData];
    final double[] yValues1Dw = new double[nData];
    final double[] yValues2Dw = new double[nData];
    final double[] yValues3Dw = new double[nData];
    final double[] xKeys = new double[10 * nData];

    yValues1 = new double[] {-1.9, -1.3, 1.9, 1.95, 2.0, 3.0, 3.01, 3.02, 3.02, 3.02 };
    yValues2 = new double[] {11.02, 11.01, 11.001, 10.999, 10.9, 3.01, 3.01, 3.0, 3.0, -3.0 };
    yValues3 = new double[] {-1.1, -1.1, -1.1, -1.1, -1.1, -1.1, -1.1, -1.1, -1.1, -1.1 };
    for (int i = 0; i < nData; ++i) {
      xValues[i] = i + 1;
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

    final MonotonicityPreservingCubicSplineInterpolator[] bareInterp = new MonotonicityPreservingCubicSplineInterpolator[] {INTERP_NAT, INTERP_NAK, INTERP_AKIMA };
    final MonotonicityPreservingCubicSplineInterpolator1D[] wrappedInterp = new MonotonicityPreservingCubicSplineInterpolator1D[] {INTERP1D_NAT, INTERP1D_NAK, INTERP1D_AKIMA };
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
        assertEquals(ref1, wrappedInterp[k].interpolate(dataBund1, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref1), 1.));
        assertEquals(ref2, wrappedInterp[k].interpolate(dataBund2, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref2), 1.));
        assertEquals(ref3, wrappedInterp[k].interpolate(dataBund3, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref3), 1.));
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
   * Checking (sub-)branches
   */
  @Test
  public void branch1test() {
    final int nData = 10;
    final double[] xValues = new double[nData];
    double[][] yValues;
    final double[] xKeys = new double[10 * nData];

    yValues = new double[][] { {3.0, 1.0, 1.0, 2.0, 1.0, 0.0, 0.0, 3.0, 1.0, 1.0 }, {0.0, 3.0, 0.0, 0.0, 1.0, 3.0, 3.0, 1.0, 4.0, 3.0 }, {2.0, 2.0, 1.0, 1.0, 0.0, 3.0, 4.0, 4.0, 0.0, 2.0 },
        {2.0, 2.0, 1.0, 1.0, 1.0, 1.0, 1.0, 4.0, 0.0, 2.0 }, {8.0, 9.0, 0.0, 6.0, 1.0, 1.0, 2.0, 1.0, 0.0, 0.0 }, {4.0, 1.0, 0.0, 7.0, 7.0, 1.0, 9.0, 1.0, 1.0, 7.0 } };
    final int dim = yValues.length;

    for (int l = 0; l < dim; ++l) {

      final double[] yValuesUp = new double[nData];
      final double[] yValuesDw = new double[nData];
      for (int i = 0; i < nData; ++i) {
        xValues[i] = i + 1;
        yValuesUp[i] = yValues[l][i];
        yValuesDw[i] = yValues[l][i];
      }

      final double xMin = xValues[0];
      final double xMax = xValues[nData - 1];
      for (int i = 0; i < 10 * nData; ++i) {
        xKeys[i] = xMin + (xMax - xMin) / (10 * nData - 1) * i;
      }

      final MonotonicityPreservingCubicSplineInterpolator[] bareInterp = new MonotonicityPreservingCubicSplineInterpolator[] {INTERP_NAT, INTERP_NAK, INTERP_AKIMA };
      final MonotonicityPreservingCubicSplineInterpolator1D[] wrappedInterp = new MonotonicityPreservingCubicSplineInterpolator1D[] {INTERP1D_NAT, INTERP1D_NAK, INTERP1D_AKIMA };
      final int nMethods = bareInterp.length;

      for (int k = 0; k < nMethods; ++k) {
        final double[] resPrim1 = bareInterp[k].interpolate(xValues, yValues[l], xKeys).getData();

        Interpolator1DDataBundle dataBund1 = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues[l]);
        for (int i = 0; i < 10 * nData; ++i) {
          final double ref1 = resPrim1[i];
          assertEquals(ref1, wrappedInterp[k].interpolate(dataBund1, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref1), 1.));
        }

        for (int j = 0; j < nData; ++j) {
          final double den1 = Math.abs(yValues[l][j]) == 0. ? EPS : yValues[l][j] * EPS;
          yValuesUp[j] = Math.abs(yValues[l][j]) == 0. ? EPS : yValues[l][j] * (1. + EPS);
          yValuesDw[j] = Math.abs(yValues[l][j]) == 0. ? -EPS : yValues[l][j] * (1. - EPS);
          Interpolator1DDataBundle dataBund1Up = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValuesUp);
          Interpolator1DDataBundle dataBund1Dw = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValuesDw);
          for (int i = 0; i < 10 * nData; ++i) {
            double res1 = 0.5 * (wrappedInterp[k].interpolate(dataBund1Up, xKeys[i]) - wrappedInterp[k].interpolate(dataBund1Dw, xKeys[i])) / den1;
            assertEquals(res1, wrappedInterp[k].getNodeSensitivitiesForValue(dataBund1, xKeys[i])[j], Math.max(Math.abs(yValues[l][j]) * EPS, EPS) * 10.);
          }
          yValuesUp[j] = yValues[l][j];
          yValuesDw[j] = yValues[l][j];
        }
      }

    }
  }

  /**
   * Another test for touching branches
   */
  @Test
  public void branch2Test() {
    final int nData = 10;
    double[] xValues;
    double[][] yValues;
    final double[] xKeys = new double[10 * nData];

    xValues = new double[] {1., 2., 3.5, 5.5, 8., 11., 13., 14., 16., 17. };
    yValues = new double[][] { {3.0, 3.0, 5.25, 1.25, 1.0, 2.0, 4.0, 3.0, 1.0, 1.0 }, {3.0, 6.0, 4.0, 5.0, 9.0, 3.0, 4.0, 8.0, 0.0, 2.0 }, {5.0, 8.0, 1.0, 5.0, 9.0, 3.0, 9.0, 0.0, 6.0, 7.0 } };
    final int dim = yValues.length;

    for (int l = 0; l < dim; ++l) {

      final double[] yValuesUp = new double[nData];
      final double[] yValuesDw = new double[nData];
      for (int i = 0; i < nData; ++i) {
        yValuesUp[i] = yValues[l][i];
        yValuesDw[i] = yValues[l][i];
      }

      final double xMin = xValues[0];
      final double xMax = xValues[nData - 1];
      for (int i = 0; i < 10 * nData; ++i) {
        xKeys[i] = xMin + (xMax - xMin) / (10 * nData - 1) * i;
      }

      final MonotonicityPreservingCubicSplineInterpolator[] bareInterp = new MonotonicityPreservingCubicSplineInterpolator[] {INTERP_NAT, INTERP_NAK, INTERP_AKIMA };
      final MonotonicityPreservingCubicSplineInterpolator1D[] wrappedInterp = new MonotonicityPreservingCubicSplineInterpolator1D[] {INTERP1D_NAT, INTERP1D_NAK, INTERP1D_AKIMA };
      final int nMethods = bareInterp.length;

      for (int k = 0; k < nMethods; ++k) {
        final double[] resPrim1 = bareInterp[k].interpolate(xValues, yValues[l], xKeys).getData();

        Interpolator1DDataBundle dataBund1 = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues[l]);
        for (int i = 0; i < 10 * nData; ++i) {
          final double ref1 = resPrim1[i];
          assertEquals(ref1, wrappedInterp[k].interpolate(dataBund1, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref1), 1.));
        }

        for (int j = 0; j < nData; ++j) {
          final double den1 = Math.abs(yValues[l][j]) == 0. ? EPS : yValues[l][j] * EPS;
          yValuesUp[j] = Math.abs(yValues[l][j]) == 0. ? EPS : yValues[l][j] * (1. + EPS);
          yValuesDw[j] = Math.abs(yValues[l][j]) == 0. ? -EPS : yValues[l][j] * (1. - EPS);
          Interpolator1DDataBundle dataBund1Up = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValuesUp);
          Interpolator1DDataBundle dataBund1Dw = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValuesDw);
          for (int i = 0; i < 10 * nData; ++i) {
            double res1 = 0.5 * (wrappedInterp[k].interpolate(dataBund1Up, xKeys[i]) - wrappedInterp[k].interpolate(dataBund1Dw, xKeys[i])) / den1;
            assertEquals(res1, wrappedInterp[k].getNodeSensitivitiesForValue(dataBund1, xKeys[i])[j], Math.max(Math.abs(yValues[l][j]) * EPS, EPS) * 10.);
          }
          yValuesUp[j] = yValues[l][j];
          yValuesDw[j] = yValues[l][j];
        }
      }

    }
  }

}
