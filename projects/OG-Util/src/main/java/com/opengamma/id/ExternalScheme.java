/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;

import org.joda.convert.FromString;
import org.joda.convert.ToString;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * A classification scheme for external identifiers.
 * <p>
 * The scheme defines a universe of identifier values.
 * Each value only has meaning within that scheme, and the same value may have
 * a different meaning in a different scheme.
 * The scheme class is a type-safe wrapper on top of a string name.
 * <p>
 * This class is immutable and thread-safe.
 */
@PublicAPI
public final class ExternalScheme implements Serializable, Comparable<ExternalScheme> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Computing cache for the schemes.
   */
  private static final LoadingCache<String, ExternalScheme> s_cache =
      CacheBuilder.newBuilder().initialCapacity(256).concurrencyLevel(4).build(
          new CacheLoader<String, ExternalScheme>() {
            public ExternalScheme load(String key) {
              return new ExternalScheme(key);
            }
          });

  /**
   * The scheme name.
   */
  private final String _name;

  /**
   * Obtains an {@code ExternalScheme} scheme using the specified name.
   * 
   * @param name  the scheme name, not empty, not null
   * @return the scheme, not null
   */
  @FromString
  public static ExternalScheme of(final String name) {
    ArgumentChecker.notEmpty(name, "name");
    return s_cache.getUnchecked(name);
  }

  /**
   * Constructs a scheme using the specified name.
   * 
   * @param name  the scheme name, not empty, not null
   */
  private ExternalScheme(final String name) {
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
  /**
   * Compares this scheme to another sorting alphabetically.
   * 
   * @param other  the other scheme, not null
   * @return negative if this is less, zero if equal, positive if greater
   */
  @Override
  public int compareTo(final ExternalScheme other) {
    return _name.compareTo(other._name);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ExternalScheme) {
      ExternalScheme other = (ExternalScheme) obj;
      return _name.equals(other._name);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  /**
   * Returns the name of the scheme.
   * 
   * @return the scheme name, not null
   */
  @Override
  @ToString
  public String toString() {
    return _name;
  }

}
