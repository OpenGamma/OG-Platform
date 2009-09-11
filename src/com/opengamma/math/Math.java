package com.opengamma.math;

import com.opengamma.math.number.ComplexNumber;

/**
 * 
 * @author emcleod
 * 
 */

public class Math {

  public static double PI = java.lang.Math.PI;
  public static double E = java.lang.Math.E;
  private static ComplexNumber I = new ComplexNumber(0, 1);
  private static ComplexNumber NEGATIVE_I = new ComplexNumber(0, -1);

  public static double abs(double a) {
    return java.lang.Math.abs(a);
  }

  public static float abs(float a) {
    return java.lang.Math.abs(a);
  }

  public static int abs(int a) {
    return java.lang.Math.abs(a);
  }

  public static long abs(long a) {
    return java.lang.Math.abs(a);
  }

  public static double acos(double a) {
    return java.lang.Math.acos(a);
  }

  public static ComplexNumber acos(ComplexNumber a) {
    ComplexNumber b = new ComplexNumber(PI / 2., 0);
    return add(b, log(add(multiply(I, a), sqrt(subtract(1, pow(a, 2))))));
  }

  public static double acosh(double a) {
    return log(a + sqrt(a * a - 1));
  }

  public static ComplexNumber acosh(ComplexNumber a) {
    return log(add(a, multiply(sqrt(subtract(a, 1)), sqrt(add(a, 1)))));
  }

  public static double add(double a, double b) {
    return a + b;
  }

  public static ComplexNumber add(double a, ComplexNumber b) {
    return new ComplexNumber(a + b.getReal(), b.getImaginary());
  }

  public static ComplexNumber add(ComplexNumber a, double b) {
    return new ComplexNumber(a.getReal() + b, a.getImaginary());
  }

  public static ComplexNumber add(ComplexNumber a, ComplexNumber b) {
    return new ComplexNumber(a.getReal() + b.getReal(), a.getImaginary() + b.getImaginary());
  }

  public static double arg(ComplexNumber a) {
    return Math.atan2(a.getImaginary(), a.getReal());
  }

  public static double asin(double a) {
    return java.lang.Math.asin(a);
  }

  public static ComplexNumber asin(ComplexNumber a) {
    return multiply(log(add(multiply(I, a), sqrt(subtract(1, pow(a, 2))))), NEGATIVE_I);
  }

  public static double asinh(double a) {
    return log(a + sqrt(a * a + 1));
  }

  public static ComplexNumber asinh(ComplexNumber a) {
    return log(add(a, sqrt(add(multiply(a, a), 1))));
  }

  public static double atan(double a) {
    return java.lang.Math.atan(a);
  }

  public static ComplexNumber atan(ComplexNumber a) {
    ComplexNumber iA = multiply(a, I);
    return multiply(0.5, multiply(I, subtract(subtract(1, iA), add(1, iA))));
  }

  public static double atanh(double a) {
    return 0.5 * log((1 + a) / (1 - a));
  }

  public static ComplexNumber atanh(ComplexNumber a) {
    return log(divide(sqrt(subtract(1, multiply(a, a))), subtract(1, a)));
  }

  public static double atan2(double y, double x) {
    return java.lang.Math.atan2(y, x);
  }

  public static double cbrt(double a) {
    return java.lang.Math.cbrt(a);
  }

  public static ComplexNumber cbrt(ComplexNumber a) {
    return pow(a, 1. / 3);
  }

  public static double ceil(double a) {
    return java.lang.Math.ceil(a);
  }

  public static ComplexNumber conjugate(ComplexNumber a) {
    return new ComplexNumber(a.getReal(), -a.getImaginary());
  }

  public static double copySign(double magnitude, double sign) {
    return java.lang.Math.copySign(magnitude, sign);
  }

  public static float copySign(float magnitude, float sign) {
    return java.lang.Math.copySign(magnitude, sign);
  }

  public static double cos(double a) {
    return java.lang.Math.cos(a);
  }

  public static ComplexNumber cos(ComplexNumber a) {
    return new ComplexNumber(cos(a.getReal()) * cosh(a.getImaginary()), -sin(a.getReal()) * sinh(a.getImaginary()));
  }

  public static double cosh(double a) {
    return java.lang.Math.cosh(a);
  }

  public static ComplexNumber cosh(ComplexNumber a) {
    return new ComplexNumber(cosh(a.getReal()) * cos(a.getImaginary()), sinh(a.getReal()) * sin(a.getImaginary()));
  }

  public static double divide(double a, double b) {
    return a / b;
  }

  public static ComplexNumber divide(ComplexNumber a, double b) {
    return new ComplexNumber(a.getReal() / b, a.getImaginary() / b);
  }

  public static ComplexNumber divide(double a, ComplexNumber b) {
    double denom = b.getReal() * b.getReal() + b.getImaginary() * b.getImaginary();
    return new ComplexNumber(a * b.getReal() / denom, -a * b.getImaginary() / denom);
  }

  public static ComplexNumber divide(ComplexNumber a, ComplexNumber b) {
    double p = a.getReal();
    double q = a.getImaginary();
    double r = b.getReal();
    double s = b.getImaginary();
    double denom = r * r + s * s;
    return new ComplexNumber((q * r + q * s) / denom, (q * r - p * s) / denom);
  }

  public static double exp(double a) {
    return java.lang.Math.exp(a);
  }

  public static ComplexNumber exp(ComplexNumber a) {
    double mult = exp(a.getReal());
    return new ComplexNumber(mult * cos(a.getImaginary()), mult * sin(a.getImaginary()));
  }

  public static double expm1(double a) {
    return java.lang.Math.expm1(a);
  }

  public static ComplexNumber expm1(ComplexNumber a) {
    return subtract(exp(a), 1);
  }

  public static double floor(double a) {
    return java.lang.Math.floor(a);
  }

  public static int getExponent(double a) {
    return java.lang.Math.getExponent(a);
  }

  public static int getExponent(float a) {
    return java.lang.Math.getExponent(a);
  }

  public static double hypot(double x, double y) {
    return java.lang.Math.hypot(x, y);
  }

  public static double IEEEremainder(double a, double b) {
    return java.lang.Math.IEEEremainder(a, b);
  }

  public static double inverse(double a) {
    return 1. / a;
  }

  public static ComplexNumber inverse(ComplexNumber a) {
    double denom = a.getReal() * a.getReal() + a.getImaginary() * a.getImaginary();
    return new ComplexNumber(a.getReal() / denom, -a.getImaginary() / denom);
  }

  public static double log(double a) {
    return java.lang.Math.log(a);
  }

  public static ComplexNumber log(ComplexNumber a) {
    return new ComplexNumber(log(hypot(a.getReal(), a.getImaginary())), atan2(a.getImaginary(), a.getReal()));
  }

  public static double log10(double a) {
    return java.lang.Math.log10(a);
  }

  public static ComplexNumber log10(ComplexNumber a) {
    return log(add(a, 1));
  }

  public static double log1p(double a) {
    return java.lang.Math.log1p(a);
  }

  public static ComplexNumber log1p(ComplexNumber a) {
    return log(add(a, 1));
  }

  public static double max(double a, double b) {
    return java.lang.Math.max(a, b);
  }

  public static float max(float a, float b) {
    return java.lang.Math.max(a, b);
  }

  public static int max(int a, int b) {
    return java.lang.Math.max(a, b);
  }

  public static long max(long a, long b) {
    return java.lang.Math.max(a, b);
  }

  public static ComplexNumber max(ComplexNumber a, ComplexNumber b) {
    double modA = mod(a);
    double modB = mod(b);
    return modA > modB ? a : b;
  }

  public static double min(double a, double b) {
    return java.lang.Math.min(a, b);
  }

  public static float min(float a, float b) {
    return java.lang.Math.min(a, b);
  }

  public static int min(int a, int b) {
    return java.lang.Math.min(a, b);
  }

  public static long min(long a, long b) {
    return java.lang.Math.min(a, b);
  }

  public static ComplexNumber min(ComplexNumber a, ComplexNumber b) {
    double modA = mod(a);
    double modB = mod(b);
    return modA < modB ? a : b;
  }

  public static double mod(ComplexNumber a) {
    return hypot(a.getReal(), a.getImaginary());
  }

  public static double multiply(double a, double b) {
    return a * b;
  }

  public static ComplexNumber multiply(ComplexNumber a, double b) {
    return new ComplexNumber(a.getReal() * b, a.getImaginary() * b);
  }

  public static ComplexNumber multiply(double a, ComplexNumber b) {
    return new ComplexNumber(a * b.getReal(), a * b.getImaginary());
  }

  public static ComplexNumber multiply(ComplexNumber a, ComplexNumber b) {
    double p = a.getReal();
    double q = a.getImaginary();
    double r = b.getReal();
    double s = b.getImaginary();
    return new ComplexNumber(p * r - q * s, p * s + q * r);
  }

  public static double nextAfter(double start, double direction) {
    return java.lang.Math.nextAfter(start, direction);
  }

  public static double nextAfter(float start, float direction) {
    return java.lang.Math.nextAfter(start, direction);
  }

  public static double nextUp(double a) {
    return java.lang.Math.nextUp(a);
  }

  public static double nextUp(float a) {
    return java.lang.Math.nextUp(a);
  }

  public static double pow(double a, double b) {
    return java.lang.Math.pow(a, b);
  }

  public static ComplexNumber pow(ComplexNumber a, double b) {
    double mod = mod(a);
    double arg = arg(a);
    double mult = pow(mod, b);
    return new ComplexNumber(mult * cos(b * arg), mult * sin(b * arg));
  }

  public static ComplexNumber pow(double a, ComplexNumber b) {
    ComplexNumber theta = multiply(b, log(a));
    return add(cos(theta), multiply(I, sin(theta)));
  }

  public static ComplexNumber pow(ComplexNumber a, ComplexNumber b) {
    double mod = mod(a);
    double arg = arg(a);
    double mult = pow(mod, b.getReal()) * exp(-b.getImaginary() * arg);
    double theta = b.getReal() * arg + b.getImaginary() * log(mod);
    return new ComplexNumber(mult * cos(theta), mult * sin(theta));
  }

  public static double random() {
    return java.lang.Math.random();
  }

  public static double rint(double a) {
    return java.lang.Math.rint(a);
  }

  public static double round(double a) {
    return java.lang.Math.round(a);
  }

  public static double round(float a) {
    return java.lang.Math.round(a);
  }

  public static double scalb(double d, int scaleFactor) {
    return java.lang.Math.scalb(d, scaleFactor);
  }

  public static float scalb(float f, int scaleFactor) {
    return java.lang.Math.scalb(f, scaleFactor);
  }

  public static double signum(double a) {
    return java.lang.Math.signum(a);
  }

  public static double signum(float a) {
    return java.lang.Math.signum(a);
  }

  public static double sin(double a) {
    return java.lang.Math.sin(a);
  }

  public static ComplexNumber sin(ComplexNumber a) {
    return new ComplexNumber(sin(a.getReal()) * cosh(a.getImaginary()), -cos(a.getReal()) * sinh(a.getImaginary()));
  }

  public static double sinh(double a) {
    return java.lang.Math.sinh(a);
  }

  public static ComplexNumber sinh(ComplexNumber a) {
    return new ComplexNumber(sinh(a.getReal()) * cos(a.getImaginary()), cosh(a.getReal()) * sin(a.getImaginary()));
  }

  public static double sqrt(double a) {
    return java.lang.Math.sqrt(a);
  }

  public static ComplexNumber sqrt(ComplexNumber a) {
    double mod = Math.hypot(a.getReal(), a.getImaginary());
    double mult = 1. / Math.sqrt(2);
    return new ComplexNumber(mult * sqrt(mod + a.getReal()), signum(a.getImaginary()) * mult * Math.sqrt(mod - a.getReal()));
  }

  public static double subtract(double a, double b) {
    return a - b;
  }

  public static ComplexNumber subtract(double a, ComplexNumber b) {
    return new ComplexNumber(a - b.getReal(), -b.getImaginary());
  }

  public static ComplexNumber subtract(ComplexNumber a, double b) {
    return new ComplexNumber(a.getReal() - b, a.getImaginary());
  }

  public static ComplexNumber subtract(ComplexNumber a, ComplexNumber b) {
    return new ComplexNumber(a.getReal() - b.getReal(), a.getImaginary() - b.getImaginary());
  }

  public static double tan(double a) {
    return java.lang.Math.tan(a);
  }

  public static ComplexNumber tan(ComplexNumber a) {
    ComplexNumber b = exp(multiply(multiply(I, 2), a));
    return divide(subtract(b, 1), multiply(I, add(b, 1)));
  }

  public static double tanh(double a) {
    return java.lang.Math.tanh(a);
  }

  public static ComplexNumber tanh(ComplexNumber a) {
    ComplexNumber b = exp(a);
    ComplexNumber c = exp(multiply(a, -1));
    return divide(subtract(b, c), add(b, c));
  }

  public static double toDegrees(double angrad) {
    return java.lang.Math.toDegrees(angrad);
  }

  public static double toRadians(double angdeg) {
    return java.lang.Math.toRadians(angdeg);
  }

  public static double ulp(double a) {
    return java.lang.Math.ulp(a);
  }

  public static double ulp(float a) {
    return java.lang.Math.ulp(a);
  }
}
