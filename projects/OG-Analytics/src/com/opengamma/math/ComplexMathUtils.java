/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math;

import org.apache.commons.lang.Validate;

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
    if (Math.abs(c) > Math.abs(d)) {
      final double dOverC = d / c;
      final double denom = c + d * dOverC;
      return new ComplexNumber((a + b * dOverC) / denom, (b - a * dOverC) / denom);
    }
    final double cOverD = c / d;
    final double denom = c * cOverD + d;
    return new ComplexNumber((a * cOverD + b) / denom, (b * cOverD - a) / denom);
  }

  public static ComplexNumber divide(final ComplexNumber z, final double x) {
    ArgumentChecker.notNull(z, "z");
    return new ComplexNumber(z.getReal() / x, z.getImaginary() / x);
  }

  public static ComplexNumber divide(final double x, final ComplexNumber z) {
    ArgumentChecker.notNull(z, "z");
    final double c = z.getReal();
    final double d = z.getImaginary();
    if (Math.abs(c) > Math.abs(d)) {
      final double dOverC = d / c;
      final double denom = c + d * dOverC;
      return new ComplexNumber(x / denom, -x * dOverC / denom);
    }
    final double cOverD = c / d;
    final double denom = c * cOverD + d;
    return new ComplexNumber(x * cOverD / denom, -x / denom);
  }

  public static ComplexNumber exp(final ComplexNumber z) {
    ArgumentChecker.notNull(z, "z");
    final double mult = Math.exp(z.getReal());
    return new ComplexNumber(mult * Math.cos(z.getImaginary()), mult * Math.sin(z.getImaginary()));
  }

  public static ComplexNumber inverse(final ComplexNumber z) {
    ArgumentChecker.notNull(z, "z");
    final double c = z.getReal();
    final double d = z.getImaginary();
    if (Math.abs(c) > Math.abs(d)) {
      final double dOverC = d / c;
      final double denom = c + d * dOverC;
      return new ComplexNumber(1 / denom, -dOverC / denom);
    }
    final double cOverD = c / d;
    final double denom = c * cOverD + d;
    return new ComplexNumber(cOverD / denom, -1 / denom);
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

  public static ComplexNumber multiply(final ComplexNumber... z) {
    ArgumentChecker.notNull(z, "z");
    final int n = z.length;
    Validate.isTrue(n > 0, "nothing to multiply");
    if (n == 1) {
      return z[0];
    } else if (n == 2) {
      return multiply(z[0], z[1]);
    } else {
      ComplexNumber product = multiply(z[0], z[1]);
      for (int i = 2; i < n; i++) {
        product = multiply(product, z[i]);
      }
      return product;
    }
  }

  public static ComplexNumber multiply(final double x, final ComplexNumber... z) {
    ComplexNumber product = multiply(z);
    return multiply(x, product);
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
    final double c = z.getReal();
    final double d = z.getImaginary();
    if (c == 0.0 && d == 0.0) {
      return z;
    }
    double w;
    if (Math.abs(c) > Math.abs(d)) {
      final double dOverC = d / c;
      w = Math.sqrt(Math.abs(c)) * Math.sqrt((1 + Math.sqrt(1 + dOverC * dOverC)) / 2);
    } else {
      final double cOverD = c / d;
      w = Math.sqrt(Math.abs(d)) * Math.sqrt((Math.abs(cOverD) + Math.sqrt(1 + cOverD * cOverD)) / 2);
    }
    if (c >= 0.0) {
      return new ComplexNumber(w, d / 2 / w);
    }
    if (d >= 0.0) {
      return new ComplexNumber(d / 2 / w, w);
    }
    return new ComplexNumber(-d / 2 / w, -w);
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
