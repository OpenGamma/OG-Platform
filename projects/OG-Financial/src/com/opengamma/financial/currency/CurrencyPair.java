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
 * <p>
 * This class is immutable and thread-safe.
 */
public final class CurrencyPair {

  /**
   * The first currency in the pair.
   */
  private final Currency _base;
  /**
   * The second currency in the pair.
   */
  private final Currency _counter;

  //-------------------------------------------------------------------------
  /**
   * Obtains a currency pair from a string with format AAA/BBB.
   * 
   * @param base  the base currency, not null
   * @param counter  the counter currency, not null
   * @return the currency pair, not null
   */
  public static CurrencyPair of(Currency base, Currency counter) {
    return new CurrencyPair(base, counter);
  }

  /**
   * Parses a currency pair from a string with format AAA/BBB.
   * 
   * @param pair  the currency pair as a string AAA/BBB, not null
   * @return the currency pair, not null
   */
  public static CurrencyPair parse(String pair) {
    if (pair.length() != 7) {
      throw new IllegalArgumentException("Currency pair format must be AAA/BBB");
    }
    String base = pair.substring(0, 3);
    String counter = pair.substring(4);
    return new CurrencyPair(Currency.of(base), Currency.of(counter));
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param base  the base currency, not null
   * @param counter  the counter currency, not null
   */
  private CurrencyPair(Currency base, Currency counter) {
    ArgumentChecker.notNull(base, "base");
    ArgumentChecker.notNull(counter, "counter");
    if (base.equals(counter)) {
      throw new IllegalArgumentException("A currency pair cannot have the same base and counter currency (" + base + ")");
    }
    _base = base;
    _counter = counter;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the base currency.
   * 
   * @return the base currency of this pair, not null
   */
  public Currency getBase() {
    return _base;
  }

  /**
   * Gets the counter currency.
   * 
   * @return the counter currency of this pair, not null
   */
  public Currency getCounter() {
    return _counter;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name of the pair, formed from the two currencies.
   * 
   * @return Base currency code / Counter currency code, not null
   */
  public String getName() {
    return _base.getCode() + "/" + _counter.getCode();
  }

  /**
   * Gets the inverse currency pair.
   * <p>
   * The inverse pair has the same currencies but in reverse order.
   * 
   * @return the inverse pair, not null
   */
  public CurrencyPair inverse() {
    return new CurrencyPair(_counter, _base);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof CurrencyPair) {
      CurrencyPair other = (CurrencyPair) obj;
      return _base.equals(other._base) && _counter.equals(other._counter);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = _base.hashCode();
    result = 31 * result + _counter.hashCode();
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "CurrencyPair[" + getName() + "]";
  }

}
