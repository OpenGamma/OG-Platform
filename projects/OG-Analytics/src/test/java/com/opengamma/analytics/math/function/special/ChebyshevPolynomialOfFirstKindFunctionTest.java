/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function.special;

import static org.testng.AssertJUnit.assertEquals;

import org.apache.commons.lang.NotImplementedException;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.DoubleFunction1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ChebyshevPolynomialOfFirstKindFunctionTest {
  private static final DoubleFunction1D T0 = new DoubleFunction1D() {

    @Override
    public Double evaluate(final Double x) {
      return 1.;
    }

  };
  private static final DoubleFunction1D T1 = new DoubleFunction1D() {

    @Override
    public Double evaluate(final Double x) {
      return x;
    }

  };
  private static final DoubleFunction1D T2 = new DoubleFunction1D() {

    @Override
    public Double evaluate(final Double x) {
      return 2 * x * x - 1;
    }

  };
  private static final DoubleFunction1D T3 = new DoubleFunction1D() {

    @Override
    public Double evaluate(final Double x) {
      return x * (4 * x * x - 3);
    }

  };
  private static final DoubleFunction1D T4 = new DoubleFunction1D() {

    @Override
    public Double evaluate(final Double x) {
      return 8 * x * x * x * x - 8 * x * x + 1;
    }

  };
  private static final DoubleFunction1D T5 = new DoubleFunction1D() {

    @Override
    public Double evaluate(final Double x) {
      return x * (16 * x * x * x * x - 20 * x * x + 5);
    }

  };
  private static final DoubleFunction1D T6 = new DoubleFunction1D() {

    @Override
    public Double evaluate(final Double x) {
      return 32 * x * x * x * x * x * x - 48 * x * x * x * x + 18 * x * x - 1;
    }

  };
  private static final DoubleFunction1D T7 = new DoubleFunction1D() {

    @Override
    public Double evaluate(final Double x) {
      return x * (64 * x * x * x * x * x * x - 112 * x * x * x * x + 56 * x * x - 7);
    }

  };
  private static final DoubleFunction1D T8 = new DoubleFunction1D() {

    @Override
    public Double evaluate(final Double x) {
      final double xSq = x * x;
      return 128 * xSq * xSq * xSq * xSq - 256 * xSq * xSq * xSq + 160 * xSq * xSq - 32 * xSq + 1;
    }

  };
  private static final DoubleFunction1D T9 = new DoubleFunction1D() {

    @Override
    public Double evaluate(final Double x) {
      final double xSq = x * x;
      return x * (256 * xSq * xSq * xSq * xSq - 576 * xSq * xSq * xSq + 432 * xSq * xSq - 120 * xSq + 9);
    }

  };
  private static final DoubleFunction1D[] T = new DoubleFunction1D[] {T0, T1, T2, T3, T4, T5, T6, T7, T8, T9};
  private static final ChebyshevPolynomialOfFirstKindFunction CHEBYSHEV = new ChebyshevPolynomialOfFirstKindFunction();
  private static final double EPS = 1e-12;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadN() {
    CHEBYSHEV.getPolynomials(-3);
  }

  @Test(expectedExceptions = NotImplementedException.class)
  public void testGetPolynomials() {
    CHEBYSHEV.getPolynomialsAndFirstDerivative(3);
  }

  @Test
  public void test() {
    DoubleFunction1D[] t = CHEBYSHEV.getPolynomials(0);
    assertEquals(t.length, 1);
    final double x = 1.23;
    assertEquals(t[0].evaluate(x), 1, EPS);
    t = CHEBYSHEV.getPolynomials(1);
    assertEquals(t.length, 2);
    assertEquals(t[1].evaluate(x), x, EPS);
    for (int i = 0; i < 10; i++) {
      t = CHEBYSHEV.getPolynomials(i);
      for (int j = 0; j <= i; j++) {
        assertEquals(T[j].evaluate(x), t[j].evaluate(x), EPS);
      }
    }
  }

}
