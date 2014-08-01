/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import com.opengamma.util.tuple.DoublesPair;

/**
 * interface for anything that provides a {@link DiscreteVolatilityFunction}
 */
public interface DiscreteVolatilityFunctionProvider {

  /**
   * 
   * @param expiryStrikePoints list of expiry-strike points, i.e. DoublesPair with the expiry as the first entry and
   * strike as the second.
   * @return a {@link DiscreteVolatilityFunction} that will produce volatilities in the same order as the list
   */
  DiscreteVolatilityFunction from(final DoublesPair[] expiryStrikePoints);

  /**
   * 
   * @return The number of model parameters
   */
  int getNumModelParameters();
}
