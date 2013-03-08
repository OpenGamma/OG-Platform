/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.cds;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.ImmutableSortedSet;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * Immutable set of tenors that represents the CreditDefaultSwapIndex security terms
 */
public final class CDSIndexTerms implements Iterable<Tenor>, Serializable {
  
  /** Serialization version. */
  private static final long serialVersionUID = 1L;
  
  /**
   * Singleton empty cdsIndex terms.
   */
  public static final CDSIndexTerms EMPTY = new CDSIndexTerms();
  
  /**
   * The set of tenors.
   */
  private final ImmutableSortedSet<Tenor> _tenors;
  
  /**
   * Creates an empty term.
   */
  private CDSIndexTerms() {
    _tenors = ImmutableSortedSet.of();
  }

  /**
   * Creates a cdsIndex terms from a set of tenors.
   * 
   * @param tenors  the set of tenors, assigned, not null
   */
  private CDSIndexTerms(ImmutableSortedSet<Tenor> tenors) {
    _tenors = tenors;
  }
      
  /**
   * Obtains a {@link CDSIndexTerms} from a tenor.
   * 
   * @param tenor  the tenor to warp in the terms, not null
   * @return the terms, not null
   */
  public static CDSIndexTerms of(Tenor tenor) {
    ArgumentChecker.notNull(tenor, "tenor");
    return new CDSIndexTerms(ImmutableSortedSet.of(tenor));
  }
  
  /**
   * Obtains an {@link CDSIndexTerms} from a collection of tenors.
   * 
   * @param tenors  the collection of tenors, no nulls, not null
   * @return the terms, not null
   */
  public static CDSIndexTerms of(Iterable<Tenor> tenors) {
    ArgumentChecker.noNulls(tenors, "tenors");
    return create(tenors);
  }
  
  /**
   * Obtains an {@link CDSIndexTerms} from an array of tenors.
   * 
   * @param tenors  an array of tenors, no nulls, not null
   * @return the terms, not null
   */
  public static CDSIndexTerms of(Tenor... tenors) {
    ArgumentChecker.noNulls(tenors, "tenors");
    return create(Arrays.asList(tenors));
  }
  
  /**
   * Obtains an {@link CDSIndexTerms} from a collection of tenors.
   * 
   * @param tenors  the collection of tenors, validated
   * @return the cdsIndex terms, not null
   */
  private static CDSIndexTerms create(Iterable<Tenor> tenors) {
    return new CDSIndexTerms(ImmutableSortedSet.copyOf(tenors));
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets the set of tenors in the cdsIndex terms.
   * 
   * @return the tenor set, unmodifiable, not null
   */
  public Set<Tenor> getTenors() {
    return _tenors;
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }
  
  

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * Returns an iterator over the tenors in the terms.
   * 
   * @return the tenors in the terms, not null
   */
  @Override
  public Iterator<Tenor> iterator() {
    return _tenors.iterator();
  }

}
