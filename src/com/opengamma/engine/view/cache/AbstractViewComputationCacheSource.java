/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import org.fudgemsg.FudgeContext;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * A base class for all {@link ViewComputationCacheSource} implementations that are based on
 * {@link StandardViewComputationCache}.
 */
public abstract class AbstractViewComputationCacheSource implements ViewComputationCacheSource {
  private final ValueSpecificationIdentifierSource _identifierSource;
  private final FudgeContext _fudgeContext;

  private final ConcurrentMap<ViewComputationCacheKey, StandardViewComputationCache> _cachesByKey = new ConcurrentHashMap<ViewComputationCacheKey, StandardViewComputationCache>();
  private final ConcurrentMap<Pair<String, Long>, Set<ValueSpecificationIdentifierBinaryDataStore>> _activeStores = new ConcurrentHashMap<Pair<String, Long>, Set<ValueSpecificationIdentifierBinaryDataStore>>();
  private final ReentrantLock _cacheManagementLock = new ReentrantLock();

  protected AbstractViewComputationCacheSource(ValueSpecificationIdentifierSource identifierSource, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(identifierSource, "Identifier source");
    ArgumentChecker.notNull(fudgeContext, "Fudge context");
    _identifierSource = identifierSource;
    _fudgeContext = fudgeContext;
  }

  /**
   * Gets the identifierSource field.
   * @return the identifierSource
   */
  public ValueSpecificationIdentifierSource getIdentifierSource() {
    return _identifierSource;
  }

  /**
   * Gets the fudgeContext field.
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public ViewComputationCache cloneCache(String viewName, String calculationConfigurationName, long timestamp) {
    final ViewComputationCacheKey key = new ViewComputationCacheKey(viewName, calculationConfigurationName, timestamp);
    final StandardViewComputationCache cache = _cachesByKey.get(key);
    final MapValueSpecificationIdentifierSource identifierSource = new MapValueSpecificationIdentifierSource();
    final MapValueSpecificationIdentifierBinaryDataStore dataStore = new MapValueSpecificationIdentifierBinaryDataStore();
    for (Pair<ValueSpecification, byte[]> value : cache) {
      dataStore.put(identifierSource.getIdentifier(value.getKey()), value.getValue());
    }
    return new StandardViewComputationCache(identifierSource, dataStore, getFudgeContext());
  }

  @Override
  public StandardViewComputationCache getCache(String viewName, String calculationConfigurationName, long timestamp) {
    ViewComputationCacheKey key = new ViewComputationCacheKey(viewName, calculationConfigurationName, timestamp);
    StandardViewComputationCache cache = _cachesByKey.get(key);
    if (cache == null) {
      cache = constructCache(key);
    }
    return cache;
  }

  protected StandardViewComputationCache constructCache(ViewComputationCacheKey key) {
    StandardViewComputationCache cache = null;
    _cacheManagementLock.lock();
    try {
      // Have to double-check. Too expensive to construct otherwise.
      cache = _cachesByKey.get(key);
      if (cache == null) {
        ValueSpecificationIdentifierBinaryDataStore dataStore = constructDataStore(key);
        addDataStoreForRelease(key, dataStore);
        cache = new StandardViewComputationCache(getIdentifierSource(), dataStore, getFudgeContext());
        _cachesByKey.put(key, cache);
      }
    } finally {
      _cacheManagementLock.unlock();
    }
    return cache;
  }

  protected abstract ValueSpecificationIdentifierBinaryDataStore constructDataStore(ViewComputationCacheKey key);

  protected void addDataStoreForRelease(ViewComputationCacheKey key, ValueSpecificationIdentifierBinaryDataStore dataStore) {
    Pair<String, Long> releaseKey = Pair.of(key.getViewName(), key.getSnapshotTimestamp());
    Set<ValueSpecificationIdentifierBinaryDataStore> dataStores = _activeStores.get(releaseKey);
    if (dataStores == null) {
      dataStores = new HashSet<ValueSpecificationIdentifierBinaryDataStore>();
      _activeStores.put(releaseKey, dataStores);
    }
    dataStores.add(dataStore);
  }

  @Override
  public void releaseCaches(String viewName, long timestamp) {
    ArgumentChecker.notNull(viewName, "View name");
    Pair<String, Long> releaseKey = Pair.of(viewName, timestamp);
    Set<ValueSpecificationIdentifierBinaryDataStore> dataStores = null;
    _cacheManagementLock.lock();
    try {
      dataStores = _activeStores.remove(releaseKey);
    } finally {
      _cacheManagementLock.unlock();
    }

    if (dataStores == null) {
      return;
    }

    for (ValueSpecificationIdentifierBinaryDataStore dataStore : dataStores) {
      dataStore.delete();
    }
  }

}
