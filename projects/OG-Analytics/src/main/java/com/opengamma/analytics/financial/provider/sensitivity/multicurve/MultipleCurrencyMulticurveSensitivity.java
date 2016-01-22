/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.multicurve;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a the sensitivity of some value (present value, par rate, etc) to a family of yield curves.
 * The currency in which the sensitivity is expressed is indicated through a map.
 */
public class MultipleCurrencyMulticurveSensitivity implements Serializable {

  /**
   * The backing map for the sensitivities in the different currencies. Not null.
   * The amount in the different currencies are not conversion of each other, they should be understood in an additive way.
   */
  private final TreeMap<Currency, MulticurveSensitivity> _sensitivity;

  /**
   * Constructor. A new map is created.
   */
  public MultipleCurrencyMulticurveSensitivity() {
    _sensitivity = new TreeMap<>();
  }

  /**
   * Private constructor from an exiting map.
   * @param sensitivity The sensitivity map.
   */
  private MultipleCurrencyMulticurveSensitivity(final TreeMap<Currency, MulticurveSensitivity> sensitivity) {
    _sensitivity = sensitivity;
  }

  /**
   * Create a new multiple currency sensitivity with one currency.
   * @param ccy The currency. Not null.
   * @param sensitivity The sensitivity associated to the currency. Not null.
   * @return The multiple currency sensitivity.
   */
  public static MultipleCurrencyMulticurveSensitivity of(final Currency ccy, final MulticurveSensitivity sensitivity) {
    ArgumentChecker.notNull(ccy, "Currency");
    ArgumentChecker.notNull(sensitivity, "Sensitivity");
    final TreeMap<Currency, MulticurveSensitivity> map = new TreeMap<>();
    map.put(ccy, sensitivity);
    return new MultipleCurrencyMulticurveSensitivity(map);
  }

  /**
   * Returns the (single currency) interest rate sensitivity associated to a given currency.
   * If the currency is not present in the map, an empty InterestRateCurveSensitivity is returned.
   * @param ccy The currency. Not null.
   * @return The (single currency) interest rate sensitivity.
   */
  public MulticurveSensitivity getSensitivity(final Currency ccy) {
    ArgumentChecker.notNull(ccy, "Currency");
    if (_sensitivity.containsKey(ccy)) {
      return _sensitivity.get(ccy);
    }
    return new MulticurveSensitivity();
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
  public MultipleCurrencyMulticurveSensitivity plus(final Currency ccy, final MulticurveSensitivity sensitivity) {
    ArgumentChecker.notNull(ccy, "Currency");
    ArgumentChecker.notNull(sensitivity, "Sensitivity");
    final TreeMap<Currency, MulticurveSensitivity> map = new TreeMap<>();
    if (_sensitivity.containsKey(ccy)) {
      map.put(ccy, sensitivity.plus(_sensitivity.get(ccy)));
      for (final Currency loopccy : _sensitivity.keySet()) {
        if (loopccy != ccy) {
          map.put(loopccy, _sensitivity.get(loopccy));
        }
      }
    } else {
      map.putAll(_sensitivity);
      map.put(ccy, sensitivity);
    }
    return new MultipleCurrencyMulticurveSensitivity(map);
  }

  /**
   * Create a new multiple currency sensitivity by adding another multiple currency sensitivity.
   * For each currency in the other multiple currency sensitivity, the currency and its associated sensitivity are added.
   * @param other The multiple currency sensitivity. Not null.
   * @return The new multiple currency sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity plus(final MultipleCurrencyMulticurveSensitivity other) {
    ArgumentChecker.notNull(other, "Sensitivity");
    final TreeMap<Currency, MulticurveSensitivity> map = new TreeMap<>();
    map.putAll(_sensitivity);
    MultipleCurrencyMulticurveSensitivity result = new MultipleCurrencyMulticurveSensitivity(map);
    for (final Currency loopccy : other._sensitivity.keySet()) {
      result = result.plus(loopccy, other.getSensitivity(loopccy));
    }
    return result;
  }

  /**
   * Create a new multiple currency sensitivity by multiplying all the sensitivities in a multiple currency sensitivity by a common factor.
   * @param factor The multiplicative factor.
   * @return The new multiple currency sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity multipliedBy(final double factor) {
    final TreeMap<Currency, MulticurveSensitivity> map = new TreeMap<>();
    for (final Currency loopccy : _sensitivity.keySet()) {
      map.put(loopccy, _sensitivity.get(loopccy).multipliedBy(factor));
    }
    return new MultipleCurrencyMulticurveSensitivity(map);
  }

  /**
   * Returns a new multiple currency sensitivity by creating clean sensitivity for each currency (see {@link InterestRateCurveSensitivity} clean() method).
   * @return The cleaned sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity cleaned() {
    final TreeMap<Currency, MulticurveSensitivity> map = new TreeMap<>();
    for (final Currency loopccy : _sensitivity.keySet()) {
      map.put(loopccy, _sensitivity.get(loopccy).cleaned());
    }
    final MultipleCurrencyMulticurveSensitivity result = new MultipleCurrencyMulticurveSensitivity(map);
    return result;
  }

  /**
   * Returns a new multiple currency sensitivity by creating clean sensitivity for each currency (see {@link InterestRateCurveSensitivity} clean() method).
   * The total value below the tolerance threshold are removed.
   * @param tolerance The tolerance.
   * @return The cleaned sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity cleaned(final double tolerance) {
    final TreeMap<Currency, MulticurveSensitivity> map = new TreeMap<>();
    for (final Currency loopccy : _sensitivity.keySet()) {
      map.put(loopccy, _sensitivity.get(loopccy).cleaned(tolerance));
    }
    final MultipleCurrencyMulticurveSensitivity result = new MultipleCurrencyMulticurveSensitivity(map);
    return result;
  }

  /**
   * Returns the set of currencies in the multiple currency sensitivities.
   * @return The set of currencies.
   */
  public Set<Currency> getCurrencies() {
    return _sensitivity.keySet();
  }

  /**
   * Create a new sensitivity which is the conversion of the multiple currency sensitivity to the sensitivity in a given currency.
   * @param ccy The currency in which the sensitivities should be converted.
   * @param fx The matrix with the exchange rates.
   * @return The one currency sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity converted(final Currency ccy, final FXMatrix fx) {
    MulticurveSensitivity sensi = new MulticurveSensitivity();
    for (final Currency c : _sensitivity.keySet()) {
      final double rate = fx.getFxRate(c, ccy);
      sensi = sensi.plus(_sensitivity.get(c).multipliedBy(rate));
    }
    return of(ccy, sensi);
  }

  /**
   * Gets the sensitivities for all currencies.
   * @return The sensitivities
   */
  public Map<Currency, MulticurveSensitivity> getSensitivities() {
    return _sensitivity;
  }

  @Override
  public String toString() {
    return _sensitivity.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _sensitivity.hashCode();
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
    final MultipleCurrencyMulticurveSensitivity other = (MultipleCurrencyMulticurveSensitivity) obj;
    if (!ObjectUtils.equals(_sensitivity, other._sensitivity)) {
      return false;
    }
    return true;
  }

}
