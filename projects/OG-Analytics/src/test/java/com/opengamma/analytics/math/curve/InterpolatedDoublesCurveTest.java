/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.interpolation.PCHIPInterpolator1D;
import com.opengamma.analytics.math.interpolation.StepInterpolator1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class InterpolatedDoublesCurveTest extends DoublesCurveTestCase {
  private static final Interpolator1D LINEAR = new LinearInterpolator1D();
  private static final Interpolator1D STEP = new StepInterpolator1D();
  private static final Interpolator1D PCHIP = new PCHIPInterpolator1D();
  private static final double EPS = 1e-15;

  @Test
  public void testEqualsAndHashCode() {
    final InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(X_PRIMITIVE, Y_PRIMITIVE, LINEAR, false, NAME1);
    InterpolatedDoublesCurve other = new InterpolatedDoublesCurve(X_PRIMITIVE, Y_PRIMITIVE, LINEAR, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoublesCurve(X_PRIMITIVE, Y_PRIMITIVE, LINEAR, false);
    assertFalse(curve.equals(other));
    other = new InterpolatedDoublesCurve(Y_PRIMITIVE, Y_PRIMITIVE, LINEAR, false, NAME1);
    assertFalse(curve.equals(other));
    other = new InterpolatedDoublesCurve(X_PRIMITIVE, X_PRIMITIVE, LINEAR, false, NAME1);
    assertFalse(curve.equals(other));
    other = new InterpolatedDoublesCurve(X_PRIMITIVE, Y_PRIMITIVE, STEP, false, NAME1);
    assertFalse(curve.equals(other));
    other = new InterpolatedDoublesCurve(X_PRIMITIVE, Y_PRIMITIVE, LINEAR, true, NAME1);
    assertFalse(curve.equals(other));
    other = new InterpolatedDoublesCurve(X_PRIMITIVE, Y_PRIMITIVE, LINEAR, false);
    assertFalse(curve.equals(other));
    other = new InterpolatedDoublesCurve(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, LINEAR, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoublesCurve(X_OBJECT, Y_OBJECT, LINEAR, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoublesCurve(X_OBJECT_SORTED, Y_OBJECT_SORTED, LINEAR, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoublesCurve(MAP, LINEAR, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoublesCurve(MAP_SORTED, LINEAR, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoublesCurve(PAIR_ARRAY, LINEAR, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoublesCurve(PAIR_ARRAY_SORTED, LINEAR, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoublesCurve(PAIR_SET, LINEAR, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoublesCurve(PAIR_SET_SORTED, LINEAR, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoublesCurve(X_LIST, Y_LIST, LINEAR, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoublesCurve(X_LIST_SORTED, Y_LIST_SORTED, LINEAR, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoublesCurve(PAIR_LIST, LINEAR, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new InterpolatedDoublesCurve(PAIR_LIST_SORTED, LINEAR, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
  }

  @Test
  public void testStaticConstruction() {
    InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(X_PRIMITIVE, Y_PRIMITIVE, LINEAR, false, NAME1);
    InterpolatedDoublesCurve other = InterpolatedDoublesCurve.from(X_PRIMITIVE, Y_PRIMITIVE, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoublesCurve(X_OBJECT, Y_OBJECT, LINEAR, false, NAME1);
    other = InterpolatedDoublesCurve.from(X_OBJECT, Y_OBJECT, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoublesCurve(MAP, LINEAR, false, NAME1);
    other = InterpolatedDoublesCurve.from(MAP, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoublesCurve(PAIR_ARRAY, LINEAR, false, NAME1);
    other = InterpolatedDoublesCurve.from(PAIR_ARRAY, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoublesCurve(PAIR_SET, LINEAR, false, NAME1);
    other = InterpolatedDoublesCurve.from(PAIR_SET, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoublesCurve(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, LINEAR, true, NAME1);
    other = InterpolatedDoublesCurve.fromSorted(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoublesCurve(X_OBJECT_SORTED, Y_OBJECT_SORTED, LINEAR, true, NAME1);
    other = InterpolatedDoublesCurve.fromSorted(X_OBJECT_SORTED, Y_OBJECT_SORTED, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoublesCurve(MAP_SORTED, LINEAR, true, NAME1);
    other = InterpolatedDoublesCurve.fromSorted(MAP_SORTED, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoublesCurve(PAIR_ARRAY_SORTED, LINEAR, true, NAME1);
    other = InterpolatedDoublesCurve.fromSorted(PAIR_ARRAY_SORTED, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoublesCurve(PAIR_SET_SORTED, LINEAR, true, NAME1);
    other = InterpolatedDoublesCurve.fromSorted(PAIR_SET_SORTED, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoublesCurve(X_LIST, Y_LIST, LINEAR, false, NAME1);
    other = InterpolatedDoublesCurve.from(X_LIST, Y_LIST, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoublesCurve(X_LIST_SORTED, Y_LIST_SORTED, LINEAR, true, NAME1);
    other = InterpolatedDoublesCurve.fromSorted(X_LIST_SORTED, Y_LIST_SORTED, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoublesCurve(PAIR_LIST, LINEAR, false, NAME1);
    other = InterpolatedDoublesCurve.from(PAIR_LIST, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoublesCurve(PAIR_LIST_SORTED, LINEAR, true, NAME1);
    other = InterpolatedDoublesCurve.fromSorted(PAIR_LIST_SORTED, LINEAR, NAME1);
    assertEquals(curve, other);
    curve = new InterpolatedDoublesCurve(X_PRIMITIVE, Y_PRIMITIVE, LINEAR, false);
    other = InterpolatedDoublesCurve.from(X_PRIMITIVE, Y_PRIMITIVE, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoublesCurve(X_OBJECT, Y_OBJECT, LINEAR, false);
    other = InterpolatedDoublesCurve.from(X_OBJECT, Y_OBJECT, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoublesCurve(MAP, LINEAR, false);
    other = InterpolatedDoublesCurve.from(MAP, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoublesCurve(PAIR_ARRAY, LINEAR, false);
    other = InterpolatedDoublesCurve.from(PAIR_ARRAY, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoublesCurve(PAIR_SET, LINEAR, false);
    other = InterpolatedDoublesCurve.from(PAIR_SET, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoublesCurve(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, LINEAR, true);
    other = InterpolatedDoublesCurve.fromSorted(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoublesCurve(X_OBJECT_SORTED, Y_OBJECT_SORTED, LINEAR, true);
    other = InterpolatedDoublesCurve.fromSorted(X_OBJECT_SORTED, Y_OBJECT_SORTED, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoublesCurve(MAP_SORTED, LINEAR, true);
    other = InterpolatedDoublesCurve.fromSorted(MAP_SORTED, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoublesCurve(PAIR_ARRAY_SORTED, LINEAR, true);
    other = InterpolatedDoublesCurve.fromSorted(PAIR_ARRAY_SORTED, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoublesCurve(PAIR_SET_SORTED, LINEAR, true);
    other = InterpolatedDoublesCurve.fromSorted(PAIR_SET_SORTED, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoublesCurve(X_LIST, Y_LIST, LINEAR, false);
    other = InterpolatedDoublesCurve.from(X_LIST, Y_LIST, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoublesCurve(X_LIST_SORTED, Y_LIST_SORTED, LINEAR, true);
    other = InterpolatedDoublesCurve.fromSorted(X_LIST_SORTED, Y_LIST_SORTED, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoublesCurve(PAIR_LIST, LINEAR, false);
    other = InterpolatedDoublesCurve.from(PAIR_LIST, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new InterpolatedDoublesCurve(PAIR_LIST_SORTED, LINEAR, true);
    other = InterpolatedDoublesCurve.fromSorted(PAIR_LIST_SORTED, LINEAR);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
  }

  @Test
  public void testGetters() {
    final InterpolatedDoublesCurve curve = InterpolatedDoublesCurve.from(PAIR_SET, PCHIP, NAME1);
    assertEquals(curve.getName(), NAME1);
    assertArrayEquals(curve.getXData(), X_OBJECT_SORTED);
    assertArrayEquals(curve.getXDataAsPrimitive(), X_PRIMITIVE_SORTED, 0);
    assertArrayEquals(curve.getYData(), Y_OBJECT_SORTED);
    assertArrayEquals(curve.getYDataAsPrimitive(), Y_PRIMITIVE_SORTED, 0);
    assertEquals(curve.getInterpolator(), PCHIP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNonExtrapolatingInterpolator1() {
    final InterpolatedDoublesCurve curve = InterpolatedDoublesCurve.from(MAP, LINEAR, NAME1);
    curve.getYValue(-20.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNonExtrapolatingInterpolator2() {
    final InterpolatedDoublesCurve curve = InterpolatedDoublesCurve.from(MAP, LINEAR, NAME1);
    curve.getYValue(120.);
  }

  @Test
  public void testGetYValueSingleInterpolator() {
    InterpolatedDoublesCurve curve = InterpolatedDoublesCurve.from(MAP, LINEAR, NAME1);
    assertEquals(curve.getYValue(2.), 6, 0);
    for (double i = 0; i < 9; i += 0.2) {
      assertEquals(curve.getYValue(i), 3 * i, EPS);
    }
    curve = InterpolatedDoublesCurve.from(MAP, LINEAR, NAME1);
    assertEquals(curve.getYValue(2.), 6, 0);
    for (double i = 0; i < 9; i += 0.2) {
      assertEquals(curve.getYValue(i), 3 * i, EPS);
    }
  }

  @Test
  public void testGetYValueManyInterpolators() {
    InterpolatedDoublesCurve curve = InterpolatedDoublesCurve.from(MAP, LINEAR, NAME1);
    for (double i = 0; i < 6; i += 1) {
      assertEquals(curve.getYValue(i), 3 * i, EPS);
    }
    for (double i = 0; i <= 5.5; i += 0.1) {
      assertEquals(curve.getYValue(i), 3 * i, EPS);
    }
    curve = InterpolatedDoublesCurve.from(MAP, STEP, NAME1);
    for (double i = 6; i < 9; i += 1) {
      assertEquals(curve.getYValue(i), 3 * Math.floor(i), EPS);
    }
    for (double i = 5.6; i < 9; i += 0.1) {
      assertEquals(curve.getYValue(i), 3 * Math.floor(i), EPS);
    }
  }

}
