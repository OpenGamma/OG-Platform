/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;

import com.opengamma.util.FirstThenSecondPairComparator;
import com.opengamma.util.Pair;

/**
 * 
 * @author emcleod
 */
public class Interpolator2DTest {
  private static final Interpolator2D DUMMY = new Interpolator2D() {

    @Override
    public InterpolationResult<Double> interpolate(Map<Pair<Double, Double>, Double> data, Pair<Double, Double> value) {
      return new InterpolationResult<Double>(0.);
    }
  };
  private static final Pair<Double, Double> PAIR1 = new Pair<Double, Double>(0., 0.);
  private static final Pair<Double, Double> PAIR2 = new Pair<Double, Double>(0., 1.);
  private static final Pair<Double, Double> PAIR3 = new Pair<Double, Double>(1., 0.);
  private static final Pair<Double, Double> PAIR4 = new Pair<Double, Double>(1., 1.);
  private static final Pair<Double, Double> PAIR5 = new Pair<Double, Double>(2., 0.);
  private static final Pair<Double, Double> PAIR6 = new Pair<Double, Double>(2., 1.);
  private static final Map<Pair<Double, Double>, Double> DATA = new HashMap<Pair<Double, Double>, Double>();
  private static final Comparator<Pair<Double, Double>> COMPARATOR = new FirstThenSecondPairComparator<Double, Double>();

  static {
    DATA.put(PAIR1, 0.);
    DATA.put(PAIR2, 0.);
    DATA.put(PAIR3, 0.);
  }

  @Test
  public void testDataInit() {
    try {
      DUMMY.initData(null, COMPARATOR);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      DUMMY.initData(DATA, null);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      DUMMY.initData(DATA, COMPARATOR);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    DATA.put(PAIR4, 0.);
    TreeSet<Pair<Double, Double>> expected = new TreeSet<Pair<Double, Double>>(COMPARATOR);
    expected.add(PAIR1);
    expected.add(PAIR2);
    expected.add(PAIR3);
    expected.add(PAIR4);
    assertEquals(expected, DUMMY.initData(DATA, COMPARATOR).keySet());
  }

  @Test
  public void testSurroundingPoints() {
    TreeSet<Pair<Double, Double>> sorted = new TreeSet<Pair<Double, Double>>(COMPARATOR);
    sorted.add(PAIR1);
    sorted.add(PAIR2);
    try {
      DUMMY.getSurroundingPointsFromGrid(sorted, new Pair<Double, Double>(0.25, 0.5));
      fail();
    } catch (InterpolationException e) {
      // Expected
    }
    sorted.add(PAIR3);
    sorted.add(PAIR4);
    sorted.add(PAIR5);
    sorted.add(PAIR6);
    try {
      DUMMY.getSurroundingPointsFromGrid(sorted, new Pair<Double, Double>(-1., 0.5));
      fail();
    } catch (InterpolationException e) {
      // Expected
    }
    try {
      DUMMY.getSurroundingPointsFromGrid(sorted, new Pair<Double, Double>(10., 0.5));
      fail();
    } catch (InterpolationException e) {
      // Expected
    }
    try {
      DUMMY.getSurroundingPointsFromGrid(sorted, new Pair<Double, Double>(0.5, -4.5));
      fail();
    } catch (InterpolationException e) {
      // Expected
    }
    try {
      DUMMY.getSurroundingPointsFromGrid(sorted, new Pair<Double, Double>(0.5, 5.));
      fail();
    } catch (InterpolationException e) {
      // Expected
    }
    List<Pair<Double, Double>> surroundingPoints = new ArrayList<Pair<Double, Double>>();
    surroundingPoints.add(PAIR3);
    surroundingPoints.add(PAIR4);
    surroundingPoints.add(PAIR5);
    surroundingPoints.add(PAIR6);
    assertEquals(DUMMY.getSurroundingPoints(sorted, new Pair<Double, Double>(1.5, 0.5), true), surroundingPoints);
    try {
      DUMMY.getSurroundingPoints(sorted, null);
      fail();
    } catch (NotImplementedException e) {
      // Expected
    }
  }
}
