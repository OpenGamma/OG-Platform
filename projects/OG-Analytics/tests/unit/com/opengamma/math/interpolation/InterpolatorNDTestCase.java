/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

/**
 * 
 */
public class InterpolatorNDTestCase {
  protected static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  protected static final Map<List<Double>, Double> FLAT_DATA = new HashMap<List<Double>, Double>();
  protected static final double VALUE = 0.3;
  static {
    double x, y, z;
    for (int i = 0; i < 20; i++) {
      x = 10 * RANDOM.nextDouble();
      y = 10 * RANDOM.nextDouble();
      z = 10 * RANDOM.nextDouble();
      FLAT_DATA.put(Arrays.asList(x, y, z), VALUE);
    }
  }

  public void testData(final InterpolatorND interpolator) {
    try {
      interpolator.checkData(null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      interpolator.checkData(Collections.singletonMap(Collections.singletonList(3.), 4.));
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final Map<List<Double>, Double> data = new HashMap<List<Double>, Double>();
    final List<Double> l1 = Arrays.asList(1., 2., 3.);
    final List<Double> l2 = Arrays.asList(4., 5., 6.);
    final List<Double> l3 = Arrays.asList(7., 8., 9., 10.);
    data.put(null, 0.1);
    data.put(null, 0.1);
    try {
      interpolator.checkData(data);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    data.clear();
    data.put(l1, 0.1);
    data.put(l2, null);
    try {
      interpolator.checkData(data);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    data.put(l1, 0.1);
    data.put(l2, 0.2);
    data.put(l3, 0.3);
    try {
      interpolator.getDimension(data.keySet());
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    data.clear();
    data.put(l1, 0.1);
    data.put(l2, 0.2);
    data.put(l2, 0.3);
    assertEquals(interpolator.getDimension(data.keySet()), 3);
  }
}
