/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * A {@link ViewComputationCacheSource} implementation based on {@link DefaultViewComputationCache} with
 * the given {@link IdentifierMap} and {@link FudgeMessageStore} objects supplied by the given
 * {@link FudgeMessageStoreFactory} instance.
 */
public class DefaultViewComputationCacheSource implements ViewComputationCacheSource {

  /**
   * Callback to receive notification when a computation cycle's caches are released.
   */
  public interface ReleaseCachesCallback {

    void onReleaseCaches(UniqueId viewCycleId);

  }

  /**
   * Callback to locate missing data.
   */
  public interface MissingValueLoader {

    FudgeMsg findMissingValue(ViewComputationCacheKey cache, long identifier);

    Map<Long, FudgeMsg> findMissingValues(ViewComputationCacheKey cache, Collection<Long> identifier);

  }

  private final IdentifierMap _identifierMap;
  private final FudgeContext _fudgeContext;

  private final ConcurrentMap<ViewComputationCacheKey, DefaultViewComputationCache> _cachesByKey = new ConcurrentHashMap<ViewComputationCacheKey, DefaultViewComputationCache>();
  private final Map<UniqueId, List<ViewComputationCacheKey>> _activeCachesByCycle = new HashMap<UniqueId, List<ViewComputationCacheKey>>();
  private final ReentrantLock _cacheManagementLock = new ReentrantLock();
  private final FudgeMessageStoreFactory _privateDataStoreFactory;
  private final FudgeMessageStoreFactory _sharedDataStoreFactory;

  private ReleaseCachesCallback _releaseCachesCallback;
  private MissingValueLoader _missingValueLoader;

  protected DefaultViewComputationCacheSource(final IdentifierMap identifierMap, final FudgeContext fudgeContext,
      final FudgeMessageStoreFactory dataStoreFactory) {
    this(identifierMap, fudgeContext, dataStoreFactory, dataStoreFactory);
  }

  public DefaultViewComputationCacheSource(final IdentifierMap identifierMap, final FudgeContext fudgeContext,
      final FudgeMessageStoreFactory privateDataStoreFactory, final FudgeMessageStoreFactory sharedDataStoreFactory) {
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
  public ViewComputationCache cloneCache(UniqueId viewCycleId, String calculationConfigurationName) {
    final ViewComputationCacheKey key = new ViewComputationCacheKey(viewCycleId, calculationConfigurationName);
    final DefaultViewComputationCache cache = _cachesByKey.get(key);
    final InMemoryIdentifierMap identifierMap = new InMemoryIdentifierMap();
    final FudgeMessageStore dataStore = new DefaultFudgeMessageStore(new InMemoryBinaryDataStore(), getFudgeContext());
    for (Pair<ValueSpecification, FudgeMsg> value : cache) {
      dataStore.put(identifierMap.getIdentifier(value.getKey()), value.getValue());
    }
    return new DefaultViewComputationCache(identifierMap, dataStore, dataStore, getFudgeContext());
  }

  @Override
  public DefaultViewComputationCache getCache(UniqueId viewCycleId, String calculationConfigurationName) {
    return getCache(new ViewComputationCacheKey(viewCycleId, calculationConfigurationName));
  }

  public DefaultViewComputationCache getCache(final ViewComputationCacheKey key) {
    DefaultViewComputationCache cache = findCache(key);
    if (cache == null) {
      cache = constructCache(key);
    }
    return cache;
  }

  protected DefaultViewComputationCache findCache(UniqueId viewCycleId, String calculationConfigurationName) {
    return findCache(new ViewComputationCacheKey(viewCycleId, calculationConfigurationName));
  }

  protected DefaultViewComputationCache findCache(final ViewComputationCacheKey key) {
    return _cachesByKey.get(key);
  }

  protected DefaultViewComputationCache constructCache(final ViewComputationCacheKey key) {
    DefaultViewComputationCache cache = null;
    _cacheManagementLock.lock();
    try {
      // Have to double-check. Too expensive to construct otherwise.
      cache = findCache(key);
      if (cache == null) {
        final FudgeMessageStore privateDataStore = _privateDataStoreFactory.createMessageStore(key);
        final FudgeMessageStore sharedDataStore = (_privateDataStoreFactory == _sharedDataStoreFactory) ? privateDataStore
            : _sharedDataStoreFactory.createMessageStore(key);
        cache = createViewComputationCache(getIdentifierMap(), privateDataStore, sharedDataStore, getFudgeContext());
        _cachesByKey.put(key, cache);
        List<ViewComputationCacheKey> caches = _activeCachesByCycle.get(key.getViewCycleId());
        if (caches == null) {
          caches = new LinkedList<ViewComputationCacheKey>();
          _activeCachesByCycle.put(key.getViewCycleId(), caches);
        }
        caches.add(key);
        final MissingValueLoader loader = getMissingValueLoader();
        if (loader != null) {
          cache.setMissingValueLoader(new DefaultViewComputationCache.MissingValueLoader() {

            @Override
            public FudgeMsg findMissingValue(final long identifier) {
              return loader.findMissingValue(key, identifier);
            }

            @Override
            public Map<Long, FudgeMsg> findMissingValues(Collection<Long> identifiers) {
              return loader.findMissingValues(key, identifiers);
            }

          });
        }
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
   * @param privateDataStore the message store for private values
   * @param sharedDataStore the message store for shared values 
   * @param fudgeContext the Fudge context
   * @return a new {@link DefaultViewComputationCache} instance
   */
  protected DefaultViewComputationCache createViewComputationCache(final IdentifierMap identifierMap,
      final FudgeMessageStore privateDataStore, final FudgeMessageStore sharedDataStore, final FudgeContext fudgeContext) {
    return new DefaultViewComputationCache(identifierMap, privateDataStore, sharedDataStore, fudgeContext);
  }

  @Override
  public void releaseCaches(UniqueId viewCycleId) {
    ArgumentChecker.notNull(viewCycleId, "viewCycleId");
    final ReleaseCachesCallback callback = getReleaseCachesCallback();
    if (callback != null) {
      callback.onReleaseCaches(viewCycleId);
    }
    DefaultViewComputationCache[] caches;
    _cacheManagementLock.lock();
    try {
      final List<ViewComputationCacheKey> cacheKeys = _activeCachesByCycle.remove(viewCycleId);
      if (cacheKeys == null) {
        return;
      }
      caches = new DefaultViewComputationCache[cacheKeys.size()];
      int i = 0;
      for (ViewComputationCacheKey key : cacheKeys) {
        caches[i++] = _cachesByKey.remove(key);
      }
    } finally {
      _cacheManagementLock.unlock();
    }
    for (DefaultViewComputationCache cache : caches) {
      cache.delete();
    }
  }

  public void setReleaseCachesCallback(final ReleaseCachesCallback releaseCachesCallback) {
    _releaseCachesCallback = releaseCachesCallback;
  }

  public ReleaseCachesCallback getReleaseCachesCallback() {
    return _releaseCachesCallback;
  }

  public void setMissingValueLoader(final MissingValueLoader missingValueLoader) {
    _missingValueLoader = missingValueLoader;
  }

  public MissingValueLoader getMissingValueLoader() {
    return _missingValueLoader;
  }

}
