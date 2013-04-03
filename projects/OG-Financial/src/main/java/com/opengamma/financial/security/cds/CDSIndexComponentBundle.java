/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.cds;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
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
   * @param components  the collection of components
   * @return the bundle, not null
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
  
  //-------------------------------------------------------------------------
  /**
   * Returns a new {@link CDSIndexComponentBundle} with the specified {@link CreditDefaultSwapIndexComponent} added. This instance is immutable and unaffected by this method call.
   * 
   * @param component the cdsindex component to add to the returned bundle, not null
   * @return the new bundle, not null
   */
  public CDSIndexComponentBundle withCDSIndexComponent(final CreditDefaultSwapIndexComponent component) {
    ArgumentChecker.notNull(component, "component");
    final Set<CreditDefaultSwapIndexComponent> components = new HashSet<>(_components);
    if (components.add(component) == false) {
      return this;
    }
    return create(components);
  }

  /**
   * Returns a new {@link CDSIndexComponentBundle} with the specified {@link CreditDefaultSwapIndexComponent}s added. This instance is immutable and unaffected by this method call.
   * 
   * @param components the identifiers to add to the returned bundle, not null
   * @return the new bundle, not null
   */
  public CDSIndexComponentBundle withCDSIndexComponents(final Iterable<CreditDefaultSwapIndexComponent> components) {
    ArgumentChecker.notNull(components, "components");
    final Set<CreditDefaultSwapIndexComponent> toAdd = ImmutableSortedSet.copyOf(components);
    final Set<CreditDefaultSwapIndexComponent> latest = new HashSet<CreditDefaultSwapIndexComponent>(_components);
    if (latest.addAll(toAdd) == false) {
      return this;
    }
    return create(latest);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a new bundle using a custom comparator for ordering. Primarily useful for display.
   * 
   * @param comparator comparator specifying how to order the ExternalIds
   * @return the new copy of the bundle, ordered by the comparator
   */
  public CDSIndexComponentBundle withCustomIdOrdering(final Comparator<CreditDefaultSwapIndexComponent> comparator) {
    return new CDSIndexComponentBundle(ImmutableSortedSet.orderedBy(comparator).addAll(_components).build());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the number of components in the bundle.
   * 
   * @return the bundle size, zero or greater
   */
  public int size() {
    return _components.size();
  }

  /**
   * Returns true if this bundle contains no components.
   * 
   * @return true if this bundle contains no components, false otherwise
   */
  public boolean isEmpty() {
    return _components.isEmpty();
  }

  /**
   * Returns an iterator over the components in the bundle.
   * 
   * @return the components in the bundle, not null
   */
  @Override
  public Iterator<CreditDefaultSwapIndexComponent> iterator() {
    return _components.iterator();
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
  
  private static class CDSIndexComponentObligorComparator implements Comparator<CreditDefaultSwapIndexComponent> {

    @Override
    public int compare(final CreditDefaultSwapIndexComponent left, final CreditDefaultSwapIndexComponent right) {
      return left.getObligorRedCode().compareTo(right.getObligorRedCode());
    }
    
  }

}
