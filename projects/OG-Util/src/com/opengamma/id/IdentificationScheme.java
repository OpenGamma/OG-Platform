/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * A classification scheme for identifiers.
 * <p>
 * The scheme defines a universe of unique identifiers.
 * Each identifier is only unique with respect to the scheme.
 * The same identifier may have a different meaning in a different scheme.
 * <p>
 * Fundamentally, this is nothing other than a type-safe wrapper on top of
 * a name describing the identification scheme.
 * <p>
 * This class is immutable and thread-safe.
 */
@PublicAPI
public final class IdentificationScheme implements Serializable, Comparable<IdentificationScheme> {

  /**
   * Computing cache for the schemes.
   */
  private static final ConcurrentMap<String, IdentificationScheme> s_cache =
      new MapMaker().initialCapacity(256).concurrencyLevel(4).makeComputingMap(new Function<String, IdentificationScheme>() {
        @Override
        public IdentificationScheme apply(String key) {
          return new IdentificationScheme(key);
        }
      });

  /**
   * The scheme name.
   */
  private final String _name;

  /**
   * Obtains an {@code IdentificationScheme} scheme using the specified name.
   * 
   * @param name  the scheme name, not empty, not null
   * @return the scheme, not null
   */
  public static IdentificationScheme of(final String name) {
    ArgumentChecker.notEmpty(name, "name");
    return s_cache.get(name);
  }

  /**
   * Constructs a scheme using the specified name.
   * 
   * @param name  the scheme name, not empty, not null
   */
  private IdentificationScheme(final String name) {
    _name = name;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the scheme name.
   * 
   * @return the scheme name, not null
   */
  public String getName() {
    return _name;
  }

  //-------------------------------------------------------------------------
  @Override
  public int compareTo(final IdentificationScheme obj) {
    return _name.compareTo(obj._name);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof IdentificationScheme) {
      IdentificationScheme other = (IdentificationScheme) obj;
      return _name.equals(other._name);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  @Override
  public String toString() {
    return _name;
  }

}
