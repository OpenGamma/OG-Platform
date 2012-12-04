/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description;

import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Interface for G2++ parameters provider for one currency.
 */
public interface ForexBlackSmileProviderInterface {

  /**
   * Create a new copy of the provider.
   * @return The bundle.
   */
  ForexBlackSmileProviderInterface copy();

  /**
   * Returns the XX
   * @return The parameters.
   */
  SmileDeltaTermStructureParametersStrikeInterpolation getSmile();

  /**
   * Returns the (Black implied) volatility
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @param time The time to expiration.
   * @param strike The strike.
   * @param forward The forward.
   * @return The volatility.
   */
  double getVolatility(final Currency ccy1, final Currency ccy2, final double time, final double strike, final double forward);

  /**
   * Returns XXX
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @param time The time to expiration.
   * @param strike The strike.
   * @param forward The forward.
   * @return XXX.
   */
  VolatilityAndBucketedSensitivities getVolatilityAndSensitivities(final Currency ccy1, final Currency ccy2, final double time, final double strike, final double forward);

  /**
   * Returns XXX
   * @return The currency pair.
   */
  Pair<Currency, Currency> getCurrencyPair();

  /**
   * XXX
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @return XXX
   */
  boolean checkCurrencies(final Currency ccy1, final Currency ccy2);

  /**
   * Returns the MulticurveProvider from which the InflationProvider is composed.
   * @return The multi-curves provider.
   */
  MulticurveProviderInterface getMulticurveProvider();

}
