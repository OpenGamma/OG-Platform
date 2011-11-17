/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.google.common.collect.ImmutableSet;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.money.Currency;

import java.util.Set;

/**
 * Contains a set of market convention {@link CurrencyPair}s and allows the pair to be looked up for two
 * currencies
 */
public class CurrencyPairs {

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

  /**
   * Returns the rate required to convert the amounts between the specified currencies using their market
   * convention currency pair.  The amounts must have opposite signs.
   * @param currency1 A currency
   * @param amount1 An amount in {@code currency1}
   * @param currency2 Another currency
   * @param amount2 An amount in {@code currency2}
   * @return The rate to convert between {@code amount1} and {@code amount2} using the market convention currency pair
   */
  public Double getRate(Currency currency1, double amount1, Currency currency2, double amount2) {
    ArgumentChecker.notNull(currency1, "currency1");
    ArgumentChecker.notNull(currency2, "currency2");
    if (CompareUtils.closeEquals(amount1, 0) || CompareUtils.closeEquals(amount2, 0)) {
      throw new IllegalArgumentException("Neither amounts can be zero. amount1: " + amount1 + ", amount2: " + amount2);
    }
    if (Math.signum(amount1) == Math.signum(amount2)) {
      throw new IllegalArgumentException("Amounts must have opposite signs. amount1: " + amount1 + ", amount2: " + amount2);
    }
    CurrencyPair pair = getCurrencyPair(currency1, currency2);
    if (pair == null) {
      return null;
    }
    if (pair.getBase().equals(currency1)) {
      return -amount2 / amount1;
    } else {
      return -amount1 / amount2;
    }
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
