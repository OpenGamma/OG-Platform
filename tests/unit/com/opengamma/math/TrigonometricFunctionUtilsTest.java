/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class TrigonometricFunctionUtilsTest {
  private static final Double X = 0.12;
  private static final ComplexNumber Y = new ComplexNumber(X, 0);
  private static final ComplexNumber Z = new ComplexNumber(X, -0.34);
  private static final double EPS = 1e-9;

  @Test
  public void testNull() {
    try {
      TrigonometricFunctionUtils.acos(null);
    } catch (final NullPointerException e) {
      assertStackTraceElement(e.getStackTrace());
    }
    try {
      TrigonometricFunctionUtils.acosh(null);
    } catch (final NullPointerException e) {
      assertStackTraceElement(e.getStackTrace());
    }
    try {
      TrigonometricFunctionUtils.asin(null);
    } catch (final NullPointerException e) {
      assertStackTraceElement(e.getStackTrace());
    }
    try {
      TrigonometricFunctionUtils.asinh(null);
    } catch (final NullPointerException e) {
      assertStackTraceElement(e.getStackTrace());
    }
    try {
      TrigonometricFunctionUtils.atan(null);
    } catch (final NullPointerException e) {
      assertStackTraceElement(e.getStackTrace());
    }
    try {
      TrigonometricFunctionUtils.atanh(null);
    } catch (final NullPointerException e) {
      assertStackTraceElement(e.getStackTrace());
    }
    try {
      TrigonometricFunctionUtils.cos(null);
    } catch (final NullPointerException e) {
      assertStackTraceElement(e.getStackTrace());
    }
    try {
      TrigonometricFunctionUtils.cosh(null);
    } catch (final NullPointerException e) {
      assertStackTraceElement(e.getStackTrace());
    }
    try {
      TrigonometricFunctionUtils.sin(null);
    } catch (final NullPointerException e) {
      assertStackTraceElement(e.getStackTrace());
    }
    try {
      TrigonometricFunctionUtils.sinh(null);
    } catch (final NullPointerException e) {
      assertStackTraceElement(e.getStackTrace());
    }
    try {
      TrigonometricFunctionUtils.tan(null);
    } catch (final NullPointerException e) {
      assertStackTraceElement(e.getStackTrace());
    }
    try {
      TrigonometricFunctionUtils.tanh(null);
    } catch (final NullPointerException e) {
      assertStackTraceElement(e.getStackTrace());
    }
  }

  @Test
  public void test() {
    assertEquals(TrigonometricFunctionUtils.acos(TrigonometricFunctionUtils.cos(X)).doubleValue(), X, EPS);
    assertEquals(TrigonometricFunctionUtils.asin(TrigonometricFunctionUtils.sin(X)).doubleValue(), X, EPS);
    assertEquals(TrigonometricFunctionUtils.atan(TrigonometricFunctionUtils.tan(X)).doubleValue(), X, EPS);
    assertComplexEquals((ComplexNumber) TrigonometricFunctionUtils.cos(Y), Math.cos(X));
    assertComplexEquals((ComplexNumber) TrigonometricFunctionUtils.sin(Y), Math.sin(X));
    assertComplexEquals((ComplexNumber) TrigonometricFunctionUtils.tan(Y), Math.tan(X));
    assertComplexEquals((ComplexNumber) TrigonometricFunctionUtils.acos(Y), Math.acos(X));
    assertComplexEquals((ComplexNumber) TrigonometricFunctionUtils.asin(Y), Math.asin(X));
    assertComplexEquals((ComplexNumber) TrigonometricFunctionUtils.atan(Y), Math.atan(X));
    assertComplexEquals((ComplexNumber) TrigonometricFunctionUtils.acos(TrigonometricFunctionUtils.cos(Z)), Z);
    assertComplexEquals((ComplexNumber) TrigonometricFunctionUtils.asin(TrigonometricFunctionUtils.sin(Z)), Z);
    assertComplexEquals((ComplexNumber) TrigonometricFunctionUtils.atan(TrigonometricFunctionUtils.tan(Z)), Z);
    assertEquals(TrigonometricFunctionUtils.acosh(TrigonometricFunctionUtils.cosh(X)).doubleValue(), X, EPS);
    assertEquals(TrigonometricFunctionUtils.asinh(TrigonometricFunctionUtils.sinh(X)).doubleValue(), X, EPS);
    assertEquals(TrigonometricFunctionUtils.atanh(TrigonometricFunctionUtils.tanh(X)).doubleValue(), X, EPS);
    assertComplexEquals((ComplexNumber) TrigonometricFunctionUtils.acosh(TrigonometricFunctionUtils.cosh(Z)), Z);
    assertComplexEquals((ComplexNumber) TrigonometricFunctionUtils.asinh(TrigonometricFunctionUtils.sinh(Z)), Z);
    assertComplexEquals((ComplexNumber) TrigonometricFunctionUtils.atanh(TrigonometricFunctionUtils.tanh(Z)), Z);
  }

  private void assertComplexEquals(final ComplexNumber z1, final ComplexNumber z2) {
    assertEquals(z1.getReal(), z2.getReal(), EPS);
    assertEquals(z1.getImaginary(), z2.getImaginary(), EPS);
  }

  private void assertComplexEquals(final ComplexNumber z, final double x) {
    assertEquals(z.getImaginary(), 0, EPS);
    assertEquals(z.getReal(), x, EPS);
  }

  private void assertStackTraceElement(final StackTraceElement[] ste) {
    assertEquals(ste[0].getClassName(), "com.opengamma.util.ArgumentChecker");
  }
}
