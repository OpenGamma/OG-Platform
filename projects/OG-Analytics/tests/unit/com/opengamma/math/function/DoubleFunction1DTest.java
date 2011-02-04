/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.function;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class DoubleFunction1DTest {
  private static final DoubleFunction1D F1 = new DoubleFunction1D() {

    @Override
    public Double evaluate(final Double x) {
      return x * x * x + 2 * x * x - 7 * x + 12;
    }

  };
  private static final DoubleFunction1D DF1 = new DoubleFunction1D() {

    @Override
    public Double evaluate(final Double x) {
      return 3 * x * x + 4 * x - 7;
    }

  };
  private static final DoubleFunction1D F2 = new DoubleFunction1D() {

    @Override
    public Double evaluate(final Double x) {
      return Math.sin(x);
    }

  };
  private static final DoubleFunction1D DF2 = new DoubleFunction1D() {

    @Override
    public Double evaluate(final Double x) {
      return Math.cos(x);
    }

  };
  private static final double X = 0.1234;
  private static final double A = 5.67;
  private static final double EPS = 1e-15;

  @Test(expected = IllegalArgumentException.class)
  public void testAddNull() {
    F1.add(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDivideNull() {
    F1.divide(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMultiplyNull() {
    F1.multiply(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSubtractNull() {
    F1.subtract(null);
  }

  @Test
  public void testAdd() {
    assertEquals(F1.add(F2).evaluate(X), F1.evaluate(X) + F2.evaluate(X), EPS);
    assertEquals(F1.add(A).evaluate(X), F1.evaluate(X) + A, EPS);
  }

  @Test
  public void testDivide() {
    assertEquals(F1.divide(F2).evaluate(X), F1.evaluate(X) / F2.evaluate(X), EPS);
    assertEquals(F1.divide(A).evaluate(X), F1.evaluate(X) / A, EPS);
  }

  @Test
  public void testMultiply() {
    assertEquals(F1.multiply(F2).evaluate(X), F1.evaluate(X) * F2.evaluate(X), EPS);
    assertEquals(F1.multiply(A).evaluate(X), F1.evaluate(X) * A, EPS);
  }

  @Test
  public void testSubtract() {
    assertEquals(F1.subtract(F2).evaluate(X), F1.evaluate(X) - F2.evaluate(X), EPS);
    assertEquals(F1.subtract(A).evaluate(X), F1.evaluate(X) - A, EPS);
  }

  @Test
  public void testDerivative() {
    assertEquals(F1.derivative().evaluate(X), DF1.evaluate(X), 1e-3);
    assertEquals(F2.derivative().evaluate(X), DF2.evaluate(X), 1e-3);
  }
}
