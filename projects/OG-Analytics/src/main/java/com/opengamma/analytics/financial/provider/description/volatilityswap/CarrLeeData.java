/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.volatilityswap;

import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;

/**
 * Interface for objects that provide the information required for Carr-Lee pricing of volatility
 * swaps. At a minimum, yield curves, volatility information, spot and realized variance are
 * required.
 * @param <CURVES_TYPE> The type of the curves data
 * @param <VOLATILITY_TYPE> The type of the volatility data.
 */
public interface CarrLeeData<CURVES_TYPE, VOLATILITY_TYPE> extends ParameterProviderInterface {

  @Override
  CarrLeeData<CURVES_TYPE, VOLATILITY_TYPE> copy();

  /**
   * Gets the volatility data.
   * @return The volatility data.
   */
  VOLATILITY_TYPE getVolatilityData();

  /**
   * Gets the spot.
   * @return The spot
   */
  double getSpot();

  /**
   * Gets the realized variance.
   * @return The realized variance
   */
  Double getRealizedVariance();

}
