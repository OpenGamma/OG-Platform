/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.forex;

import com.opengamma.analytics.financial.model.volatility.curve.BlackForexTermStructureParameters;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Interface for Forex Black with smile parameters provider for a currency pair.
 */
public class BlackForexFlatProvider implements BlackForexFlatProviderInterface {

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderInterface _multicurveProvider;
  /**
   * The volatility model for one currency pair.
   */
  private final BlackForexTermStructureParameters _volatility;
  /**
   * The currency pair for which the volatility data are valid.
   */
  private final Pair<Currency, Currency> _currencyPair;

  /**
   * Constructor from exiting multicurveProvider and volatility model. The given provider and parameters are used for the new provider (the same maps are used, not copied).
   * @param multicurves The multi-curves provider.
   * @param volatility Volatility.
   * @param currencyPair The currency pair.
   */
  public BlackForexFlatProvider(final MulticurveProviderInterface multicurves, final BlackForexTermStructureParameters volatility, final Pair<Currency, Currency> currencyPair) {
    _multicurveProvider = multicurves;
    _volatility = volatility;
    _currencyPair = currencyPair;
  }

  @Override
  public BlackForexFlatProvider copy() {
    MulticurveProviderInterface multicurveProvider = _multicurveProvider.copy();
    return new BlackForexFlatProvider(multicurveProvider, _volatility, _currencyPair);
  }

  @Override
  public BlackForexTermStructureParameters getVolatility() {
    return _volatility;
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
   * @return The volatility.
   */
  @Override
  public double getVolatility(Currency ccy1, Currency ccy2, double time) {
    ArgumentChecker.isTrue(checkCurrencies(ccy1, ccy2), "Incompatible currencies");
    return getVolatility().getVolatility(time);
  }

  @Override
  public Double[] getVolatilityTimeSensitivity(Currency ccy1, Currency ccy2, double time) {
    ArgumentChecker.isTrue(checkCurrencies(ccy1, ccy2), "Incompatible currencies");
    return getVolatility().getVolatilityTimeSensitivity(time);
  }

}
