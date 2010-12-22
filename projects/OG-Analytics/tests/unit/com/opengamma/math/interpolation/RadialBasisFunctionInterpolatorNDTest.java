/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class RadialBasisFunctionInterpolatorNDTest extends InterpolatorNDTestCase {
  private static final Function1D<Double, Double> UNIFORM_WEIGHT_FUNCTION = new MultiquadraticRadialBasisFunction();
  private static final InterpolatorND INTERPOLATOR = new RadialBasisFunctionInterpolatorND(UNIFORM_WEIGHT_FUNCTION, false);

  @Test(expected = IllegalArgumentException.class)
  public void testNullFunction() {
    new RadialBasisFunctionInterpolatorND(null, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    INTERPOLATOR.interpolate(null, Arrays.asList(4.));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullValue() {
    INTERPOLATOR.interpolate(FLAT_DATA, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongDimension() {
    INTERPOLATOR.interpolate(FLAT_DATA, Arrays.asList(2., 3., 4., 5.));
  }

  @Test
  public void testInputs() {
    super.testData(INTERPOLATOR);
  }

  @Test
  public void testSurface() {

    double x, y;

    double r0 = 1 * 10.0 / Math.sqrt(20.0);
    final InterpolatorND interpolator = new KrigingInterpolatorND(1.5);// (new GaussianRadialBasisFunction(r0), true);
    // final InterpolatorND interpolator = new ShepardInterpolatorND(4.0);
    final Map<List<Double>, Double> data = new HashMap<List<Double>, Double>();

    for (int i = 0; i < 20; i++) {
      x = 10 * RANDOM.nextDouble();
      y = 10 * RANDOM.nextDouble();

      data.put(Arrays.asList(x, y), Math.sin(Math.PI * x / 10.0) * Math.exp(-y / 5.));
    }

    for (int i = 0; i < 100; i++) {
      x = i / 10.0;
      for (int j = 0; j < 100; j++) {
        y = j / 10.0;
        double fit = interpolator.interpolate(data, Arrays.asList(x, y));
        double real = Math.sin(Math.PI * x / 10.0) * Math.exp(-y / 5.);
        double diff = real - fit;
        System.out.print(diff + "\t");
      }
      System.out.print("\n");
    }
  }

}
