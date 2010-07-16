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
public class Extrapolator1DWithSensitivityTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final Interpolator1D<? extends Interpolator1DDataBundle, InterpolationResultWithSensitivities> INTERPOLATOR = new CubicSplineInterpolatorWithSensitivities1D();
  private static final ExtrapolatorMethod<? extends Interpolator1DDataBundle, ? extends InterpolationResult> LINEAR_EM = new LinearExtrapolator<Interpolator1DDataBundle, InterpolationResult>();
  private static final ExtrapolatorMethod<? extends Interpolator1DDataBundle, ? extends InterpolationResult> FLAT_EM = new FlatExtrapolator<Interpolator1DDataBundle, InterpolationResult>();
  private static final ExtrapolatorMethod<? extends Interpolator1DDataBundle, ? extends InterpolationResultWithSensitivities> LINEAR_EM_SENSE = new LinearExtrapolatorWithSensitivity<Interpolator1DDataBundle, InterpolationResultWithSensitivities>();
  private static final ExtrapolatorMethod<? extends Interpolator1DDataBundle, ? extends InterpolationResultWithSensitivities> FLAT_EM_SENSE = new FlatExtrapolatorWithSensitivities<Interpolator1DDataBundle, InterpolationResultWithSensitivities>();

  // private static final Extrapolator1D<Interpolator1DDataBundle, InterpolationResult> FLAT_EXTRAPOLATOR = new Extrapolator1D(FLAT_EM, INTERPOLATOR);
  private static final Extrapolator1D<Interpolator1DDataBundle, InterpolationResult> EXTRAPOLATOR = new Extrapolator1D(LINEAR_EM, LINEAR_EM, INTERPOLATOR);
  private static final Extrapolator1D<Interpolator1DDataBundle, InterpolationResultWithSensitivities> EXTRAPOLATOR_SENSE = new Extrapolator1D(LINEAR_EM_SENSE, LINEAR_EM_SENSE, INTERPOLATOR);
  private static final Interpolator1DWithSensitivities<Interpolator1DDataBundle> EXTRAPOLATOR_FD = new Interpolator1DWithSensitivities<Interpolator1DDataBundle>(EXTRAPOLATOR);
  private static final Interpolator1DCubicSplineDataBundle MODEL;
  private static final double EPS = 1e-4;

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
    double[] t = new double[] {0.0, 0.5, 1.0, 2.0, 3.0, 5.0, 7.0, 10.0, 15.0, 17.5, 20.0, 25.0, 30.0};
    int n = t.length;
    double[] r = new double[n];
    for (int i = 0; i < n; i++) {
      r[i] = FUNCTION.evaluate(t[i]);
    }
    MODEL = (Interpolator1DCubicSplineDataBundle) Interpolator1DDataBundleFactory.fromSortedArrays(t, r, EXTRAPOLATOR);
  }

  @Test
  public void testSensitivities() {
    double min = -10.0;
    double max = 40.0;
    for (int i = 0; i < 100; i++) {
      final double t = RANDOM.nextDouble() * (max - min) - min;
      // double t = 0.25;
      double[] sensitivity_FD = EXTRAPOLATOR_FD.interpolate(MODEL, t).getSensitivities();
      double[] sensitivity = EXTRAPOLATOR_SENSE.interpolate(MODEL, t).getSensitivities();

      for (int j = 0; j < sensitivity.length; j++) {
        assertEquals(sensitivity_FD[j], sensitivity[j], EPS);
      }
    }
  }

}
