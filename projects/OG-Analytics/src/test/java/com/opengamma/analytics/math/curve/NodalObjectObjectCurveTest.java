/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.testng.annotations.Test;

import com.opengamma.util.ParallelArrayBinarySort;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NodalObjectObjectCurveTest {
  static final String NAME1 = "a";
  static final String NAME2 = "b";
  static final Float[] X_OBJECT;
  static final Double[] Y_OBJECT;
  static final Float[] X_OBJECT_SORTED;
  static final Double[] Y_OBJECT_SORTED;
  static final Map<Float, Double> MAP;
  static final Map<Float, Double> MAP_SORTED;
  static final Set<Pair<Float, Double>> PAIR_SET;
  static final Set<Pair<Float, Double>> PAIR_SET_SORTED;
  static final List<Float> X_LIST;
  static final List<Double> Y_LIST;
  static final List<Float> X_LIST_SORTED;
  static final List<Double> Y_LIST_SORTED;

  static {
    final int n = 10;
    X_OBJECT = new Float[n];
    Y_OBJECT = new Double[n];
    MAP = new HashMap<>();
    PAIR_SET = new HashSet<>();
    X_LIST = new ArrayList<>();
    Y_LIST = new ArrayList<>();
    float x;
    double y;
    Float xFloat;
    for (int i = 0; i < 5; i++) {
      x = 2 * i;
      y = 3 * x;
      xFloat = Float.valueOf(x);
      X_OBJECT[i] = xFloat;
      Y_OBJECT[i] = y;
      MAP.put(xFloat, y);
      PAIR_SET.add(ObjectsPair.of(xFloat, (Double) y));
      X_LIST.add(xFloat);
      Y_LIST.add(y);
    }
    for (int i = 5, j = 0; i < n; i++, j++) {
      x = 2 * j + 1;
      y = 3 * x;
      xFloat = Float.valueOf(x);
      X_OBJECT[i] = xFloat;
      Y_OBJECT[i] = y;
      MAP.put(xFloat, y);
      PAIR_SET.add(ObjectsPair.of(xFloat, (Double) y));
      X_LIST.add(xFloat);
      Y_LIST.add(y);
    }
    X_OBJECT_SORTED = Arrays.copyOf(X_OBJECT, n);
    Y_OBJECT_SORTED = Arrays.copyOf(Y_OBJECT, n);
    ParallelArrayBinarySort.parallelBinarySort(X_OBJECT_SORTED, Y_OBJECT_SORTED);
    MAP_SORTED = new TreeMap<>(MAP);
    PAIR_SET_SORTED = new TreeSet<>(new Comparator<Pair<Float, Double>>() {

      @Override
      public int compare(final Pair<Float, Double> o1, final Pair<Float, Double> o2) {
        final int compare = o1.getFirst().compareTo(o2.getFirst());
        if (compare != 0) {
          return compare;
        }
        return o1.getSecond().compareTo(o2.getSecond());
      }

    });
    PAIR_SET_SORTED.addAll(PAIR_SET);
    X_LIST_SORTED = new ArrayList<>();
    Y_LIST_SORTED = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      X_LIST_SORTED.add(X_OBJECT_SORTED[i]);
      Y_LIST_SORTED.add(Y_OBJECT_SORTED[i]);
    }
  }

  @Test
  public void testEqualsAndHashCode() {
    final NodalObjectsCurve<Float, Double> curve = new NodalObjectsCurve<>(X_OBJECT, Y_OBJECT, false, NAME1);
    NodalObjectsCurve<Float, Double> other = new NodalObjectsCurve<>(X_OBJECT, Y_OBJECT, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalObjectsCurve<>(X_OBJECT, Y_OBJECT, false);
    assertFalse(curve.equals(other));
    final Float[] temp1 = Arrays.copyOf(X_OBJECT, 10);
    temp1[0] = 1000f;
    other = new NodalObjectsCurve<>(temp1, Y_OBJECT, false, NAME1);
    assertFalse(curve.equals(other));
    final Double[] temp2 = Arrays.copyOf(Y_OBJECT, 10);
    temp2[0] = 1e14;
    other = new NodalObjectsCurve<>(X_OBJECT, temp2, false, NAME1);
    assertFalse(curve.equals(other));
    other = new NodalObjectsCurve<>(X_OBJECT, Y_OBJECT, true, NAME1);
    assertFalse(curve.equals(other));
    other = new NodalObjectsCurve<>(X_OBJECT, Y_OBJECT, false, NAME2);
    assertFalse(curve.equals(other));
    other = new NodalObjectsCurve<>(X_OBJECT_SORTED, Y_OBJECT_SORTED, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalObjectsCurve<>(MAP, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalObjectsCurve<>(MAP_SORTED, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalObjectsCurve<>(PAIR_SET, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalObjectsCurve<>(PAIR_SET_SORTED, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalObjectsCurve<>(X_LIST, Y_LIST, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalObjectsCurve<>(X_LIST_SORTED, Y_LIST_SORTED, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
  }

  @Test
  public void testStaticConstruction() {
    NodalObjectsCurve<Float, Double> curve = new NodalObjectsCurve<>(X_OBJECT, Y_OBJECT, false, NAME1);
    NodalObjectsCurve<Float, Double> other = NodalObjectsCurve.from(X_OBJECT, Y_OBJECT, NAME1);
    assertEquals(curve, other);
    curve = new NodalObjectsCurve<>(X_OBJECT_SORTED, Y_OBJECT_SORTED, true, NAME1);
    other = NodalObjectsCurve.fromSorted(X_OBJECT_SORTED, Y_OBJECT_SORTED, NAME1);
    assertEquals(curve, other);
    curve = new NodalObjectsCurve<>(MAP, false, NAME1);
    other = NodalObjectsCurve.from(MAP, NAME1);
    assertEquals(curve, other);
    curve = new NodalObjectsCurve<>(MAP_SORTED, true, NAME1);
    other = NodalObjectsCurve.fromSorted(MAP_SORTED, NAME1);
    assertEquals(curve, other);
    curve = new NodalObjectsCurve<>(PAIR_SET, false, NAME1);
    other = NodalObjectsCurve.from(PAIR_SET, NAME1);
    assertEquals(curve, other);
    curve = new NodalObjectsCurve<>(X_LIST, Y_LIST, false, NAME1);
    other = NodalObjectsCurve.from(X_LIST, Y_LIST, NAME1);
    assertEquals(curve, other);
    curve = new NodalObjectsCurve<>(X_LIST_SORTED, Y_LIST_SORTED, false, NAME1);
    other = NodalObjectsCurve.fromSorted(X_LIST_SORTED, Y_LIST_SORTED, NAME1);
    assertEquals(curve, other);
    curve = new NodalObjectsCurve<>(PAIR_SET_SORTED, true, NAME1);
    other = NodalObjectsCurve.fromSorted(PAIR_SET_SORTED, NAME1);
    assertEquals(curve, other);
    curve = new NodalObjectsCurve<>(X_OBJECT, Y_OBJECT, false);
    other = NodalObjectsCurve.from(X_OBJECT, Y_OBJECT);
    assertFalse(curve.equals(other));
    assertArrayEquals(curve.getXData(), X_OBJECT_SORTED);
    assertArrayEquals(curve.getYData(), Y_OBJECT_SORTED);
    curve = new NodalObjectsCurve<>(X_OBJECT_SORTED, Y_OBJECT_SORTED, true);
    other = NodalObjectsCurve.fromSorted(X_OBJECT_SORTED, Y_OBJECT_SORTED);
    assertFalse(curve.equals(other));
    assertArrayEquals(curve.getXData(), X_OBJECT_SORTED);
    assertArrayEquals(curve.getYData(), Y_OBJECT_SORTED);
    curve = new NodalObjectsCurve<>(MAP, false);
    other = NodalObjectsCurve.from(MAP);
    assertFalse(curve.equals(other));
    assertArrayEquals(curve.getXData(), X_OBJECT_SORTED);
    assertArrayEquals(curve.getYData(), Y_OBJECT_SORTED);
    curve = new NodalObjectsCurve<>(MAP_SORTED, true);
    other = NodalObjectsCurve.fromSorted(MAP_SORTED);
    assertFalse(curve.equals(other));
    assertArrayEquals(curve.getXData(), X_OBJECT_SORTED);
    assertArrayEquals(curve.getYData(), Y_OBJECT_SORTED);
    curve = new NodalObjectsCurve<>(PAIR_SET, false);
    other = NodalObjectsCurve.from(PAIR_SET);
    assertFalse(curve.equals(other));
    assertArrayEquals(curve.getXData(), X_OBJECT_SORTED);
    assertArrayEquals(curve.getYData(), Y_OBJECT_SORTED);
    curve = new NodalObjectsCurve<>(PAIR_SET_SORTED, true);
    other = NodalObjectsCurve.fromSorted(PAIR_SET_SORTED);
    assertFalse(curve.equals(other));
    assertArrayEquals(curve.getXData(), X_OBJECT_SORTED);
    assertArrayEquals(curve.getYData(), Y_OBJECT_SORTED);
    curve = new NodalObjectsCurve<>(X_LIST, Y_LIST, false);
    other = NodalObjectsCurve.from(X_LIST, Y_LIST);
    assertFalse(curve.equals(other));
    assertArrayEquals(curve.getXData(), X_OBJECT_SORTED);
    assertArrayEquals(curve.getYData(), Y_OBJECT_SORTED);
    curve = new NodalObjectsCurve<>(X_LIST_SORTED, Y_LIST_SORTED, true);
    other = NodalObjectsCurve.fromSorted(X_LIST_SORTED, Y_LIST_SORTED);
    assertFalse(curve.equals(other));
    assertArrayEquals(curve.getXData(), X_OBJECT_SORTED);
    assertArrayEquals(curve.getYData(), Y_OBJECT_SORTED);
  }

  @Test
  public void testGetters() {
    final NodalObjectsCurve<Float, Double> curve = NodalObjectsCurve.from(PAIR_SET, NAME1);
    assertEquals(curve.getName(), NAME1);
    assertArrayEquals(curve.getXData(), X_OBJECT_SORTED);
    assertArrayEquals(curve.getYData(), Y_OBJECT_SORTED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNonNodalPoint() {
    NodalObjectsCurve.from(MAP).getYValue(3.1f);
  }

  @Test
  public void testGetYValue() {
    final NodalObjectsCurve<Float, Double> curve = NodalObjectsCurve.from(MAP_SORTED, NAME1);
    for (int i = 0; i < 10; i++) {
      assertEquals(curve.getYValue(X_OBJECT[i]), Y_OBJECT[i], 0);
    }
  }
}
