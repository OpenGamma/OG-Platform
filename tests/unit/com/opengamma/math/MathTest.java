package com.opengamma.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.number.ComplexNumber;

/**
 * 
 * @author emcleod
 * 
 */

public class MathTest {
  private static final double X = -4.5;
  private static final double Y = 0.62;
  private static final ComplexNumber COMPLEX_X = new ComplexNumber(X, 0);
  private static final ComplexNumber A = new ComplexNumber(0.46, -0.3);
  private static final ComplexNumber B = new ComplexNumber(-3.5, -1.4);
  private static final double EPS = 1e-12;

  @Test
  public void testWrapping() {
    assertEquals(Math.abs(X), java.lang.Math.abs(X), EPS);
    assertEquals(Math.abs((float) X), java.lang.Math.abs((float) X), EPS);
    assertEquals(Math.abs((long) X), java.lang.Math.abs((long) X), EPS);
    assertEquals(Math.abs((int) X), java.lang.Math.abs((int) X), EPS);
    assertEquals(Math.acos(Y), java.lang.Math.acos(Y), EPS);
    assertEquals(Math.asin(Y), java.lang.Math.asin(Y), EPS);
    assertEquals(Math.atan(Y), java.lang.Math.atan(Y), EPS);
    assertEquals(Math.atan2(Y, X), java.lang.Math.atan2(Y, X), EPS);
    assertEquals(Math.cbrt(Y), java.lang.Math.cbrt(Y), EPS);
  }

  @Test
  public void testArithmetic() {
    assertEquals(Math.acosh(Math.cosh(Y)), Y, EPS);
    assertEquals(Math.acosh(Math.cosh(X)), Math.abs(X), EPS);
    assertEquals(Math.subtract(Math.add(X, Y), Y), X, EPS);
    assertEquals(Math.atanh(Math.tanh(Y)), Y, EPS);
  }

  @Test
  public void testComplexArithmetic() {
    // assertComplexEquals(Math.cos(Math.acos(A)), A, EPS);
    assertComplexEquals(Math.cosh(Math.acosh(A)), A, EPS);
    assertComplexEquals(Math.subtract(Math.add(X, A), A), COMPLEX_X, EPS);
    assertComplexEquals(Math.subtract(Math.add(A, X), X), A, EPS);
    assertComplexEquals(Math.subtract(Math.add(A, B), B), A, EPS);
    // assertComplexEquals(Math.sin(Math.asin(A)), A, EPS);
    assertComplexEquals(Math.sinh(Math.asinh(A)), A, EPS);
    // assertComplexEquals(Math.tan(Math.atan(A)), A, EPS);
    // assertComplexEquals(Math.tanh(Math.atanh(A)), A, EPS);
  }

  private void assertComplexEquals(ComplexNumber actual, ComplexNumber expected, double eps) {
    assertEquals(actual.getReal(), expected.getReal(), eps);
    assertEquals(actual.getImaginary(), expected.getImaginary(), eps);
  }
}
