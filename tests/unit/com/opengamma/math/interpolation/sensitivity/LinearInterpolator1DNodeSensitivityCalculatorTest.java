/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.Interpolator1DDataBundleFactory;

/**
 * 
 */
public class LinearInterpolator1DNodeSensitivityCalculatorTest {
  private static final double EPS = 1e-15;
  private static final Interpolator1DNodeSensitivityCalculator<Interpolator1DDataBundle> CALCULATOR = new LinearInterpolator1DNodeSensitivityCalculator();
  private static final Function1D<Double, Double> FUNCTION = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 2 * x - 7;
    }

  };
  private static final Interpolator1DDataBundle ARRAY_DATA;
  private static final Interpolator1DDataBundle MAP_DATA;

  static {
    final int n = 10;
    final double[] x = new double[n];
    final double[] y = new double[n];
    final Map<Double, Double> m = new HashMap<Double, Double>();
    double tempX, tempY;
    for (int i = 0; i < n; i++) {
      tempX = Double.valueOf(i);
      tempY = FUNCTION.evaluate(tempX);
      x[i] = tempX;
      y[i] = tempY;
      m.put(tempX, tempY);
    }
    ARRAY_DATA = Interpolator1DDataBundleFactory.fromSortedArrays(x, y);
    MAP_DATA = Interpolator1DDataBundleFactory.fromMap(m);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    CALCULATOR.calculate(null, 1.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullValue() {
    CALCULATOR.calculate(ARRAY_DATA, null);
  }

  //TODO move next 4 methods into the data bundle test
  @Test(expected = IllegalArgumentException.class)
  public void testLowValue1() {
    CALCULATOR.calculate(ARRAY_DATA, -1.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLowValue2() {
    CALCULATOR.calculate(MAP_DATA, -1.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighValue1() {
    CALCULATOR.calculate(ARRAY_DATA, 100.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighValue2() {
    CALCULATOR.calculate(MAP_DATA, 100.);
  }

  @Test
  public void test() {
    double[] result = CALCULATOR.calculate(ARRAY_DATA, 3.4);
    for (int i = 0; i < 3; i++) {
      assertEquals(0, result[i], 0);
    }
    assertEquals(0.6, result[3], EPS);
    assertEquals(0.4, result[4], EPS);
    for (int i = 5; i < 10; i++) {
      assertEquals(result[i], 0, 0);
    }
    result = CALCULATOR.calculate(ARRAY_DATA, 7.);
    for (int i = 0; i < 7; i++) {
      assertEquals(0, result[i], 0);
    }
    assertEquals(1, result[7], EPS);
    for (int i = 8; i < 10; i++) {
      assertEquals(0, result[i], 0);
    }
  }
}
