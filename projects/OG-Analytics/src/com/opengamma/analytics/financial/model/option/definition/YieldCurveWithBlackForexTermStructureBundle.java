/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing a yield curve bundle with Black term structure volatility for Forex options.
 */
public class YieldCurveWithBlackForexTermStructureBundle extends YieldCurveBundle {
  private final BlackForexTermStructureParameters _termStructure;
  private final Pair<Currency, Currency> _currencyPair;

  /**
   * Constructor from the smile parameters and the curves.
   * @param ycBundle The curves bundle.
   * @param termStructure The term structure parameters.
   * @param currencyPair The currency pair for which the smile is valid.
   */
  public YieldCurveWithBlackForexTermStructureBundle(final YieldCurveBundle ycBundle, final BlackForexTermStructureParameters termStructure, final Pair<Currency, Currency> currencyPair) {
    super(ycBundle);
    ArgumentChecker.notNull(termStructure, "Black term structure");
    ArgumentChecker.notNull(currencyPair, "currency pair");
    _termStructure = termStructure;
    _currencyPair = currencyPair;
  }

  @Override
  /**
   * Create a shallow copy of the bundle using a new map and the same curve and curve names.
   * @return The bundle.
   */
  public YieldCurveWithBlackForexTermStructureBundle copy() {
    return new YieldCurveWithBlackForexTermStructureBundle(this, _termStructure, _currencyPair);
  }

  /**
   * Gets the underlying volatility data.
   * @return The underlying volatility data
   */
  public BlackForexTermStructureParameters getVolatilityData() {
    return _termStructure;
  }

  /**
   * Returns the currency pair for which the Forex volatility data is valid.
   * @return The pair.
   */
  public Pair<Currency, Currency> getCurrencyPair() {
    return _currencyPair;
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
    result = prime * result + _termStructure.hashCode();
    result = prime * result + _currencyPair.hashCode();
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
    final YieldCurveWithBlackForexTermStructureBundle other = (YieldCurveWithBlackForexTermStructureBundle) obj;
    if (!ObjectUtils.equals(_termStructure, other._termStructure)) {
      return false;
    }
    if (!ObjectUtils.equals(_currencyPair, other._currencyPair)) {
      return false;
    }
    return true;
  }

}
