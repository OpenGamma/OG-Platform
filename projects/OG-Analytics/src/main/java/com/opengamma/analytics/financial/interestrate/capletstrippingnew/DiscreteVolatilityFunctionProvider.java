/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import java.util.List;

import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;

/**
 * interface for anything that provides a {@link DiscreteVolatilityFunction}
 */
public interface DiscreteVolatilityFunctionProvider {

  /**
   * 
   * @param expiries expiries in ascending order
   * @param strikes each entry in the outer array is an array of strikes (in ascending order) corresponding to an expiry 
   * @param forwards the forwards corresponding to the expiries 
   * @return a {@link DiscreteVolatilityFunction} that will produce volatilities ordered first by expiry then by strike 
   */
  DiscreteVolatilityFunction from(final double[] expiries, final double[][] strikes, final double[] forwards);

  /**
   * 
   * @param data list of options 
   * @return a {@link DiscreteVolatilityFunction} that will produce volatilities in the same order as the list
   */
  DiscreteVolatilityFunction from(List<SimpleOptionData> data);

  /**
   * 
   * @return The number of model parameters
   */
  int getNumModelParameters();
}
