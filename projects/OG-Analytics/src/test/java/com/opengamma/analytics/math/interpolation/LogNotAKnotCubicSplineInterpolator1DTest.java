/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.PiecewisePolynomialWithSensitivityFunction1D;
import com.opengamma.analytics.math.interpolation.data.InterpolationBoundedValues;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class LogNotAKnotCubicSplineInterpolator1DTest {
  private static final Interpolator1D LOG_INTERP = Interpolator1DFactory.LOG_NOTAKNOT_CUBIC_INSTANCE;
  private static final PiecewisePolynomialInterpolator BARE_INTERP = new CubicSplineInterpolator();
  private static final PiecewisePolynomialWithSensitivityFunction1D FUNCTION = new PiecewisePolynomialWithSensitivityFunction1D();

  private static final double EPS = 1.0e-12;
  private static final double DELTA = 1.0e-5;

  /**
   * Check consistency with bare interpolator
   */
  @Test
  public void sampleDataTest1() {
    double[] xValues = new double[] {-1.0, 1.0, 2.5, 4.2, 10.0, 15.0, 30.0 };
    double[] yValues = new double[] {4.0, 2.0, 1.0, 5.0, 10.0, 3.5, -2.0 };
    double[] keys = new double[] {-0.8, 0.0, 1.2, 7.8, 10.0, 17.52, 25.0 };
    int nData = yValues.length;
    int nKeys = keys.length;

    double[] yValuesExp = new double[nData];
    for (int i = 0; i < nData; ++i) {
      yValuesExp[i] = Math.exp(yValues[i]);
    }

    Interpolator1DDataBundle bundle = LOG_INTERP.getDataBundle(xValues, yValuesExp);
    Interpolator1DDataBundle bundleSorted = LOG_INTERP.getDataBundleFromSortedArrays(xValues, yValuesExp);
    assertEquals(bundle, bundleSorted);
    PiecewisePolynomialResultsWithSensitivity resBare = BARE_INTERP.interpolateWithSensitivity(xValues, yValues);
    for (int i = 0; i < nKeys; ++i) {
      double valueBare = FUNCTION.evaluate(resBare, keys[i]).getData()[0];
      double value = LOG_INTERP.interpolate(bundle, keys[i]);
      assertEquals(Math.exp(valueBare), value, EPS);

      double[] senseBare = FUNCTION.nodeSensitivity(resBare, keys[i]).getData();
      double[] sense = LOG_INTERP.getNodeSensitivitiesForValue(bundle, keys[i]);
      assertEquals(nData, sense.length);
      for (int j = 0; j < nData; ++j) {
        assertEquals(senseBare[j] * value / yValuesExp[j], sense[j], EPS);
      }
    }

    for (int i = 0; i < nData; ++i) {
      assertTrue(bundle.containsKey(xValues[i]));
      assertEquals(yValuesExp[i], bundle.getValues()[i], EPS);
      assertEquals(xValues[i], bundle.getKeys()[i], EPS);
    }

    int[] sampleIndex = new int[] {0, 1, 3 };
    for (int i = 0; i < sampleIndex.length; ++i) {
      assertEquals(xValues[sampleIndex[i]], bundle.getLowerBoundKey(xValues[sampleIndex[i]] + 0.01));
      assertEquals(sampleIndex[i], bundle.getLowerBoundIndex(xValues[sampleIndex[i]] + 0.01));

      InterpolationBoundedValues bdValues = bundle.getBoundedValues(xValues[sampleIndex[i]] + 0.02);
      assertEquals(yValuesExp[sampleIndex[i] + 1], bdValues.getHigherBoundValue(), EPS);
      assertEquals(yValuesExp[sampleIndex[i]], bdValues.getLowerBoundValue(), EPS);
    }

    /**
     * Check sensitivity by finite difference approximation
     */
    for (int i = 0; i < nData; ++i) {
      double[] yValuesExpUp = Arrays.copyOf(yValuesExp, nData);
      double[] yValuesExpDown = Arrays.copyOf(yValuesExp, nData);
      yValuesExpUp[i] += DELTA;
      yValuesExpDown[i] -= DELTA;
      Interpolator1DDataBundle upBundle = LOG_INTERP.getDataBundle(xValues, yValuesExpUp);
      Interpolator1DDataBundle downBundle = LOG_INTERP.getDataBundle(xValues, yValuesExpDown);
      for (int j = 0; j < nKeys; ++j) {
        double app = 0.5 * (LOG_INTERP.interpolate(upBundle, keys[j]) - LOG_INTERP.interpolate(downBundle, keys[j])) / DELTA;
        assertEquals(app, LOG_INTERP.getNodeSensitivitiesForValue(bundle, keys[j])[i], Math.max(Math.abs(app), 1.0) * DELTA);
      }
    }
    /**
     * Endpoint sensitivity
     */
    assertEquals(1.0, LOG_INTERP.getNodeSensitivitiesForValue(bundle, xValues[0])[0], EPS);
    assertEquals(0.0, LOG_INTERP.getNodeSensitivitiesForValue(bundle, xValues[0])[1], EPS);
    assertEquals(1.0, LOG_INTERP.getNodeSensitivitiesForValue(bundle, xValues[nData - 1])[nData - 1], EPS);
    assertEquals(0.0, LOG_INTERP.getNodeSensitivitiesForValue(bundle, xValues[nData - 1])[nData - 2], EPS);

    /**
     * Check first derivative by finite difference approximation
     */
    for (int i = 0; i < nKeys; ++i) {
      double app = 0.5 * (LOG_INTERP.interpolate(bundle, keys[i] + DELTA) - LOG_INTERP.interpolate(bundle, keys[i] - DELTA)) / DELTA;
      assertEquals(app, LOG_INTERP.firstDerivative(bundle, keys[i]), Math.max(Math.abs(app), 1.0) * DELTA);
    }
  }

  /**
   * Check consistency with bare interpolator for flat data
   */
  @Test
  public void sampleDataTest2() {
    double[] xValues = new double[] {1.0, 2.0, 3.0, 4.0, 5.0 };
    double[] yValues = new double[] {1.0, 1.0, 1.0, 1.0, 1.0 };
    double[] keys = new double[] {0.1, 1.2, 3.7 };
    int nData = yValues.length;
    int nKeys = keys.length;

    double[] yValuesExp = new double[nData];
    for (int i = 0; i < nData; ++i) {
      yValuesExp[i] = Math.exp(yValues[i]);
    }

    Interpolator1DDataBundle bundle = LOG_INTERP.getDataBundle(xValues, yValuesExp);
    Interpolator1DDataBundle bundleSorted = LOG_INTERP.getDataBundleFromSortedArrays(xValues, yValuesExp);
    assertEquals(bundle, bundleSorted);
    PiecewisePolynomialResultsWithSensitivity resBare = BARE_INTERP.interpolateWithSensitivity(xValues, yValues);
    for (int i = 0; i < nKeys; ++i) {
      double valueBare = FUNCTION.evaluate(resBare, keys[i]).getData()[0];
      double value = LOG_INTERP.interpolate(bundle, keys[i]);
      assertEquals(Math.exp(valueBare), value, EPS);

      double[] senseBare = FUNCTION.nodeSensitivity(resBare, keys[i]).getData();
      double[] sense = LOG_INTERP.getNodeSensitivitiesForValue(bundle, keys[i]);
      assertEquals(nData, sense.length);
      for (int j = 0; j < nData; ++j) {
        assertEquals(senseBare[j] * value / yValuesExp[j], sense[j], EPS);
      }
    }

    for (int i = 0; i < nData; ++i) {
      assertTrue(bundle.containsKey(xValues[i]));
      assertEquals(yValuesExp[i], bundle.getValues()[i], EPS);
      assertEquals(xValues[i], bundle.getKeys()[i], EPS);
    }

    int[] sampleIndex = new int[] {0, 1, 3 };
    for (int i = 0; i < sampleIndex.length; ++i) {
      assertEquals(xValues[sampleIndex[i]], bundle.getLowerBoundKey(xValues[sampleIndex[i]] + 0.01));
      assertEquals(sampleIndex[i], bundle.getLowerBoundIndex(xValues[sampleIndex[i]] + 0.01));

      InterpolationBoundedValues bdValues = bundle.getBoundedValues(xValues[sampleIndex[i]] + 0.02);
      assertEquals(yValuesExp[sampleIndex[i] + 1], bdValues.getHigherBoundValue(), EPS);
      assertEquals(yValuesExp[sampleIndex[i]], bdValues.getLowerBoundValue(), EPS);
    }

    /**
     * Check sensitivity by finite difference approximation
     */
    for (int i = 0; i < nData; ++i) {
      double[] yValuesExpUp = Arrays.copyOf(yValuesExp, nData);
      double[] yValuesExpDown = Arrays.copyOf(yValuesExp, nData);
      yValuesExpUp[i] += DELTA;
      yValuesExpDown[i] -= DELTA;
      Interpolator1DDataBundle upBundle = LOG_INTERP.getDataBundle(xValues, yValuesExpUp);
      Interpolator1DDataBundle downBundle = LOG_INTERP.getDataBundle(xValues, yValuesExpDown);
      for (int j = 0; j < nKeys; ++j) {
        double app = 0.5 * (LOG_INTERP.interpolate(upBundle, keys[j]) - LOG_INTERP.interpolate(downBundle, keys[j])) / DELTA;
        assertEquals(app, LOG_INTERP.getNodeSensitivitiesForValue(bundle, keys[j])[i], Math.max(Math.abs(app), 1.0) * DELTA);
      }
    }
    /**
     * Endpoint sensitivity
     */
    assertEquals(1.0, LOG_INTERP.getNodeSensitivitiesForValue(bundle, xValues[0])[0], EPS);
    assertEquals(0.0, LOG_INTERP.getNodeSensitivitiesForValue(bundle, xValues[0])[1], EPS);
    assertEquals(1.0, LOG_INTERP.getNodeSensitivitiesForValue(bundle, xValues[nData - 1])[nData - 1], EPS);
    assertEquals(0.0, LOG_INTERP.getNodeSensitivitiesForValue(bundle, xValues[nData - 1])[nData - 2], EPS);

    /**
     * Check first derivative by finite difference approximation
     */
    for (int i = 0; i < nKeys; ++i) {
      double app = 0.5 * (LOG_INTERP.interpolate(bundle, keys[i] + DELTA) - LOG_INTERP.interpolate(bundle, keys[i] - DELTA)) / DELTA;
      assertEquals(app, LOG_INTERP.firstDerivative(bundle, keys[i]), Math.max(Math.abs(app), 1.0) * DELTA);
    }
  }

  /**
   * Check consistency with bare interpolator for linear data
   */
  @Test
  public void sampleDataTest3() {
    double[] xValues = new double[] {-1.0, 2.0, 3.0, 4.0, 5.0 };
    double[] yValues = new double[] {-2.0, 4.0, 6.0, 8.0, 10.0 };
    double[] keys = new double[] {0., 1.2, 2.0, 4.7 };
    int nData = yValues.length;
    int nKeys = keys.length;

    double[] yValuesExp = new double[nData];
    for (int i = 0; i < nData; ++i) {
      yValuesExp[i] = Math.exp(yValues[i]);
    }

    Interpolator1DDataBundle bundle = LOG_INTERP.getDataBundle(xValues, yValuesExp);
    Interpolator1DDataBundle bundleSorted = LOG_INTERP.getDataBundleFromSortedArrays(xValues, yValuesExp);
    assertEquals(bundle, bundleSorted);
    PiecewisePolynomialResultsWithSensitivity resBare = BARE_INTERP.interpolateWithSensitivity(xValues, yValues);
    for (int i = 0; i < nKeys; ++i) {
      double valueBare = FUNCTION.evaluate(resBare, keys[i]).getData()[0];
      double value = LOG_INTERP.interpolate(bundle, keys[i]);
      assertEquals(Math.exp(valueBare), value, EPS);

      double[] senseBare = FUNCTION.nodeSensitivity(resBare, keys[i]).getData();
      double[] sense = LOG_INTERP.getNodeSensitivitiesForValue(bundle, keys[i]);
      assertEquals(nData, sense.length);
      for (int j = 0; j < nData; ++j) {
        assertEquals(senseBare[j] * value / yValuesExp[j], sense[j], EPS);
      }
    }

    for (int i = 0; i < nData; ++i) {
      assertTrue(bundle.containsKey(xValues[i]));
      assertEquals(yValuesExp[i], bundle.getValues()[i], EPS);
      assertEquals(xValues[i], bundle.getKeys()[i], EPS);
    }

    int[] sampleIndex = new int[] {0, 1, 3 };
    for (int i = 0; i < sampleIndex.length; ++i) {
      assertEquals(xValues[sampleIndex[i]], bundle.getLowerBoundKey(xValues[sampleIndex[i]] + 0.01));
      assertEquals(sampleIndex[i], bundle.getLowerBoundIndex(xValues[sampleIndex[i]] + 0.01));

      InterpolationBoundedValues bdValues = bundle.getBoundedValues(xValues[sampleIndex[i]] + 0.02);
      assertEquals(yValuesExp[sampleIndex[i] + 1], bdValues.getHigherBoundValue(), EPS);
      assertEquals(yValuesExp[sampleIndex[i]], bdValues.getLowerBoundValue(), EPS);
    }

    /**
     * Check sensitivity by finite difference approximation
     */
    for (int i = 0; i < nData; ++i) {
      double[] yValuesExpUp = Arrays.copyOf(yValuesExp, nData);
      double[] yValuesExpDown = Arrays.copyOf(yValuesExp, nData);
      yValuesExpUp[i] += DELTA;
      yValuesExpDown[i] -= DELTA;
      Interpolator1DDataBundle upBundle = LOG_INTERP.getDataBundle(xValues, yValuesExpUp);
      Interpolator1DDataBundle downBundle = LOG_INTERP.getDataBundle(xValues, yValuesExpDown);
      for (int j = 0; j < nKeys; ++j) {
        double app = 0.5 * (LOG_INTERP.interpolate(upBundle, keys[j]) - LOG_INTERP.interpolate(downBundle, keys[j])) / DELTA;
        assertEquals(app, LOG_INTERP.getNodeSensitivitiesForValue(bundle, keys[j])[i], Math.max(Math.abs(app), 1.0) * DELTA);
      }
    }
    /**
     * Endpoint sensitivity
     */
    assertEquals(1.0, LOG_INTERP.getNodeSensitivitiesForValue(bundle, xValues[0])[0], EPS);
    assertEquals(0.0, LOG_INTERP.getNodeSensitivitiesForValue(bundle, xValues[0])[1], EPS);
    assertEquals(1.0, LOG_INTERP.getNodeSensitivitiesForValue(bundle, xValues[nData - 1])[nData - 1], EPS);
    assertEquals(0.0, LOG_INTERP.getNodeSensitivitiesForValue(bundle, xValues[nData - 1])[nData - 2], EPS);

    /**
     * Check first derivative by finite difference approximation
     */
    for (int i = 0; i < nKeys; ++i) {
      double app = 0.5 * (LOG_INTERP.interpolate(bundle, keys[i] + DELTA) - LOG_INTERP.interpolate(bundle, keys[i] - DELTA)) / DELTA;
      assertEquals(app, LOG_INTERP.firstDerivative(bundle, keys[i]), Math.max(Math.abs(app), 1.0) * DELTA);
    }
  }

  /**
   * Exception expected
   */
  @Test
  public void errorTest() {
    double[] xValues1 = new double[] {-1.0, 1.0, 2.5, 4.2, };
    double[] yValues1 = new double[] {5.0, 10.0, 3.5, -0.0 };
    try {
      LOG_INTERP.getDataBundle(xValues1, yValues1);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("y should be positive", e.getMessage());
    }

    double[] xValues2 = new double[] {-1.0, 1.0, 2.5, 4.2, };
    double[] yValues2 = new double[] {5.0, 10.0, -3.5, 5.0 };
    try {
      LOG_INTERP.getDataBundleFromSortedArrays(xValues2, yValues2);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("y should be positive", e.getMessage());
    }
  }
}
