/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import java.util.List;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * interface for anything that provides a {@link DiscreteVolatilityFunction}
 */
public abstract class DiscreteVolatilityFunctionProvider implements VectorFunctionProvider<DoublesPair> {
  //
  //  /**
  //   * 
  //   * @param expiryStrikePoints list of expiry-strike points, i.e. DoublesPair with the expiry as the first entry and
  //   * strike as the second.
  //   * @return a {@link DiscreteVolatilityFunction} that will produce volatilities in the same order as the list
  //   */
  //  DiscreteVolatilityFunction from(final DoublesPair[] expiryStrikePoints);

  /**
   * 
   * @return The number of model parameters
   */
  public abstract int getNumModelParameters();

  @Override
  public DiscreteVolatilityFunction from(final List<DoublesPair> expiryStrikePoints) {
    ArgumentChecker.notNull(expiryStrikePoints, "expiryStrikePoints");
    return from(expiryStrikePoints.toArray(new DoublesPair[0]));
  }

  @Override
  public abstract DiscreteVolatilityFunction from(final DoublesPair[] expiryStrikePoints);
}
