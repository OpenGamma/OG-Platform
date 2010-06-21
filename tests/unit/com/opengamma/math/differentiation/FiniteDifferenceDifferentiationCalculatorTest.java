package com.opengamma.math.differentiation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.differentiation.FiniteDifferenceDifferentiationCalculator.DifferenceType;
import com.opengamma.math.function.Function1D;

public class FiniteDifferenceDifferentiationCalculatorTest {
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
  private static final FiniteDifferenceDifferentiationCalculator FORWARD = new FiniteDifferenceDifferentiationCalculator(DifferenceType.FORWARD, EPS * EPS);
  private static final FiniteDifferenceDifferentiationCalculator CENTRAL = new FiniteDifferenceDifferentiationCalculator(DifferenceType.CENTRAL, EPS * EPS);
  private static final FiniteDifferenceDifferentiationCalculator BACKWARD = new FiniteDifferenceDifferentiationCalculator(DifferenceType.BACKWARD, EPS * EPS);

  @Test(expected = IllegalArgumentException.class)
  public void testNullDifferenceType() {
    new FiniteDifferenceDifferentiationCalculator(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFunction() {
    CENTRAL.evaluate((Function1D<Double, Double>) null);
  }

  @Test
  public void test() {
    final double x = 0.2245;
    assertEquals(FORWARD.evaluate(F).evaluate(x), DX_ANALYTIC.evaluate(x), 10 * EPS);
    assertEquals(CENTRAL.evaluate(F).evaluate(x), DX_ANALYTIC.evaluate(x), 10 * EPS);
    assertEquals(BACKWARD.evaluate(F).evaluate(x), DX_ANALYTIC.evaluate(x), 10 * EPS);
  }
}
