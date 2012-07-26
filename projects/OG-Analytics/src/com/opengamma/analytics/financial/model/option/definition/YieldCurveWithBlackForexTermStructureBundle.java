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

  /**
   * The Black volatility term structure. Not null.
   */
  private final BlackForexTermStructureParameters _parameters;

  /**
   * Constructor from Black term structure parameters and an existing bundle. 
   * A new map is created for the bundle and only the curve and Forex data are copied.
   * @param parameters The Black volatility term structure. Not null.
   * @param bundle A yield curve bundle.
   */
  public YieldCurveWithBlackForexTermStructureBundle(BlackForexTermStructureParameters parameters, final YieldCurveBundle bundle) {
    super(bundle);
    ArgumentChecker.notNull(parameters, "Black volatility parameters");
    this._parameters = parameters;
  }

  @Override
  /**
   * Create a new copy of the bundle using a new map and the same curve and curve names. The same BlackSwaptionParameters is used.
   * @return The bundle.
   */
  public YieldCurveWithBlackForexTermStructureBundle copy() {
    return new YieldCurveWithBlackForexTermStructureBundle(_parameters, this);
  }

  /**
   * Gets the Black volatility term structure.
   * @return The surface.
   */
  public BlackForexTermStructureParameters getBlackParameters() {
    return _parameters;
  }

  /**
   * Returns the volatility for a given time to expiration.
   * @param time The time.
   * @return The volatility.
   */
  public double getVolatility(double time) {
    return _parameters.getVolatility(time);
  }

  /**
   * Returns the currency pair for which the Forex volatility data is valid.
   * @return The pair.
   */
  public Pair<Currency, Currency> getCurrencyPair() {
    return _parameters.getCurrencyPair();
  }

  /**
   * Check that two given currencies are compatible with the data currency pair.
   * @param ccy1 One currency.
   * @param ccy2 The other currency.
   * @return True if the currencies match the pair (in any order) and False otherwise.
   */
  public boolean checkCurrencies(Currency ccy1, Currency ccy2) {
    if ((ccy1 == _parameters.getCurrencyPair().getFirst()) && (ccy2 == _parameters.getCurrencyPair().getSecond())) {
      return true;
    }
    if ((ccy2 == _parameters.getCurrencyPair().getFirst()) && (ccy1 == _parameters.getCurrencyPair().getSecond())) {
      return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _parameters.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    YieldCurveWithBlackForexTermStructureBundle other = (YieldCurveWithBlackForexTermStructureBundle) obj;
    if (!ObjectUtils.equals(_parameters, other._parameters)) {
      return false;
    }
    return true;
  }

}
