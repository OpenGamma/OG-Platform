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

import com.opengamma.math.function.Function1D;
import com.opengamma.util.Pair;

/**
 * 
 * @author emcleod
 */
public class LinearInterpolator2DTest {
  private static final Function1D<Double, Double> FUNCTION1 = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(Double x) {
      return 4 * x - 5;
    }

  };
  private static final Map<Pair<Double, Double>, Double> DATA1 = new HashMap<Pair<Double, Double>, Double>();
  private static final Interpolator2D INTERPOLATOR = new LinearInterpolator2D();
  private static final double EPS = 1e-12;

  static {
    DATA1.put(new Pair<Double, Double>(0., 0.), FUNCTION1.evaluate(0.));
    DATA1.put(new Pair<Double, Double>(0., 1.), FUNCTION1.evaluate(0.));
    DATA1.put(new Pair<Double, Double>(1., 0.), FUNCTION1.evaluate(1.));
    DATA1.put(new Pair<Double, Double>(1., 1.), FUNCTION1.evaluate(1.));
  }

  @Test
  public void test() {
    try {
      INTERPOLATOR.interpolate(null, null);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      INTERPOLATOR.interpolate(null, new Pair<Double, Double>(null, 0.));
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      INTERPOLATOR.interpolate(null, new Pair<Double, Double>(0., null));
      fail();
    } catch (IllegalArgumentException e) {
      // Expected;
    }
    Double x = 0.3;
    Double y = 0.1;
    assertEquals(INTERPOLATOR.interpolate(DATA1, new Pair<Double, Double>(x, y)).getResult(), FUNCTION1.evaluate(x), EPS);
  }
}
