/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

/**
 * 
 */
public interface SABRFormula {

  /**
   * 
   * @param f The forward value of the underlying 
   * @param alpha The initial value of the stochastic volatility 
   * @param beta The CEV parameter 
   * @param nu The vol-of-vol
   * @param rho The correlation between the driver of the underlying and the driver of the stochastic volatility 
   * @param k The strike
   * @param t the time to maturity
   * @return The (Black) implied volatility 
   */
  double impliedVolatility(final double f, final double alpha, final double beta, final double nu, final double rho, final double k, final double t);
}
