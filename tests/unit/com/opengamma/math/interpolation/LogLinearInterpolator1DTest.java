/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public class LogLinearInterpolator1DTest {
  private static final Interpolator1D LINEAR = new LinearInterpolator1D();
  private static final Interpolator1D INTERPOLATOR = new LogLinearInterpolator1D();
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
    Map<Double, Double> transformedData = new HashMap<Double, Double>();
    double x;
    for (int i = 0; i < 10; i++) {
      x = Double.valueOf(i);
      data.put(x, FUNCTION.evaluate(x));
      transformedData.put(x, Math.log(FUNCTION.evaluate(x)));
    }
    System.out.println(INTERPOLATOR.interpolate(data, 4.5).getResult());
    System.out.println(Math.exp(LINEAR.interpolate(transformedData, 4.5).getResult()));
  }
}
