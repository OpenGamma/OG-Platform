/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.money.Currency;

/**
 * Currency-related utilities.
 */
public final class CurrencyUtils {

  private CurrencyUtils() {
  }

  /**
   * Returns the rate required to convert the amounts between the specified currencies using their market
   * convention currency pair.
   * @param currency1 A currency
   * @param currency2 Another currency
   * @param amount1 An amount in {@code currency1}
   * @param amount2 An amount in {@code currency2}
   * @param currencyPairsSource A souce of {@link CurrencyPairs}
   * @param currencyPairsName Name of a set of market convention currency pairs
   * @return The rate to convert between {@code amount1} and {@code amount2} using the market convention currency pair
   * or null if no pair can be found for the two currencies.  This will also return null if {@code currencyPairsSource}
   * doesn't match any {@link CurrencyPairs} in the system.
   */
  public static Double getRate(Currency currency1,
                               Currency currency2,
                               double amount1,
                               double amount2,
                               CurrencyPairsSource currencyPairsSource,
                               String currencyPairsName) {
    ArgumentChecker.notNull(currency1, "currency1");
    ArgumentChecker.notNull(currency2, "currency2");
    if (CompareUtils.closeEquals(amount1, 0) || CompareUtils.closeEquals(amount2, 0)) {
      throw new IllegalArgumentException("Neither amounts can be zero. amount1: " + amount1 + ", amount2: " + amount2);
    }
    CurrencyPair pair = currencyPairsSource.getCurrencyPair(currencyPairsName, currency1, currency2);
    if (pair == null) {
      return null;
    }
    if (pair.getBase().equals(currency1)) {
      return Math.abs(amount2 / amount1);
    } else {
      return Math.abs(amount1 / amount2);
    }
  }

  // TODO should the methods below here move to CurrencyPairs?

  /**
   * Returns the rate required to convert the amounts between the specified currencies using their market
   * convention currency pair.
   * @param currency1 A currency
   * @param currency2 Another currency
   * @param amount1 An amount in {@code currency1}
   * @param amount2 An amount in {@code currency2}
   * @param currencyPairs A set of {@link CurrencyPairs}
   * @return The rate to convert between {@code amount1} and {@code amount2} using the market convention currency pair
   * or null if no pair can be found for the two currencies.  This will also return null if {@code currencyPairsSource}
   * doesn't match any {@link CurrencyPairs} in the system.
   */
  public static Double getRate(Currency currency1,
                               Currency currency2,
                               double amount1,
                               double amount2,
                               CurrencyPairs currencyPairs) {
    ArgumentChecker.notNull(currency1, "currency1");
    ArgumentChecker.notNull(currency2, "currency2");
    ArgumentChecker.notNull(currencyPairs, "currencyPairs");
    if (CompareUtils.closeEquals(amount1, 0) || CompareUtils.closeEquals(amount2, 0)) {
      throw new IllegalArgumentException("Neither amounts can be zero. amount1: " + amount1 + ", amount2: " + amount2);
    }
    CurrencyPair pair = currencyPairs.getCurrencyPair(currency1, currency2);
    if (pair == null) {
      return null;
    }
    if (pair.getBase().equals(currency1)) {
      return Math.abs(amount2 / amount1);
    } else {
      return Math.abs(amount1 / amount2);
    }
  }

  public static Double getBaseAmount(Currency currency1,
                                     Currency currency2,
                                     double amount1,
                                     double amount2,
                                     CurrencyPairs currencyPairs) {
    ArgumentChecker.notNull(currency1, "currency1");
    ArgumentChecker.notNull(currency2, "currency2");
    ArgumentChecker.notNull(currencyPairs, "currencyPairs");
    CurrencyPair pair = currencyPairs.getCurrencyPair(currency1, currency2);
    if (pair == null) {
      return null;
    }
    if (pair.getBase().equals(currency1)) {
      return amount1;
    } else {
      return amount2;
    }
  }

  public static Double getCounterAmount(Currency currency1,
                                        Currency currency2,
                                        double amount1,
                                        double amount2,
                                        CurrencyPairs currencyPairs) {
    ArgumentChecker.notNull(currency1, "currency1");
    ArgumentChecker.notNull(currency2, "currency2");
    ArgumentChecker.notNull(currencyPairs, "currencyPairs");
    CurrencyPair pair = currencyPairs.getCurrencyPair(currency1, currency2);
    if (pair == null) {
      return null;
    }
    if (pair.getCounter().equals(currency1)) {
      return amount1;
    } else {
      return amount2;
    }
  }
}
