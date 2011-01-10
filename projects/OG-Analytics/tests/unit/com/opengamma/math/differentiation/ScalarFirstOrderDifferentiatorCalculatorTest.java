/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.differentiation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function1D;

public class ScalarFirstOrderDifferentiatorCalculatorTest {
  private static final Function1D<Double, Double> F = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 3 * x * x + 4 * x - Math.sin(x);
    }

  };
  private static final Function1D<Double, Double> DX_ANALYTIC = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 6 * x + 4 - Math.cos(x);
    }

  };
  private static final double EPS = 1e-5;
  private static final ScalarFirstOrderDifferentiator FORWARD = new ScalarFirstOrderDifferentiator(FiniteDifferenceType.FORWARD, EPS);
  private static final ScalarFirstOrderDifferentiator CENTRAL = new ScalarFirstOrderDifferentiator(FiniteDifferenceType.CENTRAL, EPS);
  private static final ScalarFirstOrderDifferentiator BACKWARD = new ScalarFirstOrderDifferentiator(FiniteDifferenceType.BACKWARD, EPS);

  @Test(expected = IllegalArgumentException.class)
  public void testNullDifferenceType() {
    new ScalarFirstOrderDifferentiator(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFunction() {
    CENTRAL.derivative((Function1D<Double, Double>) null);
  }

  @Test
  public void test() {
    final double x = 0.2245;
    assertEquals(FORWARD.derivative(F).evaluate(x), DX_ANALYTIC.evaluate(x), 10 * EPS);
    assertEquals(CENTRAL.derivative(F).evaluate(x), DX_ANALYTIC.evaluate(x), EPS * EPS); // This is why you use central difference
    assertEquals(BACKWARD.derivative(F).evaluate(x), DX_ANALYTIC.evaluate(x), 10 * EPS);
  }
}
