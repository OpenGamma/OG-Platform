/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

/**
 * 
 */
public class BlackFunctionData {
  private final double _f;
  private final double _df;
  private final double _sigma;

  public BlackFunctionData(final double f, final double df, final double sigma) {
    _f = f;
    _df = df;
    _sigma = sigma;
  }

  public double getF() {
    return _f;
  }

  public double getDf() {
    return _df;
  }

  public double getSigma() {
    return _sigma;
  }

}
