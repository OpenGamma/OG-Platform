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
public class ForexBlackSmileProvider implements ForexBlackSmileProviderInterface {

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
  public ForexBlackSmileProvider(final MulticurveProviderInterface multicurves, final SmileDeltaTermStructureParametersStrikeInterpolation smile, final Pair<Currency, Currency> currencyPair) {
    _multicurveProvider = multicurves;
    _smile = smile;
    _currencyPair = currencyPair;
  }

  @Override
  public ForexBlackSmileProviderInterface copy() {
    MulticurveProviderInterface multicurveProvider = _multicurveProvider.copy();
    return new ForexBlackSmileProvider(multicurveProvider, _smile, _currencyPair);
  }

  @Override
  public SmileDeltaTermStructureParametersStrikeInterpolation getSmile() {
    return _smile;
  }

  @Override
  public double getVolatility(Currency ccy1, Currency ccy2, double time, double strike, double forward) {
    if ((ccy1 == _currencyPair.getFirst()) && (ccy2 == _currencyPair.getSecond())) {
      return _smile.getVolatility(time, strike, forward);
    }
    if ((ccy2 == _currencyPair.getFirst()) && (ccy1 == _currencyPair.getSecond())) {
      return _smile.getVolatility(time, 1.0 / strike, 1.0 / forward);
    }
    throw new IllegalArgumentException("Currencies not compatible with smile data; asked for " + ccy1 + " and " + ccy2);
  }

  @Override
  public VolatilityAndBucketedSensitivities getVolatilityAndSensitivities(Currency ccy1, Currency ccy2, double time, double strike, double forward) {
    if ((ccy1 == _currencyPair.getFirst()) && (ccy2 == _currencyPair.getSecond())) {
      return _smile.getVolatilityAndSensitivities(time, strike, forward);
    }
    if ((ccy2 == _currencyPair.getFirst()) && (ccy1 == _currencyPair.getSecond())) {
      return _smile.getVolatilityAndSensitivities(time, 1.0 / strike, 1.0 / forward);
    }
    throw new IllegalArgumentException("Currencies not compatible with smile data; asked for " + ccy1 + " and " + ccy2);
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

}
