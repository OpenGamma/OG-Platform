/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.volatilityswap;

import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.util.money.Currency;

/**
 *
 */
public interface CarrLeeData<CURVES_TYPE, VOLATILITY_TYPE> extends ParameterProviderInterface {

  @Override
  CarrLeeData<CURVES_TYPE, VOLATILITY_TYPE> copy();

  VOLATILITY_TYPE getVolatilitySurface();

  double getSpot();

  double getRealizedVariance();

}
