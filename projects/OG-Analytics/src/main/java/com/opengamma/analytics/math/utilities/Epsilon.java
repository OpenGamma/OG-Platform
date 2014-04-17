/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.utilities;

/**
 * 
 */
public abstract class Epsilon {

  // Coefficients for the Taylor expansion of (e^x-1)/x and its first two derivatives
  private static final double[] COEFF1 = new double[] {1 / 24., 1 / 6., 1 / 2., 1 };
  private static final double[] COEFF2 = new double[] {1 / 144., 1 / 30., 1 / 8., 1 / 3., 1 / 2. };
  private static final double[] COEFF3 = new double[] {1 / 168., 1 / 36., 1 / 10., 1 / 4., 1 / 3. };

  /**
   * This is the Taylor expansion of $$\frac{\exp(x)-1}{x}$$ - note for $$|x| > 10^{-10}$$ the expansion is note used 
   * @param x value
   * @return result 
   */
  public static double epsilon(final double x) {
    if (Math.abs(x) > 1e-10) {
      return Math.expm1(x) / x;
    }
    return taylor(x, COEFF1);
  }

  /**
   * This is the Taylor expansion of the first derivative of $$\frac{\exp(x)-1}{x}$$
   * @param x value
   * @return result 
   */
  public static double epsilonP(final double x) {

    if (Math.abs(x) > 1e-7) {
      return ((x - 1) * Math.expm1(x) + x) / x / x;
    }
    return taylor(x, COEFF2);
  }

  /**
   * This is the Taylor expansion of the second derivative of $$\frac{\exp(x)-1}{x}$$
   * @param x value
   * @return result 
   */
  public static double epsilonPP(final double x) {

    if (Math.abs(x) > 1e-5) {
      final double x2 = x * x;
      final double x3 = x * x2;
      return (Math.expm1(x) * (x2 - 2 * x + 2) + x2 - 2 * x) / x3;
    }
    return taylor(x, COEFF3);
  }

  private static double taylor(final double x, final double[] coeff) {
    double sum = coeff[0];
    final int n = coeff.length;
    for (int i = 1; i < n; i++) {
      sum = coeff[i] + x * sum;
    }
    return sum;
  }

}
