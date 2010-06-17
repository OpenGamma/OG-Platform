/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class CubicSplineWithSensitivitiesInterpolator1DTest {

  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final Interpolator1D<Interpolator1DWithSecondDerivativeModel, InterpolationResult> INTERPOLATOR1 = new NaturalCubicSplineInterpolator1D();
  private static final Interpolator1D<Interpolator1DCubicSplineWthSensitivitiesModel, InterpolationResultWithSensitivities> INTERPOLATOR2 = new CubicSplineInterpolatorWithSensitivities1D();
  private static final Interpolator1DWithSecondDerivativeModel MODEL1;
  private static final Interpolator1DCubicSplineWthSensitivitiesModel MODEL2;
  private static final double EPS = 1e-8;
  private static final Function1D<Double, Double> FUNCTION = new Function1D<Double, Double>() {

    private static final double a = -0.045;
    private static final double b = 0.03;
    private static final double c = 0.3;
    private static final double d = 0.05;

    @Override
    public Double evaluate(Double x) {
      return (a + b * x) * Math.exp(-c * x) + d;
    }

  };

  static {
    double[] t = new double[] { 0.0, 0.5, 1.0, 2.0, 3.0, 5.0, 7.0, 10.0, 15.0, 20.0, 25.0, 30.0 };
    int n = t.length;
    double[] r = new double[n];
    for (int i = 0; i < n; i++) {
      r[i] = FUNCTION.evaluate(t[i]);
    }
    MODEL1 = (Interpolator1DWithSecondDerivativeModel) Interpolator1DModelFactory.fromSortedArrays(t, r, INTERPOLATOR1);
    MODEL2 = (Interpolator1DCubicSplineWthSensitivitiesModel) Interpolator1DModelFactory.fromSortedArrays(t, r, INTERPOLATOR2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullInputMap() {
    INTERPOLATOR1.interpolate(null, 3.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullInterpolateValue() {
    INTERPOLATOR2.interpolate(MODEL2, null);
  }

  @Test(expected = InterpolationException.class)
  public void testHighValue() {
    INTERPOLATOR2.interpolate(MODEL2, 31.);
  }

  @Test(expected = InterpolationException.class)
  public void testLowValue() {
    INTERPOLATOR2.interpolate(MODEL2, -1.);
  }

  @Test
  public void test() {
    double tmax = MODEL2.lastKey();
    for (int i = 0; i < 100; i++) {
      final double t = tmax * RANDOM.nextDouble();
      assertEquals(FUNCTION.evaluate(t), INTERPOLATOR2.interpolate(MODEL2, t).getResult(), 1e-4);
    }
  }

  @Test
  public void testAgainstNaturalCubicSplineInterpolator1D() {
    double tmax = MODEL2.lastKey();
    for (int i = 0; i < 100; i++) {
      final double t = tmax * RANDOM.nextDouble();
      assertEquals(INTERPOLATOR1.interpolate(MODEL1, t).getResult(), INTERPOLATOR2.interpolate(MODEL2, t).getResult(), EPS);
    }
  }

  @Test
  public void testSensitivities() {
    double tmax = MODEL2.lastKey();
    for (int i = 0; i < 100; i++) {
      final double t = tmax * RANDOM.nextDouble();
      double[] sense = INTERPOLATOR2.interpolate(MODEL2, t).getSensitivities();
      for (int j = 0; j < sense.length; j++) {
        assertEquals(getSensitivity(MODEL1, INTERPOLATOR1, t, j), sense[j], EPS);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends Interpolator1DModel> double getSensitivity(T model, Interpolator1D<T, ? extends InterpolationResult> interpolator, double t, int node) {
    double[] x = model.getKeys();
    double[] y = model.getValues();
    int n = y.length;
    double[] yUp = new double[n];
    double[] yDown = new double[n];
    yUp = Arrays.copyOf(y, n);
    yDown = Arrays.copyOf(y, n);
    yUp[node] += EPS;
    yDown[node] -= EPS;
    T modelUp = (T) Interpolator1DModelFactory.fromSortedArrays(x, yUp, interpolator);
    T modelDown = (T) Interpolator1DModelFactory.fromSortedArrays(x, yDown, interpolator);
    double up = interpolator.interpolate(modelUp, t).getResult();
    double down = interpolator.interpolate(modelDown, t).getResult();
    return (up - down) / 2.0 / EPS;
  }

}
