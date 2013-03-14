/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.cds;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.ImmutableSortedSet;
import com.opengamma.util.ArgumentChecker;

/**
 * Immutable set of {@link CreditDefaultSwapIndexComponent} that represents the CreditDefaultSwapIndexSecurity components
 * <p>
 * It uses a comparator based on the ObligorCode of each components as suppose to natural ordering of weight and name
 * 
 */
public final class CDSIndexComponentBundle implements Iterable<CreditDefaultSwapIndexComponent>, Serializable {
  
  /** Serialization version. */
  private static final long serialVersionUID = 1L;
  
  /**
   * Comparator to use for sorting
   */
  private static final Comparator<CreditDefaultSwapIndexComponent> s_obligorComparator = new CDSIndexComponentObligorComparator();
    
  /**
   * The set of cdsIndex components.
   */
  private final ImmutableSortedSet<CreditDefaultSwapIndexComponent> _components;
  
  /**
   * Creates a cdsIndex components bundle from a set of cdsIndex component.
   * 
   * @param tenors  the set of tenors, assigned, not null
   */
  private CDSIndexComponentBundle(ImmutableSortedSet<CreditDefaultSwapIndexComponent> components) {
    _components = components;
  }
      
  /**
   * Obtains a {@link CDSIndexComponentBundle} from a {@link CreditDefaultSwapIndexComponent}.
   * 
   * @param component  the cdsindex component to warp in the bundle, not null
   * @return the cdsIndex components bundle, not null
   */
  public static CDSIndexComponentBundle of(CreditDefaultSwapIndexComponent component) {
    ArgumentChecker.notNull(component, "component");
    return new CDSIndexComponentBundle(ImmutableSortedSet.of(component));
  }
  
  /**
   * Obtains a {@link CDSIndexComponentBundle} from a collection of CreditDefaultSwapIndexComponents.
   * 
   * @param components  the collection of components, no nulls, not null
   * @return the cdsIndex components bundle, not null
   */
  public static CDSIndexComponentBundle of(Iterable<CreditDefaultSwapIndexComponent> components) {
    return create(components);
  }
  
  /**
   * Obtains a {@link CDSIndexComponentBundle} from an array of CreditDefaultSwapIndexComponents.
   * 
   * @param components  an array of components, no nulls, not null
   * @return the cdsIndex components bundle, not null
   */
  public static CDSIndexComponentBundle of(CreditDefaultSwapIndexComponent... components) {
    return create(Arrays.asList(components));
  }
  
  /**
   * Obtains an {@link CDSIndexComponentBundle} from a collection of {@link CreditDefaultSwapIndexComponent}.
   * 
   * @param components  the collection of tenors, validated
   * @return the cdsIndex terms, not null
   */
  private static CDSIndexComponentBundle create(Iterable<CreditDefaultSwapIndexComponent> components) {
    ArgumentChecker.notEmpty(components, "components");
    ArgumentChecker.noNulls(components, "components");
    return new CDSIndexComponentBundle(ImmutableSortedSet.copyOf(s_obligorComparator, components));
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets the set of {@link CreditDefaultSwapIndexComponent} in the cdsIndex components bundle.
   * 
   * @return the tenor set, unmodifiable, not null
   */
  public Set<CreditDefaultSwapIndexComponent> getComponents() {
    return _components;
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
   * Returns an iterator over the tenors in the bundle.
   * 
   * @return the components in the bundle, not null
   */
  @Override
  public Iterator<CreditDefaultSwapIndexComponent> iterator() {
    return _components.iterator();
  }
  
  private static class CDSIndexComponentObligorComparator implements Comparator<CreditDefaultSwapIndexComponent> {

    @Override
    public int compare(final CreditDefaultSwapIndexComponent left, final CreditDefaultSwapIndexComponent right) {
      return left.getObligorRedCode().compareTo(right.getObligorRedCode());
    }
    
  }

}
