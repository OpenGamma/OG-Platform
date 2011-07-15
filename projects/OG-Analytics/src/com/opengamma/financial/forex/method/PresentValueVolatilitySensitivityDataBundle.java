/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing the present value sensitivity to a Forex currency pair volatility point.
 */
public class PresentValueVolatilitySensitivityDataBundle {

  /**
   * The currency pair.
   */
  private final Pair<Currency, Currency> _currencyPair;
  /**
   * The volatility sensitivity as a map between (time to expiry, strike) and sensitivity value. The sensitivity value is in second/domestic currency.
   */
  private final Map<DoublesPair, Double> _vega;

  /**
   * Constructor with empty sensitivities for a given currency pair.
   * @param ccy1 First currency.
   * @param ccy2 Second currency.
   * @param vega Map containing values for vega
   */
  //TODO shouldn't be allowed to passs in an empty map
  public PresentValueVolatilitySensitivityDataBundle(final Currency ccy1, final Currency ccy2, Map<DoublesPair, Double> vega) {
    Validate.notNull(ccy1, "currency 1");
    Validate.notNull(ccy2, "currency 2");
    Validate.notNull(vega, "vega");
    _currencyPair = ObjectsPair.of(ccy1, ccy2);
    _vega = new HashMap<DoublesPair, Double>();
    _vega.putAll(vega);
  }
  
  /**
   * Gets the currency pair.
   * @return The currency pair.
   */
  public Pair<Currency, Currency> getCurrencyPair() {
    return _currencyPair;
  }

  /**
   * Gets the volatility sensitivity (vega) map.
   * @return The sensitivity.
   */
  public Map<DoublesPair, Double> getVega() {
    return _vega;
  }

  /**
   * Return a new volatility sensitivity with all the exposures multiplied by a common factor.
   * @param factor The multiplicative factor.
   * @return The new sensitivity.
   */
  public PresentValueVolatilitySensitivityDataBundle multiply(final double factor) {
    Map<DoublesPair, Double> multiplied = new HashMap<DoublesPair, Double>();    
    for (Map.Entry<DoublesPair, Double> entry : _vega.entrySet()) {
      multiplied.put(entry.getKey(), entry.getValue() * factor);
    }
    return new PresentValueVolatilitySensitivityDataBundle(_currencyPair.getFirst(), _currencyPair.getSecond(), multiplied);
  }

  /**
   * Add the sensitivity at a given (expiry/strike) point. If the point is already present, the sensitivity is added.
   * @param point The expiry/strike point.
   * @param value The sensitivity value (in second/domestic currency).
   * @return A data bundle that has a point added
   */
  public PresentValueVolatilitySensitivityDataBundle add(final DoublesPair point, final double value) {
    Map<DoublesPair, Double> vega = new HashMap<DoublesPair, Double>();
    for (Map.Entry<DoublesPair, Double> entry : _vega.entrySet()) {
      vega.put(entry.getKey(), entry.getValue());
    }
    if (vega.containsKey(point)) {
      vega.put(point, value + vega.get(point));
    } else {
      vega.put(point, value);
    }
    return new PresentValueVolatilitySensitivityDataBundle(_currencyPair.getFirst(), _currencyPair.getSecond(), vega);        
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
    PresentValueVolatilitySensitivityDataBundle other = (PresentValueVolatilitySensitivityDataBundle) obj;
    if (!ObjectUtils.equals(_currencyPair, other._currencyPair)) {
      return false;
    }
    if (!ObjectUtils.equals(_vega, other._vega)) {
      return false;
    }
    return true;
  }

}
