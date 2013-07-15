/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.i18n;

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
 * A country as represented by an ISO-3166 style code.
 * <p>
 * This class represents country or territory as defined by ISO-3166.
 * Additional codes may be added if necessary.
 * <p>
 * This class is immutable and thread-safe.
 */
@PublicAPI
public final class Country implements ObjectIdentifiable, UniqueIdentifiable, Comparable<Country>, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;
  /**
   * A cache of instances.
   */
  private static final ConcurrentMap<String, Country> s_instanceMap = new ConcurrentHashMap<>(16, 0.75f, 4);
  /**
   * The scheme to use in object identifiers.
   */
  public static final String OBJECT_SCHEME = "CountryISO";

  // Europe
  /**
   * The region of 'EU' - Europe (special status in ISO-3166).
   */
  public static final Country EU = of("EU");
  /**
   * The country 'BE' - Belgium.
   */
  public static final Country BE = of("BE");
  /**
   * The country 'CH' - Switzerland.
   */
  public static final Country CH = of("CH");
  /**
   * The currency 'CZ' - Czech Republic.
   */
  public static final Country CZ = of("CZ");
  /**
   * The country 'DE' - Germany.
   */
  public static final Country DE = of("DE");
  /**
   * The country 'DK' - Denmark.
   */
  public static final Country DK = of("DK");
  /**
   * The currency 'ES' - Spain.
   */
  public static final Country ES = of("ES");
  /**
   * The currency 'FI' - Finland.
   */
  public static final Country FI = of("FI");
  /**
   * The currency 'FR' - France.
   */
  public static final Country FR = of("FR");
  /**
   * The country 'GB' - United Kingdom.
   */
  public static final Country GB = of("GB");
  /**
   * The country 'GR' - Greece.
   */
  public static final Country GR = of("GR");
  /**
   * The currency 'HU' = Hungary.
   */
  public static final Country HU = of("HU");
  /**
   * The currency 'IT' - Italy.
   */
  public static final Country IT = of("IT");
  /**
   * The currency 'LU' - Luxembourg.
   */
  public static final Country LU = of("LU");
  /**
   * The currency 'NL' - Netherlands.
   */
  public static final Country NL = of("NL");
  /**
   * The currency 'NO' - Norway.
   */
  public static final Country NO = of("NO");
  /**
   * The currency 'SK' - Slovakia.
   */
  public static final Country SK = of("SK");
  /**
   * The currency 'PL' = Poland.
   */
  public static final Country PL = of("PL");
  /**
   * The currency 'PT' - Portugal.
   */
  public static final Country PT = of("PT");
  /**
   * The currency 'RU' = Russia.
   */
  public static final Country RU = of("RU");
  /**
   * The currency 'SE' - Sweden.
   */
  public static final Country SE = of("SE");

  // Americas
  /**
   * The country 'AR' - Argentina.
   */
  public static final Country AR = of("AR");
  /**
   * The country 'BR' - Brazil.
   */
  public static final Country BR = of("BR");
  /**
   * The country 'CA' - Canada.
   */
  public static final Country CA = of("CA");
  /**
   * The country 'CL' - Chile.
   */
  public static final Country CL = of("CL");
  /**
   * The country 'MX' - Mexico.
   */
  public static final Country MX = of("MX");
  /**
   * The country 'US' - United States.
   */
  public static final Country US = of("US");

  // Asia-Pacific
  /**
   * The country 'AU' - Australia.
   */
  public static final Country AU = of("AU");
  /**
   * The country 'CN' - China.
   */
  public static final Country CN = of("CN");
  /**
   * The currency 'HK' - Hong Kong.
   */
  public static final Country HK = of("HK");
  /**
   * The country 'IN' - India.
   */
  public static final Country IN = of("IN");
  /**
   * The country 'JP' - Japan.
   */
  public static final Country JP = of("JP");
  /**
   * The country 'NZ' - New Zealand.
   */
  public static final Country NZ = of("NZ");
  /**
   * The country 'TH' - Thailand.
   */
  public static final Country TH = of("TH");

  /**
   * The country code, not null.
   */
  private final String _code;

  //-----------------------------------------------------------------------
  /**
   * Lists the available countries.
   * 
   * @return an immutable set containing all registered countries, not null
   */
  public static Set<Country> getAvailableCountries() {
    return ImmutableSet.copyOf(s_instanceMap.values());
  }

  //-----------------------------------------------------------------------
  /**
   * Obtains an instance of {@code Country} for the specified ISO-3166
   * three letter currency code dynamically creating an instance if necessary.
   * <p>
   * A country is uniquely identified by ISO-3166 two letter code.
   * This method creates the country object if it is not known.
   *
   * @param countryCode  the two letter country code, ASCII and upper case, not null
   * @return the singleton instance, not null
   * @throws IllegalArgumentException if the country code is not two letters
   */
  @FromString
  public static Country of(String countryCode) {
    ArgumentChecker.notNull(countryCode, "countryCode");
    if (countryCode.matches("[A-Z][A-Z]") == false) {
      throw new IllegalArgumentException("Invalid country code: " + countryCode);
    }
    s_instanceMap.putIfAbsent(countryCode, new Country(countryCode));
    return s_instanceMap.get(countryCode);
  }

  /**
   * Parses a string to obtain a {@code Country}.
   * <p>
   * The parse is identical to {@link #of(String)} except that it will convert
   * letters to upper case first.
   *
   * @param countryCode  the two letter currency code, ASCII, not null
   * @return the singleton instance, not null
   * @throws IllegalArgumentException if the country code is not three letters
   */
  public static Country parse(String countryCode) {
    ArgumentChecker.notNull(countryCode, "countryCode");
    return of(countryCode.toUpperCase(Locale.ENGLISH));
  }

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor.
   * 
   * @param countryCode  the two letter country code, not null
   */
  private Country(String countryCode) {
    _code = countryCode;
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
   * Gets the two letter ISO code.
   * 
   * @return the two letter ISO code, not null
   */
  @ToString
  public String getCode() {
    return _code;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the object identifier for the country.
   * <p>
   * This uses the scheme {@link #OBJECT_SCHEME CountryISO}.
   * 
   * @return the object identifier, not null
   */
  @Override
  public ObjectId getObjectId() {
    return ObjectId.of(OBJECT_SCHEME, _code);
  }

  /**
   * Gets the unique identifier for the country.
   * <p>
   * This uses the scheme {@link #OBJECT_SCHEME CountryISO}.
   * 
   * @return the unique identifier, not null
   */
  @Override
  public UniqueId getUniqueId() {
    return UniqueId.of(OBJECT_SCHEME, _code);
  }

  //-----------------------------------------------------------------------
  /**
   * Compares this country to another by alphabetical comparison of the code.
   * 
   * @param other  the other country, not null
   * @return negative if earlier alphabetically, 0 if equal, positive if greater alphabetically
   */
  @Override
  public int compareTo(Country other) {
    return _code.compareTo(other._code);
  }

  /**
   * Checks if this country equals another country.
   * <p>
   * The comparison checks the two letter country code.
   * 
   * @param obj  the other country, null returns false
   * @return true if equal
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof Country) {
      return _code.equals(((Country) obj)._code);
    }
    return false;
  }

  /**
   * Returns a suitable hash code for the country.
   * 
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return _code.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the two letter country code as a string.
   * 
   * @return the two letter country code, not null
   */
  @Override
  public String toString() {
    return _code;
  }

}
