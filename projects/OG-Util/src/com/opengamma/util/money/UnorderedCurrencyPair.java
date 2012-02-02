/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.money;

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
public final class UnorderedCurrencyPair implements UniqueIdentifiable, ObjectIdentifiable {

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
   * 
   * @return the unordered pair, not null
   */
  @Override
  public String toString() {
    return _idValue;
  }

}
