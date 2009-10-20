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

import org.junit.Test;

import com.opengamma.math.function.FunctionND;

/**
 * 
 * @author emcleod
 */
public class InterpolatorNDTest {
  private static final InterpolatorND INTERPOLATOR = new InterpolatorND() {

    @Override
    public InterpolationResult<Double> interpolate(final Map<List<Double>, Double> data, final List<Double> value) {
      return null;
    }
  };
  protected static final Map<List<Double>, Double> FLAT_DATA = new HashMap<List<Double>, Double>();
  protected static final Map<List<Double>, Double> DATA1 = new HashMap<List<Double>, Double>();
  protected static final Map<List<Double>, Double> DATA2 = new HashMap<List<Double>, Double>();
  protected static final FunctionND<Double, Double> F1 = new FunctionND<Double, Double>(3) {

    @Override
    public Double evaluateFunction(final Double[] x) {
      return 2 * x[0] * x[0] - x[1] * x[1] + x[2] * x[2] - 3 * x[0] * x[1] + 4;
    }

  };
  protected static final FunctionND<Double, Double> F2 = new FunctionND<Double, Double>(3) {

    @Override
    public Double evaluateFunction(final Double[] x) {
      return x[0] * x[0] + x[1] * x[1] + x[2] * x[2];
    }

  };
  protected static final double VALUE = 0.3;
  static {
    double x, y, z;
    for (int i = 0; i < 20; i++) {
      x = Math.random();
      y = Math.random();
      FLAT_DATA.put(Arrays.asList(x, y), VALUE);
    }
    for (int i = 0; i < 100; i++) {
      x = Math.random();
      y = Math.random();
      z = Math.random();
      DATA1.put(Arrays.asList(x, y, z), F1.evaluate(x, y, z));
      DATA2.put(Arrays.asList(x, y, z), F2.evaluate(x, y, z));
    }
  }

  @Test
  public void testData() {
    try {
      INTERPOLATOR.checkData(null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      INTERPOLATOR.checkData(Collections.singletonMap(Collections.singletonList(3.), 4.));
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
      INTERPOLATOR.checkData(data);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    data.clear();
    data.put(l1, 0.1);
    data.put(l2, null);
    try {
      INTERPOLATOR.checkData(data);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    data.put(l1, 0.1);
    data.put(l2, 0.2);
    data.put(l3, 0.3);
    try {
      INTERPOLATOR.getDimension(data.keySet());
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    data.clear();
    data.put(l1, 0.1);
    data.put(l2, 0.2);
    data.put(l2, 0.3);
    assertEquals(INTERPOLATOR.getDimension(data.keySet()), 3);
  }
}
