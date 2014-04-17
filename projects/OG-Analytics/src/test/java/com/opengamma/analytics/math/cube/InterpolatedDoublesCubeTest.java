/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.cube;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.InterpolatorND;
import com.opengamma.analytics.math.interpolation.data.InterpolatorNDDataBundle;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class InterpolatedDoublesCubeTest extends DoublesCubeTest {
  @SuppressWarnings("synthetic-access")
  private static final MyInterpolator INTERPOLATOR = new MyInterpolator();
  private static final InterpolatedDoublesCube CUBE = InterpolatedDoublesCube.from(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, DATA_PRIMITIVE, INTERPOLATOR, NAME);

  @Test
  public void testEqualsAndHashCode() {
    final InterpolatedDoublesCube cube = new InterpolatedDoublesCube(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, DATA_PRIMITIVE, INTERPOLATOR, NAME);
    InterpolatedDoublesCube other = new InterpolatedDoublesCube(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, DATA_PRIMITIVE, INTERPOLATOR, NAME);
    assertEquals(cube, other);
    assertEquals(cube.hashCode(), other.hashCode());
    other = new InterpolatedDoublesCube(Y_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, DATA_PRIMITIVE, INTERPOLATOR, NAME);
    assertFalse(other.equals(cube));
    other = new InterpolatedDoublesCube(X_PRIMITIVE, X_PRIMITIVE, Z_PRIMITIVE, DATA_PRIMITIVE, INTERPOLATOR, NAME);
    assertFalse(other.equals(cube));
    other = new InterpolatedDoublesCube(X_PRIMITIVE, Y_PRIMITIVE, Y_PRIMITIVE, DATA_PRIMITIVE, INTERPOLATOR, NAME);
    assertFalse(other.equals(cube));
    assertFalse(other.equals(cube));
    other = new InterpolatedDoublesCube(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, DATA_PRIMITIVE, INTERPOLATOR, "P");
    assertFalse(other.equals(cube));
    other = new InterpolatedDoublesCube(X_OBJECT, Y_OBJECT, Z_OBJECT, DATA_OBJECT, INTERPOLATOR, NAME);
    assertEquals(cube, other);
    other = new InterpolatedDoublesCube(X_LIST, Y_LIST, Z_LIST, DATA_LIST, INTERPOLATOR, NAME);
    assertEquals(cube, other);
    other = new InterpolatedDoublesCube(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, DATA_PRIMITIVE, INTERPOLATOR);
    assertFalse(other.equals(cube));
    assertArrayEquals(cube.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(cube.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(cube.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    other = new InterpolatedDoublesCube(X_OBJECT, Y_OBJECT, Z_OBJECT, DATA_OBJECT, INTERPOLATOR);
    assertFalse(other.equals(cube));
    assertArrayEquals(cube.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(cube.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(cube.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    other = new InterpolatedDoublesCube(X_LIST, Y_LIST, Z_LIST, DATA_LIST, INTERPOLATOR);
    assertFalse(other.equals(cube));
    assertArrayEquals(cube.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(cube.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(cube.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
  }

  @Test
  public void testStaticConstruction() {
    InterpolatedDoublesCube cube = new InterpolatedDoublesCube(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, DATA_PRIMITIVE, INTERPOLATOR, NAME);
    InterpolatedDoublesCube other = InterpolatedDoublesCube.from(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, DATA_PRIMITIVE, INTERPOLATOR, NAME);
    assertEquals(cube, other);
    cube = new InterpolatedDoublesCube(X_OBJECT, Y_OBJECT, Z_OBJECT, DATA_OBJECT, INTERPOLATOR, NAME);
    other = InterpolatedDoublesCube.from(X_OBJECT, Y_OBJECT, Z_OBJECT, DATA_OBJECT, INTERPOLATOR, NAME);
    assertEquals(cube, other);
    cube = new InterpolatedDoublesCube(X_LIST, Y_LIST, Z_LIST, DATA_LIST, INTERPOLATOR, NAME);
    other = InterpolatedDoublesCube.from(X_LIST, Y_LIST, Z_LIST, DATA_LIST, INTERPOLATOR, NAME);
    assertEquals(cube, other);
    cube = new InterpolatedDoublesCube(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, DATA_PRIMITIVE, INTERPOLATOR);
    other = InterpolatedDoublesCube.from(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, DATA_PRIMITIVE, INTERPOLATOR);
    assertFalse(other.equals(cube));
    assertArrayEquals(cube.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(cube.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(cube.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    cube = new InterpolatedDoublesCube(X_OBJECT, Y_OBJECT, Z_OBJECT, DATA_OBJECT, INTERPOLATOR);
    other = InterpolatedDoublesCube.from(X_OBJECT, Y_OBJECT, Z_OBJECT, DATA_OBJECT, INTERPOLATOR);
    assertFalse(other.equals(cube));
    assertArrayEquals(cube.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(cube.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(cube.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
    cube = new InterpolatedDoublesCube(X_LIST, Y_LIST, Z_LIST, DATA_LIST, INTERPOLATOR);
    other = InterpolatedDoublesCube.from(X_LIST, Y_LIST, Z_LIST, DATA_LIST, INTERPOLATOR);
    assertFalse(other.equals(cube));
    assertArrayEquals(cube.getXDataAsPrimitive(), other.getXDataAsPrimitive(), 0);
    assertArrayEquals(cube.getYDataAsPrimitive(), other.getYDataAsPrimitive(), 0);
    assertArrayEquals(cube.getZDataAsPrimitive(), other.getZDataAsPrimitive(), 0);
  }

  @Test
  public void testGetters() {
    final InterpolatedDoublesCube cube = InterpolatedDoublesCube.from(X_PRIMITIVE, Y_PRIMITIVE, Z_PRIMITIVE, DATA_PRIMITIVE, INTERPOLATOR, NAME);
    assertEquals(cube.getInterpolator(), INTERPOLATOR);
    assertEquals(cube.getName(), NAME);
    assertArrayEquals(cube.getXData(), X_OBJECT);
    assertArrayEquals(cube.getXDataAsPrimitive(), X_PRIMITIVE, 0);
    assertArrayEquals(cube.getYData(), Y_OBJECT);
    assertArrayEquals(cube.getYDataAsPrimitive(), Y_PRIMITIVE, 0);
    assertArrayEquals(cube.getZData(), Z_OBJECT);
    assertArrayEquals(cube.getZDataAsPrimitive(), Z_PRIMITIVE, 0);
    assertArrayEquals(cube.getValuesAsPrimitive(), DATA_PRIMITIVE, 0);
    assertEquals(cube.size(), DATA_PRIMITIVE.length);
  }

  @Test
  public void testGetZValue() {
    final double eps = 1e-15;
    assertEquals(CUBE.getValue(0., 1., 2.6), 2.6, eps);
    assertEquals(CUBE.getValue(3., 0., 0.01), 0.01, eps);
    assertEquals(CUBE.getValue(new Triple<>(0., 1., 2.6)), 2.6, eps);
    assertEquals(CUBE.getValue(new Triple<>(3., 0., 0.01)), 0.01, eps);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullX1() {
    CUBE.getValue(null, 2., 5.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullY1() {
    CUBE.getValue(1., null, 1.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullZ1() {
    CUBE.getValue(1., 1., null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullXYZ() {
    CUBE.getValue(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullX2() {
    CUBE.getValue(new Triple<Double, Double, Double>(null, 2., 5.));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullY2() {
    CUBE.getValue(new Triple<Double, Double, Double>(1., null, 1.));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullZ2() {
    CUBE.getValue(new Triple<Double, Double, Double>(1., 1., null));
  }

  private static class MyInterpolator extends InterpolatorND {

    @Override
    public Double interpolate(final InterpolatorNDDataBundle data, final double[] x) {
      return x[2];
    }

    @Override
    public InterpolatorNDDataBundle getDataBundle(final double[] x, final double[] y, final double[] z, final double[] values) {
      return new InterpolatorNDDataBundle(transformData(x, y, z, values));
    }

    @Override
    public InterpolatorNDDataBundle getDataBundle(final List<Pair<double[], Double>> data) {
      return new InterpolatorNDDataBundle(data);
    }

  }
}
