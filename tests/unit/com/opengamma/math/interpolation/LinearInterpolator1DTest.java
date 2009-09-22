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

/**
 * 
 * @author emcleod
 */
public class LinearInterpolator1DTest {
  private static final Interpolator1D INTERPOLATOR = new LinearInterpolator1D();
  private static final Function1D<Double, Double> FUNCTION = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(Double x) {
      return 2 * x - 7;
    }
  };

  @Test
  public void test() {
    try {
      INTERPOLATOR.interpolate(new HashMap<Double, Double>(), null);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    Map<Double, Double> data = new HashMap<Double, Double>();
    double x;
    for (int i = 0; i < 10; i++) {
      x = Double.valueOf(i);
      data.put(x, FUNCTION.evaluate(x));
    }
    assertEquals(INTERPOLATOR.interpolate(data, 3.4), new InterpolationResult<Double>(FUNCTION.evaluate(3.4)));
  }
}
