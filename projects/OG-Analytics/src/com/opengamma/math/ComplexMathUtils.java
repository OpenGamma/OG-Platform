/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math;

import com.opengamma.math.number.ComplexNumber;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ComplexMathUtils {

  public static ComplexNumber add(final ComplexNumber z1, final ComplexNumber z2) {
    ArgumentChecker.notNull(z1, "z1");
    ArgumentChecker.notNull(z2, "z2");
    return new ComplexNumber(z1.getReal() + z2.getReal(), z1.getImaginary() + z2.getImaginary());
  }

  public static ComplexNumber add(final ComplexNumber... z) {
    ArgumentChecker.notNull(z, "z");
    int n = z.length;
    double res = 0.0;
    double img = 0.0;
    for (int i = 0; i < n; i++) {
      res += z[i].getReal();
      img += z[i].getImaginary();
    }
    return new ComplexNumber(res, img);
  }

  public static ComplexNumber add(final ComplexNumber z, final double x) {
    ArgumentChecker.notNull(z, "z");
    return new ComplexNumber(z.getReal() + x, z.getImaginary());
  }

  public static ComplexNumber add(final double x, final ComplexNumber z) {
    ArgumentChecker.notNull(z, "z");
    return new ComplexNumber(z.getReal() + x, z.getImaginary());
  }

  public static double arg(final ComplexNumber z) {
    ArgumentChecker.notNull(z, "z");
    return Math.atan2(z.getImaginary(), z.getReal());
  }

  public static ComplexNumber conjugate(final ComplexNumber z) {
    ArgumentChecker.notNull(z, "z");
    return new ComplexNumber(z.getReal(), -z.getImaginary());
  }

  public static ComplexNumber divide(final ComplexNumber z1, final ComplexNumber z2) {
    ArgumentChecker.notNull(z1, "z1");
    ArgumentChecker.notNull(z2, "z2");
    final double a = z1.getReal();
    final double b = z1.getImaginary();
    final double c = z2.getReal();
    final double d = z2.getImaginary();
    final double denom = c * c + d * d;
    return new ComplexNumber((a * c + b * d) / denom, (b * c - a * d) / denom);
  }

  public static ComplexNumber divide(final ComplexNumber z, final double x) {
    ArgumentChecker.notNull(z, "z");
    return new ComplexNumber(z.getReal() / x, z.getImaginary() / x);
  }

  public static ComplexNumber divide(final double x, final ComplexNumber z) {
    ArgumentChecker.notNull(z, "z");
    final double denom = z.getReal() * z.getReal() + z.getImaginary() * z.getImaginary();
    return new ComplexNumber(x * z.getReal() / denom, -x * z.getImaginary() / denom);
  }

  public static ComplexNumber exp(final ComplexNumber z) {
    ArgumentChecker.notNull(z, "z");
    final double mult = Math.exp(z.getReal());
    return new ComplexNumber(mult * Math.cos(z.getImaginary()), mult * Math.sin(z.getImaginary()));
  }

  public static ComplexNumber inverse(final ComplexNumber z) {
    ArgumentChecker.notNull(z, "z");
    final double denom = z.getReal() * z.getReal() + z.getImaginary() * z.getImaginary();
    return new ComplexNumber(z.getReal() / denom, -z.getImaginary() / denom);
  }

  /**
   * Returns the principal value of log, with z the principal argument of z defined to lie in the interval (-pi, pi]
   * @param z ComplexNumber
   * @return The log 
   */
  public static ComplexNumber log(final ComplexNumber z) {
    ArgumentChecker.notNull(z, "z");
    return new ComplexNumber(Math.log(Math.hypot(z.getReal(), z.getImaginary())), Math.atan2(z.getImaginary(), z.getReal()));
  }

  public static double mod(final ComplexNumber z) {
    ArgumentChecker.notNull(z, "z");
    return Math.hypot(z.getReal(), z.getImaginary());
  }

  public static ComplexNumber square(final ComplexNumber z) {
    ArgumentChecker.notNull(z, "z");
    final double a = z.getReal();
    final double b = z.getImaginary();
    return new ComplexNumber(a * a - b * b, 2 * a * b);
  }

  public static ComplexNumber multiply(final ComplexNumber z1, final ComplexNumber z2) {
    ArgumentChecker.notNull(z1, "z1");
    ArgumentChecker.notNull(z2, "z2");
    final double a = z1.getReal();
    final double b = z1.getImaginary();
    final double c = z2.getReal();
    final double d = z2.getImaginary();
    return new ComplexNumber(a * c - b * d, a * d + b * c);
  }

  public static ComplexNumber multiply(final ComplexNumber z, final double x) {
    ArgumentChecker.notNull(z, "z");
    return new ComplexNumber(z.getReal() * x, z.getImaginary() * x);
  }

  public static ComplexNumber multiply(final double x, final ComplexNumber z) {
    ArgumentChecker.notNull(z, "z");
    return new ComplexNumber(z.getReal() * x, z.getImaginary() * x);
  }

  public static ComplexNumber pow(final ComplexNumber z1, final ComplexNumber z2) {
    ArgumentChecker.notNull(z1, "z1");
    ArgumentChecker.notNull(z2, "z2");
    final double mod = mod(z1);
    final double arg = arg(z1);
    final double mult = Math.pow(mod, z2.getReal()) * Math.exp(-z2.getImaginary() * arg);
    final double theta = z2.getReal() * arg + z2.getImaginary() * Math.log(mod);
    return new ComplexNumber(mult * Math.cos(theta), mult * Math.sin(theta));
  }

  public static ComplexNumber pow(final ComplexNumber z, final double x) {
    final double mod = mod(z);
    final double arg = arg(z);
    final double mult = Math.pow(mod, x);
    return new ComplexNumber(mult * Math.cos(x * arg), mult * Math.sin(x * arg));
  }

  public static ComplexNumber pow(final double x, final ComplexNumber z) {
    ArgumentChecker.notNull(z, "z");
    return pow(new ComplexNumber(x, 0), z);
  }

  public static ComplexNumber sqrt(final ComplexNumber z) {
    ArgumentChecker.notNull(z, "z");
    return pow(z, 0.5);
  }

  public static ComplexNumber subtract(final ComplexNumber z1, final ComplexNumber z2) {
    ArgumentChecker.notNull(z1, "z1");
    ArgumentChecker.notNull(z2, "z2");
    return new ComplexNumber(z1.getReal() - z2.getReal(), z1.getImaginary() - z2.getImaginary());
  }

  public static ComplexNumber subtract(final ComplexNumber z, final double x) {
    ArgumentChecker.notNull(z, "z");
    return new ComplexNumber(z.getReal() - x, z.getImaginary());
  }

  public static ComplexNumber subtract(final double x, final ComplexNumber z) {
    ArgumentChecker.notNull(z, "z");
    return new ComplexNumber(x - z.getReal(), -z.getImaginary());
  }
}
