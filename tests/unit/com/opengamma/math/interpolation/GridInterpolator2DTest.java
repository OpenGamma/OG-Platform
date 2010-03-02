/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function2D;
import com.opengamma.util.Pair;

/**
 * 
 * @author emcleod
 */
public class GridInterpolator2DTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final Map<Pair<Double, Double>, Double> FLAT_DATA = new HashMap<Pair<Double, Double>, Double>();
  private static final Function2D<Double, Double> F = new Function2D<Double, Double>() {

    @Override
    public Double evaluate(final Double x, final Double y) {
      return 2 * x - 3.5 * y - 3;
    }

  };
  private static final Interpolator1D INTERPOLATOR_1D = new LinearInterpolator1D();
  private static final Interpolator2D INTERPOLATOR_2D = new GridInterpolator2D(INTERPOLATOR_1D, INTERPOLATOR_1D);
  private static final double EPS = 1e-9;

  static {
    FLAT_DATA.put(new Pair<Double, Double>(1., 2.), 0.);
    FLAT_DATA.put(new Pair<Double, Double>(1., 3.), 0.);
    FLAT_DATA.put(new Pair<Double, Double>(1., 5.), 0.);
    FLAT_DATA.put(new Pair<Double, Double>(1., 7.), 0.);
    FLAT_DATA.put(new Pair<Double, Double>(2., 2.), 0.);
    FLAT_DATA.put(new Pair<Double, Double>(2., 3.), 0.);
    FLAT_DATA.put(new Pair<Double, Double>(2., 5.), 0.);
    FLAT_DATA.put(new Pair<Double, Double>(2., 7.), 0.);
    FLAT_DATA.put(new Pair<Double, Double>(5., 2.), 0.);
    FLAT_DATA.put(new Pair<Double, Double>(5., 3.), 0.);
    FLAT_DATA.put(new Pair<Double, Double>(5., 5.), 0.);
    FLAT_DATA.put(new Pair<Double, Double>(5., 7.), 0.);
  }

  @Test
  public void testInputs() {
    try {
      new GridInterpolator2D(null, INTERPOLATOR_1D);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      new GridInterpolator2D(INTERPOLATOR_1D, null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      INTERPOLATOR_2D.interpolate(FLAT_DATA, null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final Pair<Double, Double> zeroes = new Pair<Double, Double>(0., 0.);
    try {
      INTERPOLATOR_2D.interpolate(null, zeroes);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final Map<Pair<Double, Double>, Double> data = new HashMap<Pair<Double, Double>, Double>();
    data.put(zeroes, null);
    try {
      INTERPOLATOR_2D.interpolate(data, zeroes);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    data.put(new Pair<Double, Double>(0., 5.), 2.);
    data.put(new Pair<Double, Double>(0., 4.), 3.);
    data.put(new Pair<Double, Double>(0., -3.), 4.);
    try {
      INTERPOLATOR_2D.interpolate(data, zeroes);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    data.put(zeroes, 3.);
    try {
      INTERPOLATOR_2D.interpolate(data, zeroes);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testNonGrid() {
    final Map<Pair<Double, Double>, Double> nonGrid = new HashMap<Pair<Double, Double>, Double>(FLAT_DATA);
    nonGrid.put(new Pair<Double, Double>(5., 8.), 0.);
    try {
      INTERPOLATOR_2D.interpolate(nonGrid, new Pair<Double, Double>(1.5, 4.));
      fail();
    } catch (final InterpolationException e) {
      // Expected
    }
  }

  @Test
  public void test() {
    assertEquals(INTERPOLATOR_2D.interpolate(FLAT_DATA, new Pair<Double, Double>(2.5, 5.4)).getResult(), 0., EPS);
    final Map<Pair<Double, Double>, Double> nonTrivial = new HashMap<Pair<Double, Double>, Double>();
    for (final Pair<Double, Double> pair : FLAT_DATA.keySet()) {
      nonTrivial.put(pair, F.evaluate(pair.getKey(), pair.getValue()));
    }
    final Pair<Double, Double> pair = new Pair<Double, Double>(RANDOM.nextDouble() + 2, RANDOM.nextDouble() + 4);
    assertEquals(INTERPOLATOR_2D.interpolate(nonTrivial, pair).getResult(), F.evaluate(pair.getKey(), pair.getValue()), EPS);
  }
}
