/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.InterpolationResult;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class InterpolatedVolatilitySurfaceTest {
  private static final double SIGMA = 0.4;
  private static final Interpolator1D<Interpolator1DDataBundle, InterpolationResult> LINEAR = new LinearInterpolator1D();
  private static final Interpolator2D INTERPOLATOR = new GridInterpolator2D(LINEAR, LINEAR);
  private static final Map<DoublesPair, Double> DATA = new HashMap<DoublesPair, Double>();
  private static final InterpolatedVolatilitySurface SURFACE;
  private static final DoublesPair XY = Pair.of(0.5, 0.5);
  private static final double SHIFT = 0.05;
  private static final double EPS = 1e-15;

  static {
    DATA.put(Pair.of(0., 1.), SIGMA);
    DATA.put(Pair.of(1., 0.), SIGMA);
    DATA.put(Pair.of(0., 0.), SIGMA);
    DATA.put(Pair.of(1., 1.), SIGMA);
    SURFACE = new InterpolatedVolatilitySurface(DATA, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullData() {
    new InterpolatedVolatilitySurface(null, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullInterpolator() {
    new InterpolatedVolatilitySurface(DATA, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithEmptyMap() {
    new InterpolatedVolatilitySurface(Collections.<DoublesPair, Double> emptyMap(), INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNegativeData() {
    final Map<DoublesPair, Double> data = new HashMap<DoublesPair, Double>(DATA);
    data.put(XY, -SIGMA);
    new InterpolatedVolatilitySurface(data, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetVolatilityWithNullPair() {
    SURFACE.getVolatility(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetSingleShiftWithNullPair() {
    SURFACE.withSingleShift(null, 3.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMultipleShiftsWithNull() {
    SURFACE.withMultipleShifts(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMultipleShiftsWithNullShift() {
    SURFACE.withMultipleShifts(Collections.<DoublesPair, Double> singletonMap(DoublesPair.of(2., 2.), null));
  }

  @Test
  public void test() {
    assertEquals(SIGMA, SURFACE.getVolatility(XY), EPS);
    assertEquals(DATA.keySet(), SURFACE.getXYData());
  }

  @Test
  public void testParallelShift() {
    final VolatilitySurface surface = SURFACE.withParallelShift(SHIFT);
    for (final DoublesPair pair : SURFACE.getXYData()) {
      assertEquals(SIGMA + SHIFT, surface.getVolatility(pair), EPS);
    }
  }

  @Test
  public void testSingleShift() {
    final DoublesPair shiftedPoint = DATA.keySet().iterator().next();
    final VolatilitySurface surface = SURFACE.withSingleShift(shiftedPoint, SHIFT);
    for (final DoublesPair pair : SURFACE.getXYData()) {
      if (pair.equals(shiftedPoint)) {
        assertEquals(SIGMA + SHIFT, surface.getVolatility(pair), EPS);
      } else {
        assertEquals(SIGMA, surface.getVolatility(pair), EPS);
      }
    }
  }

  @Test
  public void testMultipleShift() {
    final Map<DoublesPair, Double> shifts = new HashMap<DoublesPair, Double>();
    final DoublesPair xy1 = Pair.of(0., 0.);
    final DoublesPair xy2 = Pair.of(1., 1.);
    shifts.put(xy1, SHIFT);
    shifts.put(xy2, -SHIFT);
    final InterpolatedVolatilitySurface surface = (InterpolatedVolatilitySurface) SURFACE.withMultipleShifts(shifts);
    for (final DoublesPair pair : surface.getXYData()) {
      if (pair.equals(xy1)) {
        assertEquals(SIGMA + SHIFT, surface.getVolatility(pair), EPS);
      } else if (pair.equals(xy2)) {
        assertEquals(SIGMA - SHIFT, surface.getVolatility(pair), EPS);
      } else {
        assertEquals(SIGMA, surface.getVolatility(pair), EPS);
      }
    }
  }
}
