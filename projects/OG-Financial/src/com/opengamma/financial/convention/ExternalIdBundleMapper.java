/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;

/**
 * A class to manage {@code ExternalId}, {@code ExternalIdBundle} and {@code UniqueId}
 * references to generic objects.
 * <p>
 * This is an optimized data structure for storing multiple different references to the same object.
 * The mapper also creates the unique identifier using the specified scheme.
 * <p>
 * This class is mutable and thread-safe via synchronization.
 * 
 * @param <T> the type of the object being referred to by the identifiers
 */
/* package */ class ExternalIdBundleMapper<T> {

  private final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();
  private final WriteLock _writeLock = _lock.writeLock();
  private final ReadLock _readLock = _lock.readLock();
  
  private final Multimap<ExternalId, T> _toMap = HashMultimap.create();
  private final Multimap<T, ExternalId> _fromMap = HashMultimap.create();
  private final BiMap<UniqueId, T> _uniqueIdMap = HashBiMap.create();
  private final UniqueIdSupplier _idSupplier;

  /**
   * Constructor taking the name of the scheme to use for the unique identifiers that this class generates.
   * 
   * @param uniqueIdScheme  the scheme to use for the automatically allocated unique identifiers
   */
  ExternalIdBundleMapper(String uniqueIdScheme) {
    _idSupplier = new UniqueIdSupplier(uniqueIdScheme);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a mapping from a bundle to an object.
   * 
   * @param bundle  the bundle that the object is referred to as, not null
   * @param obj  the object being referred to, not null
   * @return the created unique identifier, not null
   */
  UniqueId add(ExternalIdBundle bundle, T obj) {
    _writeLock.lock();
    try {
      _fromMap.putAll(obj, bundle.getExternalIds());
      for (ExternalId identifier : bundle.getExternalIds()) {
        _toMap.put(identifier, obj);
      }
      if (_uniqueIdMap.inverse().containsKey(obj)) {
        return _uniqueIdMap.inverse().get(obj);
      } else {
        UniqueId uniqueId = _idSupplier.get();
        _uniqueIdMap.put(uniqueId, obj);
        return uniqueId;
      }
    } finally {
      _writeLock.unlock();
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
  Collection<T> get(ExternalIdBundle bundle) {
    _readLock.lock();
    try {
      // TODO: semantics are wrong
      Collection<T> results = new HashSet<T>();
      for (ExternalId identifier : bundle) {
        if (_toMap.containsKey(identifier)) {
          results.addAll(_toMap.get(identifier));
        }
      }
      return results;
    } finally {
      _readLock.unlock();
    }
  }

  /**
   * Gets those objects which match the specified bundle.
   * 
   * @param externalId  the external identifier to search for, not null
   * @return the matching objects, not null
   */
  Collection<T> get(ExternalId externalId) {
    _readLock.lock();
    try {
      return _toMap.get(externalId);
    } finally {
      _readLock.unlock();
    }
  }

  /**
   * Gets those objects which match the specified unique identifier.
   * 
   * @param uniqueId  the unique identifier to search for, not null
   * @return the matching objects, not null
   */
  T get(UniqueId uniqueId) {
    _readLock.lock();
    try {
      return _uniqueIdMap.get(uniqueId);
    } finally {
      _readLock.unlock();
    }
  }
  
  /**
   * Gets all objects in the mapper.
   * 
   * @return an unmodifiable collection of all the objects in the mapper, not null 
   */
  Collection<T> getAll() {
    _readLock.lock();
    try {
      return ImmutableList.copyOf(_toMap.values());
    } finally {
      _readLock.unlock();
    }
  }

}
