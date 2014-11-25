/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.PiecewisePolynomialWithSensitivityFunction1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class ReciprocalExtrapolator1DTest {
  private static final PiecewisePolynomialInterpolator[] INTERP_SENSE;
  static {
    PiecewisePolynomialInterpolator cubic = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator natural = new NaturalSplineInterpolator();
    PiecewiseCubicHermiteSplineInterpolatorWithSensitivity pchip =
        new PiecewiseCubicHermiteSplineInterpolatorWithSensitivity();
    PiecewisePolynomialInterpolator hymanNat = new MonotonicityPreservingCubicSplineInterpolator(natural);
    INTERP_SENSE = new PiecewisePolynomialInterpolator[] {cubic, natural, pchip, hymanNat };
  }
  private static final PiecewisePolynomialWithSensitivityFunction1D FUNC =
      new PiecewisePolynomialWithSensitivityFunction1D();
  private static final double EPS = 1.0e-12;
  private static final double DELTA = 1.0e-6;

  /**
   * No clamped points added.
   * checking agreement with the extrapolation done by the underlying interpolation
   */
  @Test
  public void notClampedTest() {
    double[][] xValuesSet = new double[][] { {-5.0, -1.4, 3.2, 3.5, 7.6 }, {1., 2., 4.5, 12.1, 14.2 },
        {-5.2, -3.4, -3.2, -0.9, -0.2 } };
    double[][] yValuesSet = new double[][] { {-2.2, 1.1, 1.9, 2.3, -0.1 }, {3.4, 5.2, 4.3, 1.1, 0.2 },
        {1.4, 2.2, 4.1, 1.9, 0.99 } };

    for (int k = 0; k < xValuesSet.length; ++k) {
      double[] xValues = Arrays.copyOf(xValuesSet[k], xValuesSet[k].length);
      double[] yValues = Arrays.copyOf(yValuesSet[k], yValuesSet[k].length);
      int nData = xValues.length;
      int nKeys = 100;
      double interval = (xValues[2] - xValues[0]) / (nKeys - 1.0);

      int n = INTERP_SENSE.length;
      for (int i = 0; i < n; ++i) {
        ProductPiecewisePolynomialInterpolator interp = new ProductPiecewisePolynomialInterpolator(INTERP_SENSE[i]);
        PiecewisePolynomialResult result = interp.interpolateWithSensitivity(xValues, yValues);
        ReciprocalExtrapolator1D extrap1D = new ReciprocalExtrapolator1D(
            new ProductPiecewisePolynomialInterpolator1D(INTERP_SENSE[i]));
        Interpolator1DDataBundle data = extrap1D.getDataBundle(xValues, yValues);
        InterpolatorTestUtil.assertArrayRelative("notClampedTest", xValues, data.getKeys(), EPS);
        InterpolatorTestUtil.assertArrayRelative("notClampedTest", yValues, data.getValues(), EPS);
        /* left extrapolation */
        double grad = FUNC.differentiate(result, xValues[0]).getEntry(0);
        for (int j = 1; j < nKeys; ++j) {
          double key = xValues[0] - interval * j;
          double ref = grad * (key - xValues[0]) + yValues[0] * xValues[0];
          InterpolatorTestUtil.assertRelative("notClampedTest", ref / key, extrap1D.interpolate(data, key), EPS);
          double keyUp = key + DELTA;
          double keyDw = key - DELTA;
          double refDeriv = 0.5 * (extrap1D.interpolate(data, keyUp) - extrap1D.interpolate(data, keyDw)) / DELTA;
          InterpolatorTestUtil.assertRelative("notClampedTest", refDeriv,
              extrap1D.firstDerivative(data, key), DELTA);
          double[] refSense = new double[nData];
          for (int l = 0; l < nData; ++l) {
            double[] yValuesUp = Arrays.copyOf(yValues, nData);
            double[] yValuesDw = Arrays.copyOf(yValues, nData);
            yValuesUp[l] += DELTA;
            yValuesDw[l] -= DELTA;
            Interpolator1DDataBundle dataUp = extrap1D.getDataBundle(xValues, yValuesUp);
            Interpolator1DDataBundle dataDw = extrap1D.getDataBundle(xValues, yValuesDw);
            refSense[l] = 0.5 * (extrap1D.interpolate(dataUp, key) - extrap1D.interpolate(dataDw, key)) / DELTA;
          }
          InterpolatorTestUtil.assertArrayRelative("notClampedTest", extrap1D.getNodeSensitivitiesForValue(data, key),
              refSense, DELTA * 10.0);
        }
        /* right extrapolation */
        for (int j = 1; j < nKeys; ++j) {
          double key = xValues[nData - 1] + interval * j;
          InterpolatorTestUtil.assertRelative("notClampedTest", FUNC.evaluate(result, key).getEntry(0) / key,
              extrap1D.interpolate(data, key), EPS);
          double keyUp = key + DELTA;
          double keyDw = key - DELTA;
          double refDeriv = 0.5 * (extrap1D.interpolate(data, keyUp) - extrap1D.interpolate(data, keyDw)) / DELTA;
          InterpolatorTestUtil.assertRelative("notClampedTest", refDeriv,
              extrap1D.firstDerivative(data, key), DELTA);
          double[] refSense = new double[nData];
          for (int l = 0; l < nData; ++l) {
            double[] yValuesUp = Arrays.copyOf(yValues, nData);
            double[] yValuesDw = Arrays.copyOf(yValues, nData);
            yValuesUp[l] += DELTA;
            yValuesDw[l] -= DELTA;
            Interpolator1DDataBundle dataUp = extrap1D.getDataBundle(xValues, yValuesUp);
            Interpolator1DDataBundle dataDw = extrap1D.getDataBundle(xValues, yValuesDw);
            refSense[l] = 0.5 * (extrap1D.interpolate(dataUp, key) - extrap1D.interpolate(dataDw, key)) / DELTA;
          }
          InterpolatorTestUtil.assertArrayRelative("notClampedTest", extrap1D.getNodeSensitivitiesForValue(data, key),
              refSense, DELTA * 10.0);
        }
      }
    }
  }

  /**
   * Clamped points.
   * checking agreement with the extrapolation done by the underlying interpolation
   */
  @Test
  public void clampedTest() {
    double[] xValues = new double[] {-5.0, -1.4, 3.2, 3.5, 7.6 };
    double[] yValues = new double[] {-2.2, 1.1, 1.9, 2.3, -0.1 };
    double[][] xValuesClampedSet = new double[][] { {0.0 }, {-4.2, -2.5, 7.45 }, {} };
    double[][] yValuesClampedSet = new double[][] { {0.0 }, {-1.2, -1.4, 2.2 }, {} };

    for (int k = 0; k < xValuesClampedSet.length; ++k) {
      double[] xValuesClamped = Arrays.copyOf(xValuesClampedSet[k], xValuesClampedSet[k].length);
      double[] yValuesClamped = Arrays.copyOf(yValuesClampedSet[k], yValuesClampedSet[k].length);
      int nData = xValues.length;
      int nKeys = 100;
      double interval = (xValues[2] - xValues[0]) / (nKeys - 1.0);

      int n = INTERP_SENSE.length;
      for (int i = 0; i < n; ++i) {
        ProductPiecewisePolynomialInterpolator interp = new ProductPiecewisePolynomialInterpolator(INTERP_SENSE[i],
            xValuesClamped, yValuesClamped);
        PiecewisePolynomialResultsWithSensitivity result = interp.interpolateWithSensitivity(xValues, yValues);
        ReciprocalExtrapolator1D extrap1D = new ReciprocalExtrapolator1D(
            new ProductPiecewisePolynomialInterpolator1D(INTERP_SENSE[i], xValuesClamped, yValuesClamped));
        Interpolator1DDataBundle data = extrap1D.getDataBundleFromSortedArrays(xValues, yValues);
        InterpolatorTestUtil.assertArrayRelative("notClampedTest", xValues, data.getKeys(), EPS);
        InterpolatorTestUtil.assertArrayRelative("notClampedTest", yValues, data.getValues(), EPS);
        double grad = FUNC.differentiate(result, xValues[0]).getEntry(0);
        /* left extrapolation */
        for (int j = 1; j < nKeys; ++j) {
          double key = xValues[0] - interval * j;
          double ref = grad * (key - xValues[0]) + yValues[0] * xValues[0];
          InterpolatorTestUtil.assertRelative("notClampedTest", ref / key, extrap1D.interpolate(data, key), EPS);
          double keyUp = key + DELTA;
          double keyDw = key - DELTA;
          double refDeriv = 0.5 * (extrap1D.interpolate(data, keyUp) - extrap1D.interpolate(data, keyDw)) / DELTA;
          InterpolatorTestUtil.assertRelative("notClampedTest", refDeriv,
              extrap1D.firstDerivative(data, key), DELTA * 10.0);
          double[] refSense = new double[nData];
          for (int l = 0; l < nData; ++l) {
            double[] yValuesUp = Arrays.copyOf(yValues, nData);
            double[] yValuesDw = Arrays.copyOf(yValues, nData);
            yValuesUp[l] += DELTA;
            yValuesDw[l] -= DELTA;
            Interpolator1DDataBundle dataUp = extrap1D.getDataBundle(xValues, yValuesUp);
            Interpolator1DDataBundle dataDw = extrap1D.getDataBundle(xValues, yValuesDw);
            refSense[l] = 0.5 * (extrap1D.interpolate(dataUp, key) - extrap1D.interpolate(dataDw, key)) / DELTA;
          }
          InterpolatorTestUtil.assertArrayRelative("notClampedTest", extrap1D.getNodeSensitivitiesForValue(data, key),
              refSense, DELTA * 10.0);
        }
        /* right extrapolation */
        for (int j = 1; j < nKeys; ++j) {
          double key = xValues[nData - 1] + interval * j;
          InterpolatorTestUtil.assertRelative("notClampedTest " + k, FUNC.evaluate(result, key).getEntry(0) / key,
              extrap1D.interpolate(data, key), EPS);
          double keyUp = key + DELTA;
          double keyDw = key - DELTA;
          double refDeriv = 0.5 * (extrap1D.interpolate(data, keyUp) - extrap1D.interpolate(data, keyDw)) / DELTA;
          InterpolatorTestUtil.assertRelative("notClampedTest", refDeriv,
              extrap1D.firstDerivative(data, key), DELTA * 10.0);
          double[] refSense = new double[nData];
          for (int l = 0; l < nData; ++l) {
            double[] yValuesUp = Arrays.copyOf(yValues, nData);
            double[] yValuesDw = Arrays.copyOf(yValues, nData);
            yValuesUp[l] += DELTA;
            yValuesDw[l] -= DELTA;
            Interpolator1DDataBundle dataUp = extrap1D.getDataBundle(xValues, yValuesUp);
            Interpolator1DDataBundle dataDw = extrap1D.getDataBundle(xValues, yValuesDw);
            refSense[l] = 0.5 * (extrap1D.interpolate(dataUp, key) - extrap1D.interpolate(dataDw, key)) / DELTA;
          }
          InterpolatorTestUtil.assertArrayRelative("notClampedTest", extrap1D.getNodeSensitivitiesForValue(data, key),
              refSense, DELTA * 10.0);
        }
      }
    }
  }

  /**
   * Check Math.abs(value) < SMALL is smoothly connected to general cases
   */
  @Test
  public void closeToZeroTest() {
    double[] xValues = new double[] {0.1, 0.2, 0.3, 0.4 };
    double[] yValues = new double[] {0.1, 0.2, 0.3, 0.4 };
    int n = INTERP_SENSE.length;
    for (int i = 0; i < n; ++i) {
      ReciprocalExtrapolator1D extrap1D = new ReciprocalExtrapolator1D(
          new ProductPiecewisePolynomialInterpolator1D(INTERP_SENSE[i], new double[] {0.0 }, new double[] {0.0 }));
      Interpolator1DDataBundle data = extrap1D.getDataBundle(xValues, yValues);
      double eps = 1.0e-5;
      InterpolatorTestUtil.assertRelative("closeToZeroTest", extrap1D.interpolate(data, eps),
          extrap1D.interpolate(data, 0.0), eps);
      InterpolatorTestUtil.assertRelative("closeToZeroTest", extrap1D.firstDerivative(data, eps),
          extrap1D.firstDerivative(data, 0.0), eps);
    }
  }

  /**
   * value is within the data range
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void insideRangeTest() {
    double[] xValues = new double[] {2.4, 3.2, 3.5, 7.6 };
    double[] yValues = new double[] {1.1, 1.9, 2.3, -0.1 };
    ReciprocalExtrapolator1D extrap1D = new ReciprocalExtrapolator1D(
        new ProductPiecewisePolynomialInterpolator1D(INTERP_SENSE[1]));
    Interpolator1DDataBundle data = extrap1D.getDataBundle(xValues, yValues);
    extrap1D.interpolate(data, 5.2);
  }

  /**
   * value is within the data range
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void insideRangeDerivativeTest() {
    double[] xValues = new double[] {2.4, 3.2, 3.5, 7.6 };
    double[] yValues = new double[] {1.1, 1.9, 2.3, -0.1 };
    ReciprocalExtrapolator1D extrap1D = new ReciprocalExtrapolator1D(
        new ProductPiecewisePolynomialInterpolator1D(INTERP_SENSE[1]));
    Interpolator1DDataBundle data = extrap1D.getDataBundle(xValues, yValues);
    extrap1D.firstDerivative(data, 5.2);
  }

  /**
   * value is within the data range
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void insideRangeSenseTest() {
    double[] xValues = new double[] {2.4, 3.2, 3.5, 7.6 };
    double[] yValues = new double[] {1.1, 1.9, 2.3, -0.1 };
    ReciprocalExtrapolator1D extrap1D = new ReciprocalExtrapolator1D(
        new ProductPiecewisePolynomialInterpolator1D(INTERP_SENSE[1]));
    Interpolator1DDataBundle data = extrap1D.getDataBundle(xValues, yValues);
    extrap1D.getNodeSensitivitiesForValue(data, 5.2);
  }

  private static final double[] S_ARR = new double[] {1., 2., 3., 4. };
  private static final ProductPiecewisePolynomialInterpolator1D S_INTERP = new ProductPiecewisePolynomialInterpolator1D(
      INTERP_SENSE[0]);
  private static final ReciprocalExtrapolator1D S_EXTRAP = new ReciprocalExtrapolator1D(S_INTERP);
  private static final Interpolator1DDataBundle S_DATA = S_EXTRAP.getDataBundle(S_ARR, S_ARR);

  /**
   * interpolator is null
   */
  @SuppressWarnings("unused")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullInterpTest1() {
    new ReciprocalExtrapolator1D(null);
  }

  /**
   * data bundle is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDataInterpTest() {
    S_EXTRAP.interpolate(null, 5.0);
  }

  /**
   * Double value is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullValueInterpTest() {
    S_EXTRAP.interpolate(S_DATA, null);
  }

  /**
   * data bundle is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDataDerivTest() {
    S_EXTRAP.firstDerivative(null, 5.0);
  }

  /**
   * Double value is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullValueDerivTest() {
    S_EXTRAP.firstDerivative(S_DATA, null);
  }

  /**
   * data bundle is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDataSenseTest() {
    S_EXTRAP.getNodeSensitivitiesForValue(null, 5.0);
  }

  /**
   * Double value is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullValueSenseTest() {
    S_EXTRAP.getNodeSensitivitiesForValue(S_DATA, null);
  }
}
