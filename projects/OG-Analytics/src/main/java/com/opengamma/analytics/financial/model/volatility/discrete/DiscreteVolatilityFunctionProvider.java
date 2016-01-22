/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.discrete;

import java.io.Serializable;
import java.util.List;

import com.opengamma.analytics.math.function.VectorFunctionProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Abstraction for anything that provides a {@link DiscreteVolatilityFunction} for a set of expiry-strike points
 */
public abstract class DiscreteVolatilityFunctionProvider implements VectorFunctionProvider<DoublesPair>, Serializable {

  /**
   * Make a {@link DiscreteVolatilityFunction} for the given expiry-strike points; this will map from some model 
   * parameters to volatilities at the  expiry-strike points
   * @param expiryStrikePoints List of expiry-strike points that the returned {@link DiscreteVolatilityFunction}
   * must give volatilities for 
   * @return a {@link DiscreteVolatilityFunction}
   */
  @Override
  public DiscreteVolatilityFunction from(final List<DoublesPair> expiryStrikePoints) {
    ArgumentChecker.notNull(expiryStrikePoints, "expiryStrikePoints");
    return from(expiryStrikePoints.toArray(new DoublesPair[0]));
  }

  /**
   * Make a {@link DiscreteVolatilityFunction} for the given expiry-strike points; this will map from some model 
   * parameters to volatilities at the  expiry-strike points
   * @param expiryStrikePoints Arrays of expiry-strike points that the returned {@link DiscreteVolatilityFunction}
   * must give volatilities for 
   * @return a {@link DiscreteVolatilityFunction}
   */
  @Override
  public abstract DiscreteVolatilityFunction from(final DoublesPair[] expiryStrikePoints);
}
