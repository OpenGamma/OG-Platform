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
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Immutable set of {@link CreditDefaultSwapIndexComponent} that
 * represents the CreditDefaultSwapIndexDefinitionSecurity components
 * <p>
 * It uses a comparator based on the ObligorCode of each components
 * as opposed to natural ordering of weight and name.
 * <p>
 * Note that ideally we would use a Map keyed on RED code with values
 * of the components, sorted by the values. However, standard maps
 * are sorted by kets so would not be usable. Instead this class
 * maintains a Map to ensure each RED code only appears once and
 * a sorted set of the components.
 * </p>
 * 
 */
public final class CDSIndexComponentBundle implements Iterable<CreditDefaultSwapIndexComponent>, Serializable {
  
  /** Serialization version. */
  private static final long serialVersionUID = 1L;
  
  /**
   * Comparator to use for sorting if one is not specified by client
   */
  private static final Comparator<CreditDefaultSwapIndexComponent> DEFAULT_COMPARATOR = new CDSIndexComponentObligorComparator();
    
  /**
   * The set of cdsIndex components.
   */
  private final ImmutableSortedSet<CreditDefaultSwapIndexComponent> _components;

  /**
   * The map of the current red codes.
   */
  private final Map<ExternalId, CreditDefaultSwapIndexComponent> _redCodeMapping;
  
  /**
   * Creates a cdsIndex components bundle from a set of cdsIndex component.
   * 
   * @param components  the set of components assigned, not null
   */
  private CDSIndexComponentBundle(Iterable<CreditDefaultSwapIndexComponent> components) {
    this(components, DEFAULT_COMPARATOR);
  }

  /**
   * Creates a cdsIndex components bundle from a set of cdsIndex
   * component and a comparator.
   *
   * @param components  the set of components assigned, not null
   */
  private CDSIndexComponentBundle(Iterable<CreditDefaultSwapIndexComponent> components,
                                  Comparator<? super CreditDefaultSwapIndexComponent> comparator) {
    ArgumentChecker.notEmpty(components, "components");
    ArgumentChecker.noNulls(components, "components");
    ArgumentChecker.notNull(comparator, "comparator");

    _components = ImmutableSortedSet.copyOf(comparator, deduplicate(components));
    _redCodeMapping = Maps.uniqueIndex(_components, new Function<CreditDefaultSwapIndexComponent, ExternalId>() {
      @Override
      public ExternalId apply(CreditDefaultSwapIndexComponent input) {
        return input.getObligorRedCode();
      }
    });
  }

  private static Iterable<CreditDefaultSwapIndexComponent> deduplicate(Iterable<CreditDefaultSwapIndexComponent> components) {

    Map<ExternalId, CreditDefaultSwapIndexComponent> redCodeMapping = Maps.newHashMap();

    for (CreditDefaultSwapIndexComponent component : components) {
      redCodeMapping.put(component.getObligorRedCode(), component);
    }

    return redCodeMapping.values();
  }

  /**
   * Obtains a {@link CDSIndexComponentBundle} from an array of
   * CreditDefaultSwapIndexComponents.
   *
   * @param components  an array of components, no nulls, not null
   * @return the cdsIndex components bundle, not null
   */
  public static CDSIndexComponentBundle of(CreditDefaultSwapIndexComponent... components) {
    return create(Arrays.asList(components));
  }

  /**
   * Obtains a {@link CDSIndexComponentBundle} from a collection of
   * CreditDefaultSwapIndexComponents.
   *
   * @param components  the collection of components, no nulls, not null
   * @return the cdsIndex components bundle, not null
   */
  public static CDSIndexComponentBundle of(Iterable<CreditDefaultSwapIndexComponent> components) {
    return create(components);
  }

  /**
   * Obtains an {@link CDSIndexComponentBundle} from a collection of
   * {@link CreditDefaultSwapIndexComponent}.
   * 
   * @param components  the collection of components
   * @return the bundle, not null
   */
  private static CDSIndexComponentBundle create(Iterable<CreditDefaultSwapIndexComponent> components) {
    return new CDSIndexComponentBundle(components);
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets the set of {@link CreditDefaultSwapIndexComponent} in the cdsIndex
   * components bundle.
   * 
   * @return the components, unmodifiable, not null
   */
  public Iterable<CreditDefaultSwapIndexComponent> getComponents() {
    return _components;
  }
  
  /**
   * Returns a new {@link CDSIndexComponentBundle} with the specified
   * {@link CreditDefaultSwapIndexComponent}s added. This instance is immutable
   * and unaffected by this method call.
   *
   * @param components the identifiers to add to the returned bundle, not null
   * @return the new bundle, not null
   */
  public CDSIndexComponentBundle withCDSIndexComponents(final CreditDefaultSwapIndexComponent... components) {
    return withCDSIndexComponents(Arrays.asList(components));
  }

  /**
   * Returns a new {@link CDSIndexComponentBundle} with the specified
   * {@link CreditDefaultSwapIndexComponent}s added. This instance is immutable
   * and unaffected by this method call.
   * 
   * @param components the identifiers to add to the returned bundle, not null
   * @return the new bundle, not null
   */
  public CDSIndexComponentBundle withCDSIndexComponents(final Iterable<CreditDefaultSwapIndexComponent> components) {

    final Set<CreditDefaultSwapIndexComponent> updatedComponents = Sets.newLinkedHashSet(_components);

    for (CreditDefaultSwapIndexComponent component : components) {

      if (!component.equals(_redCodeMapping.get(component.getObligorRedCode()))) {
        updatedComponents.add(component);
      }
    }

    return new CDSIndexComponentBundle(updatedComponents, _components.comparator());
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a new bundle using a custom comparator for ordering. Primarily
   * useful for display.
   * 
   * @param comparator comparator specifying how to order the ExternalIds
   * @return the new copy of the bundle, ordered by the comparator
   */
  public CDSIndexComponentBundle withCustomIdOrdering(final Comparator<CreditDefaultSwapIndexComponent> comparator) {
    return new CDSIndexComponentBundle(_components, comparator);
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
  
  private static class CDSIndexComponentObligorComparator implements Comparator<CreditDefaultSwapIndexComponent>, Serializable {

    @Override
    public int compare(final CreditDefaultSwapIndexComponent left, final CreditDefaultSwapIndexComponent right) {
      return left.getObligorRedCode().compareTo(right.getObligorRedCode());
    }
  }
}
