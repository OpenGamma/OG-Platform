/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

/**
 * Gatheral's Stochastic Volatility Inspired (SVI) model
 */
public class SVIFormula {

  public double impliedVolatility(final double strike, final double a, final double b, final double rho, final double sigma, final double m) {
    final double d = strike - m;
    return a + b * (rho * d + Math.sqrt(d * d + sigma * sigma));
  }

}
