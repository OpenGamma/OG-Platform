/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.math.interpolation.ExponentialInterpolator1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.StepInterpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class InterpolatedDoubleDoubleCurveTest extends DoubleDoubleCurveTestCase {
  private static final Interpolator1D<Interpolator1DDataBundle> LINEAR = new LinearInterpolator1D();
  private static final Interpolator1D<Interpolator1DDataBundle> STEP = new StepInterpolator1D();
  private static final Interpolator1D<Interpolator1DDataBundle> EXPONENTIAL = new ExponentialInterpolator1D();
  private static final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> INTERPOLATOR = Collections.<Double, Interpolator1D<? extends Interpolator1DDataBundle>> singletonMap(
      Double.POSITIVE_INFINITY, LINEAR);
  private static final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> INTERPOLATORS;
  private static final double EPS = 1e-15;
  static {
    INTERPOLATORS = new HashMap<Double, Interpolator1D<? extends Interpolator1DDataBundle>>();
    INTERPOLATORS.put(5.5, LINEAR);
    INTERPOLATORS.put(Double.POSITIVE_INFINITY, STEP);
  }

  @Test
  public void testEqualsAndHashCode() {
    InterpolatedDoubleDoubleCurve curve = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, LINEAR, false, NAME1);
    InterpolatedDoubleDoubleCurve other = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, LINEAR, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, LINEAR, false);
    assertFalse(curve.equals(other));
    other = new InterpolatedDoubleDoubleCurve(Y_PRIMITIVE, Y_PRIMITIVE, LINEAR, false, NAME1);
    assertFalse(curve.equals(other));
    other = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE, X_PRIMITIVE, LINEAR, false, NAME1);
    assertFalse(curve.equals(other));
    other = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, STEP, false, NAME1);
    assertFalse(curve.equals(other));
    other = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, LINEAR, true, NAME1);
    assertFalse(curve.equals(other));
    other = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, LINEAR, false);
    assertFalse(curve.equals(other));
    other = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, INTERPOLATOR, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, LINEAR, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoubleDoubleCurve(X_OBJECT, Y_OBJECT, LINEAR, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoubleDoubleCurve(X_OBJECT_SORTED, Y_OBJECT_SORTED, LINEAR, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoubleDoubleCurve(MAP, LINEAR, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoubleDoubleCurve(MAP_SORTED, LINEAR, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoubleDoubleCurve(PAIR_ARRAY, LINEAR, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoubleDoubleCurve(PAIR_ARRAY_SORTED, LINEAR, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoubleDoubleCurve(PAIR_SET, LINEAR, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoubleDoubleCurve(PAIR_SET_SORTED, LINEAR, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    curve = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, INTERPOLATOR, false, NAME1);
    other = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, INTERPOLATOR, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, INTERPOLATOR, false);
    assertFalse(curve.equals(other));
    other = new InterpolatedDoubleDoubleCurve(Y_PRIMITIVE, Y_PRIMITIVE, INTERPOLATOR, false, NAME1);
    assertFalse(curve.equals(other));
    other = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE, X_PRIMITIVE, INTERPOLATOR, false, NAME1);
    assertFalse(curve.equals(other));
    other = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, INTERPOLATORS, false, NAME1);
    assertFalse(curve.equals(other));
    other = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, INTERPOLATOR, true, NAME1);
    assertFalse(curve.equals(other));
    other = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, INTERPOLATOR, false);
    assertFalse(curve.equals(other));
    other = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, INTERPOLATOR, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoubleDoubleCurve(X_OBJECT, Y_OBJECT, INTERPOLATOR, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoubleDoubleCurve(X_OBJECT_SORTED, Y_OBJECT_SORTED, INTERPOLATOR, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoubleDoubleCurve(MAP, INTERPOLATOR, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoubleDoubleCurve(MAP_SORTED, INTERPOLATOR, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoubleDoubleCurve(PAIR_ARRAY, INTERPOLATOR, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoubleDoubleCurve(PAIR_ARRAY_SORTED, INTERPOLATOR, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoubleDoubleCurve(PAIR_SET, INTERPOLATOR, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoubleDoubleCurve(PAIR_SET_SORTED, INTERPOLATOR, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
  }

  @Test
  public void testStaticConstruction() {
    InterpolatedDoubleDoubleCurve curve = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, LINEAR, false, NAME1);
    InterpolatedDoubleDoubleCurve other = InterpolatedDoubleDoubleCurve.from(X_PRIMITIVE, Y_PRIMITIVE, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoubleDoubleCurve(X_OBJECT, Y_OBJECT, LINEAR, false, NAME1);
    other = InterpolatedDoubleDoubleCurve.from(X_OBJECT, Y_OBJECT, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoubleDoubleCurve(MAP, LINEAR, false, NAME1);
    other = InterpolatedDoubleDoubleCurve.from(MAP, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoubleDoubleCurve(PAIR_ARRAY, LINEAR, false, NAME1);
    other = InterpolatedDoubleDoubleCurve.from(PAIR_ARRAY, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoubleDoubleCurve(PAIR_SET, LINEAR, false, NAME1);
    other = InterpolatedDoubleDoubleCurve.from(PAIR_SET, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, INTERPOLATORS, false, NAME1);
    other = InterpolatedDoubleDoubleCurve.from(X_PRIMITIVE, Y_PRIMITIVE, INTERPOLATORS, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoubleDoubleCurve(X_OBJECT, Y_OBJECT, INTERPOLATORS, false, NAME1);
    other = InterpolatedDoubleDoubleCurve.from(X_OBJECT, Y_OBJECT, INTERPOLATORS, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoubleDoubleCurve(MAP, INTERPOLATORS, false, NAME1);
    other = InterpolatedDoubleDoubleCurve.from(MAP, INTERPOLATORS, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoubleDoubleCurve(PAIR_ARRAY, INTERPOLATORS, false, NAME1);
    other = InterpolatedDoubleDoubleCurve.from(PAIR_ARRAY, INTERPOLATORS, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoubleDoubleCurve(PAIR_SET, INTERPOLATORS, false, NAME1);
    other = InterpolatedDoubleDoubleCurve.from(PAIR_SET, INTERPOLATORS, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, LINEAR, true, NAME1);
    other = InterpolatedDoubleDoubleCurve.fromSorted(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoubleDoubleCurve(X_OBJECT_SORTED, Y_OBJECT_SORTED, LINEAR, true, NAME1);
    other = InterpolatedDoubleDoubleCurve.fromSorted(X_OBJECT_SORTED, Y_OBJECT_SORTED, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoubleDoubleCurve(MAP_SORTED, LINEAR, true, NAME1);
    other = InterpolatedDoubleDoubleCurve.fromSorted(MAP_SORTED, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoubleDoubleCurve(PAIR_ARRAY_SORTED, LINEAR, true, NAME1);
    other = InterpolatedDoubleDoubleCurve.fromSorted(PAIR_ARRAY_SORTED, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoubleDoubleCurve(PAIR_SET_SORTED, LINEAR, true, NAME1);
    other = InterpolatedDoubleDoubleCurve.fromSorted(PAIR_SET_SORTED, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, INTERPOLATORS, true, NAME1);
    other = InterpolatedDoubleDoubleCurve.fromSorted(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, INTERPOLATORS, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoubleDoubleCurve(X_OBJECT_SORTED, Y_OBJECT_SORTED, INTERPOLATORS, true, NAME1);
    other = InterpolatedDoubleDoubleCurve.fromSorted(X_OBJECT_SORTED, Y_OBJECT_SORTED, INTERPOLATORS, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoubleDoubleCurve(MAP_SORTED, INTERPOLATORS, true, NAME1);
    other = InterpolatedDoubleDoubleCurve.fromSorted(MAP_SORTED, INTERPOLATORS, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoubleDoubleCurve(PAIR_ARRAY_SORTED, INTERPOLATORS, true, NAME1);
    other = InterpolatedDoubleDoubleCurve.fromSorted(PAIR_ARRAY_SORTED, INTERPOLATORS, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoubleDoubleCurve(PAIR_SET_SORTED, INTERPOLATORS, true, NAME1);
    other = InterpolatedDoubleDoubleCurve.fromSorted(PAIR_SET_SORTED, INTERPOLATORS, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, LINEAR, false);
    other = InterpolatedDoubleDoubleCurve.from(X_PRIMITIVE, Y_PRIMITIVE, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoubleDoubleCurve(X_OBJECT, Y_OBJECT, LINEAR, false);
    other = InterpolatedDoubleDoubleCurve.from(X_OBJECT, Y_OBJECT, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoubleDoubleCurve(MAP, LINEAR, false);
    other = InterpolatedDoubleDoubleCurve.from(MAP, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoubleDoubleCurve(PAIR_ARRAY, LINEAR, false);
    other = InterpolatedDoubleDoubleCurve.from(PAIR_ARRAY, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoubleDoubleCurve(PAIR_SET, LINEAR, false);
    other = InterpolatedDoubleDoubleCurve.from(PAIR_SET, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, INTERPOLATORS, false);
    other = InterpolatedDoubleDoubleCurve.from(X_PRIMITIVE, Y_PRIMITIVE, INTERPOLATORS);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoubleDoubleCurve(X_OBJECT, Y_OBJECT, INTERPOLATORS, false);
    other = InterpolatedDoubleDoubleCurve.from(X_OBJECT, Y_OBJECT, INTERPOLATORS);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoubleDoubleCurve(MAP, INTERPOLATORS, false);
    other = InterpolatedDoubleDoubleCurve.from(MAP, INTERPOLATORS);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoubleDoubleCurve(PAIR_ARRAY, INTERPOLATORS, false);
    other = InterpolatedDoubleDoubleCurve.from(PAIR_ARRAY, INTERPOLATORS);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoubleDoubleCurve(PAIR_SET, INTERPOLATORS, false);
    other = InterpolatedDoubleDoubleCurve.from(PAIR_SET, INTERPOLATORS);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, LINEAR, true);
    other = InterpolatedDoubleDoubleCurve.fromSorted(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoubleDoubleCurve(X_OBJECT_SORTED, Y_OBJECT_SORTED, LINEAR, true);
    other = InterpolatedDoubleDoubleCurve.fromSorted(X_OBJECT_SORTED, Y_OBJECT_SORTED, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoubleDoubleCurve(MAP_SORTED, LINEAR, true);
    other = InterpolatedDoubleDoubleCurve.fromSorted(MAP_SORTED, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoubleDoubleCurve(PAIR_ARRAY_SORTED, LINEAR, true);
    other = InterpolatedDoubleDoubleCurve.fromSorted(PAIR_ARRAY_SORTED, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoubleDoubleCurve(PAIR_SET_SORTED, LINEAR, true);
    other = InterpolatedDoubleDoubleCurve.fromSorted(PAIR_SET_SORTED, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoubleDoubleCurve(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, INTERPOLATORS, true);
    other = InterpolatedDoubleDoubleCurve.fromSorted(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, INTERPOLATORS);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoubleDoubleCurve(X_OBJECT_SORTED, Y_OBJECT_SORTED, INTERPOLATORS, true);
    other = InterpolatedDoubleDoubleCurve.fromSorted(X_OBJECT_SORTED, Y_OBJECT_SORTED, INTERPOLATORS);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoubleDoubleCurve(MAP_SORTED, INTERPOLATORS, true);
    other = InterpolatedDoubleDoubleCurve.fromSorted(MAP_SORTED, INTERPOLATORS);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoubleDoubleCurve(PAIR_ARRAY_SORTED, INTERPOLATORS, true);
    other = InterpolatedDoubleDoubleCurve.fromSorted(PAIR_ARRAY_SORTED, INTERPOLATORS);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoubleDoubleCurve(PAIR_SET_SORTED, INTERPOLATORS, true);
    other = InterpolatedDoubleDoubleCurve.fromSorted(PAIR_SET_SORTED, INTERPOLATORS);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
  }

  @Test
  public void testGetters() {
    final InterpolatedDoubleDoubleCurve curve = InterpolatedDoubleDoubleCurve.from(PAIR_SET, EXPONENTIAL, NAME1);
    assertEquals(curve.getName(), NAME1);
    assertArrayEquals(curve.getXData(), X_OBJECT_SORTED);
    assertArrayEquals(curve.getXDataAsPrimitive(), X_PRIMITIVE_SORTED, 0);
    assertArrayEquals(curve.getYData(), Y_OBJECT_SORTED);
    assertArrayEquals(curve.getYDataAsPrimitive(), Y_PRIMITIVE_SORTED, 0);

  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonExtrapolatingInterpolator1() {
    final InterpolatedDoubleDoubleCurve curve = InterpolatedDoubleDoubleCurve.from(MAP, LINEAR, NAME1);
    curve.getYValue(-20.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonExtrapolatingInterpolator2() {
    final InterpolatedDoubleDoubleCurve curve = InterpolatedDoubleDoubleCurve.from(MAP, LINEAR, NAME1);
    curve.getYValue(120.);
  }

  @Test
  public void testGetYValueSingleInterpolator() {
    InterpolatedDoubleDoubleCurve curve = InterpolatedDoubleDoubleCurve.from(MAP, LINEAR, NAME1);
    assertEquals(curve.getYValue(2.), 6, 0);
    for (double i = 0; i < 9; i += 0.2) {
      assertEquals(curve.getYValue(i), 3 * i, EPS);
    }
    curve = InterpolatedDoubleDoubleCurve.from(MAP, INTERPOLATOR, NAME1);
    assertEquals(curve.getYValue(2.), 6, 0);
    for (double i = 0; i < 9; i += 0.2) {
      assertEquals(curve.getYValue(i), 3 * i, EPS);
    }
  }

  @Test
  public void testGetYValueManyInterpolators() {
    final InterpolatedDoubleDoubleCurve curve = InterpolatedDoubleDoubleCurve.from(MAP, INTERPOLATORS, NAME1);
    for (double i = 0; i < 6; i += 1) {
      assertEquals(curve.getYValue(i), 3 * i, EPS);
    }
    for (double i = 6; i < 9; i += 1) {
      assertEquals(curve.getYValue(i), 3 * Math.floor(i), EPS);
    }
    for (double i = 0; i <= 5.5; i += 0.1) {
      assertEquals(curve.getYValue(i), 3 * i, EPS);
    }
    for (double i = 5.6; i < 9; i += 0.1) {
      assertEquals(curve.getYValue(i), 3 * Math.floor(i), EPS);
    }
  }

}
