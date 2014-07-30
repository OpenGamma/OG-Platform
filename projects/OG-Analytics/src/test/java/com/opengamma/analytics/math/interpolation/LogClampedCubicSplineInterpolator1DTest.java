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
public class LogClampedCubicSplineInterpolator1DTest {
  private static final Interpolator1D LOG_INTERP = Interpolator1DFactory.LOG_CLAMPED_CUBIC_INSTANCE;
  private static final Interpolator1D BARE_INTERP = Interpolator1DFactory.CLAMPED_CUBIC_INSTANCE;

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
    Interpolator1DDataBundle bundleBare = BARE_INTERP.getDataBundle(xValues, yValues);
    for (int i = 0; i < nKeys; ++i) {
      double valueBare = BARE_INTERP.interpolate(bundleBare, keys[i]);
      double value = LOG_INTERP.interpolate(bundle, keys[i]);
      assertEquals(Math.exp(valueBare), value, EPS);

      double[] senseBare = BARE_INTERP.getNodeSensitivitiesForValue(bundleBare, keys[i]);
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
   * Check consistency with bare interpolator using nontrivial boundary condition (i.e., first derivative values at end points)
   */
  @Test
  public void sampleDataWithBoundaryConditionTest1() {
    double[] xValues = new double[] {-1.0, 1.0, 2.5, 4.2, 10.0, 15.0, 30.0 };
    double[] yValues = new double[] {4.0, 2.0, 1.0, 5.0, 10.0, 3.5, -2.0 };
    double[] keys = new double[] {-0.8, 0.0, 1.2, 7.8, 10.0, 17.52, 25.0 };
    int nData = yValues.length;
    int nKeys = keys.length;
    double left = -.0;
    double right = 0.;

    double[] yValuesExp = new double[nData];
    for (int i = 0; i < nData; ++i) {
      yValuesExp[i] = Math.exp(yValues[i]);
    }
    double leftExp = left * yValuesExp[0];
    double rightExp = right * yValuesExp[nData - 1];

    Interpolator1DDataBundle bundle = ((PiecewisePolynomialInterpolator1D) LOG_INTERP).getDataBundleFromSortedArrays(xValues, yValuesExp, leftExp, rightExp);
    Interpolator1DDataBundle bundleBare = ((PiecewisePolynomialInterpolator1D) BARE_INTERP).getDataBundle(xValues, yValues, left, right);
    for (int i = 0; i < nKeys; ++i) {
      double valueBare = BARE_INTERP.interpolate(bundleBare, keys[i]);
      double value = LOG_INTERP.interpolate(bundle, keys[i]);
      assertEquals(Math.exp(valueBare), value, EPS);

      double[] senseBare = BARE_INTERP.getNodeSensitivitiesForValue(bundleBare, keys[i]);
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
    Interpolator1DDataBundle bundleBare = BARE_INTERP.getDataBundle(xValues, yValues);
    for (int i = 0; i < nKeys; ++i) {
      double valueBare = BARE_INTERP.interpolate(bundleBare, keys[i]);
      double value = LOG_INTERP.interpolate(bundle, keys[i]);
      assertEquals(Math.exp(valueBare), value, EPS);

      double[] senseBare = BARE_INTERP.getNodeSensitivitiesForValue(bundleBare, keys[i]);
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
    Interpolator1DDataBundle bundleBare = BARE_INTERP.getDataBundle(xValues, yValues);
    for (int i = 0; i < nKeys; ++i) {
      double valueBare = BARE_INTERP.interpolate(bundleBare, keys[i]);
      double value = LOG_INTERP.interpolate(bundle, keys[i]);
      assertEquals(Math.exp(valueBare), value, EPS);

      double[] senseBare = BARE_INTERP.getNodeSensitivitiesForValue(bundleBare, keys[i]);
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
    try {
      ((PiecewisePolynomialInterpolator1D) LOG_INTERP).getDataBundleFromSortedArrays(xValues2, yValues2, 1.0, 1.0);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("y should be positive", e.getMessage());
    }
  }

  /**
   * Print test for 3 log cubic interpolators
   */
  @Test(enabled = false)
  public void printTest() {
    PiecewisePolynomialWithSensitivityFunction1D func = new PiecewisePolynomialWithSensitivityFunction1D();

    double[] xValues = new double[] {-3.2, -0.9, 1.4, 1.8, 6.7 };
    double[] yValues = new double[] {1.0, 1.5, 2.2, 1.3, 1.8 };
    double[] yValuesClamp = new double[] {0.0, 1.0, 1.5, 2.2, 1.3, 1.8, 0.0 };
    int nData = yValues.length;
    int nKeys = 100;
    double[] keys = new double[nKeys];
    double interval = (xValues[nData - 1] - xValues[0]) / (nKeys - 1);
    for (int i = 0; i < nKeys; ++i) {
      keys[i] = xValues[0] + interval * i;
    }

    double[] yValuesExp = new double[nData];
    for (int i = 0; i < nData; ++i) {
      yValuesExp[i] = Math.exp(yValues[i]);
    }

    Interpolator1D nat = Interpolator1DFactory.LOG_NATURAL_CUBIC_INSTANCE;
    Interpolator1D knot = Interpolator1DFactory.LOG_NOTAKNOT_CUBIC_INSTANCE;
    Interpolator1D clam = Interpolator1DFactory.LOG_CLAMPED_CUBIC_INSTANCE;
    Interpolator1DDataBundle bundleNat = nat.getDataBundle(xValues, yValuesExp);
    Interpolator1DDataBundle bundleKnot = knot.getDataBundle(xValues, yValuesExp);
    Interpolator1DDataBundle bundleClam = clam.getDataBundle(xValues, yValuesExp);

    PiecewisePolynomialInterpolator natBare = new NaturalSplineInterpolator();
    PiecewisePolynomialInterpolator knotBare = new CubicSplineInterpolator();
    PiecewisePolynomialInterpolator clamBare = new CubicSplineInterpolator();
    PiecewisePolynomialResultsWithSensitivity resNat = natBare.interpolateWithSensitivity(xValues, yValues);
    PiecewisePolynomialResultsWithSensitivity resKnot = knotBare.interpolateWithSensitivity(xValues, yValues);
    PiecewisePolynomialResultsWithSensitivity resClam = clamBare.interpolateWithSensitivity(xValues, yValuesClamp);

    for (int i = 0; i < nKeys; ++i) {
      System.out.println(keys[i] + "\t" + nat.interpolate(bundleNat, keys[i]) + "\t" + knot.interpolate(bundleKnot, keys[i]) + "\t" + clam.interpolate(bundleClam, keys[i]));
    }
    System.out.println();
    for (int i = 0; i < nKeys; ++i) {
      System.out.println(keys[i] + "\t" + func.evaluate(resNat, keys[i]).getData()[0] + "\t" + func.evaluate(resKnot, keys[i]).getData()[0] + "\t" + func.evaluate(resClam, keys[i]).getData()[0]);
    }
  }
}
