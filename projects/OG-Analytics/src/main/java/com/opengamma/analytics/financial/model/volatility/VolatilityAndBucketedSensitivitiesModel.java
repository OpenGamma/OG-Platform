/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

/**
 * A volatility model {@link VolatilityModel} that returns the volatility and the bucketed sensitivities of that volatility value to the inputs to the surface
 * (e.g. the sensitivity to the market data points used to construct an interpolated volatility surface)
 * @param <T> The type of the abscissa(s)
 */
public interface VolatilityAndBucketedSensitivitiesModel<T> extends VolatilityModel<T> {

  /**
   * @param t The surface inputs
   * @return The volatility and bucketed sensitivities to the surface inputs
   */
  VolatilityAndBucketedSensitivities getVolatilityAndSensitivities(T t);
}
