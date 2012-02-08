/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.ImmutableSortedSet;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * An immutable bundle of external identifiers.
 * <p>
 * A bundle allows multiple {@link ExternalId external identifiers} to be grouped together
 * when they all refer to the same conceptual object.
 * For example, a Reuters RIC and Bloomberg Ticker might both refer to the same equity.
 * <p>
 * The bundle holds a <i>set</i> of external identifiers, not a <i>map</i> from scheme to value.
 * This permits multiple values within the same scheme to refer to the same conceptual object.
 * For example, a renamed ticker could be grouped as both the old and new value.
 * In general however, each external identifier in a bundle will be in a different scheme.
 * <p>
 * This class is immutable and thread-safe.
 */
@PublicAPI
public final class ExternalIdBundle
    implements Iterable<ExternalId>, Serializable, Comparable<ExternalIdBundle> {
  static {
    OpenGammaFudgeContext.getInstance().getTypeDictionary().registerClassRename("com.opengamma.id.IdentifierBundle", ExternalIdBundle.class);
  }

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton empty bundle.
   */
  public static final ExternalIdBundle EMPTY = new ExternalIdBundle();

  /**
   * The set of identifiers.
   */
  private final ImmutableSortedSet<ExternalId> _externalIds;
  /**
   * The cached hash code.
   */
  private transient volatile int _hashCode;

  /**
   * Obtains an {@code ExternalIdBundle} from a single scheme and value.
   * This is most useful for testing, as a bundle normally contains more than one identifier.
   * 
   * @param scheme  the scheme of the single external identifier, not empty, not null
   * @param value  the value of the single external identifier, not empty, not null
   * @return the bundle, not null
   */
  public static ExternalIdBundle of(ExternalScheme scheme, String value) {
    return of(ExternalId.of(scheme, value));
  }

  /**
   * Obtains an {@code ExternalIdBundle} from a single scheme and value.
   * This is most useful for testing, as a bundle normally contains more than one identifier.
   * 
   * @param scheme  the scheme of the single external identifier, not empty, not null
   * @param value  the value of the single external identifier, not empty, not null
   * @return the bundle, not null
   */
  public static ExternalIdBundle of(String scheme, String value) {
    return of(ExternalId.of(scheme, value));
  }

  /**
   * Obtains an {@code ExternalIdBundle} from an identifier.
   * 
   * @param externalId  the external identifier to wrap in a bundle, not null
   * @return the bundle, not null
   */
  public static ExternalIdBundle of(ExternalId externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    return new ExternalIdBundle(ImmutableSortedSet.of(externalId));
  }

  /**
   * Obtains an {@code ExternalIdBundle} from an array of identifiers.
   * 
   * @param externalIds  the array of external identifiers, no nulls, not null
   * @return the bundle, not null
   */
  public static ExternalIdBundle of(ExternalId... externalIds) {
    ArgumentChecker.noNulls(externalIds, "externalIds");
    return new ExternalIdBundle(ImmutableSortedSet.copyOf(externalIds));
  }

  /**
   * Obtains an {@code ExternalIdBundle} from a collection of identifiers.
   * 
   * @param externalIds  the collection of external identifiers, no nulls, not null
   * @return the bundle, not null
   */
  public static ExternalIdBundle of(Iterable<ExternalId> externalIds) {
    ArgumentChecker.noNulls(externalIds, "externalIds");
    return create(externalIds);
  }

  /**
   * Parses a list of strings to an {@code ExternalIdBundle}.
   * <p>
   * This uses {@link ExternalId#parse(String)} to parse each string in the input collection.
   * 
   * @param strs  the external identifiers to parse, not null
   * @return the bundle, not null
   * @throws IllegalArgumentException if any identifier cannot be parsed
   */
  public static ExternalIdBundle parse(Iterable<String> strs) {
    ArgumentChecker.noNulls(strs, "strs");
    List<ExternalId> externalIds = new ArrayList<ExternalId>();
    for (String str : strs) {
      externalIds.add(ExternalId.parse(str));
    }
    return create(externalIds);
  }

  /**
   * Obtains an {@code ExternalIdBundle} from a collection of identifiers.
   * 
   * @param externalIds  the collection of external identifiers, validated
   * @return the bundle, not null
   */
  private static ExternalIdBundle create(Iterable<ExternalId> externalIds) {
    return new ExternalIdBundle(ImmutableSortedSet.copyOf(externalIds));
  }

  /**
   * Creates an empty bundle.
   */
  private ExternalIdBundle() {
    _externalIds = ImmutableSortedSet.of();
  }

  /**
   * Creates a bundle from a set of identifiers.
   * 
   * @param identifiers  the set of identifiers, assigned, not null
   */
  private ExternalIdBundle(ImmutableSortedSet<ExternalId> identifiers) {
    _externalIds = identifiers;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the set of identifiers in the bundle.
   * 
   * @return the identifier set, unmodifiable, not null
   */
  public Set<ExternalId> getExternalIds() {
    return _externalIds;
  }

  /**
   * Gets the external identifier for the specified scheme.
   * <p>
   * This returns the first identifier in the internal set that matches.
   * The set is not sorted, so this method is not consistent.
   * 
   * @param scheme  the scheme to query, null returns null
   * @return the identifier, null if not found
   */
  public ExternalId getExternalId(ExternalScheme scheme) {
    for (ExternalId identifier : _externalIds) {
      if (ObjectUtils.equals(scheme, identifier.getScheme())) {
        return identifier;
      }
    }
    return null;
  }

  /**
   * Gets the identifier value for the specified scheme.
   * <p>
   * This returns the first identifier in the internal set that matches.
   * The set is not sorted, so this method is not consistent.
   * 
   * @param scheme  the scheme to query, null returns null
   * @return the identifier value, null if not found
   */
  public String getValue(ExternalScheme scheme) {
    for (ExternalId identifier : _externalIds) {
      if (ObjectUtils.equals(scheme, identifier.getScheme())) {
        return identifier.getValue();
      }
    }
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a new bundle with the specified identifier added.
   * This instance is immutable and unaffected by this method call.
   * 
   * @param externalId  the identifier to add to the returned bundle, not null
   * @return the new bundle, not null
   */
  public ExternalIdBundle withExternalId(ExternalId externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    Set<ExternalId> ids = new HashSet<ExternalId>(_externalIds);
    if (ids.add(externalId) == false) {
      return this;
    }
    return create(ids);
  }

  /**
   * Returns a new bundle with the specified identifier added.
   * This instance is immutable and unaffected by this method call.
   * 
   * @param externalIds  the identifiers to add to the returned bundle, not null
   * @return the new bundle, not null
   */
  public ExternalIdBundle withExternalIds(Iterable<ExternalId> externalIds) {
    ArgumentChecker.notNull(externalIds, "externalIds");
    Set<ExternalId> toAdd = ImmutableSortedSet.copyOf(externalIds);
    Set<ExternalId> ids = new HashSet<ExternalId>(_externalIds);
    if (ids.addAll(toAdd) == false) {
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
  public ExternalIdBundle withoutExternalId(ExternalId externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    Set<ExternalId> ids = new HashSet<ExternalId>(_externalIds);
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
  public ExternalIdBundle withoutScheme(ExternalScheme scheme) {
    Set<ExternalId> ids = new HashSet<ExternalId>(_externalIds.size());
    for (ExternalId id : _externalIds) {
      if (id.isScheme(scheme) == false) {
        ids.add(id);
      }
    }
    return create(ids);
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
   * Returns true if this bundle contains no identifiers.
   * 
   * @return true if this bundle contains no identifiers, false otherwise
   */
  public boolean isEmpty() {
    return _externalIds.isEmpty();
  }

  /**
   * Returns an iterator over the identifiers in the bundle.
   * 
   * @return the identifiers in the bundle, not null
   */
  @Override
  public Iterator<ExternalId> iterator() {
    return _externalIds.iterator();
  }

  /**
   * Checks if this bundle contains all the keys from the specified bundle.
   * 
   * @param bundle  the bundle to search for, empty returns true, not null
   * @return true if this bundle contains all the keys from the specified bundle
   */
  public boolean containsAll(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    for (ExternalId externalId : bundle.getExternalIds()) {
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
  public boolean containsAny(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    for (ExternalId externalId : bundle.getExternalIds()) {
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
  public boolean contains(ExternalId externalId) {
    return externalId != null && _externalIds.contains(externalId);
  }

  /**
   * Converts this bundle to a list of formatted strings.
   * 
   * @return the list of identifiers as strings, not null
   */
  public List<String> toStringList() {
    List<String> list = new ArrayList<String>();
    for (ExternalId id : this) {
      list.add(id.toString());
    }
    return list;
  }

  //-------------------------------------------------------------------
  /**
   * Compares the bundles.
   * 
   * @param other  the other external identifier, not null
   * @return negative if this is less, zero if equal, positive if greater
   */
  @Override
  public int compareTo(ExternalIdBundle other) {
    final Set<ExternalId> mySet = getExternalIds();
    final Set<ExternalId> otherSet = other.getExternalIds();
    if (mySet.size() < otherSet.size()) {
      return -1;
    }
    if (mySet.size() > otherSet.size()) {
      return 1;
    }
    final List<ExternalId> myList = new ArrayList<ExternalId>(mySet); // already sorted
    final List<ExternalId> otherList = new ArrayList<ExternalId>(otherSet); // already sorted
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
    if (obj instanceof ExternalIdBundle) {
      ExternalIdBundle other = (ExternalIdBundle) obj;
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
        .append("Bundle")
        .append("[")
        .appendWithSeparators(_externalIds, ", ")
        .append("]")
        .toString();
  }

  //-------------------------------------------------------------------------
  /**
   * This is for more efficient code within the .proto representations of securities, allowing this class
   * to be used directly as a message type instead of through the serialization framework.
   * 
   * @param serializer  the serializer, not null
   * @param msg  the message to populate, not null
   * @deprecated Use builder
   */
  @Deprecated
  public void toFudgeMsg(final FudgeSerializer serializer, final MutableFudgeMsg msg) {
    ExternalIdBundleFudgeBuilder.toFudgeMsg(serializer, this, msg);
  }

  /**
   * This is for more efficient code within the .proto representations of securities, allowing this class
   * to be used directly as a message type instead of through the serialization framework.
   * 
   * @param deserializer  the deserializer, not null
   * @param msg  the message to decode, not null
   * @return the created object, not null
   * @deprecated Use builder
   */
  @Deprecated
  public static ExternalIdBundle fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return ExternalIdBundleFudgeBuilder.fromFudgeMsg(deserializer, msg);
  }

}
