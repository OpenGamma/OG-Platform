/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.model.volatility.VolatilityModel;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing the Black volatility term structure used in option pricing.
 */
public class BlackForexTermStructureParameters implements VolatilityModel<double[]> {

  /**
   * The volatility term structure. The dimension is the expiration. Not null.
   */
  private final DoublesCurve _volatility;
  /**
   * The currency pair for which the data is valid. Not null.
   */
  private final Pair<Currency, Currency> _currencyPair;

  /**
   * Constructor from a curve.
   * @param volatility The term structure of implied volatility. Not null.
   * @param currencyPair The The currency pair for which the data is valid. Not null.
   */
  public BlackForexTermStructureParameters(DoublesCurve volatility, Pair<Currency, Currency> currencyPair) {
    ArgumentChecker.notNull(volatility, "Volatility");
    ArgumentChecker.notNull(currencyPair, "Currency pair");
    _volatility = volatility;
    _currencyPair = currencyPair;
  }

  /**
   * Returns the currency pair for which the data is valid.
   * @return The pair.
   */
  public Pair<Currency, Currency> getCurrencyPair() {
    return _currencyPair;
  }

  /**
   * Returns the volatility term structure curve.
   * @return The curve.
   */
  public DoublesCurve getVolatilityCurve() {
    return _volatility;
  }

  /**
   * Returns the implied volatility for a given expiration.
   * @param t The time to expiration.
   * @return The volatility.
   */
  public double getVolatility(double t) {
    return _volatility.getYValue(t);
  }

  /**
   * Returns the implied volatility for a given expiration.
   * @param t The time to expiration.
   * @return The volatility.
   */
  public Double[] getVolatilityParameterSensitivity(double t) {
    return _volatility.getYValueParameterSensitivity(t);
  }

  @Override
  public Double getVolatility(double[] t) {
    ArgumentChecker.isTrue(t.length == 1, "Incorrect number of data");
    return _volatility.getYValue(t[0]);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currencyPair.hashCode();
    result = prime * result + _volatility.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BlackForexTermStructureParameters other = (BlackForexTermStructureParameters) obj;
    if (!ObjectUtils.equals(_currencyPair, other._currencyPair)) {
      return false;
    }
    if (!ObjectUtils.equals(_volatility, other._volatility)) {
      return false;
    }
    return true;
  }

}
