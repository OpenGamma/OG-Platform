/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
 * A class to manage Identifier, IdentifierBundle and UniqueIdentifier references to generic objects.
 * The uniqueIdScheme in the constructor is used to construct monotonically increasing UniqueIdentifier
 * values associated with each object put into the map.
 * @param <T> the type of the object being referred to by the Identifiers
 */
public class IdentifierBundleMapper<T> {
  private Multimap<Identifier, T> _toMap = HashMultimap.create();
  private Multimap<T, Identifier> _fromMap = HashMultimap.create();
  private BiMap<UniqueIdentifier, T> _uniqueIdMap = HashBiMap.create();
  private final String _uniqueIdScheme;
  private UniqueIdentifierSupplier _idSupplier;
  
  /**
   * Constructor taking the name of the scheme to use for the unique ids that this class generates.  No
   * @param uniqueIdScheme the scheme to use for the automatically allocated unique ids
   */
  public IdentifierBundleMapper(String uniqueIdScheme) {
    _uniqueIdScheme = uniqueIdScheme;
    _idSupplier = new UniqueIdentifierSupplier(_uniqueIdScheme);
  }
  
  public synchronized UniqueIdentifier add(IdentifierBundle bundle, T obj) {
    _fromMap.putAll(obj, bundle.getIdentifiers());
    for (Identifier identifier : bundle.getIdentifiers()) {
      _toMap.put(identifier, obj);
    }
    if (_uniqueIdMap.inverse().containsKey(obj)) {
      return _uniqueIdMap.inverse().get(obj);
    } else {
      UniqueIdentifier uniqueId = _idSupplier.get();
      _uniqueIdMap.put(uniqueId, obj);
      return uniqueId;
    }
  }
  
  public Collection<T> get(IdentifierBundle bundle) {
    Collection<T> results = new HashSet<T>();
    for (Identifier identifier : bundle) {
      if (_toMap.containsKey(identifier)) {
        results.addAll(_toMap.get(identifier));
      }
    }
    return results;
  }
  
  public Collection<T> get(Identifier identifier) {
    return _toMap.get(identifier);
  }
  
  public T get(UniqueIdentifier uid) {
    return _uniqueIdMap.get(uid);
  }
  
  public IdentifierBundle getIdentifierBundle(T obj) {
    return new IdentifierBundle(_fromMap.get(obj));
  }
  
  public UniqueIdentifier getUniqueIdentifier(T obj) {
    return _uniqueIdMap.inverse().get(obj);
  }
}
