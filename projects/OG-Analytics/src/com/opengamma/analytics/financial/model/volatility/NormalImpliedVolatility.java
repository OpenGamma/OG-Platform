/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalFunctionData;

/**
 *
 * Determines implied volatility for the Normal model
 *
 */
public interface NormalImpliedVolatility {

  /**
   * Computes the implied volatility from the price in a normally distributed asset price world.
   * @param data The model data. The data volatility, if not zero, is used as a starting point for the volatility search.
   * @param option The option.
   * @param optionPrice The option price.
   * @return The implied volatility.
   */
  double getImpliedVolatility(final NormalFunctionData data, final EuropeanVanillaOption option, final double optionPrice);

}
