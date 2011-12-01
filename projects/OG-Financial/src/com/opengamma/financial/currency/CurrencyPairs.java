/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Contains a set of market convention {@link CurrencyPair}s and allows the pair to be looked up for two
 * currencies
 */
public class CurrencyPairs {

  // TODO not sure about hard-coding the name of the default set of currency pairs
  public static final String DEFAULT_CURRENCY_PAIRS = "DEFAULT";

  private final Set<CurrencyPair> _pairs;

  public CurrencyPairs(Set<CurrencyPair> pairs) {
    ArgumentChecker.notNull(pairs, "pairs");
    _pairs = ImmutableSet.copyOf(pairs);
  }

  /**
   * @param currency1 A currency
   * @param currency2 Another currency
   * @return The market convention currency pair for the two currencies
   */
  public CurrencyPair getCurrencyPair(Currency currency1, Currency currency2) {
    ArgumentChecker.notNull(currency1, "currency1");
    ArgumentChecker.notNull(currency2, "currency2");
    CurrencyPair pair = CurrencyPair.of(currency1, currency2);
    if (_pairs.contains(pair)) {
      return pair;
    }
    CurrencyPair inverse = pair.inverse();
    if (_pairs.contains(inverse)) {
      return inverse;
    }
    return null;
  }

  /* package */ Set<CurrencyPair> getPairs() {
    return _pairs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CurrencyPairs that = (CurrencyPairs) o;
    return _pairs.equals(that._pairs);
  }

  @Override
  public int hashCode() {
    return _pairs.hashCode();
  }

  @Override
  public String toString() {
    return "CurrencyPairs{" + _pairs + "}";
  }
}
