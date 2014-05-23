/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.interpolation.StepInterpolator1D;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pairs;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class InterpolatedFromCurvesDoublesSurfaceTest {
  private static final String NAME = "L";
  private static final ConstantDoublesCurve C1 = ConstantDoublesCurve.from(3.);
  private static final ConstantDoublesCurve C2 = ConstantDoublesCurve.from(5.);
  private static final ConstantDoublesCurve C3 = ConstantDoublesCurve.from(7.);
  private static final ConstantDoublesCurve C4 = ConstantDoublesCurve.from(9.);
  private static final ConstantDoublesCurve C5 = ConstantDoublesCurve.from(11.);
  private static final ConstantDoublesCurve C6 = ConstantDoublesCurve.from(12.);
  private static final LinearInterpolator1D INTERPOLATOR = new LinearInterpolator1D();
  private static final double[] POINT_PRIMITIVE = new double[] {1, 2, 4, 5, 6, 3};
  private static final double[] POINT_PRIMITIVE_SORTED = new double[] {1, 2, 3, 4, 5, 6};
  private static final Double[] POINT_OBJECT;
  private static final Double[] POINT_OBJECT_SORTED;
  @SuppressWarnings("unchecked")
  private static final Curve<Double, Double>[] CURVES = new Curve[] {C1, C2, C4, C5, C6, C3};
  @SuppressWarnings("unchecked")
  private static final Curve<Double, Double>[] CURVES_SORTED = new Curve[] {C1, C2, C3, C4, C5, C6};
  private static final List<Double> POINT_LIST;
  private static final List<Double> POINT_LIST_SORTED;
  private static final List<Curve<Double, Double>> CURVES_LIST;
  private static final List<Curve<Double, Double>> CURVES_LIST_SORTED;
  private static final Map<Double, Curve<Double, Double>> MAP;
  private static final Map<Double, Curve<Double, Double>> MAP_SORTED;
  private static final InterpolatedFromCurvesDoublesSurface SURFACE = InterpolatedFromCurvesDoublesSurface.from(true, POINT_PRIMITIVE, CURVES, INTERPOLATOR, NAME);

  static {
    final int n = 6;
    POINT_OBJECT = new Double[n];
    POINT_OBJECT_SORTED = new Double[n];
    POINT_LIST = new ArrayList<>();
    POINT_LIST_SORTED = new ArrayList<>();
    CURVES_LIST = new ArrayList<>();
    CURVES_LIST_SORTED = new ArrayList<>();
    MAP = new HashMap<>();
    MAP_SORTED = new TreeMap<>();
    for (int i = 0; i < n; i++) {
      final double x1 = POINT_PRIMITIVE[i];
      final double x2 = POINT_PRIMITIVE_SORTED[i];
      final Curve<Double, Double> c1 = CURVES[i];
      final Curve<Double, Double> c2 = CURVES_SORTED[i];
      POINT_OBJECT[i] = x1;
      POINT_OBJECT_SORTED[i] = x2;
      POINT_LIST.add(x1);
      POINT_LIST_SORTED.add(x2);
      CURVES_LIST.add(c1);
      CURVES_LIST_SORTED.add(c2);
      MAP.put(x1, c1);
      MAP_SORTED.put(x2, c2);
    }
  }

  @Test
  public void testXZCurves() {
    assertEquals(SURFACE.getZValue(3.5, 1.), 3., 0);
    assertEquals(SURFACE.getZValue(3.5, 1.5), 4., 0);
    assertEquals(SURFACE.getZValue(3.5, 2.), 5., 0);
    assertEquals(SURFACE.getZValue(3.5, 2.5), 6., 0);
    assertEquals(SURFACE.getZValue(3.5, 3.), 7., 0);
    assertEquals(SURFACE.getZValue(3.5, 3.5), 8., 0);
    assertEquals(SURFACE.getZValue(3.5, 4.), 9., 0);
    assertEquals(SURFACE.getZValue(3.5, 4.5), 10., 0);
    assertEquals(SURFACE.getZValue(3.5, 5.), 11., 0);
    assertEquals(SURFACE.getZValue(3.5, 5.5), 11.5, 0);
    assertEquals(SURFACE.getZValue(3.5, 6.), 12., 0);
    assertEquals(SURFACE.getZValue(3., 1.), 3., 0);
    assertEquals(SURFACE.getZValue(3., 1.5), 4., 0);
    assertEquals(SURFACE.getZValue(3., 2.), 5., 0);
    assertEquals(SURFACE.getZValue(3., 2.5), 6., 0);
    assertEquals(SURFACE.getZValue(3., 3.), 7., 0);
    assertEquals(SURFACE.getZValue(3., 3.5), 8., 0);
    assertEquals(SURFACE.getZValue(3., 4.), 9., 0);
    assertEquals(SURFACE.getZValue(3., 4.5), 10., 0);
    assertEquals(SURFACE.getZValue(3., 5.), 11., 0);
    assertEquals(SURFACE.getZValue(3., 5.5), 11.5, 0);
    assertEquals(SURFACE.getZValue(3., 6.), 12., 0);
  }

  @Test
  public void testYZCurves() {
    final InterpolatedFromCurvesDoublesSurface surface = InterpolatedFromCurvesDoublesSurface.fromSorted(false, MAP_SORTED, INTERPOLATOR);
    assertEquals(surface.getZValue(1., 3.5), 3., 0);
    assertEquals(surface.getZValue(1.5, 3.5), 4., 0);
    assertEquals(surface.getZValue(2., 3.5), 5., 0);
    assertEquals(surface.getZValue(2.5, 3.5), 6., 0);
    assertEquals(surface.getZValue(3., 3.5), 7., 0);
    assertEquals(surface.getZValue(3.5, 3.5), 8., 0);
    assertEquals(surface.getZValue(4., 3.5), 9., 0);
    assertEquals(surface.getZValue(4.5, 3.5), 10., 0);
    assertEquals(surface.getZValue(5., 3.5), 11., 0);
    assertEquals(surface.getZValue(5.5, 3.5), 11.5, 0);
    assertEquals(surface.getZValue(6., 3.5), 12., 0);
    assertEquals(surface.getZValue(1., 3.), 3., 0);
    assertEquals(surface.getZValue(1.5, 3.), 4., 0);
    assertEquals(surface.getZValue(2., 3.), 5., 0);
    assertEquals(surface.getZValue(2.5, 3.), 6., 0);
    assertEquals(surface.getZValue(3., 3.), 7., 0);
    assertEquals(surface.getZValue(3.5, 3.), 8., 0);
    assertEquals(surface.getZValue(4., 3.), 9., 0);
    assertEquals(surface.getZValue(4.5, 3.), 10., 0);
    assertEquals(surface.getZValue(5., 3.), 11., 0);
    assertEquals(surface.getZValue(5.5, 3.), 11.5, 0);
    assertEquals(surface.getZValue(6., 3.), 12., 0);
  }

  @Test
  public void testGetters() {
    assertArrayEquals(SURFACE.getCurves(), CURVES_SORTED);
    assertEquals(SURFACE.getInterpolator(), INTERPOLATOR);
    assertEquals(SURFACE.getName(), NAME);
    assertArrayEquals(SURFACE.getPoints(), POINT_PRIMITIVE_SORTED, 0);
    assertTrue(SURFACE.isXZCurves());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testHashCodeEqualsAndConstruction() {
    InterpolatedFromCurvesDoublesSurface other = InterpolatedFromCurvesDoublesSurface.from(true, POINT_PRIMITIVE, CURVES, INTERPOLATOR, NAME);
    assertEquals(SURFACE, other);
    assertEquals(SURFACE.hashCode(), other.hashCode());
    other = InterpolatedFromCurvesDoublesSurface.from(false, POINT_PRIMITIVE, CURVES, INTERPOLATOR, NAME);
    assertFalse(SURFACE.equals(other));
    other = InterpolatedFromCurvesDoublesSurface.from(true, new double[] {1, 3, 4, 5, 6, 7}, CURVES, INTERPOLATOR, NAME);
    assertFalse(SURFACE.equals(other));
    other = InterpolatedFromCurvesDoublesSurface.from(true, POINT_PRIMITIVE, new Curve[] {C1, C2, C3, C4, C5, ConstantDoublesCurve.from(3.)}, INTERPOLATOR, NAME);
    assertFalse(SURFACE.equals(other));
    other = InterpolatedFromCurvesDoublesSurface.from(true, POINT_PRIMITIVE, CURVES, new StepInterpolator1D(), NAME);
    assertFalse(SURFACE.equals(other));
    other = InterpolatedFromCurvesDoublesSurface.from(true, POINT_PRIMITIVE, CURVES, INTERPOLATOR, "E");
    assertFalse(SURFACE.equals(other));

    other = InterpolatedFromCurvesDoublesSurface.fromSorted(true, POINT_PRIMITIVE_SORTED, CURVES_SORTED, INTERPOLATOR, NAME);
    assertEquals(SURFACE, other);
    other = InterpolatedFromCurvesDoublesSurface.from(true, POINT_OBJECT, CURVES, INTERPOLATOR, NAME);
    assertEquals(SURFACE, other);
    other = InterpolatedFromCurvesDoublesSurface.fromSorted(true, POINT_OBJECT_SORTED, CURVES_SORTED, INTERPOLATOR, NAME);
    assertEquals(SURFACE, other);
    other = InterpolatedFromCurvesDoublesSurface.from(true, POINT_LIST, CURVES_LIST, INTERPOLATOR, NAME);
    assertEquals(SURFACE, other);
    other = InterpolatedFromCurvesDoublesSurface.fromSorted(true, POINT_LIST_SORTED, CURVES_LIST_SORTED, INTERPOLATOR, NAME);
    assertEquals(SURFACE, other);
    other = InterpolatedFromCurvesDoublesSurface.from(true, MAP, INTERPOLATOR, NAME);
    assertEquals(SURFACE, other);
    other = InterpolatedFromCurvesDoublesSurface.fromSorted(true, MAP_SORTED, INTERPOLATOR, NAME);
    assertEquals(SURFACE, other);

    other = new InterpolatedFromCurvesDoublesSurface(true, POINT_PRIMITIVE, CURVES, INTERPOLATOR, false, NAME);
    assertEquals(SURFACE, other);
    other = new InterpolatedFromCurvesDoublesSurface(true, POINT_PRIMITIVE_SORTED, CURVES_SORTED, INTERPOLATOR, true, NAME);
    assertEquals(SURFACE, other);
    other = new InterpolatedFromCurvesDoublesSurface(true, POINT_OBJECT, CURVES, INTERPOLATOR, false, NAME);
    assertEquals(SURFACE, other);
    other = new InterpolatedFromCurvesDoublesSurface(true, POINT_OBJECT_SORTED, CURVES_SORTED, INTERPOLATOR, true, NAME);
    assertEquals(SURFACE, other);
    other = new InterpolatedFromCurvesDoublesSurface(true, POINT_LIST, CURVES_LIST, INTERPOLATOR, false, NAME);
    assertEquals(SURFACE, other);
    other = new InterpolatedFromCurvesDoublesSurface(true, POINT_LIST_SORTED, CURVES_LIST_SORTED, INTERPOLATOR, true, NAME);
    assertEquals(SURFACE, other);
    other = new InterpolatedFromCurvesDoublesSurface(true, MAP, INTERPOLATOR, false, NAME);
    assertEquals(SURFACE, other);
    other = new InterpolatedFromCurvesDoublesSurface(true, MAP_SORTED, INTERPOLATOR, true, NAME);
    assertEquals(SURFACE, other);

    other = InterpolatedFromCurvesDoublesSurface.from(true, POINT_PRIMITIVE, CURVES, INTERPOLATOR);
    assertFalse(SURFACE.equals(other));
    assertArrayEquals(SURFACE.getPoints(), POINT_PRIMITIVE_SORTED, 0);
    assertArrayEquals(SURFACE.getCurves(), CURVES_SORTED);
    other = InterpolatedFromCurvesDoublesSurface.fromSorted(true, POINT_PRIMITIVE_SORTED, CURVES_SORTED, INTERPOLATOR);
    assertFalse(SURFACE.equals(other));
    assertArrayEquals(SURFACE.getPoints(), POINT_PRIMITIVE_SORTED, 0);
    assertArrayEquals(SURFACE.getCurves(), CURVES_SORTED);
    other = InterpolatedFromCurvesDoublesSurface.from(true, POINT_OBJECT, CURVES, INTERPOLATOR);
    assertFalse(SURFACE.equals(other));
    assertArrayEquals(SURFACE.getPoints(), POINT_PRIMITIVE_SORTED, 0);
    assertArrayEquals(SURFACE.getCurves(), CURVES_SORTED);
    other = InterpolatedFromCurvesDoublesSurface.fromSorted(true, POINT_OBJECT_SORTED, CURVES_SORTED, INTERPOLATOR);
    assertFalse(SURFACE.equals(other));
    assertArrayEquals(SURFACE.getPoints(), POINT_PRIMITIVE_SORTED, 0);
    assertArrayEquals(SURFACE.getCurves(), CURVES_SORTED);
    other = InterpolatedFromCurvesDoublesSurface.from(true, POINT_LIST, CURVES_LIST, INTERPOLATOR);
    assertFalse(SURFACE.equals(other));
    assertArrayEquals(SURFACE.getPoints(), POINT_PRIMITIVE_SORTED, 0);
    assertArrayEquals(SURFACE.getCurves(), CURVES_SORTED);
    other = InterpolatedFromCurvesDoublesSurface.fromSorted(true, POINT_LIST_SORTED, CURVES_LIST_SORTED, INTERPOLATOR);
    assertFalse(SURFACE.equals(other));
    assertArrayEquals(SURFACE.getPoints(), POINT_PRIMITIVE_SORTED, 0);
    assertArrayEquals(SURFACE.getCurves(), CURVES_SORTED);
    other = InterpolatedFromCurvesDoublesSurface.from(true, MAP, INTERPOLATOR);
    assertFalse(SURFACE.equals(other));
    assertArrayEquals(SURFACE.getPoints(), POINT_PRIMITIVE_SORTED, 0);
    assertArrayEquals(SURFACE.getCurves(), CURVES_SORTED);
    other = InterpolatedFromCurvesDoublesSurface.fromSorted(true, MAP_SORTED, INTERPOLATOR);
    assertFalse(SURFACE.equals(other));
    assertArrayEquals(SURFACE.getPoints(), POINT_PRIMITIVE_SORTED, 0);
    assertArrayEquals(SURFACE.getCurves(), CURVES_SORTED);

    other = new InterpolatedFromCurvesDoublesSurface(true, POINT_PRIMITIVE_SORTED, CURVES_SORTED, INTERPOLATOR, true);
    assertFalse(SURFACE.equals(other));
    assertArrayEquals(SURFACE.getPoints(), POINT_PRIMITIVE_SORTED, 0);
    assertArrayEquals(SURFACE.getCurves(), CURVES_SORTED);
    other = new InterpolatedFromCurvesDoublesSurface(true, POINT_OBJECT, CURVES, INTERPOLATOR, false);
    assertFalse(SURFACE.equals(other));
    assertArrayEquals(SURFACE.getPoints(), POINT_PRIMITIVE_SORTED, 0);
    assertArrayEquals(SURFACE.getCurves(), CURVES_SORTED);
    other = new InterpolatedFromCurvesDoublesSurface(true, POINT_OBJECT_SORTED, CURVES_SORTED, INTERPOLATOR, true);
    assertFalse(SURFACE.equals(other));
    assertArrayEquals(SURFACE.getPoints(), POINT_PRIMITIVE_SORTED, 0);
    assertArrayEquals(SURFACE.getCurves(), CURVES_SORTED);
    other = new InterpolatedFromCurvesDoublesSurface(true, POINT_LIST, CURVES_LIST, INTERPOLATOR, false);
    assertFalse(SURFACE.equals(other));
    assertArrayEquals(SURFACE.getPoints(), POINT_PRIMITIVE_SORTED, 0);
    assertArrayEquals(SURFACE.getCurves(), CURVES_SORTED);
    other = new InterpolatedFromCurvesDoublesSurface(true, POINT_LIST_SORTED, CURVES_LIST_SORTED, INTERPOLATOR, true);
    assertFalse(SURFACE.equals(other));
    assertArrayEquals(SURFACE.getPoints(), POINT_PRIMITIVE_SORTED, 0);
    assertArrayEquals(SURFACE.getCurves(), CURVES_SORTED);
    other = new InterpolatedFromCurvesDoublesSurface(true, MAP, INTERPOLATOR, false);
    assertFalse(SURFACE.equals(other));
    assertArrayEquals(SURFACE.getPoints(), POINT_PRIMITIVE_SORTED, 0);
    assertArrayEquals(SURFACE.getCurves(), CURVES_SORTED);
    other = new InterpolatedFromCurvesDoublesSurface(true, MAP_SORTED, INTERPOLATOR, true);
    assertFalse(SURFACE.equals(other));
    assertArrayEquals(SURFACE.getPoints(), POINT_PRIMITIVE_SORTED, 0);
    assertArrayEquals(SURFACE.getCurves(), CURVES_SORTED);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetX() {
    SURFACE.getXData();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetY() {
    SURFACE.getYData();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetZ() {
    SURFACE.getZData();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSize() {
    SURFACE.size();
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
  public void testNullPair() {
    SURFACE.getZValue(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullX2() {
    SURFACE.getZValue(Pairs.of((Double) null, 2.));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullY2() {
    SURFACE.getZValue(Pairs.of(1., (Double) null));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull1() {
    new InterpolatedFromCurvesDoublesSurface(true, (double[]) null, CURVES, INTERPOLATOR, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull2() {
    new InterpolatedFromCurvesDoublesSurface(true, POINT_PRIMITIVE, (Curve<Double, Double>[]) null, INTERPOLATOR, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull3() {
    new InterpolatedFromCurvesDoublesSurface(true, POINT_PRIMITIVE, CURVES, null, false);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull4() {
    new InterpolatedFromCurvesDoublesSurface(true, POINT_PRIMITIVE, new Curve[] {C1, C2, C3, C4, C5, null}, INTERPOLATOR, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull5() {
    new InterpolatedFromCurvesDoublesSurface(true, (Double[]) null, CURVES, INTERPOLATOR, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull6() {
    new InterpolatedFromCurvesDoublesSurface(true, new Double[] {1., 2., 3., 4., 5., null}, CURVES, INTERPOLATOR, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull7() {
    new InterpolatedFromCurvesDoublesSurface(true, POINT_OBJECT, (Curve<Double, Double>[]) null, INTERPOLATOR, false);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull8() {
    new InterpolatedFromCurvesDoublesSurface(true, POINT_OBJECT, new Curve[] {C1, C2, C3, C4, C5, null}, INTERPOLATOR, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull9() {
    new InterpolatedFromCurvesDoublesSurface(true, POINT_OBJECT, CURVES, null, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull10() {
    new InterpolatedFromCurvesDoublesSurface(true, (List<Double>) null, CURVES_LIST, INTERPOLATOR, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull11() {
    new InterpolatedFromCurvesDoublesSurface(true, Arrays.asList(1., 2., 3., 4., 5., null), CURVES_LIST, INTERPOLATOR, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull12() {
    new InterpolatedFromCurvesDoublesSurface(true, POINT_LIST, (List<Curve<Double, Double>>) null, INTERPOLATOR, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull13() {
    new InterpolatedFromCurvesDoublesSurface(true, (Map<Double, Curve<Double, Double>>) null, INTERPOLATOR, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull14() {
    final Map<Double, Curve<Double, Double>> m = new HashMap<>();
    m.put(null, C1);
    new InterpolatedFromCurvesDoublesSurface(true, m, INTERPOLATOR, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull15() {
    final Map<Double, Curve<Double, Double>> m = new HashMap<>();
    m.put(2., null);
    new InterpolatedFromCurvesDoublesSurface(true, m, INTERPOLATOR, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull16() {
    new InterpolatedFromCurvesDoublesSurface(true, MAP, null, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull17() {
    new InterpolatedFromCurvesDoublesSurface(true, (double[]) null, CURVES, INTERPOLATOR, false, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull18() {
    new InterpolatedFromCurvesDoublesSurface(true, POINT_PRIMITIVE, (Curve<Double, Double>[]) null, INTERPOLATOR, false, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull19() {
    new InterpolatedFromCurvesDoublesSurface(true, POINT_PRIMITIVE, CURVES, null, false, NAME);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull20() {
    new InterpolatedFromCurvesDoublesSurface(true, POINT_PRIMITIVE, new Curve[] {C1, C2, C3, C4, C5, null}, INTERPOLATOR, false, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull21() {
    new InterpolatedFromCurvesDoublesSurface(true, (Double[]) null, CURVES, INTERPOLATOR, false, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull22() {
    new InterpolatedFromCurvesDoublesSurface(true, new Double[] {1., 2., 3., 4., 5., null}, CURVES, INTERPOLATOR, false, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull23() {
    new InterpolatedFromCurvesDoublesSurface(true, POINT_OBJECT, (Curve<Double, Double>[]) null, INTERPOLATOR, false, NAME);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull24() {
    new InterpolatedFromCurvesDoublesSurface(true, POINT_OBJECT, new Curve[] {C1, C2, C3, C4, C5, null}, INTERPOLATOR, false, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull25() {
    new InterpolatedFromCurvesDoublesSurface(true, POINT_OBJECT, CURVES, null, false, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull26() {
    new InterpolatedFromCurvesDoublesSurface(true, (List<Double>) null, CURVES_LIST, INTERPOLATOR, false, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull27() {
    new InterpolatedFromCurvesDoublesSurface(true, Arrays.asList(1., 2., 3., 4., 5., null), CURVES_LIST, INTERPOLATOR, false, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull28() {
    new InterpolatedFromCurvesDoublesSurface(true, POINT_LIST, (List<Curve<Double, Double>>) null, INTERPOLATOR, false, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull29() {
    new InterpolatedFromCurvesDoublesSurface(true, (Map<Double, Curve<Double, Double>>) null, INTERPOLATOR, false, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull30() {
    final Map<Double, Curve<Double, Double>> m = new HashMap<>();
    m.put(null, C1);
    new InterpolatedFromCurvesDoublesSurface(true, m, INTERPOLATOR, false, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull31() {
    final Map<Double, Curve<Double, Double>> m = new HashMap<>();
    m.put(2., null);
    new InterpolatedFromCurvesDoublesSurface(true, m, INTERPOLATOR, false, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull32() {
    new InterpolatedFromCurvesDoublesSurface(true, MAP, null, false, NAME);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmpty1() {
    new InterpolatedFromCurvesDoublesSurface(true, new double[0], new Curve[0], INTERPOLATOR, false);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmpty2() {
    new InterpolatedFromCurvesDoublesSurface(true, new Double[0], new Curve[0], INTERPOLATOR, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmpty3() {
    new InterpolatedFromCurvesDoublesSurface(true, new ArrayList<Double>(), new ArrayList<Curve<Double, Double>>(), INTERPOLATOR, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmpty4() {
    new InterpolatedFromCurvesDoublesSurface(true, Collections.<Double, Curve<Double, Double>> emptyMap(), INTERPOLATOR, false);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmpty5() {
    new InterpolatedFromCurvesDoublesSurface(true, new double[0], new Curve[0], INTERPOLATOR, false, NAME);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmpty6() {
    new InterpolatedFromCurvesDoublesSurface(true, new Double[0], new Curve[0], INTERPOLATOR, false, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmpty7() {
    new InterpolatedFromCurvesDoublesSurface(true, new ArrayList<Double>(), new ArrayList<Curve<Double, Double>>(), INTERPOLATOR, false, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmpty8() {
    new InterpolatedFromCurvesDoublesSurface(true, Collections.<Double, Curve<Double, Double>> emptyMap(), INTERPOLATOR, false, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength1() {
    new InterpolatedFromCurvesDoublesSurface(true, new double[] {1}, CURVES, INTERPOLATOR, false);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength2() {
    new InterpolatedFromCurvesDoublesSurface(true, POINT_PRIMITIVE, new Curve[] {C1}, INTERPOLATOR, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength3() {
    new InterpolatedFromCurvesDoublesSurface(true, new Double[] {1.}, CURVES, INTERPOLATOR, false);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength4() {
    new InterpolatedFromCurvesDoublesSurface(true, POINT_OBJECT, new Curve[] {C1}, INTERPOLATOR, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength5() {
    new InterpolatedFromCurvesDoublesSurface(true, Arrays.asList(2.), CURVES_LIST, INTERPOLATOR, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength6() {
    final List<Curve<Double, Double>> l = new ArrayList<>();
    l.add(C1);
    new InterpolatedFromCurvesDoublesSurface(true, POINT_LIST, l, INTERPOLATOR, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength7() {
    new InterpolatedFromCurvesDoublesSurface(true, new double[] {1}, CURVES, INTERPOLATOR, false, NAME);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength8() {
    new InterpolatedFromCurvesDoublesSurface(true, POINT_PRIMITIVE, new Curve[] {C1}, INTERPOLATOR, false, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength9() {
    new InterpolatedFromCurvesDoublesSurface(true, new Double[] {1.}, CURVES, INTERPOLATOR, false, NAME);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength10() {
    new InterpolatedFromCurvesDoublesSurface(true, POINT_OBJECT, new Curve[] {C1}, INTERPOLATOR, false, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength11() {
    new InterpolatedFromCurvesDoublesSurface(true, Arrays.asList(2.), CURVES_LIST, INTERPOLATOR, false, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength12() {
    final List<Curve<Double, Double>> l = new ArrayList<>();
    l.add(C1);
    new InterpolatedFromCurvesDoublesSurface(true, POINT_LIST, l, INTERPOLATOR, false, NAME);
  }
}
