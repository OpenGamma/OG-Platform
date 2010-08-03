/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class DoubleQuadraticWithSensitivitiesInterpolator1DTest {

  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final Interpolator1D<Interpolator1DDoubleQuadraticDataBundle, InterpolationResult> BASE_INTERPOLATOR = new DoubleQuadraticInterpolator1D();
  private static final Interpolator1DWithSensitivities<Interpolator1DDoubleQuadraticDataBundle> FD_INTERPOLATOR = new Interpolator1DWithSensitivities<Interpolator1DDoubleQuadraticDataBundle>(
      BASE_INTERPOLATOR);
  private static final Interpolator1DWithSensitivities<Interpolator1DDoubleQuadraticDataBundle> SENSE_INTERPOLATOR = new DoubleQuadraticInterpolatorWithSensitivities1D();
  private static final Interpolator1DDoubleQuadraticDataBundle DATA;
  private static final double EPS = 1e-7;
  private static final Function1D<Double, Double> FUNCTION = new Function1D<Double, Double>() {

    private static final double a = -0.045;
    private static final double b = 0.03;
    private static final double c = 0.3;
    private static final double d = 0.05;

    @Override
    public Double evaluate(final Double x) {
      return (a + b * x) * Math.exp(-c * x) + d;
      // return a + b * x + c * x * x + d * x * x * x;
    }

  };

  static {
    final double[] t = new double[] {0.0, 0.5, 1.0, 2.0, 3.0, 5.0, 7.0, 10.0, 15.0, 17.5, 20.0, 25.0, 30.0};
    final int n = t.length;
    final double[] r = new double[n];
    for (int i = 0; i < n; i++) {
      r[i] = FUNCTION.evaluate(t[i]);
    }
    DATA = (Interpolator1DDoubleQuadraticDataBundle) Interpolator1DDataBundleFactory.fromSortedArrays(t, r, BASE_INTERPOLATOR);

  }

  @Test(expected = IllegalArgumentException.class)
  public void nullInputMap() {
    SENSE_INTERPOLATOR.interpolate(null, 3.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullInterpolateValue() {
    SENSE_INTERPOLATOR.interpolate(DATA, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighValue() {
    SENSE_INTERPOLATOR.interpolate(DATA, 31.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLowValue() {
    SENSE_INTERPOLATOR.interpolate(DATA, -1.);
  }

  @Test
  public void test() {
    final double tmax = DATA.lastKey();
    for (int i = 0; i < 100; i++) {
      final double t = tmax * RANDOM.nextDouble();
      assertEquals(FUNCTION.evaluate(t), SENSE_INTERPOLATOR.interpolate(DATA, t).getResult(), 1e-3);
    }
  }

  @Test
  public void testAgainsBaseInterpolator1D() {
    final double tmax = DATA.lastKey();
    for (int i = 0; i < 100; i++) {
      final double t = tmax * RANDOM.nextDouble();
      assertEquals(BASE_INTERPOLATOR.interpolate(DATA, t).getResult(), SENSE_INTERPOLATOR.interpolate(DATA, t).getResult(), EPS);
    }
  }

  @Test
  public void testSensitivities() {
    final double tmax = DATA.lastKey();
    for (int i = 0; i < 100; i++) {
      final double t = tmax * RANDOM.nextDouble();

      final double[] fd_sensitivity = FD_INTERPOLATOR.interpolate(DATA, t).getSensitivities();
      final double[] sensitivity = SENSE_INTERPOLATOR.interpolate(DATA, t).getSensitivities();

      for (int j = 0; j < sensitivity.length; j++) {
        assertEquals(fd_sensitivity[j], sensitivity[j], EPS);
      }
    }
  }

}
