/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.forex;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.financial.model.option.definition.SmileDeltaParameters;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParameters;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Interface for Forex Black with smile parameters provider for a currency pair.
 */
public class BlackForexVannaVolgaProvider implements BlackForexVannaVolgaProviderInterface {

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderInterface _multicurveProvider;
  /**
   * The volatility model for one currency pair.
   */
  private final SmileDeltaTermStructureParameters _smile;
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
  public BlackForexVannaVolgaProvider(final MulticurveProviderInterface multicurves, final SmileDeltaTermStructureParameters smile, final Pair<Currency, Currency> currencyPair) {
    _multicurveProvider = multicurves;
    _smile = smile;
    _currencyPair = currencyPair;
  }

  @Override
  public BlackForexVannaVolgaProvider copy() {
    MulticurveProviderInterface multicurveProvider = _multicurveProvider.copy();
    return new BlackForexVannaVolgaProvider(multicurveProvider, _smile, _currencyPair);
  }

  @Override
  public SmileDeltaTermStructureParameters getVolatility() {
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
   * Returns volatility smile for an expiration.
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @param time The expiration time.
   * @return The smile.
   */
  @Override
  public SmileDeltaParameters getSmile(final Currency ccy1, final Currency ccy2, final double time) {
    ArgumentChecker.notNull(ccy1, "first currency");
    ArgumentChecker.notNull(ccy2, "second currency");
    ArgumentChecker.isTrue(checkCurrencies(ccy1, ccy2), "Incomptabile currencies");
    final SmileDeltaParameters smile = _smile.getSmileForTime(time);
    if (ccy1.equals(getCurrencyPair().getFirst()) && ccy2.equals(getCurrencyPair().getSecond())) {
      return smile;
    }
    throw new NotImplementedException("Currency pair is not in expected order " + getCurrencyPair().toString());
  }

}
