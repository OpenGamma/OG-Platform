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

/**
 * 
 */
public class Extrapolator1DTest {

  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final Interpolator1D<Interpolator1DDoubleQuadraticDataBundle, InterpolationResult> INTERPOLATOR = new DoubleQuadraticInterpolator1D();
  private static final ExtrapolatorMethod<? extends Interpolator1DDataBundle, ? extends InterpolationResult> LINEAR_EM = new LinearExtrapolator<Interpolator1DDataBundle, InterpolationResult>();
  private static final ExtrapolatorMethod<? extends Interpolator1DDataBundle, ? extends InterpolationResult> FLAT_EM = new FlatExtrapolator<Interpolator1DDataBundle, InterpolationResult>();

  private static final Extrapolator1D<Interpolator1DDataBundle, InterpolationResult> FLAT_EXTRAPOLATOR = new Extrapolator1D(FLAT_EM, INTERPOLATOR);
  private static final Extrapolator1D<Interpolator1DDataBundle, InterpolationResult> EXTRAPOLATOR = new Extrapolator1D(LINEAR_EM, LINEAR_EM, INTERPOLATOR);
  private static final Interpolator1DDataBundle MODEL;

  private static final double[] X_DATA = new double[] {0, 0.4, 1.0, 1.8, 2.8, 5};
  private static final double[] Y_DATA = new double[] {3., 4., 3.1, 2., 7., 2.};

  private static final double[] X_TEST = new double[] {-1.0, 0, 0.3, 1.0, 5.0, 6.0};
  private static final double[] Y_TEST = new double[] {-1.1, 3.0, 3.87, 3.1, 2.0, -5.272727273};

  static {
    MODEL = Interpolator1DDataBundleFactory.fromSortedArrays(X_DATA, Y_DATA, INTERPOLATOR);
  }

  @Test
  public void testFlatExtrapolation() {
    for (int i = 0; i < 100; i++) {
      double x = RANDOM.nextDouble() * 20.0 - 10;
      if (x < 0) {
        assertEquals(3.0, FLAT_EXTRAPOLATOR.interpolate(MODEL, x).getResult(), 1e-12);
      } else if (x > 5.0) {
        assertEquals(2.0, FLAT_EXTRAPOLATOR.interpolate(MODEL, x).getResult(), 1e-12);
      } else {
        assertEquals(INTERPOLATOR.interpolate((Interpolator1DDoubleQuadraticDataBundle) MODEL, x).getResult(), FLAT_EXTRAPOLATOR.interpolate(MODEL, x).getResult(), 1e-12);
      }
    }
  }

  @Test
  public void test() {
    for (int i = 0; i < X_TEST.length; i++) {
      assertEquals(EXTRAPOLATOR.interpolate(MODEL, X_TEST[i]).getResult(), Y_TEST[i], 1e-6);
    }
  }
}
