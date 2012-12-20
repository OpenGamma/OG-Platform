/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.text.StrBuilder;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * A search request to match external identifiers.
 * <p>
 * The search combines a set of external identifiers and a matching rule.
 * <p>
 * This class is mutable and not thread-safe.
 */
@PublicAPI
public final class ExternalIdSearch implements Iterable<ExternalId>, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The set of identifiers.
   */
  private final SortedSet<ExternalId> _externalIds = new TreeSet<ExternalId>();
  /**
   * The search type.
   */
  private ExternalIdSearchType _searchType = ExternalIdSearchType.ANY;

  /**
   * Creates an empty search, with the search type set to any.
   */
  public ExternalIdSearch() {
  }

  /**
   * Creates a search matching an identifier, with the search type set to any.
   * 
   * @param identifier  the identifier, not null
   */
  public ExternalIdSearch(ExternalId identifier) {
    addExternalId(identifier);
  }

  /**
   * Creates a search matching any of a collection of identifiers.
   * 
   * @param identifiers  the collection of identifiers, not null
   */
  public ExternalIdSearch(ExternalId... identifiers) {
    addExternalIds(identifiers);
  }

  /**
   * Creates a search matching any of a collection of identifiers.
   * 
   * @param identifiers  the collection of identifiers, not null
   */
  public ExternalIdSearch(Iterable<ExternalId> identifiers) {
    this(identifiers, ExternalIdSearchType.ANY);
  }

  /**
   * Creates a search matching any of a collection of identifiers.
   * 
   * @param identifiers  the collection of identifiers, not null
   * @param searchType  the search type, not null
   */
  public ExternalIdSearch(Iterable<ExternalId> identifiers, ExternalIdSearchType searchType) {
    addExternalIds(identifiers);
    setSearchType(searchType);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the set of searched for identifiers.
   * 
   * @return the identifier set, live and modifiable, not null, no nulls
   */
  public SortedSet<ExternalId> getExternalIds() {
    return _externalIds;
  }

  /**
   * Adds an identifier to the set of searched for identifiers.
   * 
   * @param externalId  the identifier to add, not null
   */
  public void addExternalId(ExternalId externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    _externalIds.add(externalId);
  }

  /**
   * Adds a identifiers to the set of searched for identifiers.
   * 
   * @param externalIds  the identifiers to add, not null
   */
  public void addExternalIds(ExternalId... externalIds) {
    ArgumentChecker.noNulls(externalIds, "externalIds");
    _externalIds.addAll(Arrays.asList(externalIds));
  }

  /**
   * Adds a identifiers to the set of searched for identifiers.
   * 
   * @param externalIds  the identifiers to add, not null
   */
  public void addExternalIds(Iterable<ExternalId> externalIds) {
    ArgumentChecker.noNulls(externalIds, "externalIds");
    Iterables.addAll(_externalIds, externalIds);
  }

  /**
   * Removes an identifier from the set of searched for identifiers.
   * 
   * @param externalId  the identifier to remove, null ignored
   */
  public void removeExternalId(ExternalId externalId) {
    if (externalId != null) {
      _externalIds.remove(externalId);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the search type.
   * This is ANY by default.
   * 
   * @return the search type, not null
   */
  public ExternalIdSearchType getSearchType() {
    return _searchType;
  }

  /**
   * Sets the search type.
   * 
   * @param searchType  the search type, not null
   */
  public void setSearchType(ExternalIdSearchType searchType) {
    ArgumentChecker.notNull(searchType, "searchType");
    _searchType = searchType;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the number of identifiers.
   * 
   * @return the bundle size, zero or greater
   */
  public int size() {
    return _externalIds.size();
  }

  /**
   * Returns an iterator over the identifiers.
   * 
   * @return the identifiers, not null
   */
  public Iterator<ExternalId> iterator() {
    return _externalIds.iterator();
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this search matches the identifier.
   * <p>
   * An EXACT match returns true if there is one stored identifier and it is equal to the passed in identifier.<br />
   * An ALL match returns true if this is empty or has a single identifier equal to the input.<br />
   * An ANY match returns true if the passed in identifier matches any of the stored identifiers.<br />
   * A NONE match returns true if the passed in identifier does not match any stored identifier.<br />
   * 
   * @param otherId  the identifier to search for, not null
   * @return true if this search contains all of the keys specified
   */
  public boolean matches(ExternalId otherId) {
    ArgumentChecker.notNull(otherId, "otherId");
    switch (_searchType) {
      case EXACT:
        return Sets.newHashSet(otherId).equals(_externalIds);
      case ALL:
        return Sets.newHashSet(otherId).containsAll(_externalIds);
      case ANY:
        return contains(otherId);
      case NONE:
        return contains(otherId) == false;
    }
    return false;
  }

  /**
   * Checks if this search matches the identifiers.
   * <p>
   * An EXACT match returns true if the passed in identifiers are the same set as the stored identifiers.<br />
   * An ALL match returns true if the passed in identifiers match are a superset or equal the stored identifiers.<br />
   * An ANY match returns true if the passed in identifiers match any of the stored identifiers.<br />
   * A NONE match returns true if none of the passed in identifiers match a stored identifier.<br />
   * 
   * @param otherId  the identifiers to search for, empty returns true, not null
   * @return true if this search contains all of the keys specified
   */
  public boolean matches(Iterable<ExternalId> otherId) {
    ArgumentChecker.notNull(otherId, "otherId");
    switch (_searchType) {
      case EXACT:
        return Sets.newHashSet(otherId).equals(_externalIds);
      case ALL:
        return Sets.newHashSet(otherId).containsAll(_externalIds);
      case ANY:
        return containsAny(otherId);
      case NONE:
        return containsAny(otherId) == false;
    }
    return false;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this search contains all the keys from the specified identifiers.
   * <p>
   * This is the opposite check to the ALL search type in {@code matches()}.
   * This method checks if this is a superset or equal to the passed in identifiers.
   * The ALL check checks the superset the other way around.
   * 
   * @param otherId  the identifiers to search for, empty returns true, not null
   * @return true if this search contains all of the keys specified
   */
  public boolean containsAll(Iterable<ExternalId> otherId) {
    ArgumentChecker.notNull(otherId, "otherId");
    for (ExternalId identifier : otherId) {
      if (_externalIds.contains(identifier) == false) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if this search contains any key from the specified identifiers.
   * 
   * @param otherId  the identifiers to search for, empty returns false, not null
   * @return true if this search contains any of the keys specified
   */
  public boolean containsAny(Iterable<ExternalId> otherId) {
    ArgumentChecker.notNull(otherId, "otherId");
    for (ExternalId identifier : otherId) {
      if (_externalIds.contains(identifier)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if this search contains the specified key.
   * 
   * @param externalId  the key to search for, null returns false
   * @return true if this search contains the specified key
   */
  public boolean contains(ExternalId externalId) {
    return externalId != null && _externalIds.contains(externalId);
  }

  //-------------------------------------------------------------------
  /**
   * Checks if the specified instance can match anything.
   * 
   * @param idSearch  the identifier search, null returns true
   * @return true if the search can match anything
   */
  public static boolean canMatch(final ExternalIdSearch idSearch) {
    if (idSearch == null) {
      return true;
    }
    if (idSearch.getSearchType() == ExternalIdSearchType.NONE) {
      return true;
    }
    return idSearch.size() > 0;
  }

  /**
   * Checks if this search always matches.
   * 
   * @return true if the search always matches
   */
  public boolean alwaysMatches() {
    return (getSearchType() == ExternalIdSearchType.NONE && size() == 0);
  }

  //-------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ExternalIdSearch) {
      ExternalIdSearch other = (ExternalIdSearch) obj;
      return _externalIds.equals(other._externalIds);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _externalIds.hashCode() ^ _searchType.hashCode();
  }

  @Override
  public String toString() {
    return new StrBuilder()
        .append("Search")
        .append("[")
        .appendWithSeparators(_externalIds, ", ")
        .append("]")
        .toString();
  }

}
