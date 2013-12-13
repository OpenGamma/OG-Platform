/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NodalDoublesSurfaceTest extends DoublesSurfaceTestCase {
  private static final NodalDoublesSurface SURFACE = new NodalDoublesSurface(XYZ_LIST);

  @Test
  public void testEqualsAndHashCode() {
    final NodalDoublesSurface surface = new NodalDoublesSurface(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, NAME);
    NodalDoublesSurface other = new NodalDoublesSurface(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, NAME);
    assertEquals(surface, other);
    assertEquals(surface.hashCode(), other.hashCode());
    other = new NodalDoublesSurface(Y_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, NAME);
    assertFalse(other.equals(surface));
    other = new NodalDoublesSurface(X_PRIMITIVE, X_PRIMITIVE, Z_PRIMITIVE, NAME);
    assertFalse(other.equals(surface));
    other = new NodalDoublesSurface(X_PRIMITIVE, Y_PRIMITIVE, Y_PRIMITIVE, NAME);
    assertFalse(other.equals(surface));
    other = new NodalDoublesSurface(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, "P");
    assertFalse(other.equals(surface));
    other = new NodalDoublesSurface(X_OBJECT, Y_OBJECT, Z_OBJECT, NAME);
    assertEquals(surface, other);
    other = new NodalDoublesSurface(X_LIST, Y_LIST, Z_LIST, NAME);
    assertEquals(surface, other);
    other = new NodalDoublesSurface(XY_PAIR, Z_PRIMITIVE, NAME);
    assertEquals(surface, other);
    other = new NodalDoublesSurface(XY_PAIR, Z_OBJECT, NAME);
    assertEquals(surface, other);
    other = new NodalDoublesSurface(XY_PAIR_LIST, Z_LIST, NAME);
    assertEquals(surface, other);
    other = new NodalDoublesSurface(XYZ_MAP, NAME);
    assertEquals(surface, other);
    other = new NodalDoublesSurface(XYZ_LIST, NAME);
    assertEquals(surface, other);
    other = new NodalDoublesSurface(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    other = new NodalDoublesSurface(X_OBJECT, Y_OBJECT, Z_OBJECT);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    other = new NodalDoublesSurface(X_LIST, Y_LIST, Z_LIST);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    other = new NodalDoublesSurface(XY_PAIR, Z_PRIMITIVE);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    other = new NodalDoublesSurface(XY_PAIR, Z_OBJECT);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    other = new NodalDoublesSurface(XY_PAIR_LIST, Z_LIST);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    other = new NodalDoublesSurface(XYZ_MAP);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    other = new NodalDoublesSurface(XYZ_LIST);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
  }

  @Test
  public void testStaticConstruction() {
    NodalDoublesSurface surface = new NodalDoublesSurface(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, NAME);
    NodalDoublesSurface other = NodalDoublesSurface.from(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, NAME);
    assertEquals(surface, other);
    surface = new NodalDoublesSurface(X_OBJECT, Y_OBJECT, Z_OBJECT, NAME);
    other = NodalDoublesSurface.from(X_OBJECT, Y_OBJECT, Z_OBJECT, NAME);
    assertEquals(surface, other);
    surface = new NodalDoublesSurface(X_LIST, Y_LIST, Z_LIST, NAME);
    other = NodalDoublesSurface.from(X_LIST, Y_LIST, Z_LIST, NAME);
    assertEquals(surface, other);
    surface = new NodalDoublesSurface(XY_PAIR, Z_PRIMITIVE, NAME);
    other = NodalDoublesSurface.from(XY_PAIR, Z_PRIMITIVE, NAME);
    assertEquals(surface, other);
    surface = new NodalDoublesSurface(XY_PAIR, Z_OBJECT, NAME);
    other = NodalDoublesSurface.from(XY_PAIR, Z_OBJECT, NAME);
    assertEquals(surface, other);
    surface = new NodalDoublesSurface(XY_PAIR_LIST, Z_LIST, NAME);
    other = NodalDoublesSurface.from(XY_PAIR_LIST, Z_LIST, NAME);
    assertEquals(surface, other);
    surface = new NodalDoublesSurface(XYZ_MAP, NAME);
    other = NodalDoublesSurface.from(XYZ_MAP, NAME);
    assertEquals(surface, other);
    surface = new NodalDoublesSurface(XYZ_LIST, NAME);
    other = NodalDoublesSurface.from(XYZ_LIST, NAME);
    assertEquals(surface, other);
    surface = new NodalDoublesSurface(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE);
    other = NodalDoublesSurface.from(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    surface = new NodalDoublesSurface(X_OBJECT, Y_OBJECT, Z_OBJECT);
    other = NodalDoublesSurface.from(X_OBJECT, Y_OBJECT, Z_OBJECT);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    surface = new NodalDoublesSurface(X_LIST, Y_LIST, Z_LIST);
    other = NodalDoublesSurface.from(X_LIST, Y_LIST, Z_LIST);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    surface = new NodalDoublesSurface(XY_PAIR, Z_PRIMITIVE);
    other = NodalDoublesSurface.from(XY_PAIR, Z_PRIMITIVE);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    surface = new NodalDoublesSurface(XY_PAIR, Z_OBJECT);
    other = NodalDoublesSurface.from(XY_PAIR, Z_OBJECT);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    surface = new NodalDoublesSurface(XY_PAIR_LIST, Z_LIST);
    other = NodalDoublesSurface.from(XY_PAIR_LIST, Z_LIST);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    surface = new NodalDoublesSurface(XYZ_MAP);
    other = NodalDoublesSurface.from(XYZ_MAP);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    surface = new NodalDoublesSurface(XYZ_LIST);
    other = NodalDoublesSurface.from(XYZ_LIST);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
  }

  @Test
  public void testGetters() {
    final NodalDoublesSurface surface = NodalDoublesSurface.from(XYZ_LIST, NAME);
    assertEquals(surface.getName(), NAME);
    assertArrayEquals(surface.getXData(), X_OBJECT);
    assertArrayEquals(surface.getXDataAsPrimitive(), X_PRIMITIVE, 0);
    assertArrayEquals(surface.getYData(), Y_OBJECT);
    assertArrayEquals(surface.getYDataAsPrimitive(), Y_PRIMITIVE, 0);
    assertArrayEquals(surface.getZData(), Z_OBJECT);
    assertArrayEquals(surface.getZDataAsPrimitive(), Z_PRIMITIVE, 0);
    assertEquals(surface.size(), Z_PRIMITIVE.length);
  }

  @Test
  public void testGetZValue() {
    final double eps = 1e-15;
    assertEquals(SURFACE.getZValue(0., 1.), 0., eps);
    assertEquals(SURFACE.getZValue(4., 0.), 16., eps);
    assertEquals(SURFACE.getZValue(1., 1.), 4., eps);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullX1() {
    SURFACE.getZValue(null, 2.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullY1() {
    SURFACE.getZValue(1., null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    SURFACE.getZValue(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNonNodalX1() {
    SURFACE.getZValue(1.1, 2.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNonNodalY1() {
    SURFACE.getZValue(1., 3.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNonNodalX2() {
    SURFACE.getZValue(DoublesPair.of(1.1, 2.));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNonNodalY2() {
    SURFACE.getZValue(DoublesPair.of(1., 3.4));
  }
}
