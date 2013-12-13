/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class GridInterpolator2DTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final Map<DoublesPair, Double> FLAT_DATA = new HashMap<>();
  private static final Function2D<Double, Double> F = new Function2D<Double, Double>() {

    @Override
    public Double evaluate(final Double x, final Double y) {
      return 2 * x - 3.5 * y - 3;
    }

  };
  private static final Interpolator1D INTERPOLATOR_1D = new LinearInterpolator1D();
  private static final GridInterpolator2D INTERPOLATOR_2D = new GridInterpolator2D(INTERPOLATOR_1D, INTERPOLATOR_1D);
  private static final Map<Double, Interpolator1DDataBundle> FLAT_DATA_BUNDLE;
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
    FLAT_DATA_BUNDLE = INTERPOLATOR_2D.getDataBundle(FLAT_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullXInterpolator() {
    new GridInterpolator2D(null, INTERPOLATOR_1D);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYInterpolator() {
    new GridInterpolator2D(INTERPOLATOR_1D, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataBundle() {
    INTERPOLATOR_2D.interpolate(null, DoublesPair.of(2., 4.));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValue() {
    INTERPOLATOR_2D.interpolate(FLAT_DATA_BUNDLE, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPair() {
    final Map<DoublesPair, Double> map = new HashMap<>();
    map.put(DoublesPair.of(1., 0.), null);
    INTERPOLATOR_2D.interpolate(INTERPOLATOR_2D.getDataBundle(map), DoublesPair.of(0.5, 0.5));
  }

  @Test
  public void testObject() {
    assertEquals(INTERPOLATOR_2D.getXInterpolator(), INTERPOLATOR_1D);
    assertEquals(INTERPOLATOR_2D.getYInterpolator(), INTERPOLATOR_1D);
    GridInterpolator2D other = new GridInterpolator2D(INTERPOLATOR_1D, INTERPOLATOR_1D);
    assertEquals(INTERPOLATOR_2D, other);
    assertEquals(INTERPOLATOR_2D.hashCode(), other.hashCode());
    other = new GridInterpolator2D(Interpolator1DFactory.LOG_LINEAR_INSTANCE, INTERPOLATOR_1D);
    assertFalse(INTERPOLATOR_2D.equals(other));
    other = new GridInterpolator2D(INTERPOLATOR_1D, Interpolator1DFactory.LOG_LINEAR_INSTANCE);
    assertFalse(INTERPOLATOR_2D.equals(other));
  }

  @Test
  public void test() {
    assertEquals(INTERPOLATOR_2D.interpolate(FLAT_DATA_BUNDLE, DoublesPair.of(2.5, 5.4)), 0., EPS);
    final Map<DoublesPair, Double> nonTrivial = new HashMap<>();
    for (final DoublesPair pair : FLAT_DATA.keySet()) {
      nonTrivial.put(pair, F.evaluate(pair.getFirst(), pair.getSecond()));
    }
    final DoublesPair pair = DoublesPair.of(RANDOM.nextDouble() + 2, RANDOM.nextDouble() + 4);
    assertEquals(INTERPOLATOR_2D.interpolate(INTERPOLATOR_2D.getDataBundle(nonTrivial), pair), F.evaluate(pair.getFirst(), pair.getSecond()), EPS);
  }
}
