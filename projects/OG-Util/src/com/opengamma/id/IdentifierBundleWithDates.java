/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.text.StrBuilder;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.util.ArgumentChecker;

/**
 * An immutable bundle of identifiers with dates
 * <p>
 * This represent a bundle of {@code IdentifierWithDates} where multiple identifiers are used
 * that all refer to the same conceptual object.
 * Each bundle will typically be in a different scheme.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class IdentifierBundleWithDates implements Iterable<IdentifierWithDates>, Serializable, Comparable<IdentifierBundleWithDates> {

  /**
   * Singleton empty bundle.
   */
  public static final IdentifierBundleWithDates EMPTY = new IdentifierBundleWithDates();
  /**
   * Fudge message key for the identifier set.
   */
  public static final String ID_FUDGE_FIELD_NAME = "ID";

  /**
   * The set of identifiers.
   */
  private final Set<IdentifierWithDates> _identifiers;
  /**
   * The cached hash code.
   */
  private transient volatile int _hashCode;

  /**
   * Creates a bundle from a collection of identifiers.
   * 
   * @param identifiers  the collection of identifiers, not null, no nulls in array
   * @return the identifier bundle, not null
   */
  public static IdentifierBundleWithDates of(IdentifierWithDates... identifiers) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    return new IdentifierBundleWithDates(identifiers);
  }

  /**
   * Create a bundle from an bundle of identifiers.
   * 
   * @param identifierBundle the identifier bundle, not null
   * @return the identifier bundle with dates set to null, not null
   */
  public static IdentifierBundleWithDates of(IdentifierBundle identifierBundle) {
    ArgumentChecker.notNull(identifierBundle, "identifierBundle");
    Set<IdentifierWithDates> identifiers = new HashSet<IdentifierWithDates>();
    for (Identifier identifier : identifierBundle) {
      identifiers.add(IdentifierWithDates.of(identifier, null, null));
    }
    return new IdentifierBundleWithDates(identifiers);
  }

  /**
   * Creates an empty bundle.
   */
  public IdentifierBundleWithDates() {
    _identifiers = Collections.emptySet();
    _hashCode = calcHashCode();
  }

  /**
   * Creates a bundle from a single identifier.
   * 
   * @param identifier  the identifier, null returns an empty bundle
   */
  public IdentifierBundleWithDates(IdentifierWithDates identifier) {
    if (identifier == null) {
      _identifiers = Collections.emptySet();
    } else {
      _identifiers = Collections.singleton(identifier);
    }
    _hashCode = calcHashCode();
  }

  /**
   * Creates a bundle from an array of identifiers.
   * 
   * @param identifiers  the array of identifiers, null returns an empty bundle
   */
  public IdentifierBundleWithDates(IdentifierWithDates... identifiers) {
    if ((identifiers == null) || (identifiers.length == 0)) {
      _identifiers = Collections.emptySet();
    } else {
      ArgumentChecker.noNulls(identifiers, "identifiers");
      _identifiers = Collections.unmodifiableSet(new TreeSet<IdentifierWithDates>(Arrays.asList(identifiers)));
    }
    _hashCode = calcHashCode();
  }

  /**
   * Creates a bundle from a collection of identifiers.
   * 
   * @param identifiers  the collection of identifiers, null returns an empty bundle, no nulls in array
   */
  public IdentifierBundleWithDates(Collection<? extends IdentifierWithDates> identifiers) {
    if (identifiers == null) {
      _identifiers = Collections.emptySet();
    } else {
      ArgumentChecker.noNulls(identifiers, "identifiers");
      _identifiers = Collections.unmodifiableSet(new TreeSet<IdentifierWithDates>(identifiers));
    }
    _hashCode = calcHashCode();
  }

  /**
   * Recalculate the hash code on deserialization.
   * 
   * @param in  the input stream, not null
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    _hashCode = calcHashCode();
  }

  /**
   * Calculates the hash code.
   * 
   * @return the hash code
   */
  private int calcHashCode() {
    return 31 + _identifiers.hashCode();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the collection of identifiers in the bundle.
   * 
   * @return the identifier collection, unmodifiable, not null
   */
  public Set<IdentifierWithDates> getIdentifiers() {
    return _identifiers;
  }
  
  /**
   * Returns a new bundle with the specified identifier added.
   * This instance is immutable and unaffected by this method call.
   * 
   * @param identifier  the identifier to add to the returned bundle, not null
   * @return the new bundle, not null
   */
  public IdentifierBundleWithDates withIdentifier(IdentifierWithDates identifier) {
    Set<IdentifierWithDates> ids = new HashSet<IdentifierWithDates>(_identifiers);
    ids.add(identifier);
    return new IdentifierBundleWithDates(ids);
  }
  
  /**
   * Returns a new bundle with the specified identifier removed.
   * This instance is immutable and unaffected by this method call.
   * 
   * @param identifier  the identifier to remove from the returned bundle, null ignored
   * @return the new bundle, not null
   */
  public IdentifierBundleWithDates withoutIdentifier(IdentifierWithDates identifier) {
    Set<IdentifierWithDates> ids = new HashSet<IdentifierWithDates>(_identifiers);
    ids.remove(identifier);
    return new IdentifierBundleWithDates(ids);
  }
  
  /**
   * Gets the number of identifiers in the bundle.
   * 
   * @return the bundle size, zero or greater
   */
  public int size() {
    return _identifiers.size();
  }
  
  /**
   * Returns an iterator over the identifiers in the bundle.
   * 
   * @return the identifiers in the bundle, not null
   */
  @Override
  public Iterator<IdentifierWithDates> iterator() {
    return _identifiers.iterator();
  }
  
  /**
   * Checks if this bundle contains any key from the specified bundle.
   * 
   * @param bundle  the bundle to search for, not null
   * @return true if this bundle contains any key from the specified bundle
   */
  public boolean containsAny(IdentifierBundleWithDates bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    for (IdentifierWithDates identifier : bundle.getIdentifiers()) {
      if (_identifiers.contains(identifier)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Checks if this bundle contains the specified key.
   * 
   * @param identifier  the key to search for, null returns false
   * @return true if this bundle contains any key from the specified bundle
   */
  public boolean contains(IdentifierWithDates identifier) {
    return identifier != null && _identifiers.contains(identifier);
  }

  /**
   * Converts this bundle to a list of formatted strings.
   * 
   * @return the list of identifiers as strings, not null
   */
  public List<String> toStringList() {
    List<String> list = new ArrayList<String>();
    for (IdentifierWithDates id : this) {
      list.add(id.toString());
    }
    return list;
  }

  /**
   * Returns the IdentifierBundle without dates.
   * 
   * @return the equivalent bundle, without the dates, not null
   */
  public IdentifierBundle asIdentifierBundle() {
    Set<Identifier> ids = new HashSet<Identifier>();
    for (IdentifierWithDates identifier : _identifiers) {
      ids.add(identifier.asIdentifier());
    }
    return IdentifierBundle.of(ids);
  }

  //-------------------------------------------------------------------
  @Override
  public int compareTo(IdentifierBundleWithDates other) {
    final Set<IdentifierWithDates> mySet = getIdentifiers();
    final Set<IdentifierWithDates> otherSet = other.getIdentifiers();
    if (mySet.size() < otherSet.size()) {
      return -1;
    }
    if (mySet.size() > otherSet.size()) {
      return 1;
    }
    final List<IdentifierWithDates> myList = new ArrayList<IdentifierWithDates>(mySet);  // already sorted as TreeSet
    final List<IdentifierWithDates> otherList = new ArrayList<IdentifierWithDates>(otherSet);  // already sorted as TreeSet
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
    if (obj instanceof IdentifierBundleWithDates) {
      IdentifierBundleWithDates other = (IdentifierBundleWithDates) obj;
      return _identifiers.equals(other._identifiers);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _hashCode;
  }

  @Override
  public String toString() {
    return new StrBuilder()
      .append("BundleWithDates")
      .append("[")
      .appendWithSeparators(_identifiers, ", ")
      .append("]")
      .toString();
  }

  //-------------------------------------------------------------------------
  public MutableFudgeFieldContainer toFudgeMsg(final FudgeMessageFactory factory, final MutableFudgeFieldContainer message) {
    ArgumentChecker.notNull(factory, "factory");
    ArgumentChecker.notNull(message, "message");
    for (IdentifierWithDates identifier : getIdentifiers()) {
      message.add(ID_FUDGE_FIELD_NAME, identifier.toFudgeMsg(factory));
    }
    return message;
  }

  /**
   * Serializes this pair to a Fudge message.
   * @param factory  the Fudge context, not null
   * @return the Fudge message, not null
   */
  public FudgeFieldContainer toFudgeMsg(FudgeMessageFactory factory) {
    return toFudgeMsg(factory, factory.newMessage());
  }

  /**
   * Deserializes this pair from a Fudge message.
   * @param msg  the Fudge message, not null
   * @return the pair, not null
   */
  public static IdentifierBundleWithDates fromFudgeMsg(FudgeFieldContainer msg) {
    Set<IdentifierWithDates> identifiers = new HashSet<IdentifierWithDates>();
    for (FudgeField field : msg.getAllByName(ID_FUDGE_FIELD_NAME)) {
      if (field.getValue() instanceof FudgeFieldContainer == false) {
        throw new IllegalArgumentException("Message provider has field named " + ID_FUDGE_FIELD_NAME + " which doesn't contain a sub-Message");
      }
      identifiers.add(IdentifierWithDates.fromFudgeMsg((FudgeFieldContainer) field.getValue()));
    }
    return new IdentifierBundleWithDates(identifiers);
  }

}
