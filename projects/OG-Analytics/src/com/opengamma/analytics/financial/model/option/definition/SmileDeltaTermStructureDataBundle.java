/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing the data required to price instruments with the volatility delta and time dependent.
 */
public class SmileDeltaTermStructureDataBundle extends YieldCurveBundle {

  /**
   * The smile parameters for one currency pair.
   */
  private final SmileDeltaTermStructureParametersStrikeInterpolation _smile;
  /**
   * The currency pair for which the smile data is valid.
   */
  private final Pair<Currency, Currency> _currencyPair;

  /**
   * Constructor from the smile parameters and the curves.
   * @param ycBundle The curves bundle.
   * @param smile The smile parameters.
   * @param currencyPair The currency pair for which the smile is valid.
   */
  public SmileDeltaTermStructureDataBundle(final YieldCurveBundle ycBundle, final SmileDeltaTermStructureParametersStrikeInterpolation smile, final Pair<Currency, Currency> currencyPair) {
    super(ycBundle);
    ArgumentChecker.notNull(smile, "Smile parameters");
    ArgumentChecker.notNull(currencyPair, "currency pair");
    final Collection<Currency> currencies = getCcyMap().values();
    final Currency firstCurrency = currencyPair.getFirst();
    final Currency secondCurrency = currencyPair.getSecond();
    ArgumentChecker.isTrue(currencies.contains(firstCurrency), "Curve currency map does not contain currency {}; have {}", firstCurrency, currencies);
    ArgumentChecker.isTrue(currencies.contains(secondCurrency), "Curve currency map does not contain currency {}, have {}", secondCurrency, currencies);
    //TODO: check rate is available for currency pair.
    _smile = smile;
    _currencyPair = currencyPair;
  }

  @Override
  /**
   * Create a new copy of the bundle using a new map and the same curve and curve names. The same FXMatrix is used.
   * @return The bundle.
   */
  public SmileDeltaTermStructureDataBundle copy() {
    return new SmileDeltaTermStructureDataBundle(this, _smile, _currencyPair);
  }

  /**
   * Gets the underlying volatility data.
   * @return The underlying volatility data.
   */
  public SmileDeltaTermStructureParametersStrikeInterpolation getVolatilityData() {
    return _smile;
  }

  public Pair<Currency, Currency> getCurrencyPair() {
    return _currencyPair;
  }

  /**
   * Get the volatility at a given time/strike/forward taking the currency pair order in account. See {@link SmileDeltaTermStructureParametersStrikeInterpolation} for the interpolation/extrapolation.
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @param time The time to expiration.
   * @param strike The strike.
   * @param forward The forward.
   * @return The volatility.
   */
  public double getVolatility(final Currency ccy1, final Currency ccy2, final double time, final double strike, final double forward) {
    if ((ccy1 == _currencyPair.getFirst()) && (ccy2 == _currencyPair.getSecond())) {
      return _smile.getVolatility(time, strike, forward);
    }
    if ((ccy2 == _currencyPair.getFirst()) && (ccy1 == _currencyPair.getSecond())) {
      return _smile.getVolatility(time, 1.0 / strike, 1.0 / forward);
    }
    throw new IllegalArgumentException("Currencies not compatible with smile data; asked for " + ccy1 + " and " + ccy2 + ", have " + getCcyMap().values());
  }

  /**
   * Computes the volatility and the volatility sensitivity with respect to the volatility data points.
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @param time The time to expiration.
   * @param strike The strike.
   * @param forward The forward.
   * @param bucketSensitivity The array is changed by the method. The array should have the correct size. After the methods, it contains the volatility sensitivity to the data points.
   * Only the lines of impacted dates are changed. The input data on the other lines will not be changed.
   * @return The volatility.
   */
  public double getVolatility(final Currency ccy1, final Currency ccy2, final double time, final double strike, final double forward, final double[][] bucketSensitivity) {
    if ((ccy1 == _currencyPair.getFirst()) && (ccy2 == _currencyPair.getSecond())) {
      return _smile.getVolatility(time, strike, forward, bucketSensitivity);
    }
    if ((ccy2 == _currencyPair.getFirst()) && (ccy1 == _currencyPair.getSecond())) {
      return _smile.getVolatility(time, 1.0 / strike, 1.0 / forward, bucketSensitivity);
    }
    Validate.isTrue(false, "Currencies not compatible with smile data");
    return 0.0;
  }

  /**
   * Check that two given currencies are compatible with the data currency pair.
   * @param ccy1 One currency.
   * @param ccy2 The other currency.
   * @return True if the currencies match the pair (in any order) and False otherwise.
   */
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
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _currencyPair.hashCode();
    result = prime * result + _smile.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SmileDeltaTermStructureDataBundle other = (SmileDeltaTermStructureDataBundle) obj;
    if (!ObjectUtils.equals(_currencyPair, other._currencyPair)) {
      return false;
    }
    if (!ObjectUtils.equals(_smile, other._smile)) {
      return false;
    }
    return true;
  }


}
