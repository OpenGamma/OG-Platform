/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.util.Collection;
import java.util.HashSet;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * A class to manage {@code Identifier}, {@code IdentifierBundle} and {@code UniqueId}
 * references to generic objects.
 * <p>
 * This is an optimized data structure for storing multiple different references to the same object.
 * The mapper also creates the unique identifier using the specified scheme.
 * <p>
 * This class is mutable and thread-safe via synchronization.
 * 
 * @param <T> the type of the object being referred to by the Identifiers
 */
public class IdentifierBundleMapper<T> {

  private final Multimap<Identifier, T> _toMap = HashMultimap.create();
  private final Multimap<T, Identifier> _fromMap = HashMultimap.create();
  private final BiMap<UniqueId, T> _uniqueIdMap = HashBiMap.create();
  private final String _uniqueIdScheme;
  private final UniqueIdSupplier _idSupplier;

  /**
   * Constructor taking the name of the scheme to use for the unique identifiers that this class generates.
   * 
   * @param uniqueIdScheme  the scheme to use for the automatically allocated unique identifiers
   */
  public IdentifierBundleMapper(String uniqueIdScheme) {
    _uniqueIdScheme = uniqueIdScheme;
    _idSupplier = new UniqueIdSupplier(_uniqueIdScheme);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a mapping from a bundle to an object.
   * 
   * @param bundle  the bundle that the object is referred to as, not null
   * @param obj  the object being referred to, not null
   * @return the created unique identifier, not null
   */
  public synchronized UniqueId add(IdentifierBundle bundle, T obj) {
    _fromMap.putAll(obj, bundle.getIdentifiers());
    for (Identifier identifier : bundle.getIdentifiers()) {
      _toMap.put(identifier, obj);
    }
    if (_uniqueIdMap.inverse().containsKey(obj)) {
      return _uniqueIdMap.inverse().get(obj);
    } else {
      UniqueId uniqueId = _idSupplier.get();
      _uniqueIdMap.put(uniqueId, obj);
      return uniqueId;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets those objects which match the specified bundle.
   * <p>
   * Note that this method erroneously treats a bundle as a collection of individual identifiers.
   * 
   * @param bundle  the bundle to search for, not null
   * @return the matching objects, not null
   */
  public Collection<T> get(IdentifierBundle bundle) {
    // TODO: semantics are wrong
    Collection<T> results = new HashSet<T>();
    for (Identifier identifier : bundle) {
      if (_toMap.containsKey(identifier)) {
        results.addAll(_toMap.get(identifier));
      }
    }
    return results;
  }

  /**
   * Gets those objects which match the specified bundle.
   * 
   * @param identifier  the identifier to search for, not null
   * @return the matching objects, not null
   */
  public Collection<T> get(Identifier identifier) {
    return _toMap.get(identifier);
  }

  /**
   * Gets those objects which match the specified unique identifier.
   * 
   * @param uniqueId  the unique identifier to search for, not null
   * @return the matching objects, not null
   */
  public T get(UniqueId uniqueId) {
    return _uniqueIdMap.get(uniqueId);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the bundle of identifiers associated with the object.
   * 
   * @param obj  the object to search for, not null
   * @return the matching bundle, not null
   */
  public IdentifierBundle getIdentifierBundle(T obj) {
    return IdentifierBundle.of(_fromMap.get(obj));
  }

  /**
   * Gets the unique identifier associated with the object.
   * 
   * @param obj  the object to search for, not null
   * @return the matching unique identifier, not null
   */
  public UniqueId getUniqueId(T obj) {
    return _uniqueIdMap.inverse().get(obj);
  }

}
