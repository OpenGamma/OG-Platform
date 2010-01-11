/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math;

import com.opengamma.math.number.ComplexNumber;

/**
 * 
 * @author emcleod
 */

public class ComplexMath {
  public static double PI = Math.PI;
  public static double E = Math.E;
  private static ComplexNumber I = new ComplexNumber(0, 1);
  private static ComplexNumber NEGATIVE_I = new ComplexNumber(0, -1);

  public static Number abs(final Number a) {
    if (a instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get the absolute value of a ComplexNumber");
    if (a instanceof Double)
      return Math.abs(a.doubleValue());
    if (a instanceof Float)
      return Math.abs(a.floatValue());
    if (a instanceof Long)
      return Math.abs(a.longValue());
    if (a instanceof Integer)
      return Math.abs(a.intValue());
    throw new IllegalArgumentException("a was not a Double, Float, Integer or Long");
  }

  public static Number acos(final Number a) {
    if (a instanceof ComplexNumber) {
      final ComplexNumber z1 = (ComplexNumber) a;
      final ComplexNumber z2 = new ComplexNumber(PI / 2., 0);
      return add(z2, log(add(multiply(I, z1), sqrt(subtract(1, pow(z1, 2))))));
    }
    return Math.acos(a.doubleValue());
  }

  public static Number acosh(final Number a) {
    if (a instanceof ComplexNumber) {
      return log(add(a, multiply(sqrt(subtract(a, 1)), sqrt(add(a, 1)))));
    }
    final double x = a.doubleValue();
    return Math.log(x + Math.sqrt(x * x - 1));
  }

  public static Number add(final Number a, final Number b) {
    if (a instanceof ComplexNumber) {
      final ComplexNumber z1 = (ComplexNumber) a;
      if (b instanceof ComplexNumber) {
        final ComplexNumber z2 = (ComplexNumber) b;
        return new ComplexNumber(z1.getReal() + z2.getReal(), z1.getImaginary() + z2.getImaginary());
      }
      return new ComplexNumber(z1.getReal() + b.doubleValue(), z1.getImaginary());
    }
    if (b instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) b;
      return new ComplexNumber(z.getReal() + a.doubleValue(), z.getImaginary());
    }
    return a.doubleValue() + b.doubleValue();
  }

  public static Number arg(final Number a) {
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      return Math.atan2(z.getImaginary(), z.getReal());
    }
    throw new UnsupportedOperationException("Cannot find the argument of a real number");
  }

  public static Number asin(final Number a) {
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      return multiply(log(add(multiply(I, z), sqrt(subtract(1, pow(z, 2))))), NEGATIVE_I);
    }
    return Math.asin(a.doubleValue());
  }

  public static Number asinh(final Number a) {
    if (a instanceof ComplexNumber) {
      return log(add(a, sqrt(add(multiply(a, a), 1))));
    }
    final double x = a.doubleValue();
    return Math.log(x + Math.sqrt(x * x + 1));
  }

  public static Number atan(final Number a) {
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      final ComplexNumber iZ = (ComplexNumber) multiply(z, I);
      return multiply(0.5, multiply(I, subtract(subtract(1, iZ), add(1, iZ))));
    }
    return Math.atan(a.doubleValue());
  }

  public static Number atanh(final Number a) {
    if (a instanceof ComplexNumber) {
      return log(divide(sqrt(subtract(1, multiply(a, a))), subtract(1, a)));
    }
    final double x = a.doubleValue();
    return 0.5 * Math.log((1 + x) / (1 - x));
  }

  public static Number atan2(final Number y, final Number x) {
    if (y instanceof ComplexNumber || x instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get atan2() for ComplexNumbers");
    return Math.atan2(y.doubleValue(), x.doubleValue());
  }

  public static Number cbrt(final Number a) {
    if (a instanceof ComplexNumber) {
      return pow(a, 1. / 3);
    }
    return Math.cbrt(a.doubleValue());
  }

  public static Number ceil(final Number a) {
    if (a instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get ceiling value for a ComplexNumber");
    return Math.ceil(a.doubleValue());
  }

  public static Number conjugate(final Number a) {
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      return new ComplexNumber(z.getReal(), -z.getImaginary());
    }
    return a;
  }

  public static Number copySign(final Number magnitude, final Number sign) {
    if (magnitude instanceof ComplexNumber || sign instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot perform copySign() on ComplexNumbers");
    if (magnitude instanceof Float || sign instanceof Float && !(magnitude instanceof Double || sign instanceof Double)) {
      return Math.copySign(magnitude.floatValue(), sign.floatValue());
    }
    return Math.copySign(magnitude.doubleValue(), sign.doubleValue());
  }

  public static Number cos(final Number a) {
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      return new ComplexNumber(Math.cos(z.getReal()) * Math.cosh(z.getImaginary()), -Math.sin(z.getReal()) * Math.sinh(z.getImaginary()));
    }
    return Math.cos(a.doubleValue());
  }

  public static Number cosh(final Number a) {
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      return new ComplexNumber(Math.cosh(z.getReal()) * Math.cos(z.getImaginary()), Math.sinh(z.getReal()) * Math.sin(z.getImaginary()));
    }
    return Math.cosh(a.doubleValue());
  }

  public static Number divide(final Number a, final Number b) {
    if (a instanceof ComplexNumber) {
      final ComplexNumber z1 = (ComplexNumber) a;
      if (b instanceof ComplexNumber) {
        final ComplexNumber z2 = (ComplexNumber) b;
        final double p = z1.getReal();
        final double q = z1.getImaginary();
        final double r = z2.getReal();
        final double s = z2.getImaginary();
        final double denom = r * r + s * s;
        return new ComplexNumber((q * r + q * s) / denom, (q * r - p * s) / denom);
      }
      final double x = b.doubleValue();
      return new ComplexNumber(z1.getReal() / x, z1.getImaginary() / x);
    }
    if (b instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) b;
      final double denom = z.getReal() * z.getReal() + z.getImaginary() * z.getImaginary();
      final double x = a.doubleValue();
      return new ComplexNumber(x * z.getReal() / denom, -x * z.getImaginary() / denom);
    }
    return a.doubleValue() / b.doubleValue();
  }

  public static Number exp(final Number a) {
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      final double mult = Math.exp(z.getReal());
      return new ComplexNumber(mult * Math.cos(z.getImaginary()), mult * Math.sin(z.getImaginary()));
    }
    return Math.exp(a.doubleValue());
  }

  public static Number expm1(final Number a) {
    if (a instanceof ComplexNumber) {
      return subtract(exp(a), 1);
    }
    return Math.expm1(a.doubleValue());
  }

  public static Number floor(final Number a) {
    if (a instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get a floor value for a ComplexNumber");
    return Math.floor(a.doubleValue());
  }

  public static Number getExponent(final Number a) {
    if (a instanceof Double) {
      return Math.getExponent(a.doubleValue());
    }
    if (a instanceof Float) {
      return Math.getExponent(a.floatValue());
    }
    throw new UnsupportedOperationException("Can only get exponent for a Double or Float");
  }

  public static Number hypot(final Number x, final Number y) {
    if (x instanceof ComplexNumber || y instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get hypotenuse for a ComplexNumber");
    return Math.hypot(x.doubleValue(), y.doubleValue());
  }

  public static Number IEEEremainder(final Number a, final Number b) {
    if (a instanceof ComplexNumber || b instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get IEEE remainder for a ComplexNumber");
    return Math.IEEEremainder(a.doubleValue(), b.doubleValue());
  }

  public static Number inverse(final Number a) {
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      final double denom = z.getReal() * z.getReal() + z.getImaginary() * z.getImaginary();
      return new ComplexNumber(z.getReal() / denom, -z.getImaginary() / denom);
    }
    return 1. / a.doubleValue();
  }

  public static Number log(final Number a) {
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      return new ComplexNumber(Math.log(Math.hypot(z.getReal(), z.getImaginary())), Math.atan2(z.getImaginary(), z.getReal()));
    }
    return Math.log(a.doubleValue());
  }

  // TODO add methods to calculate in arbitrary base and to do conversions

  public static Number log10(final Number a) {
    if (a instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get log10 of a ComplexNumber");
    return Math.log10(a.doubleValue());
  }

  public static Number log1p(final Number a) {
    if (a instanceof ComplexNumber) {
      return log(add(a, 1));
    }
    return Math.log1p(a.doubleValue());
  }

  // TODO not sure if adding complex numbers to this makes sense - could be done
  // by modulus
  public static Number max(final Number a, final Number b) {
    if (a instanceof ComplexNumber || b instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get the maximum of two ComplexNumbers");
    if (a instanceof Integer) {
      if (b instanceof Long)
        return Math.max(a.longValue(), b.longValue());
      if (b instanceof Float)
        return Math.max(a.floatValue(), b.floatValue());
      if (b instanceof Double)
        return Math.max(a.doubleValue(), b.doubleValue());
      return Math.max(a.intValue(), b.intValue());
    }
    if (a instanceof Long) {
      if (b instanceof Float)
        return Math.max(a.floatValue(), b.floatValue());
      if (b instanceof Double)
        return Math.max(a.doubleValue(), b.doubleValue());
      return Math.max(a.longValue(), b.longValue());
    }
    if (a instanceof Float) {
      if (b instanceof Double)
        return Math.max(a.doubleValue(), b.doubleValue());
      return Math.max(a.floatValue(), b.floatValue());
    }
    return Math.max(a.doubleValue(), b.doubleValue());
  }

  public static Number min(final Number a, final Number b) {
    if (a instanceof ComplexNumber || b instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get the minumum of two ComplexNumbers");
    if (a instanceof Integer) {
      if (b instanceof Long)
        return Math.min(a.longValue(), b.longValue());
      if (b instanceof Float)
        return Math.min(a.floatValue(), b.floatValue());
      if (b instanceof Double)
        return Math.min(a.doubleValue(), b.doubleValue());
      return Math.min(a.intValue(), b.intValue());
    }
    if (a instanceof Long) {
      if (b instanceof Float)
        return Math.min(a.floatValue(), b.floatValue());
      if (b instanceof Double)
        return Math.min(a.doubleValue(), b.doubleValue());
      return Math.min(a.longValue(), b.longValue());
    }
    if (a instanceof Float) {
      if (b instanceof Double)
        return Math.min(a.doubleValue(), b.doubleValue());
      return Math.min(a.floatValue(), b.floatValue());
    }
    return Math.min(a.doubleValue(), b.doubleValue());
  }

  public static Number mod(final Number a) {
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      return hypot(z.getReal(), z.getImaginary());
    }
    throw new UnsupportedOperationException("Can only get the modulus of a ComplexNumber");
  }

  public static Number multiply(final Number a, final Number b) {
    if (a instanceof ComplexNumber) {
      final ComplexNumber z1 = (ComplexNumber) a;
      if (b instanceof ComplexNumber) {
        final ComplexNumber z2 = (ComplexNumber) b;
        final double p = z1.getReal();
        final double q = z1.getImaginary();
        final double r = z2.getReal();
        final double s = z2.getImaginary();
        return new ComplexNumber(p * r - q * s, p * s + q * r);
      }
      final double x = b.doubleValue();
      return new ComplexNumber(z1.getReal() * x, z1.getImaginary() * x);
    }
    if (b instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) b;
      final double x = a.doubleValue();
      return new ComplexNumber(z.getReal() * x, z.getImaginary() * x);
    }
    return a.doubleValue() * b.doubleValue();
  }

  public static Number nextAfter(final Number start, final Number direction) {
    if (start instanceof ComplexNumber || direction instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get nextAfter for a ComplexNumber");
    if (start instanceof Float || direction instanceof Float && !(start instanceof Double || direction instanceof Double)) {
      return Math.nextAfter(start.floatValue(), direction.floatValue());
    }
    return Math.nextAfter(start.doubleValue(), direction.doubleValue());
  }

  // TODO check whether an int gives a float or a double
  public static Number nextUp(final Number start) {
    if (start instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get nextUp for a ComplexNumber");
    if (start instanceof Float) {
      return Math.nextUp(start.floatValue());
    }
    return Math.nextUp(start.doubleValue());
  }

  public static Number pow(final Number a, final Number b) {
    if (a instanceof ComplexNumber) {
      final ComplexNumber z1 = (ComplexNumber) a;
      if (b instanceof ComplexNumber) {
        final ComplexNumber z2 = (ComplexNumber) b;
        final double mod = mod(z1).doubleValue();
        final double arg = arg(z1).doubleValue();
        final double mult = pow(mod, z2.getReal()).doubleValue() * exp(-z2.getImaginary() * arg).doubleValue();
        final double theta = z2.getReal() * arg + z2.getImaginary() * Math.log(mod);
        return new ComplexNumber(mult * Math.cos(theta), mult * Math.sin(theta));
      }
      final double mod = mod(a).doubleValue();
      final double arg = arg(a).doubleValue();
      final double x = b.doubleValue();
      final double mult = pow(mod, x).doubleValue();
      return new ComplexNumber(mult * Math.cos(x * arg), mult * Math.sin(x * arg));
    }
    if (b instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) b;
      final ComplexNumber theta = (ComplexNumber) multiply(z, log(a.doubleValue()));
      return add(cos(theta), multiply(I, sin(theta)));
    }
    return Math.pow(a.doubleValue(), b.doubleValue());
  }

  public static Number random() {
    return Math.random();
  }

  public static Number rint(final Number a) {
    if (a instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get rint for a ComplexNumber");
    return Math.rint(a.doubleValue());
  }

  // TODO check if int returns float or double in original
  public static Number round(final Number a) {
    if (a instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get round for a ComplexNumber");
    if (a instanceof Float)
      return Math.round(a.floatValue());
    return Math.round(a.doubleValue());
  }

  // TODO check if int returns float or double in original
  public static Number scalb(final Number d, final Number scaleFactor) {
    if (d instanceof ComplexNumber || scaleFactor instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get scalb for a ComplexNumber");
    if (!(scaleFactor instanceof Integer))
      throw new IllegalArgumentException("Scale factor must be an Integer");
    if (d instanceof Float)
      return Math.scalb(d.floatValue(), scaleFactor.intValue());
    return Math.scalb(d.doubleValue(), scaleFactor.intValue());
  }

  // TODO check if int returns float or double in original
  public static Number signum(final Number a) {
    if (a instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get signum for a ComplexNumber");
    if (a instanceof Float)
      return Math.signum(a.floatValue());
    return Math.signum(a.doubleValue());
  }

  public static Number sin(final Number a) {
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      return new ComplexNumber(Math.sin(z.getReal()) * Math.cosh(z.getImaginary()), -Math.cos(z.getReal()) * Math.sinh(z.getImaginary()));
    }
    return Math.sin(a.doubleValue());
  }

  public static Number sinh(final Number a) {
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      return new ComplexNumber(Math.sinh(z.getReal()) * Math.cos(z.getImaginary()), Math.cosh(z.getReal()) * Math.sin(z.getImaginary()));
    }
    return Math.sinh(a.doubleValue());
  }

  public static Number sqrt(final Number a) {
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      final double mod = Math.hypot(z.getReal(), z.getImaginary());
      final double mult = 1. / Math.sqrt(2);
      return new ComplexNumber(mult * Math.sqrt(mod + z.getReal()), Math.signum(z.getImaginary()) * mult * Math.sqrt(mod - z.getReal()));
    }
    if (a.doubleValue() < 0) {
      return new ComplexNumber(0, Math.sqrt(Math.abs(a.doubleValue())));
    }
    return Math.sqrt(a.doubleValue());
  }

  public static Number subtract(final Number a, final Number b) {
    if (a instanceof ComplexNumber) {
      final ComplexNumber z1 = (ComplexNumber) a;
      if (b instanceof ComplexNumber) {
        final ComplexNumber z2 = (ComplexNumber) b;
        return new ComplexNumber(z1.getReal() - z2.getReal(), z1.getImaginary() - z2.getImaginary());
      }
      return new ComplexNumber(z1.getReal() - b.doubleValue(), z1.getImaginary());
    }
    if (b instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) b;
      return new ComplexNumber(a.doubleValue() - z.getReal(), -z.getImaginary());
    }
    return a.doubleValue() - b.doubleValue();
  }

  public static double tan(final double a) {
    return Math.tan(a);
  }

  public static Number tan(final Number a) {
    if (a instanceof ComplexNumber) {
      final ComplexNumber z = (ComplexNumber) a;
      final ComplexNumber b = (ComplexNumber) exp(multiply(multiply(I, 2), z));
      return divide(subtract(b, 1), multiply(I, add(b, 1)));
    }
    return Math.tan(a.doubleValue());
  }

  public static double tanh(final double a) {
    return Math.tanh(a);
  }

  public static Number tanh(final Number a) {
    if (a instanceof ComplexNumber) {
      final ComplexNumber z1 = (ComplexNumber) a;
      final ComplexNumber z2 = (ComplexNumber) exp(z1);
      final ComplexNumber z3 = (ComplexNumber) exp(multiply(z1, -1));
      return divide(subtract(z2, z3), add(z2, z3));
    }
    return Math.tanh(a.doubleValue());
  }

  public static Number toDegrees(final Number angrad) {
    if (angrad instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot calculate toDegrees on a ComplexNumber");
    return Math.toDegrees(angrad.doubleValue());
  }

  public static Number toRadians(final Number angdeg) {
    if (angdeg instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot calculate toRadians on a ComplexNumber");
    return Math.toRadians(angdeg.doubleValue());
  }

  // TODO check what happens with int input in original
  public static Number ulp(final Number a) {
    if (a instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get ulp for a ComplexNumber");
    if (a instanceof Float)
      return Math.ulp(a.floatValue());
    return Math.ulp(a.doubleValue());
  }
}
