/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.forex;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
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
   * @param multicurves The multi-curves provider, not null
   * @param smile Smile, not null
   * @param currencyPair The currency pair, not null
   */
  public BlackForexSmileProvider(final MulticurveProviderInterface multicurves, final SmileDeltaTermStructureParametersStrikeInterpolation smile, final Pair<Currency, Currency> currencyPair) {
    ArgumentChecker.notNull(multicurves, "multicurves");
    ArgumentChecker.notNull(smile, "smile");
    ArgumentChecker.notNull(currencyPair, "currencyPair");
    _multicurveProvider = multicurves;
    _smile = smile;
    _currencyPair = currencyPair;
  }

  @Override
  public BlackForexSmileProvider copy() {
    final MulticurveProviderInterface multicurveProvider = _multicurveProvider.copy();
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
  public boolean checkCurrencies(final Currency ccy1, final Currency ccy2) {
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
   * Returns volatility for a expiration, strike and forward. The volatility take into account the currency order.
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @param time The expiration time.
   * @param strike The strike.
   * @param forward The forward rate.
   * @return The volatility.
   */
  @Override
  public double getVolatility(final Currency ccy1, final Currency ccy2, final double time, final double strike, final double forward) {
    if (getCurrencyPair().getFirst().equals(ccy1) && getCurrencyPair().getSecond().equals(ccy2)) {
      return getVolatility().getVolatility(time, strike, forward);
    }
    if (getCurrencyPair().getFirst().equals(ccy2) && getCurrencyPair().getSecond().equals(ccy1)) {
      return getVolatility().getVolatility(time, 1.0 / strike, 1.0 / forward);
    }
    throw new IllegalArgumentException("Currencies not compatible with smile data; asked for " + ccy1 + " and " + ccy2);
  }

  @Override
  public VolatilityAndBucketedSensitivities getVolatilityAndSensitivities(final Currency ccy1, final Currency ccy2, final double time, final double strike, final double forward) {
    if (getCurrencyPair().getFirst().equals(ccy1) && getCurrencyPair().getSecond().equals(ccy2)) {
      return getVolatility().getVolatilityAndSensitivities(time, strike, forward);
    }
    if (getCurrencyPair().getFirst().equals(ccy2) && getCurrencyPair().getSecond().equals(ccy1)) {
      return getVolatility().getVolatilityAndSensitivities(time, 1.0 / strike, 1.0 / forward);
    }
    throw new IllegalArgumentException("Currencies not compatible with smile data; asked for " + ccy1 + " and " + ccy2);
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    return _multicurveProvider.parameterSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    return _multicurveProvider.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _multicurveProvider.getAllCurveNames();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currencyPair.hashCode();
    result = prime * result + _multicurveProvider.hashCode();
    result = prime * result + _smile.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BlackForexSmileProvider)) {
      return false;
    }
    final BlackForexSmileProvider other = (BlackForexSmileProvider) obj;
    if (!ObjectUtils.equals(_currencyPair, other._currencyPair)) {
      return false;
    }
    if (!ObjectUtils.equals(_multicurveProvider, other._multicurveProvider)) {
      return false;
    }
    if (!ObjectUtils.equals(_smile, other._smile)) {
      return false;
    }
    return true;
  }

}
