/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.text.StrBuilder;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgFactory;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * A search request to match identifiers.
 * <p>
 * The search combines a set of identifiers and a matching rule.
 * <p>
 * This class is mutable and not thread-safe.
 */
@PublicAPI
public final class IdentifierSearch implements Iterable<Identifier>, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The set of identifiers.
   */
  private final SortedSet<Identifier> _identifiers = new TreeSet<Identifier>();
  /**
   * The search type.
   */
  private IdentifierSearchType _searchType = IdentifierSearchType.ANY;

  /**
   * Creates an empty search, with the search type set to any.
   */
  public IdentifierSearch() {
  }

  /**
   * Creates a search matching an identifier, with the search type set to any.
   * 
   * @param identifier  the identifier, not null
   */
  public IdentifierSearch(Identifier identifier) {
    addIdentifier(identifier);
  }

  /**
   * Creates a search matching any of a collection of identifiers.
   * 
   * @param identifiers  the collection of identifiers, not null
   */
  public IdentifierSearch(Identifier... identifiers) {
    addIdentifiers(identifiers);
  }

  /**
   * Creates a search matching any of a collection of identifiers.
   * 
   * @param identifiers  the collection of identifiers, not null
   */
  public IdentifierSearch(Iterable<Identifier> identifiers) {
    addIdentifiers(identifiers);
  }

  /**
   * Creates a search matching any of a collection of identifiers.
   * 
   * @param identifiers  the collection of identifiers, not null
   * @param searchType  the search type, not null
   */
  public IdentifierSearch(Iterable<Identifier> identifiers, IdentifierSearchType searchType) {
    addIdentifiers(identifiers);
    setSearchType(searchType);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the set of searched for identifiers.
   * 
   * @return the identifier set, live and modifiable, not null, no nulls
   */
  public SortedSet<Identifier> getIdentifiers() {
    return _identifiers;
  }

  /**
   * Adds an identifier to the set of searched for identifiers.
   * 
   * @param identifier  the identifier to add, not null
   */
  public void addIdentifier(Identifier identifier) {
    ArgumentChecker.notNull(identifier, "identifier");
    _identifiers.add(identifier);
  }

  /**
   * Adds a identifiers to the set of searched for identifiers.
   * 
   * @param identifiers  the identifiers to add, not null
   */
  public void addIdentifiers(Identifier... identifiers) {
    ArgumentChecker.noNulls(identifiers, "identifiers");
    _identifiers.addAll(Arrays.asList(identifiers));
  }

  /**
   * Adds a identifiers to the set of searched for identifiers.
   * 
   * @param identifiers  the identifiers to add, not null
   */
  public void addIdentifiers(Iterable<Identifier> identifiers) {
    ArgumentChecker.noNulls(identifiers, "identifiers");
    Iterables.addAll(_identifiers, identifiers);
  }

  /**
   * Removes an identifier from the set of searched for identifiers.
   * 
   * @param identifier  the identifier to remove, null ignored
   */
  public void removeIdentifier(Identifier identifier) {
    if (identifier != null) {
      _identifiers.remove(identifier);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the search type.
   * This is ANY by default.
   * 
   * @return the search type, not null
   */
  public IdentifierSearchType getSearchType() {
    return _searchType;
  }

  /**
   * Sets the search type.
   * 
   * @param searchType  the search type, not null
   */
  public void setSearchType(IdentifierSearchType searchType) {
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
    return _identifiers.size();
  }

  /**
   * Returns an iterator over the identifiers.
   * 
   * @return the identifiers, not null
   */
  public Iterator<Identifier> iterator() {
    return _identifiers.iterator();
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this search matches the identifier.
   * 
   * @param otherIdentifier  the identifier to search for, not null
   * @return true if this search contains all of the keys specified
   */
  public boolean matches(Identifier otherIdentifier) {
    ArgumentChecker.notNull(otherIdentifier, "otherIdentifier");
    switch (_searchType) {
      case EXACT:
        return Sets.newHashSet(otherIdentifier).equals(_identifiers);
      case ALL:
        return Sets.newHashSet(otherIdentifier).containsAll(_identifiers);
      case ANY:
        return contains(otherIdentifier);
      case NONE:
        return contains(otherIdentifier) == false;
    }
    return false;
  }

  /**
   * Checks if this search matches the identifiers.
   * 
   * @param otherIdentifiers  the identifiers to search for, empty returns true, not null
   * @return true if this search contains all of the keys specified
   */
  public boolean matches(Iterable<Identifier> otherIdentifiers) {
    ArgumentChecker.notNull(otherIdentifiers, "otherIdentifiers");
    switch (_searchType) {
      case EXACT:
        return Sets.newHashSet(otherIdentifiers).equals(_identifiers);
      case ALL:
        return Sets.newHashSet(otherIdentifiers).containsAll(_identifiers);
      case ANY:
        return containsAny(otherIdentifiers);
      case NONE:
        return containsAny(otherIdentifiers) == false;
    }
    return false;
  }

  /**
   * Checks if this search contains all the keys from the specified identifiers.
   * 
   * @param otherIdentifiers  the identifiers to search for, empty returns true, not null
   * @return true if this search contains all of the keys specified
   */
  public boolean containsAll(Iterable<Identifier> otherIdentifiers) {
    ArgumentChecker.notNull(otherIdentifiers, "otherIdentifiers");
    for (Identifier identifier : otherIdentifiers) {
      if (_identifiers.contains(identifier) == false) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if this search contains any key from the specified identifiers.
   * 
   * @param otherIdentifiers  the identifiers to search for, empty returns false, not null
   * @return true if this search contains any of the keys specified
   */
  public boolean containsAny(Iterable<Identifier> otherIdentifiers) {
    ArgumentChecker.notNull(otherIdentifiers, "otherIdentifiers");
    for (Identifier identifier : otherIdentifiers) {
      if (_identifiers.contains(identifier)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if this search contains the specified key.
   * 
   * @param identifier  the key to search for, null returns false
   * @return true if this search contains the specified key
   */
  public boolean contains(Identifier identifier) {
    return identifier != null && _identifiers.contains(identifier);
  }

  //-------------------------------------------------------------------
  /**
   * Checks if the specified instance can match anything.
   * 
   * @param idSearch  the identifier search, null returns true
   * @return true if the search can match anything
   */
  public static boolean canMatch(final IdentifierSearch idSearch) {
    if (idSearch == null) {
      return true;
    }
    if (idSearch.getSearchType() == IdentifierSearchType.NONE) {
      return true;
    }
    return idSearch.size() > 0;
  }

  //-------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof IdentifierSearch) {
      IdentifierSearch other = (IdentifierSearch) obj;
      return _identifiers.equals(other._identifiers);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _identifiers.hashCode() ^ _searchType.hashCode();
  }

  @Override
  public String toString() {
    return new StrBuilder()
        .append("Search")
        .append("[")
        .appendWithSeparators(_identifiers, ", ")
        .append("]")
        .toString();
  }

  //-------------------------------------------------------------------------
  /**
   * Serializes this identifier search to a Fudge message.
   * 
   * @param factory a message creator, not {@code null}
   * @return the serialized Fudge message
   */
  public FudgeFieldContainer toFudgeMsg(FudgeMsgFactory factory) {
    MutableFudgeFieldContainer msg = factory.newMessage();
    for (Identifier identifier : getIdentifiers()) {
      msg.add("identifier", identifier.toFudgeMsg(factory));
    }
    msg.add("searchType", getSearchType().name());
    return msg;
  }

  /**
   * Deserializes an identifier search from a Fudge message.
   * 
   * @param msg the Fudge message, not null
   * @return the identifier bundle
   */
  public static IdentifierSearch fromFudgeMsg(FudgeFieldContainer msg) {
    Set<Identifier> identifiers = new HashSet<Identifier>();
    for (FudgeField field : msg.getAllByName("identifier")) {
      identifiers.add(Identifier.fromFudgeMsg((FudgeFieldContainer) field.getValue()));
    }
    IdentifierSearchType type = IdentifierSearchType.valueOf(msg.getString("searchType"));
    return new IdentifierSearch(identifiers, type);
  }

}
