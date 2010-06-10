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
 */
public class LogLinearInterpolator1DTest {
  private static final Interpolator1D<Interpolator1DModel> LINEAR = new LinearInterpolator1D();
  private static final Interpolator1D<Interpolator1DModel> INTERPOLATOR = new LogLinearInterpolator1D();
  private static final Function1D<Double, Double> FUNCTION = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 2 * x - 7;
    }
  };
  private static final double EPS = 1e-9;

  @Test
  public void test() {
    try {
      INTERPOLATOR.interpolate(Interpolator1DModelFactory.fromMap(new HashMap<Double, Double>()), null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final Map<Double, Double> data = new HashMap<Double, Double>();
    final Map<Double, Double> transformedData = new HashMap<Double, Double>();
    double x;
    for (int i = 0; i < 10; i++) {
      x = Double.valueOf(i);
      data.put(x, FUNCTION.evaluate(x));
      transformedData.put(x, Math.log(FUNCTION.evaluate(x)));
    }
    x = 3.4;
    assertEquals(Math.exp(INTERPOLATOR.interpolate(Interpolator1DModelFactory.fromMap(data), x).getResult()), LINEAR.interpolate(Interpolator1DModelFactory.fromMap(transformedData), x).getResult(),
        EPS);
  }
}
