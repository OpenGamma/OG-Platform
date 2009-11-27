package com.opengamma.math;

import com.opengamma.math.number.ComplexNumber;

/**
 * 
 * @author emcleod
 * 
 */

public class ComplexMath {

  public static double PI = java.lang.Math.PI;
  public static double E = java.lang.Math.E;
  private static ComplexNumber I = new ComplexNumber(0, 1);
  private static ComplexNumber NEGATIVE_I = new ComplexNumber(0, -1);

  public static Number abs(Number a) {
    if (a instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get the absolute value of a ComplexNumber");
    if (a instanceof Double)
      return java.lang.Math.abs(a.doubleValue());
    if (a instanceof Float)
      return java.lang.Math.abs(a.floatValue());
    if (a instanceof Long)
      return java.lang.Math.abs(a.longValue());
    if (a instanceof Integer)
      return java.lang.Math.abs(a.intValue());
    throw new IllegalArgumentException("a was not a Double, Float, Integer or Long");
  }

  public static Number acos(Number a) {
    if (a instanceof ComplexNumber) {
      ComplexNumber z1 = (ComplexNumber) a;
      ComplexNumber z2 = new ComplexNumber(PI / 2., 0);
      return add(z2, log(add(multiply(I, z1), sqrt(subtract(1, pow(z1, 2))))));
    }
    return java.lang.Math.acos(a.doubleValue());
  }

  public static Number acosh(Number a) {
    if (a instanceof ComplexNumber) {
      return log(add(a, multiply(sqrt(subtract(a, 1)), sqrt(add(a, 1)))));
    }
    double x = a.doubleValue();
    return java.lang.Math.log(x + java.lang.Math.sqrt(x * x - 1));
  }

  public static Number add(Number a, Number b) {
    if (a instanceof ComplexNumber) {
      ComplexNumber z1 = (ComplexNumber) a;
      if (b instanceof ComplexNumber) {
        ComplexNumber z2 = (ComplexNumber) b;
        return new ComplexNumber(z1.getReal() + z2.getReal(), z1.getImaginary() + z2.getImaginary());
      }
      return new ComplexNumber(z1.getReal() + b.doubleValue(), z1.getImaginary());
    }
    if (b instanceof ComplexNumber) {
      ComplexNumber z = (ComplexNumber) b;
      return new ComplexNumber(z.getReal() + a.doubleValue(), z.getImaginary());
    }
    return a.doubleValue() + b.doubleValue();
  }

  public static Number arg(Number a) {
    if (a instanceof ComplexNumber) {
      ComplexNumber z = (ComplexNumber) a;
      return java.lang.Math.atan2(z.getImaginary(), z.getReal());
    }
    throw new UnsupportedOperationException("Cannot find the argument of a real number");
  }

  public static Number asin(Number a) {
    if (a instanceof ComplexNumber) {
      ComplexNumber z = (ComplexNumber) a;
      return multiply(log(add(multiply(I, z), sqrt(subtract(1, pow(z, 2))))), NEGATIVE_I);
    }
    return java.lang.Math.asin(a.doubleValue());
  }

  public static Number asinh(Number a) {
    if (a instanceof ComplexNumber) {
      return log(add(a, sqrt(add(multiply(a, a), 1))));
    }
    double x = a.doubleValue();
    return java.lang.Math.log(x + java.lang.Math.sqrt(x * x + 1));
  }

  public static Number atan(Number a) {
    if (a instanceof ComplexNumber) {
      ComplexNumber z = (ComplexNumber) a;
      ComplexNumber iZ = (ComplexNumber) multiply(z, I);
      return multiply(0.5, multiply(I, subtract(subtract(1, iZ), add(1, iZ))));
    }
    return java.lang.Math.atan(a.doubleValue());
  }

  public static Number atanh(Number a) {
    if (a instanceof ComplexNumber) {
      return log(divide(sqrt(subtract(1, multiply(a, a))), subtract(1, a)));
    }
    double x = a.doubleValue();
    return 0.5 * java.lang.Math.log((1 + x) / (1 - x));
  }

  public static Number atan2(Number y, Number x) {
    if (y instanceof ComplexNumber || x instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get atan2() for ComplexNumbers");
    return java.lang.Math.atan2(y.doubleValue(), x.doubleValue());
  }

  public static Number cbrt(Number a) {
    if (a instanceof ComplexNumber) {
      return pow(a, 1. / 3);
    }
    return java.lang.Math.cbrt(a.doubleValue());
  }

  public static Number ceil(Number a) {
    if (a instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get ceiling value for a ComplexNumber");
    return java.lang.Math.ceil(a.doubleValue());
  }

  public static Number conjugate(Number a) {
    if (a instanceof ComplexNumber) {
      ComplexNumber z = (ComplexNumber) a;
      return new ComplexNumber(z.getReal(), -z.getImaginary());
    }
    return a;
  }

  public static Number copySign(Number magnitude, Number sign) {
    if (magnitude instanceof ComplexNumber || sign instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot perform copySign() on ComplexNumbers");
    if (magnitude instanceof Float || sign instanceof Float && !(magnitude instanceof Double || sign instanceof Double)) {
      return java.lang.Math.copySign(magnitude.floatValue(), sign.floatValue());
    }
    return java.lang.Math.copySign(magnitude.doubleValue(), sign.doubleValue());
  }

  public static Number cos(Number a) {
    if (a instanceof ComplexNumber) {
      ComplexNumber z = (ComplexNumber) a;
      return new ComplexNumber(java.lang.Math.cos(z.getReal()) * java.lang.Math.cosh(z.getImaginary()), -java.lang.Math.sin(z.getReal()) * java.lang.Math.sinh(z.getImaginary()));
    }
    return java.lang.Math.cos(a.doubleValue());
  }

  public static Number cosh(Number a) {
    if (a instanceof ComplexNumber) {
      ComplexNumber z = (ComplexNumber) a;
      return new ComplexNumber(java.lang.Math.cosh(z.getReal()) * java.lang.Math.cos(z.getImaginary()), java.lang.Math.sinh(z.getReal()) * java.lang.Math.sin(z.getImaginary()));
    }
    return java.lang.Math.cosh(a.doubleValue());
  }

  public static Number divide(Number a, Number b) {
    if (a instanceof ComplexNumber) {
      ComplexNumber z1 = (ComplexNumber) a;
      if (b instanceof ComplexNumber) {
        ComplexNumber z2 = (ComplexNumber) b;
        double p = z1.getReal();
        double q = z1.getImaginary();
        double r = z2.getReal();
        double s = z2.getImaginary();
        double denom = r * r + s * s;
        return new ComplexNumber((q * r + q * s) / denom, (q * r - p * s) / denom);
      }
      double x = b.doubleValue();
      return new ComplexNumber(z1.getReal() / x, z1.getImaginary() / x);
    }
    if (b instanceof ComplexNumber) {
      ComplexNumber z = (ComplexNumber) b;
      double denom = z.getReal() * z.getReal() + z.getImaginary() * z.getImaginary();
      double x = a.doubleValue();
      return new ComplexNumber(x * z.getReal() / denom, -x * z.getImaginary() / denom);
    }
    return a.doubleValue() / b.doubleValue();
  }

  public static Number exp(Number a) {
    if (a instanceof ComplexNumber) {
      ComplexNumber z = (ComplexNumber) a;
      double mult = java.lang.Math.exp(z.getReal());
      return new ComplexNumber(mult * java.lang.Math.cos(z.getImaginary()), mult * java.lang.Math.sin(z.getImaginary()));
    }
    return java.lang.Math.exp(a.doubleValue());
  }

  public static Number expm1(Number a) {
    if (a instanceof ComplexNumber) {
      return subtract(exp(a), 1);
    }
    return java.lang.Math.expm1(a.doubleValue());
  }

  public static Number floor(Number a) {
    if (a instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get a floor value for a ComplexNumber");
    return java.lang.Math.floor(a.doubleValue());
  }

  public static Number getExponent(Number a) {
    if (a instanceof Double) {
      return java.lang.Math.getExponent(a.doubleValue());
    }
    if (a instanceof Float) {
      return java.lang.Math.getExponent(a.floatValue());
    }
    throw new UnsupportedOperationException("Can only get exponent for a Double or Float");
  }

  public static Number hypot(Number x, Number y) {
    if (x instanceof ComplexNumber || y instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get hypotenuse for a ComplexNumber");
    return java.lang.Math.hypot(x.doubleValue(), y.doubleValue());
  }

  public static Number IEEEremainder(Number a, Number b) {
    if (a instanceof ComplexNumber || b instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get IEEE remainder for a ComplexNumber");
    return java.lang.Math.IEEEremainder(a.doubleValue(), b.doubleValue());
  }

  public static Number inverse(Number a) {
    if (a instanceof ComplexNumber) {
      ComplexNumber z = (ComplexNumber) a;
      double denom = z.getReal() * z.getReal() + z.getImaginary() * z.getImaginary();
      return new ComplexNumber(z.getReal() / denom, -z.getImaginary() / denom);
    }
    return 1. / a.doubleValue();
  }

  public static Number log(Number a) {
    if (a instanceof ComplexNumber) {
      ComplexNumber z = (ComplexNumber) a;
      return new ComplexNumber(java.lang.Math.log(java.lang.Math.hypot(z.getReal(), z.getImaginary())), java.lang.Math.atan2(z.getImaginary(), z.getReal()));
    }
    return java.lang.Math.log(a.doubleValue());
  }

  // TODO add methods to calculate in arbitrary base and to do conversions

  public static Number log10(Number a) {
    if (a instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get log10 of a ComplexNumber");
    return java.lang.Math.log10(a.doubleValue());
  }

  public static Number log1p(Number a) {
    if (a instanceof ComplexNumber) {
      return log(add(a, 1));
    }
    return java.lang.Math.log1p(a.doubleValue());
  }

  // TODO not sure if adding complex numbers to this makes sense - could be done
  // by modulus
  public static Number max(Number a, Number b) {
    if (a instanceof ComplexNumber || b instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get the maximum of two ComplexNumbers");
    if (a instanceof Integer) {
      if (b instanceof Long)
        return java.lang.Math.max(a.longValue(), b.longValue());
      if (b instanceof Float)
        return java.lang.Math.max(a.floatValue(), b.floatValue());
      if (b instanceof Double)
        return java.lang.Math.max(a.doubleValue(), b.doubleValue());
      return java.lang.Math.max(a.intValue(), b.intValue());
    }
    if (a instanceof Long) {
      if (b instanceof Float)
        return java.lang.Math.max(a.floatValue(), b.floatValue());
      if (b instanceof Double)
        return java.lang.Math.max(a.doubleValue(), b.doubleValue());
      return java.lang.Math.max(a.longValue(), b.longValue());
    }
    if (a instanceof Float) {
      if (b instanceof Double)
        return java.lang.Math.max(a.doubleValue(), b.doubleValue());
      return java.lang.Math.max(a.floatValue(), b.floatValue());
    }
    return java.lang.Math.max(a.doubleValue(), b.doubleValue());
  }

  public static Number min(Number a, Number b) {
    if (a instanceof ComplexNumber || b instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get the minumum of two ComplexNumbers");
    if (a instanceof Integer) {
      if (b instanceof Long)
        return java.lang.Math.min(a.longValue(), b.longValue());
      if (b instanceof Float)
        return java.lang.Math.min(a.floatValue(), b.floatValue());
      if (b instanceof Double)
        return java.lang.Math.min(a.doubleValue(), b.doubleValue());
      return java.lang.Math.min(a.intValue(), b.intValue());
    }
    if (a instanceof Long) {
      if (b instanceof Float)
        return java.lang.Math.min(a.floatValue(), b.floatValue());
      if (b instanceof Double)
        return java.lang.Math.min(a.doubleValue(), b.doubleValue());
      return java.lang.Math.min(a.longValue(), b.longValue());
    }
    if (a instanceof Float) {
      if (b instanceof Double)
        return java.lang.Math.min(a.doubleValue(), b.doubleValue());
      return java.lang.Math.min(a.floatValue(), b.floatValue());
    }
    return java.lang.Math.min(a.doubleValue(), b.doubleValue());
  }

  public static Number mod(Number a) {
    if (a instanceof ComplexNumber) {
      ComplexNumber z = (ComplexNumber) a;
      return hypot(z.getReal(), z.getImaginary());
    }
    throw new UnsupportedOperationException("Can only get the modulus of a ComplexNumber");
  }

  public static Number multiply(Number a, Number b) {
    if (a instanceof ComplexNumber) {
      ComplexNumber z1 = (ComplexNumber) a;
      if (b instanceof ComplexNumber) {
        ComplexNumber z2 = (ComplexNumber) b;
        double p = z1.getReal();
        double q = z1.getImaginary();
        double r = z2.getReal();
        double s = z2.getImaginary();
        return new ComplexNumber(p * r - q * s, p * s + q * r);
      }
      double x = b.doubleValue();
      return new ComplexNumber(z1.getReal() * x, z1.getImaginary() * x);
    }
    if (b instanceof ComplexNumber) {
      ComplexNumber z = (ComplexNumber) b;
      double x = a.doubleValue();
      return new ComplexNumber(z.getReal() * x, z.getImaginary() * x);
    }
    return a.doubleValue() * b.doubleValue();
  }

  public static Number nextAfter(Number start, Number direction) {
    if (start instanceof ComplexNumber || direction instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get nextAfter for a ComplexNumber");
    if (start instanceof Float || direction instanceof Float && !(start instanceof Double || direction instanceof Double)) {
      return java.lang.Math.nextAfter(start.floatValue(), direction.floatValue());
    }
    return java.lang.Math.nextAfter(start.doubleValue(), direction.doubleValue());
  }

  // TODO check whether an int gives a float or a double
  public static Number nextUp(Number start) {
    if (start instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get nextUp for a ComplexNumber");
    if (start instanceof Float) {
      return java.lang.Math.nextUp(start.floatValue());
    }
    return java.lang.Math.nextUp(start.doubleValue());
  }

  public static Number pow(Number a, Number b) {
    if (a instanceof ComplexNumber) {
      ComplexNumber z1 = (ComplexNumber) a;
      if (b instanceof ComplexNumber) {
        ComplexNumber z2 = (ComplexNumber) b;
        double mod = mod(z1).doubleValue();
        double arg = arg(z1).doubleValue();
        double mult = pow(mod, z2.getReal()).doubleValue() * exp(-z2.getImaginary() * arg).doubleValue();
        double theta = z2.getReal() * arg + z2.getImaginary() * java.lang.Math.log(mod);
        return new ComplexNumber(mult * java.lang.Math.cos(theta), mult * java.lang.Math.sin(theta));
      }
      double mod = mod(a).doubleValue();
      double arg = arg(a).doubleValue();
      double x = b.doubleValue();
      double mult = pow(mod, x).doubleValue();
      return new ComplexNumber(mult * java.lang.Math.cos(x * arg), mult * java.lang.Math.sin(x * arg));
    }
    if (b instanceof ComplexNumber) {
      ComplexNumber z = (ComplexNumber) b;
      ComplexNumber theta = (ComplexNumber) multiply(z, log(a.doubleValue()));
      return add(cos(theta), multiply(I, sin(theta)));
    }
    return java.lang.Math.pow(a.doubleValue(), b.doubleValue());
  }

  public static Number random() {
    return java.lang.Math.random();
  }

  public static Number rint(Number a) {
    if (a instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get rint for a ComplexNumber");
    return java.lang.Math.rint(a.doubleValue());
  }

  // TODO check if int returns float or double in original
  public static Number round(Number a) {
    if (a instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get round for a ComplexNumber");
    if (a instanceof Float)
      return java.lang.Math.round(a.floatValue());
    return java.lang.Math.round(a.doubleValue());
  }

  // TODO check if int returns float or double in original
  public static Number scalb(Number d, Number scaleFactor) {
    if (d instanceof ComplexNumber || scaleFactor instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get scalb for a ComplexNumber");
    if (!(scaleFactor instanceof Integer))
      throw new IllegalArgumentException("Scale factor must be an Integer");
    if (d instanceof Float)
      return java.lang.Math.scalb(d.floatValue(), scaleFactor.intValue());
    return java.lang.Math.scalb(d.doubleValue(), scaleFactor.intValue());
  }

  // TODO check if int returns float or double in original
  public static Number signum(Number a) {
    if (a instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get signum for a ComplexNumber");
    if (a instanceof Float)
      return java.lang.Math.signum(a.floatValue());
    return java.lang.Math.signum(a.doubleValue());
  }

  public static Number sin(Number a) {
    if (a instanceof ComplexNumber) {
      ComplexNumber z = (ComplexNumber) a;
      return new ComplexNumber(java.lang.Math.sin(z.getReal()) * java.lang.Math.cosh(z.getImaginary()), -java.lang.Math.cos(z.getReal()) * java.lang.Math.sinh(z.getImaginary()));
    }
    return java.lang.Math.sin(a.doubleValue());
  }

  public static Number sinh(Number a) {
    if (a instanceof ComplexNumber) {
      ComplexNumber z = (ComplexNumber) a;
      return new ComplexNumber(java.lang.Math.sinh(z.getReal()) * java.lang.Math.cos(z.getImaginary()), java.lang.Math.cosh(z.getReal()) * java.lang.Math.sin(z.getImaginary()));
    }
    return java.lang.Math.sinh(a.doubleValue());
  }

  public static Number sqrt(Number a) {
    if (a instanceof ComplexNumber) {
      ComplexNumber z = (ComplexNumber) a;
      double mod = java.lang.Math.hypot(z.getReal(), z.getImaginary());
      double mult = 1. / java.lang.Math.sqrt(2);
      return new ComplexNumber(mult * java.lang.Math.sqrt(mod + z.getReal()), java.lang.Math.signum(z.getImaginary()) * mult * java.lang.Math.sqrt(mod - z.getReal()));
    }
    if (a.doubleValue() < 0) {
      return new ComplexNumber(0, java.lang.Math.sqrt(java.lang.Math.abs(a.doubleValue())));
    }
    return java.lang.Math.sqrt(a.doubleValue());
  }

  public static Number subtract(Number a, Number b) {
    if (a instanceof ComplexNumber) {
      ComplexNumber z1 = (ComplexNumber) a;
      if (b instanceof ComplexNumber) {
        ComplexNumber z2 = (ComplexNumber) b;
        return new ComplexNumber(z1.getReal() - z2.getReal(), z1.getImaginary() - z2.getImaginary());
      }
      return new ComplexNumber(z1.getReal() - b.doubleValue(), z1.getImaginary());
    }
    if (b instanceof ComplexNumber) {
      ComplexNumber z = (ComplexNumber) b;
      return new ComplexNumber(a.doubleValue() - z.getReal(), -z.getImaginary());
    }
    return a.doubleValue() - b.doubleValue();
  }

  public static double tan(double a) {
    return java.lang.Math.tan(a);
  }

  public static Number tan(Number a) {
    if (a instanceof ComplexNumber) {
      ComplexNumber z = (ComplexNumber) a;
      ComplexNumber b = (ComplexNumber) exp(multiply(multiply(I, 2), z));
      return divide(subtract(b, 1), multiply(I, add(b, 1)));
    }
    return java.lang.Math.tan(a.doubleValue());
  }

  public static double tanh(double a) {
    return java.lang.Math.tanh(a);
  }

  public static Number tanh(Number a) {
    if (a instanceof ComplexNumber) {
      ComplexNumber z1 = (ComplexNumber) a;
      ComplexNumber z2 = (ComplexNumber) exp(z1);
      ComplexNumber z3 = (ComplexNumber) exp(multiply(z1, -1));
      return divide(subtract(z2, z3), add(z2, z3));
    }
    return java.lang.Math.tanh(a.doubleValue());
  }

  public static Number toDegrees(Number angrad) {
    if (angrad instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot calculate toDegrees on a ComplexNumber");
    return java.lang.Math.toDegrees(angrad.doubleValue());
  }

  public static Number toRadians(Number angdeg) {
    if (angdeg instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot calculate toRadians on a ComplexNumber");
    return java.lang.Math.toRadians(angdeg.doubleValue());
  }

  // TODO check what happens with int input in original
  public static Number ulp(Number a) {
    if (a instanceof ComplexNumber)
      throw new UnsupportedOperationException("Cannot get ulp for a ComplexNumber");
    if (a instanceof Float)
      return java.lang.Math.ulp(a.floatValue());
    return java.lang.Math.ulp(a.doubleValue());
  }
}
