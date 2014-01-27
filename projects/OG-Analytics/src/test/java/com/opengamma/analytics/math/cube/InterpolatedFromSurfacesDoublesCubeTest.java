/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.cube;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.Plane;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.interpolation.LogLinearInterpolator1D;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Triple;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class InterpolatedFromSurfacesDoublesCubeTest {
  private static final double A0 = 0;
  private static final double A1 = 2;
  private static final double A2 = 10;
  private static final Function<Double, Double> F1 = new Function<Double, Double>() {

    @Override
    public Double evaluate(final Double... xy) {
      return -4.;
    }

  };
  private static final Function<Double, Double> F2 = new Function<Double, Double>() {

    @Override
    public Double evaluate(final Double... xy) {
      final double x = xy[0];
      final double y = xy[1];
      return x * x + y * y + x * y;
    }

  };
  private static final Surface<Double, Double, Double> S1 = FunctionalDoublesSurface.from(F1, "A");
  private static final Surface<Double, Double, Double> S2 = FunctionalDoublesSurface.from(F2, "B");
  private static final Surface<Double, Double, Double> S3 = FunctionalDoublesSurface.from(F2, "C");
  private static final String NAME = "K";
  private static final double[] POINTS_PRIMITIVE = new double[] {A0, A1, A2};
  private static final Double[] POINTS_OBJECT;
  private static final List<Double> POINTS_LIST;
  @SuppressWarnings("unchecked")
  private static final Surface<Double, Double, Double>[] SURFACE_ARRAY = new Surface[] {S1, S2, S3};
  private static final List<Surface<Double, Double, Double>> SURFACE_LIST;
  private static final Map<Double, Surface<Double, Double, Double>> SURFACE_MAP;
  private static final double[] UNSORTED_POINTS_PRIMITIVE = new double[] {A0, A2, A1};
  private static final Double[] UNSORTED_POINTS_OBJECT;
  private static final List<Double> UNSORTED_POINTS_LIST;
  @SuppressWarnings("unchecked")
  private static final Surface<Double, Double, Double>[] UNSORTED_SURFACE_ARRAY = new Surface[] {S1, S3, S2};
  private static final List<Surface<Double, Double, Double>> UNSORTED_SURFACE_LIST;
  private static final Map<Double, Surface<Double, Double, Double>> UNSORTED_SURFACE_MAP;
  private static final LinearInterpolator1D INTERPOLATOR = new LinearInterpolator1D();
  private static final InterpolatedFromSurfacesDoublesCube XY_CUBE;
  private static final InterpolatedFromSurfacesDoublesCube XZ_CUBE;
  private static final InterpolatedFromSurfacesDoublesCube YZ_CUBE;

  static {
    final int n = POINTS_PRIMITIVE.length;
    POINTS_OBJECT = new Double[n];
    POINTS_LIST = new ArrayList<>();
    SURFACE_LIST = new ArrayList<>();
    SURFACE_MAP = new TreeMap<>();
    UNSORTED_POINTS_OBJECT = new Double[n];
    UNSORTED_POINTS_LIST = new ArrayList<>();
    UNSORTED_SURFACE_LIST = new ArrayList<>();
    UNSORTED_SURFACE_MAP = new TreeMap<>();
    for (int i = 0; i < n; i++) {
      POINTS_OBJECT[i] = POINTS_PRIMITIVE[i];
      POINTS_LIST.add(POINTS_PRIMITIVE[i]);
      SURFACE_LIST.add(SURFACE_ARRAY[i]);
      SURFACE_MAP.put(POINTS_PRIMITIVE[i], SURFACE_ARRAY[i]);
      UNSORTED_POINTS_OBJECT[i] = UNSORTED_POINTS_PRIMITIVE[i];
      UNSORTED_POINTS_LIST.add(UNSORTED_POINTS_PRIMITIVE[i]);
      UNSORTED_SURFACE_LIST.add(UNSORTED_SURFACE_ARRAY[i]);
      UNSORTED_SURFACE_MAP.put(UNSORTED_POINTS_PRIMITIVE[i], UNSORTED_SURFACE_ARRAY[i]);
    }
    XY_CUBE = InterpolatedFromSurfacesDoublesCube.fromSorted(Plane.XY, SURFACE_MAP, INTERPOLATOR, NAME);
    XZ_CUBE = InterpolatedFromSurfacesDoublesCube.fromSorted(Plane.ZX, SURFACE_MAP, INTERPOLATOR, NAME);
    YZ_CUBE = InterpolatedFromSurfacesDoublesCube.fromSorted(Plane.YZ, SURFACE_MAP, INTERPOLATOR, NAME);
  }

  @Test
  public void testCubes() {
    double x = 3;
    double y = 4;
    double z = A1;
    assertEquals(XY_CUBE.getValue(x, y, z), F2.evaluate(x, y), 0);
    assertEquals(XY_CUBE.getValue(new Triple<>(x, y, z)), F2.evaluate(x, y), 0);
    z = A1 * Math.random();
    double lower = F1.evaluate(x, y);
    double higher = F2.evaluate(x, y);
    double value = (higher - lower) * (z / A1) + lower;
    assertEquals(XY_CUBE.getValue(x, y, z), value, 0);
    assertEquals(XY_CUBE.getValue(new Triple<>(x, y, z)), value, 0);
    x = 2.4;
    y = A2;
    z = 5.6;
    assertEquals(XZ_CUBE.getValue(x, y, z), F2.evaluate(x, z), 0);
    assertEquals(XZ_CUBE.getValue(new Triple<>(x, y, z)), F2.evaluate(x, z), 0);
    y = A1 * Math.random();
    lower = F1.evaluate(x, z);
    higher = F2.evaluate(x, z);
    value = (higher - lower) * (y / A1) + lower;
    assertEquals(XZ_CUBE.getValue(x, y, z), value, 0);
    assertEquals(XZ_CUBE.getValue(new Triple<>(x, y, z)), value, 0);
    x = A0;
    y = 1.34;
    z = 5.6;
    assertEquals(YZ_CUBE.getValue(x, y, z), F1.evaluate(y, z), 0);
    assertEquals(YZ_CUBE.getValue(new Triple<>(x, y, z)), F1.evaluate(y, z), 0);
    x = A1 * Math.random();
    lower = F1.evaluate(y, z);
    higher = F2.evaluate(y, z);
    value = (higher - lower) * (x / A1) + lower;
    assertEquals(YZ_CUBE.getValue(x, y, z), value, 0);
    assertEquals(YZ_CUBE.getValue(new Triple<>(x, y, z)), value, 0);
  }

  @Test
  public void testHashCodeAndEquals() {
    InterpolatedFromSurfacesDoublesCube other = InterpolatedFromSurfacesDoublesCube.fromSorted(Plane.XY, SURFACE_MAP, INTERPOLATOR, NAME);
    assertEquals(other, XY_CUBE);
    assertEquals(other.hashCode(), XY_CUBE.hashCode());
    assertEquals(other.getInterpolator(), INTERPOLATOR);
    assertArrayEquals(other.getPoints(), POINTS_PRIMITIVE, 0);
    assertArrayEquals(other.getSurfaces(), SURFACE_ARRAY);
    assertEquals(other.getName(), NAME);
    assertEquals(other.getPlane(), Plane.XY);
    assertFalse(other.equals(XZ_CUBE));
    TreeMap<Double, Surface<Double, Double, Double>> m = new TreeMap<>();
    m.put(1., S1);
    m.put(2.5, S2);
    m.put(3., S3);
    other = InterpolatedFromSurfacesDoublesCube.fromSorted(Plane.XY, m, INTERPOLATOR, NAME);
    assertFalse(other.equals(XY_CUBE));
    m = new TreeMap<>();
    m.put(1., S1);
    m.put(2., S1);
    m.put(3., S3);
    other = InterpolatedFromSurfacesDoublesCube.fromSorted(Plane.XY, m, INTERPOLATOR, NAME);
    assertFalse(other.equals(XY_CUBE));
    other = InterpolatedFromSurfacesDoublesCube.fromSorted(Plane.XY, SURFACE_MAP, new LogLinearInterpolator1D(), NAME);
    assertFalse(other.equals(XY_CUBE));
    other = InterpolatedFromSurfacesDoublesCube.fromSorted(Plane.XY, SURFACE_MAP, INTERPOLATOR, NAME + "_");
    assertFalse(other.equals(XY_CUBE));
  }

  @Test
  public void testConstructors() {
    final InterpolatedFromSurfacesDoublesCube cube1 = new InterpolatedFromSurfacesDoublesCube(Plane.XY, SURFACE_MAP, INTERPOLATOR, true, NAME);
    InterpolatedFromSurfacesDoublesCube cube2 = new InterpolatedFromSurfacesDoublesCube(Plane.XY, SURFACE_MAP, INTERPOLATOR, true);
    assertFalse(cube1.equals(cube2));
    assertEquals(cube1.getInterpolator(), cube2.getInterpolator());
    assertArrayEquals(cube1.getPoints(), cube2.getPoints(), 0);
    assertArrayEquals(cube1.getSurfaces(), cube2.getSurfaces());
    assertEquals(cube1.getPlane(), cube2.getPlane());
    cube2 = new InterpolatedFromSurfacesDoublesCube(Plane.XY, POINTS_PRIMITIVE, SURFACE_ARRAY, INTERPOLATOR, true, NAME);
    assertEquals(cube1, cube2);
    cube2 = new InterpolatedFromSurfacesDoublesCube(Plane.XY, POINTS_PRIMITIVE, SURFACE_ARRAY, INTERPOLATOR, true);
    assertFalse(cube1.equals(cube2));
    assertEquals(cube1.getInterpolator(), cube2.getInterpolator());
    assertArrayEquals(cube1.getPoints(), cube2.getPoints(), 0);
    assertArrayEquals(cube1.getSurfaces(), cube2.getSurfaces());
    assertEquals(cube1.getPlane(), cube2.getPlane());
    cube2 = new InterpolatedFromSurfacesDoublesCube(Plane.XY, POINTS_OBJECT, SURFACE_ARRAY, INTERPOLATOR, true, NAME);
    assertEquals(cube1, cube2);
    cube2 = new InterpolatedFromSurfacesDoublesCube(Plane.XY, POINTS_OBJECT, SURFACE_ARRAY, INTERPOLATOR, true);
    assertFalse(cube1.equals(cube2));
    assertEquals(cube1.getInterpolator(), cube2.getInterpolator());
    assertArrayEquals(cube1.getPoints(), cube2.getPoints(), 0);
    assertArrayEquals(cube1.getSurfaces(), cube2.getSurfaces());
    assertEquals(cube1.getPlane(), cube2.getPlane());
    cube2 = new InterpolatedFromSurfacesDoublesCube(Plane.XY, POINTS_LIST, SURFACE_LIST, INTERPOLATOR, true, NAME);
    assertEquals(cube1, cube2);
    cube2 = new InterpolatedFromSurfacesDoublesCube(Plane.XY, POINTS_LIST, SURFACE_LIST, INTERPOLATOR, true);
    assertFalse(cube1.equals(cube2));
    assertEquals(cube1.getInterpolator(), cube2.getInterpolator());
    assertArrayEquals(cube1.getPoints(), cube2.getPoints(), 0);
    assertArrayEquals(cube1.getSurfaces(), cube2.getSurfaces());
    assertEquals(cube1.getPlane(), cube2.getPlane());
    cube2 = new InterpolatedFromSurfacesDoublesCube(Plane.XY, UNSORTED_SURFACE_MAP, INTERPOLATOR, false, NAME);
    assertEquals(cube1, cube2);
    cube2 = new InterpolatedFromSurfacesDoublesCube(Plane.XY, UNSORTED_SURFACE_MAP, INTERPOLATOR, false);
    assertFalse(cube1.equals(cube2));
    assertEquals(cube1.getInterpolator(), cube2.getInterpolator());
    assertArrayEquals(cube1.getPoints(), cube2.getPoints(), 0);
    assertArrayEquals(cube1.getSurfaces(), cube2.getSurfaces());
    assertEquals(cube1.getPlane(), cube2.getPlane());
    cube2 = new InterpolatedFromSurfacesDoublesCube(Plane.XY, UNSORTED_POINTS_PRIMITIVE, UNSORTED_SURFACE_ARRAY, INTERPOLATOR, false, NAME);
    assertEquals(cube1, cube2);
    cube2 = new InterpolatedFromSurfacesDoublesCube(Plane.XY, UNSORTED_POINTS_PRIMITIVE, UNSORTED_SURFACE_ARRAY, INTERPOLATOR, false);
    assertFalse(cube1.equals(cube2));
    assertEquals(cube1.getInterpolator(), cube2.getInterpolator());
    assertArrayEquals(cube1.getPoints(), cube2.getPoints(), 0);
    assertArrayEquals(cube1.getSurfaces(), cube2.getSurfaces());
    assertEquals(cube1.getPlane(), cube2.getPlane());
    cube2 = new InterpolatedFromSurfacesDoublesCube(Plane.XY, UNSORTED_POINTS_OBJECT, UNSORTED_SURFACE_ARRAY, INTERPOLATOR, false, NAME);
    assertEquals(cube1, cube2);
    cube2 = new InterpolatedFromSurfacesDoublesCube(Plane.XY, UNSORTED_POINTS_OBJECT, UNSORTED_SURFACE_ARRAY, INTERPOLATOR, false);
    assertFalse(cube1.equals(cube2));
    assertEquals(cube1.getInterpolator(), cube2.getInterpolator());
    assertArrayEquals(cube1.getPoints(), cube2.getPoints(), 0);
    assertArrayEquals(cube1.getSurfaces(), cube2.getSurfaces());
    assertEquals(cube1.getPlane(), cube2.getPlane());
    cube2 = new InterpolatedFromSurfacesDoublesCube(Plane.XY, UNSORTED_POINTS_LIST, UNSORTED_SURFACE_LIST, INTERPOLATOR, false, NAME);
    assertEquals(cube1, cube2);
    cube2 = new InterpolatedFromSurfacesDoublesCube(Plane.XY, UNSORTED_POINTS_LIST, UNSORTED_SURFACE_LIST, INTERPOLATOR, false);
    assertFalse(cube1.equals(cube2));
    assertEquals(cube1.getInterpolator(), cube2.getInterpolator());
    assertArrayEquals(cube1.getPoints(), cube2.getPoints(), 0);
    assertArrayEquals(cube1.getSurfaces(), cube2.getSurfaces());
    assertEquals(cube1.getPlane(), cube2.getPlane());
  }

  @Test
  public void testStaticConstruction() {
    InterpolatedFromSurfacesDoublesCube cube = InterpolatedFromSurfacesDoublesCube.fromSorted(Plane.XY, SURFACE_MAP, INTERPOLATOR, NAME);
    assertEquals(cube, XY_CUBE);
    cube = InterpolatedFromSurfacesDoublesCube.from(Plane.XY, UNSORTED_SURFACE_MAP, INTERPOLATOR, NAME);
    assertEquals(cube, XY_CUBE);
    cube = InterpolatedFromSurfacesDoublesCube.fromSorted(Plane.XY, SURFACE_MAP, INTERPOLATOR);
    assertFalse(cube.equals(XY_CUBE));
    assertEquals(cube.getInterpolator(), XY_CUBE.getInterpolator());
    assertArrayEquals(cube.getPoints(), XY_CUBE.getPoints(), 0);
    assertArrayEquals(cube.getSurfaces(), XY_CUBE.getSurfaces());
    assertEquals(cube.getPlane(), XY_CUBE.getPlane());
    cube = InterpolatedFromSurfacesDoublesCube.from(Plane.XY, UNSORTED_SURFACE_MAP, INTERPOLATOR);
    assertFalse(cube.equals(XY_CUBE));
    assertEquals(cube.getInterpolator(), XY_CUBE.getInterpolator());
    assertArrayEquals(cube.getPoints(), XY_CUBE.getPoints(), 0);
    assertArrayEquals(cube.getSurfaces(), XY_CUBE.getSurfaces());
    assertEquals(cube.getPlane(), XY_CUBE.getPlane());
    cube = InterpolatedFromSurfacesDoublesCube.fromSorted(Plane.XY, POINTS_PRIMITIVE, SURFACE_ARRAY, INTERPOLATOR, NAME);
    assertEquals(cube, XY_CUBE);
    cube = InterpolatedFromSurfacesDoublesCube.from(Plane.XY, UNSORTED_POINTS_PRIMITIVE, UNSORTED_SURFACE_ARRAY, INTERPOLATOR, NAME);
    assertEquals(cube, XY_CUBE);
    cube = InterpolatedFromSurfacesDoublesCube.fromSorted(Plane.XY, POINTS_PRIMITIVE, SURFACE_ARRAY, INTERPOLATOR);
    assertFalse(cube.equals(XY_CUBE));
    assertEquals(cube.getInterpolator(), XY_CUBE.getInterpolator());
    assertArrayEquals(cube.getPoints(), XY_CUBE.getPoints(), 0);
    assertArrayEquals(cube.getSurfaces(), XY_CUBE.getSurfaces());
    assertEquals(cube.getPlane(), XY_CUBE.getPlane());
    cube = InterpolatedFromSurfacesDoublesCube.from(Plane.XY, UNSORTED_POINTS_PRIMITIVE, UNSORTED_SURFACE_ARRAY, INTERPOLATOR);
    assertFalse(cube.equals(XY_CUBE));
    assertEquals(cube.getInterpolator(), XY_CUBE.getInterpolator());
    assertArrayEquals(cube.getPoints(), XY_CUBE.getPoints(), 0);
    assertArrayEquals(cube.getSurfaces(), XY_CUBE.getSurfaces());
    assertEquals(cube.getPlane(), XY_CUBE.getPlane());
    cube = InterpolatedFromSurfacesDoublesCube.fromSorted(Plane.XY, POINTS_LIST, SURFACE_LIST, INTERPOLATOR, NAME);
    assertEquals(cube, XY_CUBE);
    cube = InterpolatedFromSurfacesDoublesCube.from(Plane.XY, UNSORTED_POINTS_LIST, UNSORTED_SURFACE_LIST, INTERPOLATOR, NAME);
    assertEquals(cube, XY_CUBE);
    cube = InterpolatedFromSurfacesDoublesCube.fromSorted(Plane.XY, POINTS_LIST, SURFACE_LIST, INTERPOLATOR);
    assertFalse(cube.equals(XY_CUBE));
    assertEquals(cube.getInterpolator(), XY_CUBE.getInterpolator());
    assertArrayEquals(cube.getPoints(), XY_CUBE.getPoints(), 0);
    assertArrayEquals(cube.getSurfaces(), XY_CUBE.getSurfaces());
    assertEquals(cube.getPlane(), XY_CUBE.getPlane());
    cube = InterpolatedFromSurfacesDoublesCube.from(Plane.XY, UNSORTED_POINTS_LIST, UNSORTED_SURFACE_LIST, INTERPOLATOR);
    assertFalse(cube.equals(XY_CUBE));
    assertEquals(cube.getInterpolator(), XY_CUBE.getInterpolator());
    assertArrayEquals(cube.getPoints(), XY_CUBE.getPoints(), 0);
    assertArrayEquals(cube.getSurfaces(), XY_CUBE.getSurfaces());
    assertEquals(cube.getPlane(), XY_CUBE.getPlane());
    cube = InterpolatedFromSurfacesDoublesCube.fromSorted(Plane.XY, POINTS_OBJECT, SURFACE_ARRAY, INTERPOLATOR, NAME);
    assertEquals(cube, XY_CUBE);
    cube = InterpolatedFromSurfacesDoublesCube.from(Plane.XY, UNSORTED_POINTS_OBJECT, UNSORTED_SURFACE_ARRAY, INTERPOLATOR, NAME);
    assertEquals(cube, XY_CUBE);
    cube = InterpolatedFromSurfacesDoublesCube.fromSorted(Plane.XY, POINTS_OBJECT, SURFACE_ARRAY, INTERPOLATOR);
    assertFalse(cube.equals(XY_CUBE));
    assertEquals(cube.getInterpolator(), XY_CUBE.getInterpolator());
    assertArrayEquals(cube.getPoints(), XY_CUBE.getPoints(), 0);
    assertArrayEquals(cube.getSurfaces(), XY_CUBE.getSurfaces());
    assertEquals(cube.getPlane(), XY_CUBE.getPlane());
    cube = InterpolatedFromSurfacesDoublesCube.from(Plane.XY, UNSORTED_POINTS_OBJECT, UNSORTED_SURFACE_ARRAY, INTERPOLATOR);
    assertFalse(cube.equals(XY_CUBE));
    assertEquals(cube.getInterpolator(), XY_CUBE.getInterpolator());
    assertArrayEquals(cube.getPoints(), XY_CUBE.getPoints(), 0);
    assertArrayEquals(cube.getSurfaces(), XY_CUBE.getSurfaces());
    assertEquals(cube.getPlane(), XY_CUBE.getPlane());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPlane1() {
    new InterpolatedFromSurfacesDoublesCube(null, POINTS_PRIMITIVE, SURFACE_ARRAY, INTERPOLATOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPlane2() {
    new InterpolatedFromSurfacesDoublesCube(null, POINTS_PRIMITIVE, SURFACE_ARRAY, INTERPOLATOR, true, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPlane3() {
    new InterpolatedFromSurfacesDoublesCube(null, POINTS_OBJECT, SURFACE_ARRAY, INTERPOLATOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPlane4() {
    new InterpolatedFromSurfacesDoublesCube(null, POINTS_OBJECT, SURFACE_ARRAY, INTERPOLATOR, true, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPlane5() {
    new InterpolatedFromSurfacesDoublesCube(null, POINTS_LIST, SURFACE_LIST, INTERPOLATOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPlane6() {
    new InterpolatedFromSurfacesDoublesCube(null, POINTS_LIST, SURFACE_LIST, INTERPOLATOR, true, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPlane7() {
    new InterpolatedFromSurfacesDoublesCube(null, SURFACE_MAP, INTERPOLATOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPlane8() {
    new InterpolatedFromSurfacesDoublesCube(null, SURFACE_MAP, INTERPOLATOR, true, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPoints1() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, (double[]) null, SURFACE_ARRAY, INTERPOLATOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPoints2() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, (double[]) null, SURFACE_ARRAY, INTERPOLATOR, true, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPoints3() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, (Double[]) null, SURFACE_ARRAY, INTERPOLATOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPoints4() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, (Double[]) null, SURFACE_ARRAY, INTERPOLATOR, true, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPoints5() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, (List<Double>) null, SURFACE_LIST, INTERPOLATOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPoints6() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, (List<Double>) null, SURFACE_LIST, INTERPOLATOR, true, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSurfaces1() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, POINTS_PRIMITIVE, null, INTERPOLATOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSurfaces2() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, POINTS_PRIMITIVE, null, INTERPOLATOR, true, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSurfaces3() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, POINTS_OBJECT, null, INTERPOLATOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSurfaces4() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, POINTS_OBJECT, null, INTERPOLATOR, true, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSurfaces5() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, POINTS_LIST, null, INTERPOLATOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSurfaces6() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, POINTS_LIST, null, INTERPOLATOR, true, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPointValue1() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, new Double[] {1., 2., null}, SURFACE_ARRAY, INTERPOLATOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPointValue2() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, new Double[] {1., 2., null}, SURFACE_ARRAY, INTERPOLATOR, true, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPointValue3() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, Arrays.asList(1., 2., null), SURFACE_LIST, INTERPOLATOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPointValue4() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, Arrays.asList(1., 2., null), SURFACE_LIST, INTERPOLATOR, true, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMap1() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, null, INTERPOLATOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMap2() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, null, INTERPOLATOR, true, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEntry1() {
    final TreeMap<Double, Surface<Double, Double, Double>> m = new TreeMap<>();
    m.put(1., S1);
    m.put(2., null);
    m.put(3., S3);
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, m, INTERPOLATOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEntry2() {
    final TreeMap<Double, Surface<Double, Double, Double>> m = new TreeMap<>();
    m.put(1., S1);
    m.put(2., null);
    m.put(3., S3);
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, m, INTERPOLATOR, true, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength1() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, new double[] {1, 2, 3, 4, 5}, SURFACE_ARRAY, INTERPOLATOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength2() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, new double[] {1, 2, 3, 4, 5}, SURFACE_ARRAY, INTERPOLATOR, true, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength3() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, new Double[] {1., 2., 3., 4., 5.}, SURFACE_ARRAY, INTERPOLATOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength4() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, new Double[] {1., 2., 3., 4., 5.}, SURFACE_ARRAY, INTERPOLATOR, true, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength5() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, Arrays.asList(1., 2., 3., 4.), SURFACE_LIST, INTERPOLATOR, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength6() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, Arrays.asList(1., 2., 3., 4.), SURFACE_LIST, INTERPOLATOR, true, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator1() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, POINTS_PRIMITIVE, SURFACE_ARRAY, null, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator2() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, POINTS_PRIMITIVE, SURFACE_ARRAY, null, true, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator3() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, POINTS_OBJECT, SURFACE_ARRAY, null, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator4() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, POINTS_OBJECT, SURFACE_ARRAY, null, true, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator5() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, POINTS_LIST, SURFACE_LIST, null, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator6() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, POINTS_LIST, SURFACE_LIST, null, true, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator7() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, SURFACE_MAP, null, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator8() {
    new InterpolatedFromSurfacesDoublesCube(Plane.XY, SURFACE_MAP, null, true, NAME);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetXData() {
    XY_CUBE.getXData();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetYData() {
    XY_CUBE.getYData();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetZData() {
    XY_CUBE.getZData();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetValues() {
    XY_CUBE.getValues();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSize() {
    XY_CUBE.size();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetValueWithNull1() {
    XY_CUBE.getValue(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetValueWithNull2() {
    XY_CUBE.getValue(null, 2., 3.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetValueWithNull3() {
    XY_CUBE.getValue(1., null, 3.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetValueWithNull4() {
    XY_CUBE.getValue(1., 2., null);
  }

}
