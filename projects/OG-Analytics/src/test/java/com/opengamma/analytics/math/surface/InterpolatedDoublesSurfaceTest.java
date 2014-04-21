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

import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.interpolation.StepInterpolator1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class InterpolatedDoublesSurfaceTest extends DoublesSurfaceTestCase {
  private static final LinearInterpolator1D INTERPOLATOR_1D = Interpolator1DFactory.LINEAR_INSTANCE;
  private static final Interpolator2D INTERPOLATOR = new GridInterpolator2D(INTERPOLATOR_1D, INTERPOLATOR_1D);
  private static final InterpolatedDoublesSurface SURFACE = new InterpolatedDoublesSurface(XYZ_LIST, INTERPOLATOR);

  @Test
  public void testEqualsAndHashCode() {
    final InterpolatedDoublesSurface surface = new InterpolatedDoublesSurface(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, INTERPOLATOR, NAME);
    InterpolatedDoublesSurface other = new InterpolatedDoublesSurface(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, INTERPOLATOR, NAME);
    assertEquals(surface, other);
    assertEquals(surface.hashCode(), other.hashCode());
    other = new InterpolatedDoublesSurface(Y_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, INTERPOLATOR, NAME);
    assertFalse(other.equals(surface));
    other = new InterpolatedDoublesSurface(X_PRIMITIVE, X_PRIMITIVE, Z_PRIMITIVE, INTERPOLATOR, NAME);
    assertFalse(other.equals(surface));
    other = new InterpolatedDoublesSurface(X_PRIMITIVE, Y_PRIMITIVE, Y_PRIMITIVE, INTERPOLATOR, NAME);
    assertFalse(other.equals(surface));
    other = new InterpolatedDoublesSurface(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE,
        new GridInterpolator2D(INTERPOLATOR_1D, new StepInterpolator1D()), NAME);
    assertFalse(other.equals(surface));
    other = new InterpolatedDoublesSurface(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, INTERPOLATOR, "P");
    assertFalse(other.equals(surface));
    other = new InterpolatedDoublesSurface(X_OBJECT, Y_OBJECT, Z_OBJECT, INTERPOLATOR, NAME);
    assertEquals(surface, other);
    other = new InterpolatedDoublesSurface(X_LIST, Y_LIST, Z_LIST, INTERPOLATOR, NAME);
    assertEquals(surface, other);
    other = new InterpolatedDoublesSurface(XY_PAIR, Z_PRIMITIVE, INTERPOLATOR, NAME);
    assertEquals(surface, other);
    other = new InterpolatedDoublesSurface(XY_PAIR, Z_OBJECT, INTERPOLATOR, NAME);
    assertEquals(surface, other);
    other = new InterpolatedDoublesSurface(XY_PAIR_LIST, Z_LIST, INTERPOLATOR, NAME);
    assertEquals(surface, other);
    other = new InterpolatedDoublesSurface(XYZ_MAP, INTERPOLATOR, NAME);
    assertEquals(surface, other);
    other = new InterpolatedDoublesSurface(XYZ_LIST, INTERPOLATOR, NAME);
    assertEquals(surface, other);
    other = new InterpolatedDoublesSurface(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, INTERPOLATOR);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    other = new InterpolatedDoublesSurface(X_OBJECT, Y_OBJECT, Z_OBJECT, INTERPOLATOR);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    other = new InterpolatedDoublesSurface(X_LIST, Y_LIST, Z_LIST, INTERPOLATOR);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    other = new InterpolatedDoublesSurface(XY_PAIR, Z_PRIMITIVE, INTERPOLATOR);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    other = new InterpolatedDoublesSurface(XY_PAIR, Z_OBJECT, INTERPOLATOR);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    other = new InterpolatedDoublesSurface(XY_PAIR_LIST, Z_LIST, INTERPOLATOR);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    other = new InterpolatedDoublesSurface(XYZ_MAP, INTERPOLATOR);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    other = new InterpolatedDoublesSurface(XYZ_LIST, INTERPOLATOR);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
  }

  @Test
  public void testStaticConstruction() {
    InterpolatedDoublesSurface surface = new InterpolatedDoublesSurface(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, INTERPOLATOR, NAME);
    InterpolatedDoublesSurface other = InterpolatedDoublesSurface.from(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, INTERPOLATOR, NAME);
    assertEquals(surface, other);
    surface = new InterpolatedDoublesSurface(X_OBJECT, Y_OBJECT, Z_OBJECT, INTERPOLATOR, NAME);
    other = InterpolatedDoublesSurface.from(X_OBJECT, Y_OBJECT, Z_OBJECT, INTERPOLATOR, NAME);
    assertEquals(surface, other);
    surface = new InterpolatedDoublesSurface(X_LIST, Y_LIST, Z_LIST, INTERPOLATOR, NAME);
    other = InterpolatedDoublesSurface.from(X_LIST, Y_LIST, Z_LIST, INTERPOLATOR, NAME);
    assertEquals(surface, other);
    surface = new InterpolatedDoublesSurface(XY_PAIR, Z_PRIMITIVE, INTERPOLATOR, NAME);
    other = InterpolatedDoublesSurface.from(XY_PAIR, Z_PRIMITIVE, INTERPOLATOR, NAME);
    assertEquals(surface, other);
    surface = new InterpolatedDoublesSurface(XY_PAIR, Z_OBJECT, INTERPOLATOR, NAME);
    other = InterpolatedDoublesSurface.from(XY_PAIR, Z_OBJECT, INTERPOLATOR, NAME);
    assertEquals(surface, other);
    surface = new InterpolatedDoublesSurface(XY_PAIR_LIST, Z_LIST, INTERPOLATOR, NAME);
    other = InterpolatedDoublesSurface.from(XY_PAIR_LIST, Z_LIST, INTERPOLATOR, NAME);
    assertEquals(surface, other);
    surface = new InterpolatedDoublesSurface(XYZ_MAP, INTERPOLATOR, NAME);
    other = InterpolatedDoublesSurface.from(XYZ_MAP, INTERPOLATOR, NAME);
    assertEquals(surface, other);
    surface = new InterpolatedDoublesSurface(XYZ_LIST, INTERPOLATOR, NAME);
    other = InterpolatedDoublesSurface.from(XYZ_LIST, INTERPOLATOR, NAME);
    assertEquals(surface, other);
    surface = new InterpolatedDoublesSurface(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, INTERPOLATOR);
    other = InterpolatedDoublesSurface.from(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, INTERPOLATOR);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    surface = new InterpolatedDoublesSurface(X_OBJECT, Y_OBJECT, Z_OBJECT, INTERPOLATOR);
    other = InterpolatedDoublesSurface.from(X_OBJECT, Y_OBJECT, Z_OBJECT, INTERPOLATOR);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    surface = new InterpolatedDoublesSurface(X_LIST, Y_LIST, Z_LIST, INTERPOLATOR);
    other = InterpolatedDoublesSurface.from(X_LIST, Y_LIST, Z_LIST, INTERPOLATOR);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    surface = new InterpolatedDoublesSurface(XY_PAIR, Z_PRIMITIVE, INTERPOLATOR);
    other = InterpolatedDoublesSurface.from(XY_PAIR, Z_PRIMITIVE, INTERPOLATOR);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    surface = new InterpolatedDoublesSurface(XY_PAIR, Z_OBJECT, INTERPOLATOR);
    other = InterpolatedDoublesSurface.from(XY_PAIR, Z_OBJECT, INTERPOLATOR);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    surface = new InterpolatedDoublesSurface(XY_PAIR_LIST, Z_LIST, INTERPOLATOR);
    other = InterpolatedDoublesSurface.from(XY_PAIR_LIST, Z_LIST, INTERPOLATOR);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    surface = new InterpolatedDoublesSurface(XYZ_MAP, INTERPOLATOR);
    other = InterpolatedDoublesSurface.from(XYZ_MAP, INTERPOLATOR);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    surface = new InterpolatedDoublesSurface(XYZ_LIST, INTERPOLATOR);
    other = InterpolatedDoublesSurface.from(XYZ_LIST, INTERPOLATOR);
    assertFalse(other.equals(surface));
    assertArrayEquals(surface.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
  }

  @Test
  public void testGetters() {
    final InterpolatedDoublesSurface surface = InterpolatedDoublesSurface.from(XYZ_LIST, INTERPOLATOR, NAME);
    assertEquals(surface.getInterpolator(), INTERPOLATOR);
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
    assertEquals(SURFACE.getZValue(3., 0.), 12., eps);
    assertEquals(SURFACE.getZValue(1., 0.), 4., eps);
    assertEquals(SURFACE.getZValue(0.5, 0.), 2., eps);
    assertEquals(SURFACE.getZValue(2.5, 1.), 10., eps);
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

}
