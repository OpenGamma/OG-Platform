/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CurveSpreadFunctionTest {
  private static final CurveSpreadFunction ADD = CurveSpreadFunctionFactory.of("+");
  private static final CurveSpreadFunction DIVIDE = CurveSpreadFunctionFactory.of("/");
  private static final CurveSpreadFunction MULTIPLY = CurveSpreadFunctionFactory.of("*");
  private static final CurveSpreadFunction SUBTRACT = CurveSpreadFunctionFactory.of("-");
  private static final double[] X = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9};
  private static final double[] Y1 = new double[] {2, 4, 6, 8, 10, 12, 14, 16, 18};
  private static final double[] Y2 = new double[] {1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1};
  private static final InterpolatedDoublesCurve INTERPOLATED1 = InterpolatedDoublesCurve.fromSorted(X, Y1, new LinearInterpolator1D());
  private static final InterpolatedDoublesCurve INTERPOLATED2 = InterpolatedDoublesCurve.fromSorted(X, Y2, new LinearInterpolator1D());
  private static final ConstantDoublesCurve CONSTANT1 = ConstantDoublesCurve.from(2);
  private static final ConstantDoublesCurve CONSTANT2 = ConstantDoublesCurve.from(1.1);
  private static final Function1D<Double, Double> F1 = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x * x;
    }

  };
  private static final Function1D<Double, Double> F2 = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 3 * x;
    }

  };
  private static final FunctionalDoublesCurve FUNCTIONAL1 = FunctionalDoublesCurve.from(F1);
  private static final FunctionalDoublesCurve FUNCTIONAL2 = FunctionalDoublesCurve.from(F2);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurves1() {
    ADD.evaluate((Curve<Double, Double>[]) null);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyCurves1() {
    ADD.evaluate(new Curve[0]);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurves2() {
    ADD.evaluate((Curve<Double, Double>[]) null);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyCurves2() {
    ADD.evaluate(new Curve[0]);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurves3() {
    ADD.evaluate((Curve<Double, Double>[]) null);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyCurves3() {
    ADD.evaluate(new Curve[0]);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurves4() {
    ADD.evaluate((Curve<Double, Double>[]) null);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyCurves4() {
    ADD.evaluate(new Curve[0]);
  }

  @Test
  public void testOperationName() {
    assertEquals(ADD.getOperationName(), "+");
    assertEquals(DIVIDE.getOperationName(), "/");
    assertEquals(MULTIPLY.getOperationName(), "*");
    assertEquals(SUBTRACT.getOperationName(), "-");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testConstant() {
    final Curve<Double, Double>[] curves = new Curve[] {CONSTANT1, CONSTANT2};
    final double y1 = 2;
    final double y2 = 1.1;
    Function<Double, Double> f = ADD.evaluate(curves);
    assertEquals(f.evaluate(3.), y1 + y2, 0);
    f = DIVIDE.evaluate(curves);
    assertEquals(f.evaluate(3.), y1 / y2, 0);
    f = MULTIPLY.evaluate(curves);
    assertEquals(f.evaluate(3.), y1 * y2, 0);
    f = SUBTRACT.evaluate(curves);
    assertEquals(f.evaluate(3.), y1 - y2, 0);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFunctional() {
    final double x = 3.5;
    final Curve<Double, Double>[] curves = new Curve[] {FUNCTIONAL1, FUNCTIONAL2};
    Function<Double, Double> f = ADD.evaluate(curves);
    assertEquals(f.evaluate(x), F1.evaluate(x) + F2.evaluate(x), 0);
    f = DIVIDE.evaluate(curves);
    assertEquals(f.evaluate(x), F1.evaluate(x) / F2.evaluate(x), 0);
    f = MULTIPLY.evaluate(curves);
    assertEquals(f.evaluate(x), F1.evaluate(x) * F2.evaluate(x), 0);
    f = SUBTRACT.evaluate(curves);
    assertEquals(f.evaluate(x), F1.evaluate(x) - F2.evaluate(x), 0);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testInterpolated() {
    final double x = 3.5;
    final Curve<Double, Double>[] curves = new Curve[] {INTERPOLATED1, INTERPOLATED2};
    Function<Double, Double> f = ADD.evaluate(curves);
    assertEquals(f.evaluate(x), INTERPOLATED1.getYValue(x) + INTERPOLATED2.getYValue(x), 0);
    f = DIVIDE.evaluate(curves);
    assertEquals(f.evaluate(x), INTERPOLATED1.getYValue(x) / INTERPOLATED2.getYValue(x), 0);
    f = MULTIPLY.evaluate(curves);
    assertEquals(f.evaluate(x), INTERPOLATED1.getYValue(x) * INTERPOLATED2.getYValue(x), 0);
    f = SUBTRACT.evaluate(curves);
    assertEquals(f.evaluate(x), INTERPOLATED1.getYValue(x) - INTERPOLATED2.getYValue(x), 0);
  }
}
