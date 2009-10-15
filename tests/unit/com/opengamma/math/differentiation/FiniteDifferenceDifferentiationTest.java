package com.opengamma.math.differentiation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.opengamma.math.differentiation.FiniteDifferenceDifferentiation.DifferenceType;
import com.opengamma.math.function.Function2D;

public class FiniteDifferenceDifferentiationTest {
  private static final Function2D<Double, Double> F = new Function2D<Double, Double>() {

    @Override
    public Double evaluate(final Double x, final Double y) {
      return 3 * x * x + 4 * x * y - Math.pow(y, -3);
    }

  };
  private static final Function2D<Double, Double> DXF = new Function2D<Double, Double>() {

    @Override
    public Double evaluate(final Double x, final Double y) {
      return 6 * x + 4 * y;
    }

  };
  private static final Function2D<Double, Double> DYF = new Function2D<Double, Double>() {

    @Override
    public Double evaluate(final Double x, final Double y) {
      return 4 * x + 3 * Math.pow(y, -4);
    }

  };
  private static final Function2D<Double, Double> DXDXF = new Function2D<Double, Double>() {

    @Override
    public Double evaluate(final Double x, final Double y) {
      return 6.;
    }

  };
  private static final Function2D<Double, Double> DYDYF = new Function2D<Double, Double>() {

    @Override
    public Double evaluate(final Double x, final Double y) {
      return -12 * Math.pow(y, -5);
    }

  };
  private static final Function2D<Double, Double> DXDYF = new Function2D<Double, Double>() {

    @Override
    public Double evaluate(final Double x, final Double y) {
      return 4.;
    }

  };
  private static final Double[] VARS = new Double[] { 4.5, -2.3 };
  private static final double EPS = 1e-4;
  private static final double ERROR = 1e-2;

  @Test
  public void testInputs() {
    testFirstOrderArguments(null, null, 0, EPS, null);
    testFirstOrderArguments(F, null, 0, EPS, null);
    testFirstOrderArguments(F, new Double[0], 0, EPS, null);
    testFirstOrderArguments(F, VARS, 0, EPS, null);
    testFirstOrderArguments(F, VARS, -1, EPS, null);
    testFirstOrderArguments(F, VARS, 0, EPS, null);
    testSecondOrderArguments(null, null, 0, EPS);
    testSecondOrderArguments(F, null, 0, EPS);
    testSecondOrderArguments(F, new Double[0], 0, EPS);
    testSecondOrderArguments(F, VARS, -1, EPS);
  }

  private void testFirstOrderArguments(final Function2D<Double, Double> f, final Double[] vars, final int index, final double eps, final DifferenceType type) {
    try {
      FiniteDifferenceDifferentiation.getFirstOrder(f, vars, index, eps, type);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  private void testSecondOrderArguments(final Function2D<Double, Double> f, final Double[] vars, final int index, final double eps) {
    try {
      FiniteDifferenceDifferentiation.getSecondOrder(f, vars, index, eps);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void test() {
    assertEquals(DXF.evaluate(VARS), FiniteDifferenceDifferentiation.getFirstOrder(F, VARS, 0, EPS, DifferenceType.BACKWARD), ERROR);
    assertEquals(DXF.evaluate(VARS), FiniteDifferenceDifferentiation.getFirstOrder(F, VARS, 0, EPS, DifferenceType.FORWARD), ERROR);
    assertEquals(DXF.evaluate(VARS), FiniteDifferenceDifferentiation.getFirstOrder(F, VARS, 0, EPS, DifferenceType.CENTRAL), ERROR);
    assertEquals(DYF.evaluate(VARS), FiniteDifferenceDifferentiation.getFirstOrder(F, VARS, 1, EPS, DifferenceType.BACKWARD), ERROR);
    assertEquals(DYF.evaluate(VARS), FiniteDifferenceDifferentiation.getFirstOrder(F, VARS, 1, EPS, DifferenceType.FORWARD), ERROR);
    assertEquals(DYF.evaluate(VARS), FiniteDifferenceDifferentiation.getFirstOrder(F, VARS, 1, EPS, DifferenceType.CENTRAL), ERROR);
    assertEquals(DXDXF.evaluate(VARS), FiniteDifferenceDifferentiation.getSecondOrder(F, VARS, 0, EPS), ERROR);
    assertEquals(DYDYF.evaluate(VARS), FiniteDifferenceDifferentiation.getSecondOrder(F, VARS, 1, EPS), ERROR);
    assertEquals(DXDYF.evaluate(VARS), FiniteDifferenceDifferentiation.getMixedSecondOrder(F, VARS, 0, 1, EPS, EPS), ERROR);
    assertEquals(DXDYF.evaluate(VARS), FiniteDifferenceDifferentiation.getMixedSecondOrder(F, VARS, 1, 0, EPS, EPS), ERROR);
  }
}
