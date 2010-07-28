/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;

import com.opengamma.financial.model.volatility.curve.ConstantVolatilityCurve;
import com.opengamma.financial.model.volatility.curve.FunctionalVolatilityCurve;
import com.opengamma.financial.model.volatility.curve.VolatilityCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.LogLinearInterpolator1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class VolatilityCurvesToInterpolatedVolatilitySurfaceTest {
  private static final Function1D<Double, Double> LINEAR = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x;
    }

  };
  private static final Function1D<Double, Double> QUADRATIC = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x * x;
    }

  };
  private static final Function1D<Double, Double> CUBIC = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x * x * x;
    }

  };
  private static final double T0 = 1;
  private static final double T1 = 3;
  private static final double T2 = 7;
  private static final Map<Double, VolatilityCurve> CURVES = new HashMap<Double, VolatilityCurve>();
  private static final LinearInterpolator1D INTERPOLATOR = new LinearInterpolator1D();
  private static final VolatilityCurvesToInterpolatedVolatilitySurface SURFACE;
  private static final double EPS = 1e-12;

  static {
    CURVES.put(T0, new FunctionalVolatilityCurve(LINEAR));
    CURVES.put(T1, new FunctionalVolatilityCurve(QUADRATIC));
    CURVES.put(T2, new FunctionalVolatilityCurve(CUBIC));
    SURFACE = new VolatilityCurvesToInterpolatedVolatilitySurface(CURVES, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurves() {
    new VolatilityCurvesToInterpolatedVolatilitySurface(null, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyCurves() {
    new VolatilityCurvesToInterpolatedVolatilitySurface(Collections.<Double, VolatilityCurve> emptyMap(), INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullElement() {
    final Map<Double, VolatilityCurve> map = Collections.singletonMap(1., null);
    new VolatilityCurvesToInterpolatedVolatilitySurface(map, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullInterpolator() {
    new VolatilityCurvesToInterpolatedVolatilitySurface(CURVES, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPair() {
    SURFACE.getVolatility(null);
  }

  @Test(expected = NotImplementedException.class)
  public void testSingleShift() {
    SURFACE.withSingleShift(null, 0);
  }

  @Test(expected = NotImplementedException.class)
  public void testMultipleShifts() {
    SURFACE.withMultipleShifts(null);
  }

  @Test(expected = NotImplementedException.class)
  public void testParallelShift() {
    SURFACE.withParallelShift(0.4);
  }

  @Test
  public void testEqualsAndHashCode() {
    VolatilityCurvesToInterpolatedVolatilitySurface other = new VolatilityCurvesToInterpolatedVolatilitySurface(CURVES, INTERPOLATOR);
    assertEquals(other, SURFACE);
    assertEquals(other.hashCode(), SURFACE.hashCode());
    other = new VolatilityCurvesToInterpolatedVolatilitySurface(Collections.<Double, VolatilityCurve> singletonMap(1., new ConstantVolatilityCurve(0.3)), INTERPOLATOR);
    assertFalse(other.equals(SURFACE));
    other = new VolatilityCurvesToInterpolatedVolatilitySurface(CURVES, new LogLinearInterpolator1D());
    assertFalse(other.equals(SURFACE));
  }

  @Test
  public void testGetters() {
    assertEquals(SURFACE.getCurves(), CURVES);
  }

  @Test
  public void testExtrapolation() {
    DoublesPair xy = DoublesPair.of(T0 - Math.random(), Math.random());
    assertEquals(SURFACE.getVolatility(xy), LINEAR.evaluate(xy.second), EPS);
    xy = DoublesPair.of(T2 + Math.random(), Math.random());
    assertEquals(SURFACE.getVolatility(xy), CUBIC.evaluate(xy.second), EPS);
  }

  @Test
  public void test() {
    DoublesPair xy = DoublesPair.of(T0, Math.random());
    assertEquals(SURFACE.getVolatility(xy), LINEAR.evaluate(xy.second), EPS);
    xy = DoublesPair.of(T1, Math.random());
    assertEquals(SURFACE.getVolatility(xy), QUADRATIC.evaluate(xy.second), EPS);
    xy = DoublesPair.of(T2, Math.random());
    assertEquals(SURFACE.getVolatility(xy), CUBIC.evaluate(xy.second), EPS);
    double x;
    for (int i = (int) T0; i < T2; i++) {
      x = i + 0.5;
      xy = DoublesPair.of(x, 1);
      assertEquals(SURFACE.getVolatility(xy), 1, EPS);
      xy = DoublesPair.of(x, 0);
      assertEquals(SURFACE.getVolatility(xy), 0, EPS);
    }
    final double y = 6.7;
    xy = DoublesPair.of(2., y);
    assertEquals(SURFACE.getVolatility(xy), (LINEAR.evaluate(y) + QUADRATIC.evaluate(y)) / 2, EPS);
    xy = DoublesPair.of(5., y);
    assertEquals(SURFACE.getVolatility(xy), (QUADRATIC.evaluate(y) + CUBIC.evaluate(y)) / 2, EPS);
  }
}
