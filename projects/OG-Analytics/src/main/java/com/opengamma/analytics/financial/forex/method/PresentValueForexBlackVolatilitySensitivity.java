/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing the present value sensitivity to a Forex currency pair volatility point.
 */
public class PresentValueForexBlackVolatilitySensitivity {

  /**
   * The currency pair.
   */
  private final Pair<Currency, Currency> _currencyPair;
  /**
   * The volatility sensitivity as a map between (time to expiry, strike) and sensitivity value. The sensitivity value is in second/domestic currency.
   */
  private final SurfaceValue _vega;

  /**
   * Constructor with given sensitivities for a given currency pair.
   * @param ccy1 First currency.
   * @param ccy2 Second currency.
   * @param vega Values for vega. A new map is created for the new object.
   */
  public PresentValueForexBlackVolatilitySensitivity(final Currency ccy1, final Currency ccy2, final SurfaceValue vega) {
    ArgumentChecker.notNull(ccy1, "currency 1");
    ArgumentChecker.notNull(ccy2, "currency 2");
    ArgumentChecker.notNull(vega, "vega");
    ArgumentChecker.isTrue(!vega.getMap().isEmpty(), "vega map was empty");
    _currencyPair = ObjectsPair.of(ccy1, ccy2);
    _vega = SurfaceValue.from(vega.getMap());
  }

  /**
   * Gets the currency pair.
   * @return The currency pair.
   */
  public Pair<Currency, Currency> getCurrencyPair() {
    return _currencyPair;
  }

  /**
   * Gets the volatility sensitivity.
   * @return The sensitivity.
   */
  public SurfaceValue getVega() {
    return _vega;
  }

  /**
   * Gets the number of elements in the sensitivity.
   * @return The number of elements.
   */
  public int getNumberOfElements() {
    return _vega.getNumberOfElements();
  }

  /**
   * Return a new volatility sensitivity by adding another sensitivity.
   * @param other The Black volatility sensitivity. Not null.
   * @return The new sensitivity.
   */
  public PresentValueForexBlackVolatilitySensitivity plus(final PresentValueForexBlackVolatilitySensitivity other) {
    ArgumentChecker.isTrue(_currencyPair.equals(other._currencyPair), "Currency pairs incompatible");
    return new PresentValueForexBlackVolatilitySensitivity(_currencyPair.getFirst(), _currencyPair.getSecond(), SurfaceValue.plus(_vega, other._vega));
  }

  /**
   * Return a new volatility sensitivity with all the exposures multiplied by a common factor.
   * @param factor The multiplicative factor.
   * @return The new sensitivity.
   */
  public PresentValueForexBlackVolatilitySensitivity multipliedBy(final double factor) {
    return new PresentValueForexBlackVolatilitySensitivity(_currencyPair.getFirst(), _currencyPair.getSecond(), SurfaceValue.multiplyBy(_vega, factor));
  }

  /**
   * Add the sensitivity at a given (expiry/strike) point. The object is modified.
   * If the point is already present, the sensitivity is added.
   * @param point The expiry/strike point.
   * @param value The sensitivity value (in second/domestic currency).
   */
  public void add(final DoublesPair point, final double value) {
    _vega.add(point, value);
  }

  /**
   * Compare two sensitivities with a given tolerance. Return "true" if the currency pairs are the same and all the sensitivities are within the tolerance.
   * @param value1 The first sensitivity.
   * @param value2 The second sensitivity.
   * @param tolerance The tolerance.
   * @return The comparison flag.
   */
  public static boolean compare(final PresentValueForexBlackVolatilitySensitivity value1, final PresentValueForexBlackVolatilitySensitivity value2, final double tolerance) {
    if (!value1._currencyPair.equals(value2._currencyPair)) {
      return false;
    }
    return SurfaceValue.compare(value1._vega, value2._vega, tolerance);
  }

  /**
   * Collapse the sensitivity to one CurrencyAmount. The points on which the sensitivities occur are ignored and the values summed.
   * @return The amount.
   */
  public CurrencyAmount toSingleValue() {
    final Currency ccy = _currencyPair.getSecond();
    return CurrencyAmount.of(ccy, _vega.toSingleValue());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currencyPair.hashCode();
    result = prime * result + _vega.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PresentValueForexBlackVolatilitySensitivity other = (PresentValueForexBlackVolatilitySensitivity) obj;
    if (!ObjectUtils.equals(_currencyPair, other._currencyPair)) {
      return false;
    }
    if (!ObjectUtils.equals(_vega, other._vega)) {
      return false;
    }
    return true;
  }

}
