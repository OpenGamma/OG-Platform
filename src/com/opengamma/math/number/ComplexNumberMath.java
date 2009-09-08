package com.opengamma.math.number;

/**
 * @author emcleod
 */

/**
 * Contains commonly-used arithmetic, trigonometric and hypergeometric functions
 * of complex numbers.
 * <p>
 * Note that unless a real number is the expected result (e.g. modulus), a
 * ComplexNumber will be returned even if the result has no imaginary component
 * i.e. <br/>
 * <i>(3 + 4i) + (7 - 4i) = 10 + 0i</i>
 */
public class ComplexNumberMath {
  private static final ComplexNumber I = new ComplexNumber(0, 1);
  private static final ComplexNumber ONE = new ComplexNumber(1, 0);
  private static final ComplexNumber NEGATIVE_I = scale(I, -1);

  /**
   * Adds two ComplexNumbers
   * 
   * @param z1
   *          An argument
   * @param z2
   *          Another argument
   * @return z1 + z2
   */
  public static ComplexNumber add(ComplexNumber z1, ComplexNumber z2) {
    return new ComplexNumber(z1.getReal() + z2.getReal(), z1.getImaginary() + z2.getImaginary());
  }

  /**
   * Subtracts two ComplexNumbers
   * 
   * @param z1
   *          An argument
   * @param z2
   *          Another argument
   * @return z1 - z2
   */
  public static ComplexNumber subtract(ComplexNumber z1, ComplexNumber z2) {
    return new ComplexNumber(z1.getReal() - z2.getReal(), z1.getImaginary() - z2.getImaginary());
  }

  /**
   * Multiplies two ComplexNumbers
   * 
   * 
   * @param z1
   *          An argument
   * @param z2
   *          Another argument
   * @return z1 * z2
   */
  public static ComplexNumber multiply(ComplexNumber z1, ComplexNumber z2) {
    double a = z1.getReal();
    double b = z1.getImaginary();
    double c = z2.getReal();
    double d = z2.getImaginary();
    return new ComplexNumber(a * c - b * d, a * d + b * c);
  }

  /**
   * Divides two ComplexNumbers. This method uses java.lang.Math.hypot to avoid
   * intermediate over- or underflow when calculating the denominator
   * 
   * @param z1
   *          An argument
   * @param z2
   *          Another argument
   * @return x / y
   * @throws IllegalArgumentException
   *           An exception is thrown if the divisor is zero (0 + 0i)
   */
  public static ComplexNumber divide(ComplexNumber z1, ComplexNumber z2) {
    double a = z1.getReal();
    double b = z1.getImaginary();
    double c = z2.getReal();
    double d = z2.getImaginary();
    if (c == 0 && d == 0) {
      throw new IllegalArgumentException("Attempted division by zero (0 + 0i)");
    }
    double temp = Math.hypot(c, d);
    double denom = temp * temp;
    return new ComplexNumber((a * c + b * d) / denom, (b * c - a * d) / denom);
  }

  /**
   * Scales a ComplexNumber
   * 
   * @param z
   *          An argument (a + ib)
   * @param scale
   *          The scale
   * @return scale * a + scale * b)i
   */
  public static ComplexNumber scale(ComplexNumber z, double scale) {
    return new ComplexNumber(z.getReal() * scale, z.getImaginary() * scale);
  }

  /**
   * Conjugate of a ComplexNumber
   * 
   * @param z
   *          An argument
   * @return z<sup>*</sup>
   */
  public static ComplexNumber conjugate(ComplexNumber z) {
    return new ComplexNumber(z.getReal(), -z.getImaginary());
  }

  /**
   * Calculates the argument of a ComplexNumber using java.lang.Math.atan2.
   * Consult <a href="http://java.sun.com/javase/6/docs/api/"></a> for a
   * description of the behaviour of this function when the real or imaginary
   * part is zero, NaN or infinite
   * 
   * @param z
   *          An argument (a + ib)
   * @return tan<sup>-1</sup>(b / a)
   */
  public static double arg(ComplexNumber z) {
    return Math.atan2(z.getImaginary(), z.getReal());
  }

  /**
   * Calculates the modulus of a ComplexNumber. Uses java.lang.Math.hypot to
   * avoid intermediate over- or underflow
   * 
   * @param z
   *          An argument (a + ib)
   * @return &radic;(a<sup>2</sup> + b<sup>2</sup>)
   */
  public static double mod(ComplexNumber z) {
    return Math.hypot(z.getReal(), z.getImaginary());
  }

  /**
   * Calculates the reciprocal of a ComplexNumber. Uses java.lang.Math.hypot to
   * avoid intermediate over- or underflow in calculation of the denominator
   * 
   * @param z
   * @return z<sup>-1</sup>
   * @throws IllegalArgumentException
   *           An exception is thrown if the ComplexNumber is zero (0 + 0i)
   */
  public static ComplexNumber reciprocal(ComplexNumber z) {
    if (z.getReal() == 0 && z.getReal() == 0) {
      throw new IllegalArgumentException("Tried to find reciprocal of zero (0 + 0i)");
    }
    double abs = Math.hypot(z.getReal(), z.getImaginary());
    double denom = abs * abs;
    return new ComplexNumber(z.getReal() / denom, -z.getImaginary() / denom);
  }

  /**
   * Calculates the square root of a ComplexNumber. The branch cut is along the
   * negative real axis.
   * 
   * @param z
   * @return &radic;z
   */
  public static ComplexNumber sqrt(ComplexNumber z) {
    double mod = Math.hypot(z.getReal(), z.getImaginary());
    double mult = 1. / Math.sqrt(2);
    return new ComplexNumber(mult * Math.sqrt(mod + z.getReal()), Math.signum(z.getImaginary()) * mult * Math.sqrt(mod - z.getReal()));
  }

  /**
   * Uses Euler's theorem to raise a ComplexNumber to a real power.
   * 
   * @param z
   *          Complex argument
   * @param y
   *          Real argument
   * @return z<sup>y</sup>
   */
  public static ComplexNumber pow(ComplexNumber z, double y) {
    double mod = mod(z);
    double arg = arg(z);
    double mult = Math.pow(mod, y);
    return new ComplexNumber(mult * Math.cos(y * arg), mult * Math.sin(y * arg));
  }

  /**
   * Calculates exponential of a ComplexNumber
   * 
   * @param z
   *          An argument
   * @return e<sup>z</sup>
   */
  public static ComplexNumber exp(ComplexNumber z) {
    double mult = Math.exp(z.getReal());
    return new ComplexNumber(mult * Math.cos(z.getImaginary()), mult * Math.sin(z.getImaginary()));
  }

  /**
   * Calculates the natural logarithm of a ComplexNumber
   * 
   * @param z
   *          An argument
   * @return ln(z)
   */

  public static ComplexNumber log(ComplexNumber z) {
    return new ComplexNumber(Math.log(Math.hypot(z.getReal(), z.getImaginary())), Math.atan2(z.getImaginary(), z.getReal()));
  }

  /**
   * Calculates the sine of a ComplexNumber
   * 
   * @param z
   *          An argument
   * @return sin(z)
   */

  public static ComplexNumber sin(ComplexNumber z) {
    return new ComplexNumber(Math.sin(z.getReal()) * Math.cosh(z.getImaginary()), -Math.cos(z.getReal()) * Math.sinh(z.getImaginary()));
  }

  /**
   * Calculates the cosine of a ComplexNumber
   * 
   * @param z
   *          An argument
   * @return cos(z)
   */
  public static ComplexNumber cos(ComplexNumber z) {
    return new ComplexNumber(Math.cos(z.getReal()) * Math.cosh(z.getImaginary()), -Math.sin(z.getReal()) * Math.sinh(z.getImaginary()));
  }

  /**
   * Calculates the tangent of a ComplexNumber
   * 
   * @param z
   *          An argument
   * @return tan(z)
   */
  public static ComplexNumber tan(ComplexNumber z) {
    return divide(sin(z), cos(z));
  }

  /**
   * Calculates the inverse sine of a ComplexNumber
   * 
   * @param z
   *          An argument
   * @return sin<sup>-1</sup>(z)
   */
  public static ComplexNumber asin(ComplexNumber z) {
    return multiply(log(add(multiply(I, z), sqrt(subtract(ONE, pow(z, 2))))), NEGATIVE_I);
  }

  /**
   * Calculates the inverse cosine of a ComplexNumber
   * 
   * @param z
   *          An argument
   * @return cos<sup>-1</sup>(z)
   */
  public static ComplexNumber acos(ComplexNumber z) {
    ComplexNumber z1 = new ComplexNumber(Math.PI / 2., 0);
    return add(z1, log(add(multiply(I, z), sqrt(subtract(ONE, pow(z, 2))))));
  }

  /**
   * Calculates the inverse tangent of a ComplexNumber
   * 
   * @param z
   *          An argument
   * @return tan<sup>-1</sup>
   */
  public static ComplexNumber atan(ComplexNumber z) {
    throw new UnsupportedOperationException();

  }

  /**
   * Calculates the inverse tangent of a ComplexNumber
   * 
   * @param z
   *          An argument
   * @return tan<sup>-1</sup>
   */
  public static ComplexNumber sinh(ComplexNumber z) {
    return new ComplexNumber(Math.sinh(z.getReal()) * Math.cos(z.getImaginary()), Math.cosh(z.getReal()) * Math.sin(z.getImaginary()));
  }

  /**
   * Calculates the inverse tangent of a ComplexNumber
   * 
   * @param z
   *          An argument
   * @return tan<sup>-1</sup>
   */
  public static ComplexNumber cosh(ComplexNumber z) {
    return new ComplexNumber(Math.cosh(z.getReal()) * Math.cos(z.getImaginary()), Math.sinh(z.getReal()) * Math.sin(z.getImaginary()));
  }

  /**
   * Calculates the inverse tangent of a ComplexNumber
   * 
   * @param z
   *          An argument
   * @return tan<sup>-1</sup>
   */
  public static ComplexNumber tanh(ComplexNumber z) {
    return divide(sinh(z), cosh(z));
  }

  /**
   * Calculates the inverse tangent of a ComplexNumber
   * 
   * @param z
   *          An argument
   * @return asinh<sup>-1</sup>
   */
  public static ComplexNumber asinh(ComplexNumber z) {
    throw new UnsupportedOperationException();
  }

  /**
   * Calculates the inverse tangent of a ComplexNumber
   * 
   * @param z
   *          An argument
   * @return cosh<sup>-1</sup>
   */
  public static ComplexNumber acosh(ComplexNumber z) {
    throw new UnsupportedOperationException();
  }

  /**
   * Calculates the inverse tangent of a ComplexNumber
   * 
   * @param z
   *          An argument
   * @return tanh<sup>-1</sup>
   */
  public static ComplexNumber atanh(ComplexNumber z) {
    throw new UnsupportedOperationException();

  }
}
