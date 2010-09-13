/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Test;

import com.opengamma.math.ParallelArrayBinarySort;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class NodalObjectObjectCurveTest {
  protected static final String NAME1 = "a";
  protected static final String NAME2 = "b";
  protected static final Float[] X_OBJECT;
  protected static final Double[] Y_OBJECT;
  protected static final Float[] X_OBJECT_SORTED;
  protected static final Double[] Y_OBJECT_SORTED;
  protected static final Map<Float, Double> MAP;
  protected static final Map<Float, Double> MAP_SORTED;
  protected static final Set<Pair<Float, Double>> PAIR_SET;
  protected static final Set<Pair<Float, Double>> PAIR_SET_SORTED;

  static {
    final int n = 10;
    X_OBJECT = new Float[n];
    Y_OBJECT = new Double[n];
    MAP = new HashMap<Float, Double>();
    PAIR_SET = new HashSet<Pair<Float, Double>>();
    double x, y;
    Float xFloat;
    for (int i = 0; i < 5; i++) {
      x = 2 * i;
      y = 3 * x;
      xFloat = Float.valueOf((float) x);
      X_OBJECT[i] = xFloat;
      Y_OBJECT[i] = y;
      MAP.put(xFloat, y);
      PAIR_SET.add(new ObjectsPair<Float, Double>(xFloat, y));
    }
    for (int i = 5, j = 0; i < 10; i++, j++) {
      x = 2 * j + 1;
      y = 3 * x;
      xFloat = Float.valueOf((float) x);
      X_OBJECT[i] = xFloat;
      Y_OBJECT[i] = y;
      MAP.put(xFloat, y);
      PAIR_SET.add(new ObjectsPair<Float, Double>(xFloat, y));
    }
    X_OBJECT_SORTED = Arrays.copyOf(X_OBJECT, n);
    Y_OBJECT_SORTED = Arrays.copyOf(Y_OBJECT, n);
    ParallelArrayBinarySort.parallelBinarySort(X_OBJECT_SORTED, Y_OBJECT_SORTED);
    MAP_SORTED = new TreeMap<Float, Double>(MAP);
    PAIR_SET_SORTED = new TreeSet<Pair<Float, Double>>();
    PAIR_SET_SORTED.addAll(PAIR_SET);
  }

  @Test
  public void testEqualsAndHashCode() {
    final NodalObjectObjectCurve<Float, Double> curve = new NodalObjectObjectCurve<Float, Double>(X_OBJECT, Y_OBJECT, false, NAME1);
    NodalObjectObjectCurve<Float, Double> other = new NodalObjectObjectCurve<Float, Double>(X_OBJECT, Y_OBJECT, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalObjectObjectCurve<Float, Double>(X_OBJECT, Y_OBJECT, false);
    assertFalse(curve.equals(other));
    final Float[] temp1 = Arrays.copyOf(X_OBJECT, 10);
    temp1[0] = 1000f;
    other = new NodalObjectObjectCurve<Float, Double>(temp1, Y_OBJECT, false, NAME1);
    assertFalse(curve.equals(other));
    final Double[] temp2 = Arrays.copyOf(Y_OBJECT, 10);
    temp2[0] = 1e14;
    other = new NodalObjectObjectCurve<Float, Double>(X_OBJECT, temp2, false, NAME1);
    assertFalse(curve.equals(other));
    other = new NodalObjectObjectCurve<Float, Double>(X_OBJECT, Y_OBJECT, true, NAME1);
    assertFalse(curve.equals(other));
    other = new NodalObjectObjectCurve<Float, Double>(X_OBJECT, Y_OBJECT, false, NAME2);
    assertFalse(curve.equals(other));
    other = new NodalObjectObjectCurve<Float, Double>(X_OBJECT_SORTED, Y_OBJECT_SORTED, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalObjectObjectCurve<Float, Double>(MAP, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalObjectObjectCurve<Float, Double>(MAP_SORTED, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalObjectObjectCurve<Float, Double>(PAIR_SET, false, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new NodalObjectObjectCurve<Float, Double>(PAIR_SET_SORTED, true, NAME1);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
  }

  @Test
  public void testStaticConstruction() {
    NodalObjectObjectCurve<Float, Double> curve = new NodalObjectObjectCurve<Float, Double>(X_OBJECT, Y_OBJECT, false, NAME1);
    NodalObjectObjectCurve<Float, Double> other = NodalObjectObjectCurve.from(X_OBJECT, Y_OBJECT, NAME1);
    assertEquals(curve, other);
    curve = new NodalObjectObjectCurve<Float, Double>(X_OBJECT_SORTED, Y_OBJECT_SORTED, true, NAME1);
    other = NodalObjectObjectCurve.fromSorted(X_OBJECT_SORTED, Y_OBJECT_SORTED, NAME1);
    assertEquals(curve, other);
    curve = new NodalObjectObjectCurve<Float, Double>(MAP, false, NAME1);
    other = NodalObjectObjectCurve.from(MAP, NAME1);
    assertEquals(curve, other);
    curve = new NodalObjectObjectCurve<Float, Double>(MAP_SORTED, true, NAME1);
    other = NodalObjectObjectCurve.fromSorted(MAP_SORTED, NAME1);
    assertEquals(curve, other);
    curve = new NodalObjectObjectCurve<Float, Double>(PAIR_SET, false, NAME1);
    other = NodalObjectObjectCurve.from(PAIR_SET, NAME1);
    assertEquals(curve, other);
    curve = new NodalObjectObjectCurve<Float, Double>(PAIR_SET_SORTED, true, NAME1);
    other = NodalObjectObjectCurve.fromSorted(PAIR_SET_SORTED, NAME1);
    assertEquals(curve, other);
    curve = new NodalObjectObjectCurve<Float, Double>(X_OBJECT, Y_OBJECT, false);
    other = NodalObjectObjectCurve.from(X_OBJECT, Y_OBJECT);
    assertFalse(curve.equals(other));
    curve = new NodalObjectObjectCurve<Float, Double>(X_OBJECT_SORTED, Y_OBJECT_SORTED, true);
    other = NodalObjectObjectCurve.fromSorted(X_OBJECT_SORTED, Y_OBJECT_SORTED);
    assertFalse(curve.equals(other));
    curve = new NodalObjectObjectCurve<Float, Double>(MAP, false);
    other = NodalObjectObjectCurve.from(MAP);
    assertFalse(curve.equals(other));
    curve = new NodalObjectObjectCurve<Float, Double>(MAP_SORTED, true);
    other = NodalObjectObjectCurve.fromSorted(MAP_SORTED);
    assertFalse(curve.equals(other));
    curve = new NodalObjectObjectCurve<Float, Double>(PAIR_SET, false);
    other = NodalObjectObjectCurve.from(PAIR_SET);
    assertFalse(curve.equals(other));
    curve = new NodalObjectObjectCurve<Float, Double>(PAIR_SET_SORTED, true);
    other = NodalObjectObjectCurve.fromSorted(PAIR_SET_SORTED);
    assertFalse(curve.equals(other));
  }

  @Test
  public void testGetters() {
    final NodalObjectObjectCurve<Float, Double> curve = NodalObjectObjectCurve.from(PAIR_SET, NAME1);
    assertEquals(curve.getName(), NAME1);
    assertArrayEquals(curve.getXData(), X_OBJECT_SORTED);
    assertArrayEquals(curve.getYData(), Y_OBJECT_SORTED);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonNodalPoint() {
    NodalObjectObjectCurve.from(MAP).getYValue(3.1f);
  }

  @Test
  public void testGetYValue() {
    final NodalObjectObjectCurve<Float, Double> curve = NodalObjectObjectCurve.from(MAP_SORTED, NAME1);
    for (int i = 0; i < 10; i++) {
      assertEquals(curve.getYValue(X_OBJECT[i]), Y_OBJECT[i], 0);
    }
  }
}
