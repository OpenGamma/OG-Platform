/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.forex;

import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Interface for Forex Black with smile parameters provider for a currency pair.
 */
public class BlackForexSmileProvider implements BlackForexSmileProviderInterface {

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderInterface _multicurveProvider;
  /**
   * The volatility model for one currency pair.
   */
  private final SmileDeltaTermStructureParametersStrikeInterpolation _smile;
  /**
   * The currency pair for which the volatility data are valid.
   */
  private final Pair<Currency, Currency> _currencyPair;

  /**
   * Constructor from exiting multicurveProvider and volatility model. The given provider and parameters are used for the new provider (the same maps are used, not copied).
   * @param multicurves The multi-curves provider.
   * @param smile Smile.
   * @param currencyPair The currency pair.
   */
  public BlackForexSmileProvider(final MulticurveProviderInterface multicurves, final SmileDeltaTermStructureParametersStrikeInterpolation smile, final Pair<Currency, Currency> currencyPair) {
    _multicurveProvider = multicurves;
    _smile = smile;
    _currencyPair = currencyPair;
  }

  @Override
  public BlackForexSmileProvider copy() {
    MulticurveProviderInterface multicurveProvider = _multicurveProvider.copy();
    return new BlackForexSmileProvider(multicurveProvider, _smile, _currencyPair);
  }

  @Override
  public SmileDeltaTermStructureParametersStrikeInterpolation getVolatility() {
    return _smile;
  }

  @Override
  public Pair<Currency, Currency> getCurrencyPair() {
    return _currencyPair;
  }

  @Override
  public boolean checkCurrencies(Currency ccy1, Currency ccy2) {
    if ((ccy1.equals(_currencyPair.getFirst())) && ccy2.equals(_currencyPair.getSecond())) {
      return true;
    }
    if ((ccy2.equals(_currencyPair.getFirst())) && ccy1.equals(_currencyPair.getSecond())) {
      return true;
    }
    return false;
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _multicurveProvider;
  }

  /**
   * Returns volatility for a expiration, strike and forward. The volatility take into account the curerncy order.
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @param time The expiration time.
   * @param strike The strike.
   * @param forward The forward rate.
   * @return The volatility.
   */
  @Override
  public double getVolatility(Currency ccy1, Currency ccy2, double time, double strike, double forward) {
    if ((ccy1 == getCurrencyPair().getFirst()) && (ccy2 == getCurrencyPair().getSecond())) {
      return getVolatility().getVolatility(time, strike, forward);
    }
    if ((ccy2 == getCurrencyPair().getFirst()) && (ccy1 == getCurrencyPair().getSecond())) {
      return getVolatility().getVolatility(time, 1.0 / strike, 1.0 / forward);
    }
    throw new IllegalArgumentException("Currencies not compatible with smile data; asked for " + ccy1 + " and " + ccy2);
  }

  @Override
  public VolatilityAndBucketedSensitivities getVolatilityAndSensitivities(Currency ccy1, Currency ccy2, double time, double strike, double forward) {
    if ((ccy1 == getCurrencyPair().getFirst()) && (ccy2 == getCurrencyPair().getSecond())) {
      return getVolatility().getVolatilityAndSensitivities(time, strike, forward);
    }
    if ((ccy2 == getCurrencyPair().getFirst()) && (ccy1 == getCurrencyPair().getSecond())) {
      return getVolatility().getVolatilityAndSensitivities(time, 1.0 / strike, 1.0 / forward);
    }
    throw new IllegalArgumentException("Currencies not compatible with smile data; asked for " + ccy1 + " and " + ccy2);
  }

}
