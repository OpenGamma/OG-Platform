/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import org.joda.convert.FromString;
import org.joda.convert.ToString;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.PrimitiveComputationTargetType;
import com.opengamma.engine.target.resolver.AbstractPrimitiveResolver;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * An ordered pair of currencies for quoting rates in FX deals.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class CurrencyPair implements UniqueIdentifiable {

  private static final String OBJECT_SCHEME = CurrencyPair.class.getSimpleName();

  /**
   * An OG-Engine type so an instance can be used as a target in a dependency graph.
   */
  public static final PrimitiveComputationTargetType<CurrencyPair> TYPE = PrimitiveComputationTargetType.of(ComputationTargetType.of(CurrencyPair.class), CurrencyPair.class,
      new AbstractPrimitiveResolver<CurrencyPair>(OBJECT_SCHEME) {

        @Override
        protected CurrencyPair resolveObject(final String identifier) {
          return parse(identifier);
        }

      });

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
   * @param base the base currency, not null
   * @param counter the counter currency, not null
   * @return the currency pair, not null
   */
  public static CurrencyPair of(Currency base, Currency counter) {
    return new CurrencyPair(base, counter);
  }

  /**
   * Parses a currency pair from a string with format AAA/BBB.
   * <p>
   * The parsed format is '${baseCurrency}/${counterCurrency}'.
   * 
   * @param pairStr the currency pair as a string AAA/BBB, not null
   * @return the currency pair, not null
   */
  @FromString
  public static CurrencyPair parse(String pairStr) {
    ArgumentChecker.notNull(pairStr, "pairStr");
    if (pairStr.length() != 7) {
      throw new IllegalArgumentException("Currency pair format must be AAA/BBB");
    }
    Currency base = Currency.of(pairStr.substring(0, 3));
    Currency counter = Currency.of(pairStr.substring(4));
    return new CurrencyPair(base, counter);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param base the base currency, not null
   * @param counter the counter currency, not null
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

  /**
   * Indicates if the currency pair contains the supplied currency as either its base or counter.
   * 
   * @param currency the currency to check against the pair
   * @return true if the currency is either the base or counter currency in the pair.
   */
  public boolean contains(Currency currency) {
    return _base.equals(currency) || _counter.equals(currency);
  }

  /**
   * Return the pair's complementing currency for the supplied currency. i.e. if the supplied currency is the pair's base, then the counter currency is returned.
   * 
   * @param currency the currency to find the complement for
   * @return the complementing currency
   * @throws IllegalArgumentException if the supplied currency is not a member of the pair
   */
  public Currency getComplement(Currency currency) {

    if (_base.equals(currency)) {
      return _counter;
    } else if (_counter.equals(currency)) {
      return _base;
    } else {
      throw new IllegalArgumentException("Currency [" + currency + "] is not a member of " + this.toString());
    }
  }

  @Override
  public UniqueId getUniqueId() {
    return UniqueId.of(OBJECT_SCHEME, getName());
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
  /**
   * Returns the formatted string version of the currency pair.
   * <p>
   * The format is '${baseCurrency}/${counterCurrency}'.
   * 
   * @return the formatted string, not null
   */
  @Override
  @ToString
  public String toString() {
    return getName();
  }

}
