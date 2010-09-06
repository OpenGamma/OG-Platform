/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 */
public class NodalDoubleDoubleCurveTest extends DoubleDoubleCurveTestCase {

  @Test
  public void testEqualsAndHashCode() {
    final NodalDoubleDoubleCurve curve = new NodalDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, false, NAME1);
    NodalDoubleDoubleCurve other = new NodalDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, false);
    assertFalse(curve.equals(other));
    other = new NodalDoubleDoubleCurve(Y_PRIMITIVE, Y_PRIMITIVE, false, NAME1);
    assertFalse(curve.equals(other));
    other = new NodalDoubleDoubleCurve(X_PRIMITIVE, X_PRIMITIVE, false, NAME1);
    assertFalse(curve.equals(other));
    other = new NodalDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, true, NAME1);
    assertFalse(curve.equals(other));
    other = new NodalDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, false, NAME2);
    assertFalse(curve.equals(other));
    other = new NodalDoubleDoubleCurve(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoubleDoubleCurve(X_OBJECT, Y_OBJECT, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoubleDoubleCurve(X_OBJECT_SORTED, Y_OBJECT_SORTED, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoubleDoubleCurve(MAP, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoubleDoubleCurve(MAP_SORTED, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoubleDoubleCurve(PAIR_ARRAY, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoubleDoubleCurve(PAIR_ARRAY_SORTED, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoubleDoubleCurve(PAIR_SET, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoubleDoubleCurve(PAIR_SET_SORTED, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
  }

  @Test
  public void testStaticConstruction() {
    NodalDoubleDoubleCurve curve = new NodalDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, false, NAME1);
    NodalDoubleDoubleCurve other = NodalDoubleDoubleCurve.of(X_PRIMITIVE, Y_PRIMITIVE, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoubleDoubleCurve(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, true, NAME1);
    other = NodalDoubleDoubleCurve.ofSorted(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoubleDoubleCurve(X_OBJECT, Y_OBJECT, false, NAME1);
    other = NodalDoubleDoubleCurve.of(X_OBJECT, Y_OBJECT, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoubleDoubleCurve(X_OBJECT_SORTED, Y_OBJECT_SORTED, true, NAME1);
    other = NodalDoubleDoubleCurve.ofSorted(X_OBJECT_SORTED, Y_OBJECT_SORTED, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoubleDoubleCurve(MAP, false, NAME1);
    other = NodalDoubleDoubleCurve.of(MAP, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoubleDoubleCurve(MAP_SORTED, true, NAME1);
    other = NodalDoubleDoubleCurve.ofSorted(MAP_SORTED, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoubleDoubleCurve(PAIR_ARRAY, false, NAME1);
    other = NodalDoubleDoubleCurve.of(PAIR_ARRAY, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoubleDoubleCurve(PAIR_ARRAY_SORTED, true, NAME1);
    other = NodalDoubleDoubleCurve.ofSorted(PAIR_ARRAY_SORTED, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoubleDoubleCurve(PAIR_SET, false, NAME1);
    other = NodalDoubleDoubleCurve.of(PAIR_SET, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoubleDoubleCurve(PAIR_SET_SORTED, true, NAME1);
    other = NodalDoubleDoubleCurve.ofSorted(PAIR_SET_SORTED, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoubleDoubleCurve(X_PRIMITIVE, Y_PRIMITIVE, false);
    other = NodalDoubleDoubleCurve.of(X_PRIMITIVE, Y_PRIMITIVE);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoubleDoubleCurve(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, true);
    other = NodalDoubleDoubleCurve.ofSorted(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoubleDoubleCurve(X_OBJECT, Y_OBJECT, false);
    other = NodalDoubleDoubleCurve.of(X_OBJECT, Y_OBJECT);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoubleDoubleCurve(X_OBJECT_SORTED, Y_OBJECT_SORTED, true);
    other = NodalDoubleDoubleCurve.ofSorted(X_OBJECT_SORTED, Y_OBJECT_SORTED);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoubleDoubleCurve(MAP, false);
    other = NodalDoubleDoubleCurve.of(MAP);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoubleDoubleCurve(MAP_SORTED, true);
    other = NodalDoubleDoubleCurve.ofSorted(MAP_SORTED);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoubleDoubleCurve(PAIR_ARRAY, false);
    other = NodalDoubleDoubleCurve.of(PAIR_ARRAY);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoubleDoubleCurve(PAIR_ARRAY_SORTED, true);
    other = NodalDoubleDoubleCurve.ofSorted(PAIR_ARRAY_SORTED);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoubleDoubleCurve(PAIR_SET, false);
    other = NodalDoubleDoubleCurve.of(PAIR_SET);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoubleDoubleCurve(PAIR_SET_SORTED, true);
    other = NodalDoubleDoubleCurve.ofSorted(PAIR_SET_SORTED);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
  }

  @Test
  public void testGetters() {
    final NodalDoubleDoubleCurve curve = NodalDoubleDoubleCurve.of(PAIR_ARRAY, NAME1);
    assertEquals(curve.getName(), NAME1);
    assertArrayEquals(curve.getXData(), X_OBJECT_SORTED);
    assertArrayEquals(curve.getXDataAsPrimitive(), X_PRIMITIVE_SORTED, 0);
    assertArrayEquals(curve.getYData(), Y_OBJECT_SORTED);
    assertArrayEquals(curve.getYDataAsPrimitive(), Y_PRIMITIVE_SORTED, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonNodalPoint() {
    NodalDoubleDoubleCurve.of(MAP).getYValue(3.5);
  }

  @Test
  public void testGetYValue() {
    final NodalDoubleDoubleCurve curve = NodalDoubleDoubleCurve.of(PAIR_ARRAY, NAME1);
    for (int i = 0; i < 10; i++) {
      assertEquals(curve.getYValue(X_PRIMITIVE[i]), Y_PRIMITIVE[i], 0);
    }
  }
}
