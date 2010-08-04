/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.math.interpolation.Interpolator1DDoubleQuadraticDataBundle;

/**
 * 
 */
public class DoubleQuadraticInterpolator1DNodeSensitivityCalculatorTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final DoubleQuadraticInterpolator1D INTERPOLATOR = new DoubleQuadraticInterpolator1D();
  private static final Interpolator1DNodeSensitivityCalculator<Interpolator1DDoubleQuadraticDataBundle> CALCULATOR = new DoubleQuadraticInterpolator1DNodeSensitivityCalculator();
  private static final Interpolator1DDoubleQuadraticDataBundle DATA;
  private static final double EPS = 1e-7;
  private static final Function1D<Double, Double> FUNCTION = new Function1D<Double, Double>() {

    private static final double a = -0.045;
    private static final double b = 0.03;
    private static final double c = 0.3;
    private static final double d = 0.05;

    @Override
    public Double evaluate(final Double x) {
      return (a + b * x) * Math.exp(-c * x) + d;
    }

  };

  static {
    final double[] t = new double[] {0.0, 0.5, 1.0, 2.0, 3.0, 5.0, 7.0, 10.0, 15.0, 17.5, 20.0, 25.0, 30.0};
    final int n = t.length;
    final double[] r = new double[n];
    for (int i = 0; i < n; i++) {
      r[i] = FUNCTION.evaluate(t[i]);
    }
    DATA = INTERPOLATOR.getDataBundleFromSortedArrays(t, r);

  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullInterpolator() {
    CALCULATOR.calculate(null, DATA, 3.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullInputData() {
    CALCULATOR.calculate(INTERPOLATOR, null, 3.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullInterpolateValue() {
    CALCULATOR.calculate(INTERPOLATOR, DATA, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighValue() {
    CALCULATOR.calculate(INTERPOLATOR, DATA, 31.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLowValue() {
    CALCULATOR.calculate(INTERPOLATOR, DATA, -1.);
  }

  @Test
  public void testSensitivities() {
    final double tmax = DATA.lastKey();
    for (int i = 0; i < 100; i++) {
      final double t = tmax * RANDOM.nextDouble();
      final double[] sensitivity = CALCULATOR.calculate(INTERPOLATOR, DATA, t);
      for (int j = 0; j < sensitivity.length; j++) {
        assertEquals(fd_sensitivity[j], sensitivity[j], EPS);
      }
    }
  }
}
