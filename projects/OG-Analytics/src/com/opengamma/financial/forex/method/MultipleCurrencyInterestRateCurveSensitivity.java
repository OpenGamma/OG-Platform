/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.util.money.Currency;

/**
 * Class describing a the sensitivity of some value (present value, par rate, etc) to a family of yield curves. 
 * The currency in which the sensitivity is expressed is indicated through a map.
 */
public final class MultipleCurrencyInterestRateCurveSensitivity {

  /**
   * The backing map for the sensitivities in the different currencies. Not null.
   * The amount in the different currencies are not conversion of each other, they should be understood in an additive way.
   */
  private final TreeMap<Currency, InterestRateCurveSensitivity> _sensitivity;

  /**
   * Private constructor from an exiting map.
   * @param sensitivity The sensitivity map.
   */
  private MultipleCurrencyInterestRateCurveSensitivity(TreeMap<Currency, InterestRateCurveSensitivity> sensitivity) {
    _sensitivity = sensitivity;
  }

  /**
   * Create a new multiple currency sensitivity with one currency.
   * @param ccy The currency. Not null.
   * @param sensitivity The sensitivity associated to the currency. Not null.
   * @return The multiple currency sensitivity.
   */
  public static MultipleCurrencyInterestRateCurveSensitivity of(final Currency ccy, final InterestRateCurveSensitivity sensitivity) {
    Validate.notNull(ccy, "Currency");
    Validate.notNull(sensitivity, "Sensitivity");
    TreeMap<Currency, InterestRateCurveSensitivity> map = new TreeMap<Currency, InterestRateCurveSensitivity>();
    map.put(ccy, sensitivity);
    return new MultipleCurrencyInterestRateCurveSensitivity(map);
  }

  /**
   * Returns the (single currency) interest rate sensitivity associated to a given currency.
   * If the currency is not present in the map, an empty InterestRateCurveSensitivity is returned.
   * @param ccy The currency. Not null.
   * @return The (single currency) interest rate sensitivity.
   */
  public InterestRateCurveSensitivity getSensitivity(final Currency ccy) {
    Validate.notNull(ccy, "Currency");
    if (_sensitivity.containsKey(ccy)) {
      return _sensitivity.get(ccy);
    } else {
      return new InterestRateCurveSensitivity();
    }
  }

  /**
   * Create a new multiple currency sensitivity by adding the sensitivity associated to a given currency. 
   * If the currency is not yet present in the existing sensitivity a new map is created with the extra entry.
   * If the currency is already present, the associated sensitivities are added (in the sense of {@link InterestRateCurveSensitivity}) and a new map is created with all the other
   * existing entries and the entry with the currency and the sum sensitivity.
   * @param ccy The currency. Not null.
   * @param sensitivity The sensitivity associated to the currency. Not null.
   * @return The new multiple currency sensitivity.
   */
  public MultipleCurrencyInterestRateCurveSensitivity plus(final Currency ccy, final InterestRateCurveSensitivity sensitivity) {
    Validate.notNull(ccy, "Currency");
    Validate.notNull(sensitivity, "Sensitivity");
    TreeMap<Currency, InterestRateCurveSensitivity> map = new TreeMap<Currency, InterestRateCurveSensitivity>();
    if (_sensitivity.containsKey(ccy)) {
      map.put(ccy, sensitivity.add(_sensitivity.get(ccy)));
      for (Currency loopccy : _sensitivity.keySet()) {
        if (loopccy != ccy) {
          map.put(loopccy, _sensitivity.get(loopccy));
        }
      }
    } else {
      map.putAll(_sensitivity);
      map.put(ccy, sensitivity);
    }
    return new MultipleCurrencyInterestRateCurveSensitivity(map);
  }

  /**
   * Create a new multiple currency sensitivity by adding another multiple currency sensitivity. 
   * For each currency in the other multiple currency sensitivity, the currency and its associated sensitivity are added.
   * @param other The multiple currency sensitivity. Not null.
   * @return The new multiple currency sensitivity.
   */
  public MultipleCurrencyInterestRateCurveSensitivity plus(final MultipleCurrencyInterestRateCurveSensitivity other) {
    Validate.notNull(other, "Sensitivity");
    TreeMap<Currency, InterestRateCurveSensitivity> map = new TreeMap<Currency, InterestRateCurveSensitivity>();
    map.putAll(_sensitivity);
    MultipleCurrencyInterestRateCurveSensitivity result = new MultipleCurrencyInterestRateCurveSensitivity(map);
    for (Currency loopccy : other._sensitivity.keySet()) {
      result.plus(loopccy, other.getSensitivity(loopccy));
    }
    return result;
  }

  /**
   * Returns a new multiple currency sensitivity by creating clean sensitivity for each currency (see {@link InterestRateCurveSensitivity} clean() method).
   * @return The cleaned sensitivity.
   */
  public MultipleCurrencyInterestRateCurveSensitivity clean() {
    TreeMap<Currency, InterestRateCurveSensitivity> map = new TreeMap<Currency, InterestRateCurveSensitivity>();
    for (Currency loopccy : _sensitivity.keySet()) {
      map.put(loopccy, _sensitivity.get(loopccy).clean());
    }
    MultipleCurrencyInterestRateCurveSensitivity result = new MultipleCurrencyInterestRateCurveSensitivity(map);
    return result;
  }

  /**
   * Returns the set of currencies in the multiple currency sensitivities.
   * @return The set of currencies.
   */
  public Set<Currency> getCurrencies() {
    return _sensitivity.keySet();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _sensitivity.hashCode();
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
    MultipleCurrencyInterestRateCurveSensitivity other = (MultipleCurrencyInterestRateCurveSensitivity) obj;
    if (!ObjectUtils.equals(_sensitivity, other._sensitivity)) {
      return false;
    }
    return true;
  }

}
