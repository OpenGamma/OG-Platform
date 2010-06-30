/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.opengamma.OpenGammaRuntimeException;

/**
 * A class to manage Identifier, IdentifierBundle and UniqueIdentifier references to generic objects.
 * The uniqueIdScheme in the constructor is used to construct monotonically increasing UniqueIdentifier
 * values associated with each object put into the map.
 * @param <T> the type of the object being referred to by the Identifiers
 */
public class IdentifierBundleMapper<T> {
  private Map<Identifier, T> _toMap = new HashMap<Identifier, T>();
  private Map<T, IdentifierBundle> _fromMap = new HashMap<T, IdentifierBundle>();
  private BiMap<UniqueIdentifier, T> _uniqueIdMap = HashBiMap.create();
  private final String _uniqueIdScheme;
  private InMemoryUniqueIdentifierFactory _idFactory;
  
  /**
   * Constructor taking the name of the scheme to use for the unique ids that this class generates.  No
   * @param uniqueIdScheme the scheme to use for the automatically allocated unique ids
   */
  public IdentifierBundleMapper(String uniqueIdScheme) {
    _uniqueIdScheme = uniqueIdScheme;
    _idFactory = new InMemoryUniqueIdentifierFactory(_uniqueIdScheme);
  }
  
  public synchronized UniqueIdentifier add(IdentifierBundle bundle, T obj) {
    
    for (Identifier identifier : bundle) {
      T existing = _toMap.get(identifier);
      if ((existing != null) && (!(existing.equals(obj)))) {
        throw new OpenGammaRuntimeException("Trying to associate object (" + obj + ") with identifier " + identifier + " when already associated with (" + existing + ")");
      }      
    }
    // doing the check first makes sure we don't throw an exception half way through 'registering' an object in the map.
    for (Identifier identifier : bundle) {
      _toMap.put(identifier, obj);
    }
    if (_fromMap.containsKey(obj)) {
      IdentifierBundle existingBundle = _fromMap.get(obj);
      Set<Identifier> unionBundle = new HashSet<Identifier>(existingBundle.getIdentifiers());
      unionBundle.addAll(bundle.getIdentifiers());
      _fromMap.put(obj, new IdentifierBundle(unionBundle));
      return _uniqueIdMap.inverse().get(obj);
    } else {
      _fromMap.put(obj, bundle); // as it's not in the from map, we haven't seen it before so no existing other ids point to it.
      UniqueIdentifier uid = _idFactory.getNextUniqueIdentifier();
      _uniqueIdMap.put(uid, obj);
      return uid;
    }    
  }
  
  public T get(IdentifierBundle bundle) {
    for (Identifier identifier : bundle) {
      if (_toMap.containsKey(identifier)) {
        return _toMap.get(identifier);
      }
    }
    return null;
  }
  
  public T get(Identifier identifier) {
    return _toMap.get(identifier);
  }
  
  public T get(UniqueIdentifier uid) {
    return _uniqueIdMap.get(uid);
  }
  
  public IdentifierBundle getIdentifierBundle(T obj) {
    return _fromMap.get(obj);
  }
  
  public UniqueIdentifier getUniqueIdentifier(T obj) {
    return _uniqueIdMap.inverse().get(obj);
  }
}
