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
 * A set of market convention currency pairs.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class CurrencyPairs {

  // TODO not sure about hard-coding the name of the default set of currency pairs
  /**
   * The default market convention name.
   */
  public static final String DEFAULT_CURRENCY_PAIRS = "DEFAULT";

  /**
   * The pairs.
   */
  private final Set<CurrencyPair> _pairs;

  /**
   * Obtains an instance based on a set of pairs.
   * 
   * @param pairs  the pairs, not null
   * @return the currency pairs instance, not null
   */
  public static CurrencyPairs of(Set<CurrencyPair> pairs) {
    return new CurrencyPairs(pairs);
  }

  /**
   * Creates an instance based on a set of pairs.
   * 
   * @param pairs  the pairs, not null
   */
  private CurrencyPairs(Set<CurrencyPair> pairs) {
    ArgumentChecker.notNull(pairs, "pairs");
    _pairs = ImmutableSet.copyOf(pairs);
  }

  //-------------------------------------------------------------------------
  /**
   * Finds the currency pair instance for the two currencies.
   * 
   * @param currency1  the first currency, not null
   * @param currency2  the second currency, not null
   * @return the market convention currency pair for the two currencies, null if not found
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
   * The immutable set of pairs.
   * 
   * @return the immutable pairs set, not null
   */
  /* package */ Set<CurrencyPair> getPairs() {
    return _pairs;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof CurrencyPairs) {
      CurrencyPairs other = (CurrencyPairs) obj;
      return _pairs.equals(other._pairs);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _pairs.hashCode();
  }

  @Override
  public String toString() {
    return "CurrencyPairs[" + _pairs + "]";
  }

}
