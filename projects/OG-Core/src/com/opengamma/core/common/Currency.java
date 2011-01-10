/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.common;

import java.io.Serializable;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

// REVIEW kirk 2009-09-15 -- This REALLY needs to be renamed.

// REVIEW kirk 2009-09-16 -- This needs to be worked out better for serialization.
// It's really not serialization clean, and everything at this level needs to be
// serialization friendly.

/**
 * A currency.
 */
public final class Currency implements UniqueIdentifiable, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;
  /**
   * A scheme for the unique identifier.
   */
  public static final String IDENTIFICATION_DOMAIN = "CurrencyISO";
  /**
   * A cache of instances.
   */
  private static ConcurrentMap<String, Currency> s_instanceMap = new ConcurrentHashMap<String, Currency>();

  /**
   * The identifier.
   */
  private UniqueIdentifier _identifier;

  /**
   * Obtains a currency.
   * 
   * @param isoCode  the 3 letter ISO code, not null
   * @return the currency instance, not null
   */
  public static Currency getInstance(String isoCode) {
    ArgumentChecker.notNull(isoCode, "ISO Code");
    if (isoCode.length() != 3) {
      throw new IllegalArgumentException("Invalid ISO code: " + isoCode);
    }
    isoCode = isoCode.toUpperCase(Locale.ENGLISH);
    s_instanceMap.putIfAbsent(isoCode, new Currency(isoCode));
    return s_instanceMap.get(isoCode);
  }

  /**
   * Restricted constructor.
   * 
   * @param isoCode  the 3 letter ISO code, not null
   */
  private Currency(String isoCode) {
    _identifier = UniqueIdentifier.of(IDENTIFICATION_DOMAIN, isoCode);
  }

  /**
   * Ensure singleton on deserialization.
   * 
   * @return the singleton, not null
   */
  public Object readResolve() {
    return getInstance(getISOCode());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the 3 letter ISO code.
   * 
   * @return the 3 letter ISO code, not null
   */
  public String getISOCode() {
    return _identifier.getValue();
  }

  /**
   * Gets the unique identifier for the currency.
   * 
   * @return the identifier, not null
   */
  @Override
  public UniqueIdentifier getUniqueId() {
    return _identifier;
  }

  //-------------------------------------------------------------------------
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj instanceof Currency) {
      return (this == obj);  // relies on caching of instances
    }
    return false;
  }

  public int hashCode() {
    return _identifier.hashCode();
  }

  public String toString() {
    return getISOCode();
  }

}
