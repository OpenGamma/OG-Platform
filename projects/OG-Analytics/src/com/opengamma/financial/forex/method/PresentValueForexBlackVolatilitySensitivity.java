/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.util.money.Currency;
import com.opengamma.util.surface.SurfaceValue;
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
    Validate.notNull(ccy1, "currency 1");
    Validate.notNull(ccy2, "currency 2");
    Validate.notNull(vega, "vega");
    Validate.isTrue(!vega.getMap().isEmpty(), "vega map was empty");
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
   * Return a new volatility sensitivity with all the exposures multiplied by a common factor.
   * @param sensi The sensitivity.
   * @param factor The multiplicative factor.
   * @return The new sensitivity.
   */
  public static PresentValueForexBlackVolatilitySensitivity multiplyBy(final PresentValueForexBlackVolatilitySensitivity sensi, final double factor) {
    return new PresentValueForexBlackVolatilitySensitivity(sensi._currencyPair.getFirst(), sensi._currencyPair.getSecond(), SurfaceValue.multiplyBy(sensi._vega, factor));
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
