/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * An ordered pair of currencies for quoting rates in FX deals.
 */
public class CurrencyPair {

  /** The first currency in the pair */
  private final Currency _base;
  /** The second currency in the pair */
  private final Currency _counter;

  private CurrencyPair(Currency base, Currency counter) {
    ArgumentChecker.notNull(base, "base");
    ArgumentChecker.notNull(counter, "counter");
    if (base.equals(counter)) {
      throw new IllegalArgumentException("A currency pair cannot have the same base and counter currency (" + base + ")");
    }
    _base = base;
    _counter = counter;
  }

  /**
   * @param base The base currency
   * @param counter The counter currency
   * @return A new currency pair with a base currency of {@code base} and counter currency of {@code counter}
   */
  public static CurrencyPair of(Currency base, Currency counter) {
    return new CurrencyPair(base, counter);
  }

  /**
   * Creates a currency pair from a string with format AAA/BBB.
   * @param pair A currency pair as a string AAA/BBB
   * @return The currency pair
   */
  public static CurrencyPair of(String pair) {
    if (pair.length() != 7) {
      throw new IllegalArgumentException("Currency pair format must be AAA/BBB");
    }
    String base = pair.substring(0, 3);
    String counter = pair.substring(4);
    return new CurrencyPair(Currency.of(base), Currency.of(counter));
  }

  /**
   * @return Base currency code / Counter currency code
   */
  public String getName() {
    return _base.getCode() + "/" + _counter.getCode();
  }

  // getName(String separator) ?

  @Override
  public String toString() {
    return "CurrencyPair{" + getName() + "}";
  }

  /**
   * @return A new currency pair with the same currencies as this pair but in the reverse order
   */
  public CurrencyPair inverse() {
    return new CurrencyPair(_counter, _base);
  }

  /**
   * @return This pair's base currency
   */
  public Currency getBase() {
    return _base;
  }

  /**
   * @return This pair's counter currency
   */
  public Currency getCounter() {
    return _counter;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CurrencyPair other = (CurrencyPair) o;
    return _base.equals(other._base) && _counter.equals(other._counter);
  }

  @Override
  public int hashCode() {
    int result = _base.hashCode();
    result = 31 * result + _counter.hashCode();
    return result;
  }
}
