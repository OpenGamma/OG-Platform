/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.text.StrBuilder;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableSortedSet;
import com.opengamma.util.ArgumentChecker;

/**
 * A bundle of external identifiers with validity dates.
 * <p>
 * This is similar to {@link ExternalIdBundle} but permits each external identifier
 * to be limited by validity dates.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class ExternalIdBundleWithDates
    implements Iterable<ExternalIdWithDates>, Serializable, Comparable<ExternalIdBundleWithDates> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton empty bundle.
   */
  public static final ExternalIdBundleWithDates EMPTY = new ExternalIdBundleWithDates();

  /**
   * The set of identifiers.
   */
  private final ImmutableSortedSet<ExternalIdWithDates> _externalIds;
  /**
   * The cached hash code.
   */
  private transient volatile int _hashCode;

  /**
   * Obtains an {@code ExternalIdBundleWithDates} from an array of identifiers.
   * 
   * @param externalIds  the array of external identifiers, no nulls, not null
   * @return the bundle, not null
   */
  public static ExternalIdBundleWithDates of(ExternalIdWithDates... externalIds) {
    ArgumentChecker.notNull(externalIds, "identifiers");
    return new ExternalIdBundleWithDates(externalIds);
  }

  /**
   * Obtains an {@code ExternalIdBundleWithDates} from an iterable of identifiers.
   * 
   * @param externalIds the iterable of external identifiers, not null
   * @return the identifier bundle with dates set to null, not null
   */
  public static ExternalIdBundleWithDates of(Iterable<ExternalIdWithDates> externalIds) {
    ArgumentChecker.notNull(externalIds, "externalIds");
    return create(externalIds);
  }

  /**
   * Obtains an {@code ExternalIdBundleWithDates} from a bundle of identifiers.
   * 
   * @param bundle the identifier bundle, not null
   * @return the identifier bundle with dates set to null, not null
   */
  public static ExternalIdBundleWithDates of(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    Set<ExternalIdWithDates> identifiers = new HashSet<ExternalIdWithDates>();
    for (ExternalId identifier : bundle) {
      identifiers.add(ExternalIdWithDates.of(identifier, null, null));
    }
    return new ExternalIdBundleWithDates(identifiers);
  }

  /**
   * Obtains an {@code ExternalIdBundleWithDates} from a collection of identifiers.
   * 
   * @param externalIds  the collection of external identifiers, validated
   * @return the bundle, not null
   */
  private static ExternalIdBundleWithDates create(Iterable<ExternalIdWithDates> externalIds) {
    return new ExternalIdBundleWithDates(ImmutableSortedSet.copyOf(externalIds));
  }

  /**
   * Creates an empty bundle.
   */
  private ExternalIdBundleWithDates() {
    _externalIds = ImmutableSortedSet.of();
  }

  /**
   * Creates a bundle from an array of identifiers.
   * 
   * @param externalIds  the array of identifiers, null returns an empty bundle
   */
  public ExternalIdBundleWithDates(ExternalIdWithDates... externalIds) {
    if ((externalIds == null) || (externalIds.length == 0)) {
      _externalIds = ImmutableSortedSet.of();
    } else {
      ArgumentChecker.noNulls(externalIds, "externalIds");
      _externalIds = ImmutableSortedSet.copyOf(externalIds);
    }
  }

  /**
   * Creates a bundle from a collection of identifiers.
   * 
   * @param externalIds  the collection of identifiers, null returns an empty bundle, no nulls in array
   */
  public ExternalIdBundleWithDates(Collection<? extends ExternalIdWithDates> externalIds) {
    if (externalIds == null) {
      _externalIds = ImmutableSortedSet.of();
    } else {
      ArgumentChecker.noNulls(externalIds, "identifiers");
      _externalIds = ImmutableSortedSet.copyOf(externalIds);
    }
  }

  /**
   * Creates a bundle from a collection of identifiers with a given comparator to specify ordering.
   * Note that the comparator is not preserved over Fudge encoding.
   * 
   * @param externalIds  the collection of identifiers, null returns an empty bundle, no nulls in array
   */
  private ExternalIdBundleWithDates(Collection<? extends ExternalIdWithDates> externalIds, Comparator<ExternalIdWithDates> comparator) {
    if (externalIds == null) {
      _externalIds = ImmutableSortedSet.orderedBy(comparator).build();
    } else {
      ArgumentChecker.noNulls(externalIds, "identifiers");
      _externalIds = ImmutableSortedSet.orderedBy(comparator).addAll(externalIds).build();
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the set of identifiers in the bundle.
   * 
   * @return the identifier set, unmodifiable, not null
   */
  public Set<ExternalIdWithDates> getExternalIds() {
    return _externalIds;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a new bundle with the specified identifier added.
   * This instance is immutable and unaffected by this method call.
   * 
   * @param externalId  the identifier to add to the returned bundle, not null
   * @return the new bundle, not null
   */
  public ExternalIdBundleWithDates withExternalId(ExternalIdWithDates externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    Set<ExternalIdWithDates> ids = new HashSet<ExternalIdWithDates>(_externalIds);
    if (ids.add(externalId) == false) {
      return this;
    }
    return create(ids);
  }

  /**
   * Returns a new bundle with the specified identifier removed.
   * This instance is immutable and unaffected by this method call.
   * 
   * @param externalId  the identifier to remove from the returned bundle, not null
   * @return the new bundle, not null
   */
  public ExternalIdBundleWithDates withoutExternalId(ExternalIdWithDates externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    Set<ExternalIdWithDates> ids = new HashSet<ExternalIdWithDates>(_externalIds);
    if (ids.remove(externalId) == false) {
      return this;
    }
    return create(ids);
  }

  /**
   * Returns a new bundle with all references to the specified scheme removed.
   * This instance is immutable and unaffected by this method call.
   * 
   * @param scheme  the scheme to remove from the returned bundle, null ignored
   * @return the new bundle, not null
   */
  public ExternalIdBundleWithDates withoutScheme(ExternalScheme scheme) {
    Set<ExternalIdWithDates> ids = new HashSet<ExternalIdWithDates>(_externalIds.size());
    for (ExternalIdWithDates id : _externalIds) {
      if (id.getExternalId().isScheme(scheme) == false) {
        ids.add(id);
      }
    }
    return create(ids);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a new bundle using a custom comparator for ordering. Primarily useful for display.
   * 
   * @param comparator comparator specifying how to order the ExternalIds
   * @return the new copy of the bundle, ordered by the comparator
   */
  public ExternalIdBundleWithDates withCustomIdOrdering(Comparator<ExternalIdWithDates> comparator) {
    return new ExternalIdBundleWithDates(_externalIds, comparator);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the number of identifiers in the bundle.
   * 
   * @return the bundle size, zero or greater
   */
  public int size() {
    return _externalIds.size();
  }

  /**
   * Returns an iterator over the identifiers in the bundle.
   * 
   * @return the identifiers in the bundle, not null
   */
  @Override
  public Iterator<ExternalIdWithDates> iterator() {
    return _externalIds.iterator();
  }

  /**
   * Checks if this bundle contains all the keys from the specified bundle.
   * 
   * @param bundle  the bundle to search for, empty returns true, not null
   * @return true if this bundle contains all the keys from the specified bundle
   */
  public boolean containsAll(ExternalIdBundleWithDates bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    for (ExternalIdWithDates externalId : bundle.getExternalIds()) {
      if (_externalIds.contains(externalId) == false) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if this bundle contains any key from the specified bundle.
   * 
   * @param bundle  the bundle to search for, empty returns false, not null
   * @return true if this bundle contains any key from the specified bundle
   */
  public boolean containsAny(ExternalIdBundleWithDates bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    for (ExternalIdWithDates externalId : bundle.getExternalIds()) {
      if (_externalIds.contains(externalId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if this bundle contains the specified key.
   * 
   * @param externalId  the identifier to search for, null returns false
   * @return true if this bundle contains any key from the specified bundle
   */
  public boolean contains(ExternalIdWithDates externalId) {
    return externalId != null && _externalIds.contains(externalId);
  }

  /**
   * Converts this bundle to a list of formatted strings.
   * 
   * @return the list of identifiers as strings, not null
   */
  public List<String> toStringList() {
    List<String> list = new ArrayList<String>();
    for (ExternalIdWithDates id : this) {
      list.add(id.toString());
    }
    return list;
  }

  /**
   * Returns the bundle without dates.
   * <p>
   * This returns all the identifiers ignoring the validity dates.
   * See {@link #toBundle(LocalDate)} for a better choice.
   * 
   * @return the equivalent bundle, without the dates, not null
   */
  public ExternalIdBundle toBundle() {
    return toBundle(null);
  }

  /**
   * Returns the bundle without dates as of a specific date.
   * 
   * @param validOn  the validity date, null returns all
   * @return the equivalent bundle, without the dates, not null
   */
  public ExternalIdBundle toBundle(LocalDate validOn) {
    Set<ExternalId> ids = new HashSet<ExternalId>();
    for (ExternalIdWithDates identifier : _externalIds) {
      if (identifier.isValidOn(validOn)) {
        ids.add(identifier.toExternalId());
      }
    }
    return ExternalIdBundle.of(ids);
  }

  //-------------------------------------------------------------------
  /**
   * Compares the bundles.
   * 
   * @param other  the other external identifier, not null
   * @return negative if this is less, zero if equal, positive if greater
   */
  @Override
  public int compareTo(ExternalIdBundleWithDates other) {
    final Set<ExternalIdWithDates> mySet = getExternalIds();
    final Set<ExternalIdWithDates> otherSet = other.getExternalIds();
    if (mySet.size() < otherSet.size()) {
      return -1;
    }
    if (mySet.size() > otherSet.size()) {
      return 1;
    }
    final List<ExternalIdWithDates> myList = new ArrayList<ExternalIdWithDates>(mySet); // already sorted as TreeSet
    final List<ExternalIdWithDates> otherList = new ArrayList<ExternalIdWithDates>(otherSet); // already sorted as TreeSet
    for (int i = 0; i < myList.size(); i++) {
      int c = myList.get(i).compareTo(otherList.get(i));
      if (c != 0) {
        return c;
      }
    }
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ExternalIdBundleWithDates) {
      ExternalIdBundleWithDates other = (ExternalIdBundleWithDates) obj;
      return _externalIds.equals(other._externalIds);
    }
    return false;
  }

  @Override
  public int hashCode() {
    if (_hashCode == 0) {
      _hashCode = 31 + _externalIds.hashCode();
    }
    return _hashCode;
  }

  /**
   * Returns a string representation of the bundle.
   * 
   * @return a string representation of the bundle, not null
   */
  @Override
  public String toString() {
    return new StrBuilder()
        .append("BundleWithDates")
        .append("[")
        .appendWithSeparators(_externalIds, ", ")
        .append("]")
        .toString();
  }

}
