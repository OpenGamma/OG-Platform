/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.scratchpad;

/**
 * 
 */
public class InterestRateProcess {

  private final double _r0;
  private final double _a;
  private final double _b;
  private final double _sigma;

  public InterestRateProcess(final double r0, final double a, final double b, final double sigma) {

    _r0 = r0;
    _a = a;
    _b = b;
    _sigma = sigma;

  }

  public double getR0() {
    return _r0;
  }

  public double getA() {
    return _a;
  }

  public double getB() {
    return _b;
  }

  public double getSigma() {
    return _sigma;
  }

}
