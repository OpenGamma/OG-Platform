/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import java.io.Serializable;
import java.util.regex.Pattern;

import org.joda.convert.FromString;
import org.joda.convert.ToString;

import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * Stores a pair of currencies without any implied ordering.
 * <p>
 * This acts like a two element {@code Set}, thus
 * {@code UnorderedCurrencyPair(USD, EUR) == UnorderedCurrencyPair(EUR, USD)}.
 */
public final class UnorderedCurrencyPair implements UniqueIdentifiable, ObjectIdentifiable, Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * The scheme to use in object identifiers.
   */
  public static final String OBJECT_SCHEME = "UnorderedCurrencyPair";

  /**
   * One of the two currencies.
   */
  private Currency _ccy1;
  /**
   * One of the two currencies.
   */
  private Currency _ccy2;
  /**
   * The cached value of the identifier.
   */
  private String _idValue;

  /**
   * Obtains an {@code UnorderedCurrencyPair} from two currencies.
   * 
   * @param ccy1  one of the currencies, not null
   * @param ccy2  one of the currencies, not null
   * @return the pair, not null
   */
  public static UnorderedCurrencyPair of(Currency ccy1, Currency ccy2) {
    return new UnorderedCurrencyPair(ccy1, ccy2);
  }

  /**
   * Extracts an {@code UnorderedCurrencyPair} from a unique identifier.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return the pair, not null
   * @throws IllegalArgumentException if the input is invalid
   */
  public static UnorderedCurrencyPair of(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "unique id");
    if (uniqueId.getScheme().equals(OBJECT_SCHEME)) {
      Pattern validate = Pattern.compile("[A-Z]{6}");
      String value = uniqueId.getValue();
      if (validate.matcher(value).matches()) {
        Currency ccy1 = Currency.of(value.substring(0, 3));
        Currency ccy2 = Currency.of(value.substring(3));
        return new UnorderedCurrencyPair(ccy1, ccy2);
      }
    }
    throw new IllegalArgumentException("Cannot create an UnorderedCurrencyPair from this UniqueId; need an ObjectScheme of UnorderedCurrencyPair, have " + uniqueId.getScheme());
  }

  /**
   * Parses the string to produce a {@code UnorderedCurrencyPair}.
   * <p>
   * This parses the {@code toString} format of '${currency1}${currency2}'
   * where the currencies are in alphabetical order.
   * 
   * @param pairStr  the amount string, not null
   * @return the currency amount
   * @throws IllegalArgumentException if the amount cannot be parsed
   */
  @FromString
  public static UnorderedCurrencyPair parse(final String pairStr) {
    ArgumentChecker.notNull(pairStr, "pairStr");
    if (pairStr.length() != 6) {
      throw new IllegalArgumentException("Unable to parse amount, invalid format: " + pairStr);
    }
    try {
      Currency cur1 = Currency.parse(pairStr.substring(0, 3));
      Currency cur2 = Currency.parse(pairStr.substring(3));
      return new UnorderedCurrencyPair(cur1, cur2);
    } catch (RuntimeException ex) {
      throw new IllegalArgumentException("Unable to parse pair: " + pairStr, ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Constructs a new instance.
   * 
   * @param currency1  one of the currencies, not null
   * @param currency2  one of the currencies, not null
   */
  private UnorderedCurrencyPair(Currency currency1, Currency currency2) {
    ArgumentChecker.notNull(currency1, "currency1");
    ArgumentChecker.notNull(currency2, "currency2");
    if (currency1.getCode().compareTo(currency2.getCode()) <= 0) {
      _ccy1 = currency1;
      _ccy2 = currency2;
      _idValue = currency1.getCode() + currency2.getCode();
    } else {
      _ccy1 = currency2;
      _ccy2 = currency1;
      _idValue = currency2.getCode() + currency1.getCode();
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets one of the two currencies.
   * 
   * @return one of the two currencies, not null
   */
  public Currency getFirstCurrency() {
    return _ccy1;
  }

  /**
   * Gets one of the two currencies.
   * 
   * @return one of the two currencies, not null
   */
  public Currency getSecondCurrency() {
    return _ccy2;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the object identifier for the pair.
   * <p>
   * This uses the scheme {@link #OBJECT_SCHEME UnorderedCurrencyPair}.
   * 
   * @return the object identifier, not null
   */
  @Override
  public ObjectId getObjectId() {
    return ObjectId.of(OBJECT_SCHEME, _idValue);
  }

  /**
   * Gets the unique identifier for the pair.
   * <p>
   * This uses the scheme {@link #OBJECT_SCHEME UnorderedCurrencyPair}.
   * 
   * @return the unique identifier, not null
   */
  @Override
  public UniqueId getUniqueId() {
    return UniqueId.of(OBJECT_SCHEME, _idValue);
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this unordered pair equals another unordered pair.
   * <p>
   * The comparison checks both currencies.
   * 
   * @param obj  the other currency, null returns false
   * @return true if equal
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof UnorderedCurrencyPair) {
      return _idValue.equals(((UnorderedCurrencyPair) obj)._idValue);
    }
    return false;
  }

  /**
   * Returns a suitable hash code for the unordered pair.
   * 
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return _idValue.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unordered pair as a string.
   * <p>
   * This uses the format of '${currency1}${currency2}'
   * where the currencies are in alphabetical order.
   * 
   * @return the unordered pair, not null
   */
  @Override
  @ToString
  public String toString() {
    return _idValue;
  }

}
