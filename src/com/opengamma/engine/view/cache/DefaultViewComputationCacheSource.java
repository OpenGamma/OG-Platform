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

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * A {@link ViewComputationCacheSource} implementation based on {@link DefaultViewComputationCache} with
 * the given {@link IdentifierMap} and {@link BinaryDataStore} objects supplied by the given
 * {@link BinaryDataStoreFactory} instance.
 */
public class DefaultViewComputationCacheSource implements ViewComputationCacheSource {
  private final IdentifierMap _identifierMap;
  private final FudgeContext _fudgeContext;

  private final ConcurrentMap<ViewComputationCacheKey, DefaultViewComputationCache> _cachesByKey = new ConcurrentHashMap<ViewComputationCacheKey, DefaultViewComputationCache>();
  private final ConcurrentMap<Pair<String, Long>, Set<BinaryDataStore>> _activeStores = new ConcurrentHashMap<Pair<String, Long>, Set<BinaryDataStore>>();
  private final ReentrantLock _cacheManagementLock = new ReentrantLock();
  private final BinaryDataStoreFactory _privateDataStoreFactory;
  private final BinaryDataStoreFactory _sharedDataStoreFactory;

  protected DefaultViewComputationCacheSource(final IdentifierMap identifierMap, final FudgeContext fudgeContext, final BinaryDataStoreFactory dataStoreFactory) {
    this(identifierMap, fudgeContext, dataStoreFactory, dataStoreFactory);
  }

  public DefaultViewComputationCacheSource(final IdentifierMap identifierMap, final FudgeContext fudgeContext, final BinaryDataStoreFactory privateDataStoreFactory,
      final BinaryDataStoreFactory sharedDataStoreFactory) {
    ArgumentChecker.notNull(identifierMap, "Identifier map");
    ArgumentChecker.notNull(fudgeContext, "Fudge context");
    ArgumentChecker.notNull(privateDataStoreFactory, "Private data store factory");
    ArgumentChecker.notNull(sharedDataStoreFactory, "Shared data store factory");
    _identifierMap = identifierMap;
    _fudgeContext = fudgeContext;
    _privateDataStoreFactory = privateDataStoreFactory;
    _sharedDataStoreFactory = sharedDataStoreFactory;
  }

  /**
   * Gets the identifierMap field.
   * @return the identifierMap
   */
  public IdentifierMap getIdentifierMap() {
    return _identifierMap;
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
    final DefaultViewComputationCache cache = _cachesByKey.get(key);
    final InMemoryIdentifierMap identifierMap = new InMemoryIdentifierMap();
    final InMemoryBinaryDataStore dataStore = new InMemoryBinaryDataStore();
    for (Pair<ValueSpecification, byte[]> value : cache) {
      dataStore.put(identifierMap.getIdentifier(value.getKey()), value.getValue());
    }
    return new DefaultViewComputationCache(identifierMap, dataStore, dataStore, getFudgeContext());
  }

  @Override
  public DefaultViewComputationCache getCache(String viewName, String calculationConfigurationName, long timestamp) {
    ViewComputationCacheKey key = new ViewComputationCacheKey(viewName, calculationConfigurationName, timestamp);
    DefaultViewComputationCache cache = _cachesByKey.get(key);
    if (cache == null) {
      cache = constructCache(key);
    }
    return cache;
  }

  protected DefaultViewComputationCache constructCache(ViewComputationCacheKey key) {
    DefaultViewComputationCache cache = null;
    _cacheManagementLock.lock();
    try {
      // Have to double-check. Too expensive to construct otherwise.
      cache = _cachesByKey.get(key);
      if (cache == null) {
        final BinaryDataStore privateDataStore = _privateDataStoreFactory.createDataStore(key);
        addDataStoreForRelease(key, privateDataStore);
        final BinaryDataStore sharedDataStore;
        if (_privateDataStoreFactory == _sharedDataStoreFactory) {
          // If factories are the same, don't create another
          sharedDataStore = privateDataStore;
        } else {
          sharedDataStore = _sharedDataStoreFactory.createDataStore(key);
          addDataStoreForRelease(key, sharedDataStore);
        }
        cache = createViewComputationCache(getIdentifierMap(), privateDataStore, sharedDataStore, getFudgeContext());
        _cachesByKey.put(key, cache);
      }
    } finally {
      _cacheManagementLock.unlock();
    }
    return cache;
  }

  /**
   * Override this method if you need to create a different sub-class of {@link DefaultViewComputationCache}.
   * 
   * @param identifierMap the identifier map
   * @param privateDataStore the binary data store for private values
   * @param sharedDataStore the binary data store for shared values 
   * @param fudgeContext the Fudge context
   * @return a new {@link DefaultViewComputationCache} instance
   */
  protected DefaultViewComputationCache createViewComputationCache(final IdentifierMap identifierMap, final BinaryDataStore privateDataStore, final BinaryDataStore sharedDataStore,
      final FudgeContext fudgeContext) {
    return new DefaultViewComputationCache(identifierMap, privateDataStore, sharedDataStore, fudgeContext);
  }

  protected void addDataStoreForRelease(ViewComputationCacheKey key, BinaryDataStore dataStore) {
    Pair<String, Long> releaseKey = Pair.of(key.getViewName(), key.getSnapshotTimestamp());
    Set<BinaryDataStore> dataStores = _activeStores.get(releaseKey);
    if (dataStores == null) {
      dataStores = new HashSet<BinaryDataStore>();
      _activeStores.put(releaseKey, dataStores);
    }
    dataStores.add(dataStore);
  }

  @Override
  public void releaseCaches(String viewName, long timestamp) {
    ArgumentChecker.notNull(viewName, "View name");
    Pair<String, Long> releaseKey = Pair.of(viewName, timestamp);
    Set<BinaryDataStore> dataStores = null;
    _cacheManagementLock.lock();
    try {
      dataStores = _activeStores.remove(releaseKey);
    } finally {
      _cacheManagementLock.unlock();
    }

    if (dataStores == null) {
      return;
    }

    for (BinaryDataStore dataStore : dataStores) {
      dataStore.delete();
    }
  }

  // TODO 2010-08-18 There's a memory leak here; the _cachesByKey never gets emptied of things

}
