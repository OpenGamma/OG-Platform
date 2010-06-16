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

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class LinearInterpolator1DTest {
  private static final Interpolator1D<Interpolator1DModel> INTERPOLATOR = new LinearInterpolator1D();
  private static final Function1D<Double, Double> FUNCTION = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 2 * x - 7;
    }
  };
  private static final Interpolator1DModel MODEL = Interpolator1DModelFactory.fromArrays(new double[] { 1, 2, 3 },
      new double[] { 4, 5, 6 });

  @Test(expected = IllegalArgumentException.class)
  public void testNullModel() {
    INTERPOLATOR.interpolate(null, 2.3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullValue() {
    INTERPOLATOR.interpolate(MODEL, null);
  }

  @Test(expected = InterpolationException.class)
  public void testLowValue() {
    INTERPOLATOR.interpolate(MODEL, -4.);
  }

  @Test(expected = InterpolationException.class)
  public void testHighValue() {
    INTERPOLATOR.interpolate(MODEL, 10.);
  }

  @Test
  public void test() {
    final Map<Double, Double> data = new HashMap<Double, Double>();
    double x;
    for (int i = 0; i < 10; i++) {
      x = Double.valueOf(i);
      data.put(x, FUNCTION.evaluate(x));
    }
    assertEquals(INTERPOLATOR.interpolate(Interpolator1DModelFactory.fromMap(data), 3.4), FUNCTION.evaluate(3.4));
  }
}
