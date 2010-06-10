/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
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
  private static final Double[] COEFF = new Double[] {-0.4, 0.05, 0.2, 1.};
  private static final Interpolator1D<Interpolator1DWithSecondDerivativeModel> INTERPOLATOR = new NaturalCubicSplineInterpolator1D();
  private static final Function1D<Double, Double> CUBIC = new PolynomialFunction1D(COEFF);
  private static final double EPS = 1e-1;

  @Test(expected = IllegalArgumentException.class)
  public void nullInputMap() {
    INTERPOLATOR.interpolate((Interpolator1DWithSecondDerivativeModel) null, 3.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void emptyInputMap() {
    INTERPOLATOR.interpolate(Interpolator1DModelFactory.toModelWithSecondDerivative(Interpolator1DModelFactory.fromMap(Collections.<Double, Double>emptyMap())), 3.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullInterpolateValue() {
    final Map<Double, Double> map = new HashMap<Double, Double>();
    map.put(1., 2.);
    map.put(2., 4.);
    INTERPOLATOR.interpolate(Interpolator1DModelFactory.toModelWithSecondDerivative(Interpolator1DModelFactory.fromMap(map)), null);
  }

  @Test
  public void test() {
    final Map<Double, Double> data = new HashMap<Double, Double>();
    for (int i = 0; i < 12; i++) {
      final double x = i / 10.;
      data.put(x, CUBIC.evaluate(x));
    }
    for (int i = 0; i < 100; i++) {
      final double x = RANDOM.nextDouble();
      assertEquals(CUBIC.evaluate(x), INTERPOLATOR.interpolate(Interpolator1DModelFactory.toModelWithSecondDerivative(Interpolator1DModelFactory.fromMap(data)), x).getResult(), EPS);
    }
  }
}
