/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function2D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class GridInterpolator2DTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final Map<DoublesPair, Double> FLAT_DATA = new HashMap<DoublesPair, Double>();
  private static final Function2D<Double, Double> F = new Function2D<Double, Double>() {

    @Override
    public Double evaluate(final Double x, final Double y) {
      return 2 * x - 3.5 * y - 3;
    }

  };
  private static final Interpolator1D<Interpolator1DDataBundle> INTERPOLATOR_1D = new LinearInterpolator1D();
  private static final Interpolator2D INTERPOLATOR_2D = new GridInterpolator2D(INTERPOLATOR_1D, INTERPOLATOR_1D);
  private static final double EPS = 1e-9;

  static {
    FLAT_DATA.put(DoublesPair.of(1., 2.), 0.);
    FLAT_DATA.put(DoublesPair.of(1., 3.), 0.);
    FLAT_DATA.put(DoublesPair.of(1., 5.), 0.);
    FLAT_DATA.put(DoublesPair.of(1., 7.), 0.);
    FLAT_DATA.put(DoublesPair.of(2., 2.), 0.);
    FLAT_DATA.put(DoublesPair.of(2., 3.), 0.);
    FLAT_DATA.put(DoublesPair.of(2., 5.), 0.);
    FLAT_DATA.put(DoublesPair.of(2., 7.), 0.);
    FLAT_DATA.put(DoublesPair.of(5., 2.), 0.);
    FLAT_DATA.put(DoublesPair.of(5., 3.), 0.);
    FLAT_DATA.put(DoublesPair.of(5., 5.), 0.);
    FLAT_DATA.put(DoublesPair.of(5., 7.), 0.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullXInterpolator() {
    new GridInterpolator2D(null, INTERPOLATOR_1D);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullYInterpolator() {
    new GridInterpolator2D(INTERPOLATOR_1D, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDataBundle() {
    INTERPOLATOR_2D.interpolate(null, Pair.of(2., 4.));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullValue() {
    INTERPOLATOR_2D.interpolate(FLAT_DATA, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPair() {
    final Map<DoublesPair, Double> map = new HashMap<DoublesPair, Double>();
    map.put(Pair.of(1., 0.), null);
    INTERPOLATOR_2D.interpolate(map, DoublesPair.of(0.5, 0.5));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInputs() {
    final Map<DoublesPair, Double> data = new HashMap<DoublesPair, Double>();
    data.put(Pair.of(0., 5.), 2.);
    data.put(Pair.of(0., 4.), 3.);
    data.put(Pair.of(0., -3.), 4.);
    INTERPOLATOR_2D.interpolate(data, DoublesPair.of(0., 2.));
  }

  @Test(expected = MathException.class)
  public void testNonGrid() {
    final Map<DoublesPair, Double> nonGrid = new HashMap<DoublesPair, Double>(FLAT_DATA);
    nonGrid.put(Pair.of(5., 8.), 0.);
    INTERPOLATOR_2D.interpolate(nonGrid, DoublesPair.of(1.5, 4.));
  }

  @Test
  public void test() {
    assertEquals(INTERPOLATOR_2D.interpolate(FLAT_DATA, Pair.of(2.5, 5.4)), 0., EPS);
    final Map<DoublesPair, Double> nonTrivial = new HashMap<DoublesPair, Double>();
    for (final DoublesPair pair : FLAT_DATA.keySet()) {
      nonTrivial.put(pair, F.evaluate(pair.getKey(), pair.getValue()));
    }
    final DoublesPair pair = Pair.of(RANDOM.nextDouble() + 2, RANDOM.nextDouble() + 4);
    assertEquals(INTERPOLATOR_2D.interpolate(nonTrivial, pair), F.evaluate(pair.getKey(), pair.getValue()), EPS);
  }
}
