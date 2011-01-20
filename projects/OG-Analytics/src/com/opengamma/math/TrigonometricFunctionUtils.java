/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math;

import org.apache.commons.lang.Validate;

import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */

public class TrigonometricFunctionUtils {
  private static final ComplexNumber I = new ComplexNumber(0, 1);
  private static final ComplexNumber NEGATIVE_I = new ComplexNumber(0, -1);

  public static Number acos(final Number a) {
    Validate.notNull(a, "a");
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      return ComplexMathUtils.multiply(NEGATIVE_I, ComplexMathUtils.log(ComplexMathUtils.add(z, ComplexMathUtils.sqrt(ComplexMathUtils.subtract(ComplexMathUtils.multiply(z, z), 1)))));
    }
    return Math.acos(a.doubleValue());
  }

  public static Number acosh(final Number a) {
    Validate.notNull(a, "a");
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      return ComplexMathUtils.log(ComplexMathUtils.add(z, ComplexMathUtils.sqrt(ComplexMathUtils.subtract(ComplexMathUtils.multiply(z, z), 1))));
    }
    final double x = a.doubleValue();
    final double y = x * x - 1;
    if (y < 0) {
      return acosh(new ComplexNumber(x, 0));
    }
    return Math.log(x + Math.sqrt(x * x - 1));
  }

  public static Number asin(final Number a) {
    Validate.notNull(a, "a");
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      return ComplexMathUtils.multiply(NEGATIVE_I, ComplexMathUtils.log(ComplexMathUtils.add(ComplexMathUtils.multiply(I, z), ComplexMathUtils.sqrt(ComplexMathUtils.subtract(1, ComplexMathUtils
          .multiply(z, z))))));
    }
    return Math.asin(a.doubleValue());
  }

  public static Number asinh(final Number a) {
    Validate.notNull(a, "a");
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      return ComplexMathUtils.log(ComplexMathUtils.add(z, ComplexMathUtils.sqrt(ComplexMathUtils.add(ComplexMathUtils.multiply(z, z), 1))));
    }
    final double x = a.doubleValue();
    return Math.log(x + Math.sqrt(x * x + 1));
  }

  public static Number atan(final Number a) {
    Validate.notNull(a, "a");
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      final ComplexNumber iZ = ComplexMathUtils.multiply(z, I);
      final ComplexNumber half = new ComplexNumber(0, 0.5);
      return ComplexMathUtils.multiply(half, ComplexMathUtils.log(ComplexMathUtils.divide(ComplexMathUtils.subtract(1, iZ), ComplexMathUtils.add(1, iZ))));
    }
    return Math.atan(a.doubleValue());
  }

  public static Number atanh(final Number a) {
    Validate.notNull(a, "a");
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      return ComplexMathUtils.log(ComplexMathUtils.divide(ComplexMathUtils.sqrt(ComplexMathUtils.subtract(1, ComplexMathUtils.multiply(z, z))), ComplexMathUtils.subtract(1, z)));
    }
    final double x = a.doubleValue();
    return 0.5 * Math.log((1 + x) / (1 - x));
  }

  public static Number cos(final Number a) {
    Validate.notNull(a, "a");
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      final double x = z.getReal();
      final double y = z.getImaginary();
      return new ComplexNumber(Math.cos(x) * Math.cosh(y), -Math.sin(x) * Math.sinh(y));
    }
    return Math.cos(a.doubleValue());
  }

  public static Number cosh(final Number a) {
    Validate.notNull(a, "a");
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      return new ComplexNumber(Math.cosh(z.getReal()) * Math.cos(z.getImaginary()), Math.sinh(z.getReal()) * Math.sin(z.getImaginary()));
    }
    return Math.cosh(a.doubleValue());
  }

  public static Number sin(final Number a) {
    Validate.notNull(a, "a");
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      final double x = z.getReal();
      final double y = z.getImaginary();
      return new ComplexNumber(Math.sin(x) * Math.cosh(y), Math.cos(x) * Math.sinh(y));
    }
    return Math.sin(a.doubleValue());
  }

  public static Number sinh(final Number a) {
    Validate.notNull(a, "a");
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      return new ComplexNumber(Math.sinh(z.getReal()) * Math.cos(z.getImaginary()), Math.cosh(z.getReal()) * Math.sin(z.getImaginary()));
    }
    return Math.sinh(a.doubleValue());
  }

  public static Number tan(final Number a) {
    Validate.notNull(a, "a");
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      final ComplexNumber b = ComplexMathUtils.exp(ComplexMathUtils.multiply(ComplexMathUtils.multiply(I, 2), z));
      return ComplexMathUtils.divide(ComplexMathUtils.subtract(b, 1), ComplexMathUtils.multiply(I, ComplexMathUtils.add(b, 1)));
      //return ComplexMathUtils.divide((ComplexNumber) sin(a), (ComplexNumber) cos(a));
    }
    return Math.tan(a.doubleValue());
  }

  public static Number tanh(final Number a) {
    Validate.notNull(a, "a");
    if (a instanceof ComplexNumber) {
      final ComplexNumber z1 = (ComplexNumber) a;
      final ComplexNumber z2 = ComplexMathUtils.exp(z1);
      final ComplexNumber z3 = ComplexMathUtils.exp(ComplexMathUtils.multiply(z1, -1));
      return ComplexMathUtils.divide(ComplexMathUtils.subtract(z2, z3), ComplexMathUtils.add(z2, z3));
    }
    return Math.tanh(a.doubleValue());
  }

}
