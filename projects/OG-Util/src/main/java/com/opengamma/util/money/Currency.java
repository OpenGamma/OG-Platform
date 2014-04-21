/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import java.io.Serializable;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.joda.convert.FromString;
import org.joda.convert.ToString;

import com.google.common.collect.ImmutableSet;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * A unit of currency.
 * <p>
 * This class represents a unit of currency such as the British Pound, Euro or US Dollar.
 * <p>
 * This class is immutable and thread-safe.
 */
@PublicAPI
public final class Currency implements ObjectIdentifiable, UniqueIdentifiable, Comparable<Currency>, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * A cache of instances.
   */
  private static final ConcurrentMap<String, Currency> s_instanceMap = new ConcurrentHashMap<String, Currency>();
  /**
   * The scheme to use in object identifiers.
   */
  public static final String OBJECT_SCHEME = "CurrencyISO";

  // a selection of commonly traded, stable currencies
  /**
   * The currency 'USD' - United States Dollar.
   */
  public static final Currency USD = of("USD");
  /**
   * The currency 'EUR' - Euro.
   */
  public static final Currency EUR = of("EUR");
  /**
   * The currency 'JPY' - Japanese Yen.
   */
  public static final Currency JPY = of("JPY");
  /**
   * The currency 'GBP' - British pound.
   */
  public static final Currency GBP = of("GBP");
  /**
   * The currency 'EUR' - Swiss Franc.
   */
  public static final Currency CHF = of("CHF");
  /**
   * The currency 'AUD' - Australian Dollar.
   */
  public static final Currency AUD = of("AUD");
  /**
   * The currency 'CAD' - Canadian Dollar.
   */
  public static final Currency CAD = of("CAD");

  // a selection of other currencies
  /**
   * The currency 'NZD' - New Zealand Dollar.
   */
  public static final Currency NZD = of("NZD");
  /**
   * The currency 'DKK' - Danish Krone
   */
  public static final Currency DKK = of("DKK");
  /**
   * The currency 'DEM' - Deutsche Mark
   */
  public static final Currency DEM = of("DEM");
  /**
   * The currency 'CZK' - Czeck Krona
   */
  public static final Currency CZK = of("CZK");
  /**
   * The currency 'SEK' - Swedish Krona
   */
  public static final Currency SEK = of("SEK");
  /**
   * The currency 'SKK' - Slovak Korona
   */
  public static final Currency SKK = of("SKK"); 
  /**
   * The currency 'ITL' - Italian Lira
   */
  public static final Currency ITL = of("ITL");
  /**
   * The currency 'HUF' = Hugarian Forint
   */
  public static final Currency HUF = of("HUF");
  /**
   * The currency 'FRF' - French Franc
   */
  public static final Currency FRF = of("FRF");
  /**
   * The currency 'NOK' - Norwegian Krone 
   */
  public static final Currency NOK = of("NOK");
  /**
   * The currency 'HKD' - Hong Kong Dollar
   */
  public static final Currency HKD = of("HKD");
  /**
   * The currency 'BRL' - Brazil Dollar
   */
  public static final Currency BRL = of("BRL");
  /**
   * The currency 'ZAR' - South African Rand
   */
  public static final Currency ZAR = of("ZAR");
  /**
   * The currency 'PLN' - Polish Zloty
   */
  public static final Currency PLN = of("PLN");
  /**
   * The currency 'SGD' - Singapore Dollar
   */
  public static final Currency SGD = of("SGD");
  /**
   * The currency 'MXN' - Mexican Peso
   */
  public static final Currency MXN = of("MXN");

  /**
   * The currency code, not null.
   */
  private final String _code;

  //-----------------------------------------------------------------------
  /**
   * Lists the available currencies.
   * 
   * @return an immutable set containing all registered currencies, not null
   */
  public static Set<Currency> getAvailableCurrencies() {
    return ImmutableSet.copyOf(s_instanceMap.values());
  }

  //-----------------------------------------------------------------------
  /**
   * Obtains an instance of {@code Currency} matching the specified JDK currency.
   * <p>
   * This converts the JDK currency instance to a currency unit using the code.
   *
   * @param currency  the currency, not null
   * @return the singleton instance, not null
   */
  public static Currency of(java.util.Currency currency) {
    ArgumentChecker.notNull(currency, "currency");
    return of(currency.getCurrencyCode());
  }

  /**
   * Obtains an instance of {@code Currency} for the specified ISO-4217
   * three letter currency code dynamically creating a currency if necessary.
   * <p>
   * A currency is uniquely identified by ISO-4217 three letter code.
   * This method creates the currency if it is not known.
   *
   * @param currencyCode  the three letter currency code, ASCII and upper case, not null
   * @return the singleton instance, not null
   * @throws IllegalArgumentException if the currency code is not three letters
   */
  @FromString
  public static Currency of(String currencyCode) {
    ArgumentChecker.notNull(currencyCode, "currencyCode");
    // check cache before matching
    Currency previous = s_instanceMap.get(currencyCode);
    if (previous != null) {
      return previous;
    }
    if (currencyCode.matches("[A-Z][A-Z][A-Z]") == false) {
      throw new IllegalArgumentException("Invalid currency code: " + currencyCode);
    }
    s_instanceMap.putIfAbsent(currencyCode, new Currency(currencyCode));
    return s_instanceMap.get(currencyCode);
  }

  /**
   * Parses a string to obtain a {@code Currency}.
   * <p>
   * The parse is identical to {@link #of(String)} except that it will convert
   * letters to upper case first.
   *
   * @param currencyCode  the three letter currency code, ASCII, not null
   * @return the singleton instance, not null
   * @throws IllegalArgumentException if the currency code is not three letters
   */
  public static Currency parse(String currencyCode) {
    ArgumentChecker.notNull(currencyCode, "currencyCode");
    return of(currencyCode.toUpperCase(Locale.ENGLISH));
  }

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor.
   * 
   * @param currencyCode  the three letter currency code, not null
   */
  private Currency(String currencyCode) {
    _code = currencyCode;
  }

  /**
   * Ensure singleton on deserialization.
   * 
   * @return the singleton, not null
   */
  private Object readResolve() {
    return of(_code);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the three letter ISO code.
   * 
   * @return the three letter ISO code, not null
   */
  @ToString
  public String getCode() {
    return _code;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the object identifier for the currency.
   * <p>
   * This uses the scheme {@link #OBJECT_SCHEME CurrencyISO}.
   * 
   * @return the object identifier, not null
   */
  @Override
  public ObjectId getObjectId() {
    return ObjectId.of(OBJECT_SCHEME, _code);
  }

  /**
   * Gets the unique identifier for the currency.
   * <p>
   * This uses the scheme {@link #OBJECT_SCHEME CurrencyISO}.
   * 
   * @return the unique identifier, not null
   */
  @Override
  public UniqueId getUniqueId() {
    return UniqueId.of(OBJECT_SCHEME, _code);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the JDK currency instance equivalent to this currency.
   * <p>
   * This attempts to convert a {@code Currency} to a JDK {@code Currency}.
   * 
   * @return the JDK currency instance, not null
   * @throws IllegalArgumentException if no matching currency exists in the JDK
   */
  public java.util.Currency toCurrency() {
    return java.util.Currency.getInstance(_code);
  }

  //-----------------------------------------------------------------------
  /**
   * Compares this currency to another by alphabetical comparison of the code.
   * 
   * @param other  the other currency, not null
   * @return negative if earlier alphabetically, 0 if equal, positive if greater alphabetically
   */
  @Override
  public int compareTo(Currency other) {
    return _code.compareTo(other._code);
  }

  /**
   * Checks if this currency equals another currency.
   * <p>
   * The comparison checks the three letter currency code.
   * 
   * @param obj  the other currency, null returns false
   * @return true if equal
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof Currency) {
      return _code.equals(((Currency) obj)._code);
    }
    return false;
  }

  /**
   * Returns a suitable hash code for the currency.
   * 
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return _code.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the three letter currency code as a string.
   * 
   * @return the three letter currency code, not null
   */
  @Override
  public String toString() {
    return _code;
  }

}
