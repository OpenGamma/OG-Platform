/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.PolynomialFunction1D;

/**
 * 
 */
public class NaturalCubicSplineInterpolator1DTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final Double[] COEFF = new Double[] { -0.4, 0.05, 0.2, 1. };
  private static final Interpolator1D<Interpolator1DWithSecondDerivativeModel, InterpolationResult> INTERPOLATOR = new NaturalCubicSplineInterpolator1D();
  private static final Function1D<Double, Double> CUBIC = new PolynomialFunction1D(COEFF);
  private static final double EPS = 1e-2;
  private static final Interpolator1DWithSecondDerivativeModel MODEL;

  static {
    final Map<Double, Double> data = new HashMap<Double, Double>();
    for (int i = 0; i < 12; i++) {
      final double x = i / 10.;
      data.put(x, CUBIC.evaluate(x));
    }
    MODEL = (Interpolator1DWithSecondDerivativeModel) Interpolator1DModelFactory.fromMap(data, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullInputMap() {
    INTERPOLATOR.interpolate((Interpolator1DWithSecondDerivativeModel) null, 3.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullInterpolateValue() {
    INTERPOLATOR.interpolate(MODEL, null);
  }

  @Test(expected = InterpolationException.class)
  public void testHighValue() {
    INTERPOLATOR.interpolate(MODEL, 15.);
  }

  @Test(expected = InterpolationException.class)
  public void testLowValue() {
    INTERPOLATOR.interpolate(MODEL, -12.);
  }

  @Test
  public void test() {
    for (int i = 0; i < 100; i++) {
      final double x = RANDOM.nextDouble();
      assertEquals(CUBIC.evaluate(x), INTERPOLATOR.interpolate(MODEL, x).getResult(), EPS);
    }
  }
}
