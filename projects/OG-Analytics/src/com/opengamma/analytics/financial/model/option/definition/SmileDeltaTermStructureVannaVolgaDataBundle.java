/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing the data required to price instruments with the volatility delta and time dependent.
 */
public class SmileDeltaTermStructureVannaVolgaDataBundle extends YieldCurveBundle {

  /**
   * The smile parameters for the currency pair.
   */
  private final SmileDeltaTermStructureParameters _smile;
  /**
   * The currency pair for which the smile data is valid.
   */
  private final Pair<Currency, Currency> _currencyPair;

  /**
   * Constructor from the smile parameters and the curves.
   * @param ycBundle The curves bundle, not null
   * @param smile The smile parameters, not null
   * @param currencyPair The currency pair for which the smile is valid, not null
   */
  public SmileDeltaTermStructureVannaVolgaDataBundle(final YieldCurveBundle ycBundle, final SmileDeltaTermStructureParameters smile, final Pair<Currency, Currency> currencyPair) {
    super(ycBundle);
    ArgumentChecker.notNull(smile, "Smile parameters");
    ArgumentChecker.isTrue(smile.getNumberStrike() == 3, "Vanna-volga methods work only with three strikes");
    ArgumentChecker.notNull(currencyPair, "currency pair");
    //TODO: check rate is available for currency pair.
    _smile = smile;
    _currencyPair = currencyPair;
  }

  @Override
  /**
   * Create a  copy of the bundle.
   * @return The bundle.
   */
  public SmileDeltaTermStructureVannaVolgaDataBundle copy() {
    final YieldCurveBundle curves = super.copy();
    final SmileDeltaTermStructureParameters smile = _smile.copy();
    final Pair<Currency, Currency> currencyPair = Pair.of(_currencyPair.getFirst(), _currencyPair.getSecond());
    return new SmileDeltaTermStructureVannaVolgaDataBundle(curves, smile, currencyPair);
  }

  /**
   * Gets the smile parameters.
   * @return The smile parameters.
   */
  public SmileDeltaTermStructureParameters getSmile() {
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
   * @return The volatility.
   */
  public SmileDeltaParameters getSmile(final Currency ccy1, final Currency ccy2, final double time) {
    final SmileDeltaParameters smile = _smile.getSmileForTime(time);
    if ((ccy1 == _currencyPair.getFirst()) && (ccy2 == _currencyPair.getSecond())) {
      return smile;
    }
    throw new NotImplementedException("Currency pair is not in expected order " + _currencyPair.toString());
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
    final SmileDeltaTermStructureVannaVolgaDataBundle other = (SmileDeltaTermStructureVannaVolgaDataBundle) obj;
    if (!ObjectUtils.equals(_currencyPair, other._currencyPair)) {
      return false;
    }
    if (!ObjectUtils.equals(_smile, other._smile)) {
      return false;
    }
    return true;
  }


}
