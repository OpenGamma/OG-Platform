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

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NodalDoublesCurveTest extends DoublesCurveTestCase {

  @Test
  public void testEqualsAndHashCode() {
    final NodalDoublesCurve curve = new NodalDoublesCurve(X_PRIMITIVE, Y_PRIMITIVE, false, NAME1);
    NodalDoublesCurve other = new NodalDoublesCurve(X_PRIMITIVE, Y_PRIMITIVE, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoublesCurve(X_PRIMITIVE, Y_PRIMITIVE, false);
    assertFalse(curve.equals(other));
    other = new NodalDoublesCurve(Y_PRIMITIVE, Y_PRIMITIVE, false, NAME1);
    assertFalse(curve.equals(other));
    other = new NodalDoublesCurve(X_PRIMITIVE, X_PRIMITIVE, false, NAME1);
    assertFalse(curve.equals(other));
    other = new NodalDoublesCurve(X_PRIMITIVE, Y_PRIMITIVE, true, NAME1);
    assertFalse(curve.equals(other));
    other = new NodalDoublesCurve(X_PRIMITIVE, Y_PRIMITIVE, false, NAME2);
    assertFalse(curve.equals(other));
    other = new NodalDoublesCurve(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoublesCurve(X_OBJECT, Y_OBJECT, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoublesCurve(X_OBJECT_SORTED, Y_OBJECT_SORTED, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoublesCurve(MAP, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoublesCurve(MAP_SORTED, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoublesCurve(PAIR_ARRAY, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoublesCurve(PAIR_ARRAY_SORTED, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoublesCurve(PAIR_SET, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoublesCurve(PAIR_SET_SORTED, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoublesCurve(X_LIST, Y_LIST, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoublesCurve(X_LIST_SORTED, Y_LIST_SORTED, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoublesCurve(PAIR_LIST, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalDoublesCurve(PAIR_LIST_SORTED, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
  }

  @Test
  public void testStaticConstruction() {
    NodalDoublesCurve curve = new NodalDoublesCurve(X_PRIMITIVE, Y_PRIMITIVE, false, NAME1);
    NodalDoublesCurve other = NodalDoublesCurve.from(X_PRIMITIVE, Y_PRIMITIVE, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoublesCurve(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, true, NAME1);
    other = NodalDoublesCurve.fromSorted(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoublesCurve(X_OBJECT, Y_OBJECT, false, NAME1);
    other = NodalDoublesCurve.from(X_OBJECT, Y_OBJECT, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoublesCurve(X_OBJECT_SORTED, Y_OBJECT_SORTED, true, NAME1);
    other = NodalDoublesCurve.fromSorted(X_OBJECT_SORTED, Y_OBJECT_SORTED, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoublesCurve(MAP, false, NAME1);
    other = NodalDoublesCurve.from(MAP, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoublesCurve(MAP_SORTED, true, NAME1);
    other = NodalDoublesCurve.fromSorted(MAP_SORTED, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoublesCurve(PAIR_ARRAY, false, NAME1);
    other = NodalDoublesCurve.from(PAIR_ARRAY, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoublesCurve(PAIR_ARRAY_SORTED, true, NAME1);
    other = NodalDoublesCurve.fromSorted(PAIR_ARRAY_SORTED, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoublesCurve(PAIR_SET, false, NAME1);
    other = NodalDoublesCurve.from(PAIR_SET, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoublesCurve(PAIR_SET_SORTED, true, NAME1);
    other = NodalDoublesCurve.fromSorted(PAIR_SET_SORTED, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoublesCurve(X_LIST, Y_LIST, false, NAME1);
    other = NodalDoublesCurve.from(X_LIST, Y_LIST, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoublesCurve(X_LIST_SORTED, Y_LIST_SORTED, true, NAME1);
    other = NodalDoublesCurve.fromSorted(X_LIST_SORTED, Y_LIST_SORTED, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoublesCurve(PAIR_LIST, false, NAME1);
    other = NodalDoublesCurve.from(PAIR_LIST, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoublesCurve(PAIR_LIST_SORTED, true, NAME1);
    other = NodalDoublesCurve.fromSorted(PAIR_LIST_SORTED, NAME1);
    assertEquals(curve, other);
    curve = new NodalDoublesCurve(X_PRIMITIVE, Y_PRIMITIVE, false);
    other = NodalDoublesCurve.from(X_PRIMITIVE, Y_PRIMITIVE);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoublesCurve(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED, true);
    other = NodalDoublesCurve.fromSorted(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoublesCurve(X_OBJECT, Y_OBJECT, false);
    other = NodalDoublesCurve.from(X_OBJECT, Y_OBJECT);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoublesCurve(X_OBJECT_SORTED, Y_OBJECT_SORTED, true);
    other = NodalDoublesCurve.fromSorted(X_OBJECT_SORTED, Y_OBJECT_SORTED);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoublesCurve(MAP, false);
    other = NodalDoublesCurve.from(MAP);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoublesCurve(MAP_SORTED, true);
    other = NodalDoublesCurve.fromSorted(MAP_SORTED);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoublesCurve(PAIR_ARRAY, false);
    other = NodalDoublesCurve.from(PAIR_ARRAY);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoublesCurve(PAIR_ARRAY_SORTED, true);
    other = NodalDoublesCurve.fromSorted(PAIR_ARRAY_SORTED);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoublesCurve(PAIR_SET, false);
    other = NodalDoublesCurve.from(PAIR_SET);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoublesCurve(PAIR_SET_SORTED, true);
    other = NodalDoublesCurve.fromSorted(PAIR_SET_SORTED);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoublesCurve(X_LIST, Y_LIST, false);
    other = NodalDoublesCurve.from(X_LIST, Y_LIST);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoublesCurve(X_LIST_SORTED, Y_LIST_SORTED, true);
    other = NodalDoublesCurve.fromSorted(X_LIST_SORTED, Y_LIST_SORTED);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoublesCurve(PAIR_LIST, false);
    other = NodalDoublesCurve.from(PAIR_LIST);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    curve = new NodalDoublesCurve(PAIR_LIST_SORTED, true);
    other = NodalDoublesCurve.fromSorted(PAIR_LIST_SORTED);
    assertArrayEquals(curve.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(curve.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
  }

  @Test
  public void testGetters() {
    final NodalDoublesCurve curve = NodalDoublesCurve.from(PAIR_ARRAY, NAME1);
    assertEquals(curve.getName(), NAME1);
    assertArrayEquals(curve.getXData(), X_OBJECT_SORTED);
    assertArrayEquals(curve.getXDataAsPrimitive(), X_PRIMITIVE_SORTED, 0);
    assertArrayEquals(curve.getYData(), Y_OBJECT_SORTED);
    assertArrayEquals(curve.getYDataAsPrimitive(), Y_PRIMITIVE_SORTED, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNonNodalPoint() {
    NodalDoublesCurve.from(MAP).getYValue(3.5);
  }

  @Test
  public void testGetYValue() {
    final NodalDoublesCurve curve = NodalDoublesCurve.from(PAIR_ARRAY, NAME1);
    for (int i = 0; i < 10; i++) {
      assertEquals(curve.getYValue(X_PRIMITIVE[i]), Y_PRIMITIVE[i], 0);
    }
  }
}
