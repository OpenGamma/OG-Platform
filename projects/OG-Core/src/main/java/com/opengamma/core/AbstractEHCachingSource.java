/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Partial implementation of a cache on top of another source implementation using {@code EHCache}.
 * 
 * @param <V> the type returned by the source
 * @param <S> the source
 */
public abstract class AbstractEHCachingSource<V extends UniqueIdentifiable, S extends Source<V>> extends AbstractSource<V> implements Source<V>, ChangeProvider {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractEHCachingSource.class);

  /** The oid cache key. */
  private final String _oidCacheName = getClass().getName() + "-oid-cache";
  /** The uid cache key. */
  private final String _uidCacheName = getClass().getName() + "-uid-cache";

  /**
   * EHCache doesn't like being hammered repeatedly for the same objects. Also, if the window of objects being requested is bigger than the in memory window then new objects get created as the on-disk
   * values get deserialized. The solution is to maintain a soft referenced buffer so that all the while the objects we have previously returned are in use we won't re-query EHCache for them.
   */
  private final ConcurrentMap<UniqueId, V> _frontCacheByUID = new MapMaker().weakValues().makeMap();

  /**
   * The underlying cache.
   */
  private final S _underlying;
  /**
   * The cache manager.
   */
  private final CacheManager _manager;
  /**
   * The oid cache.
   */
  private final Cache _oidCache;
  /**
   * The uid cache.
   */
  private final Cache _uidCache;

  /**
   * Creates an instance over an underlying source specifying the cache manager.
   * 
   * @param underlying the underlying security source, not null
   * @param cacheManager the cache manager, not null
   */
  public AbstractEHCachingSource(final S underlying, final CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _underlying = underlying;
    EHCacheUtils.addCache(cacheManager, _oidCacheName);
    EHCacheUtils.addCache(cacheManager, _uidCacheName);
    _oidCache = EHCacheUtils.getCacheFromManager(cacheManager, _oidCacheName);
    _uidCache = EHCacheUtils.getCacheFromManager(cacheManager, _uidCacheName);
    _manager = cacheManager;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying source of items.
   * 
   * @return the underlying source of items, not null
   */
  protected S getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the cache manager.
   * 
   * @return the cache manager, not null
   */
  protected CacheManager getCacheManager() {
    return _manager;
  }

  @SuppressWarnings("unchecked")
  @Override
  public V get(final UniqueId uid) {
    ArgumentChecker.notNull(uid, "uid");
    V result;
    if (uid.isLatest()) {
      result = cacheItem(getUnderlying().get(uid));
    } else {
      result = _frontCacheByUID.get(uid);
      if (result == null) {
        final Element e = _uidCache.get(uid);
        if (e != null) {
          result = (V) e.getObjectValue();
          s_logger.debug("retrieved object: {} from uid-cache", result);
          V existing = _frontCacheByUID.putIfAbsent(uid, result);
          if (existing != null) {
            result = existing;
          }
        } else {
          result = cacheItem(getUnderlying().get(uid));
        }
      }
    }
    return result;
  }

  @Override
  public Map<UniqueId, V> get(final Collection<UniqueId> uids) {
    final Map<UniqueId, V> results = Maps.newHashMapWithExpectedSize(uids.size());
    final Collection<UniqueId> misses = new ArrayList<UniqueId>(uids.size());
    for (UniqueId uid : uids) {
      if (uid.isLatest()) {
        misses.add(uid);
      } else {
        V result = _frontCacheByUID.get(uid);
        if (result != null) {
          results.put(uid, result);
        } else {
          final Element e = _uidCache.get(uid);
          if (e != null) {
            @SuppressWarnings("unchecked")
            V objectValue = (V) e.getObjectValue();
            result = objectValue;
            s_logger.debug("retrieved object: {} from uid-cache", result);
            V existing = _frontCacheByUID.putIfAbsent(uid, result);
            if (existing != null) {
              result = existing;
            }
            results.put(uid, result);
          } else {
            misses.add(uid);
          }
        }
      }
    }
    if (!misses.isEmpty()) {
      final Map<UniqueId, V> underlying = getUnderlying().get(misses);
      for (UniqueId uid : misses) {
        V result = underlying.get(uid);
        if (result != null) {
          result = cacheItem(result);
          results.put(uid, result);
        }
      }
    }
    return results;
  }

  @Override
  public V get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    V result;
    if (versionCorrection.containsLatest()) {
      result = cacheItem(getUnderlying().get(objectId, versionCorrection));
    } else {
      final Pair<ObjectId, VersionCorrection> key = Pairs.of(objectId, versionCorrection);
      final Element e = _oidCache.get(key);
      if (e != null) {
        final UniqueId uid = (UniqueId) e.getObjectValue();
        result = get(uid);
      } else {
        result = cacheItem(getUnderlying().get(objectId, versionCorrection));
        _oidCache.put(new Element(key, result.getUniqueId()));
      }
    }
    return result;
  }

  @Override
  public Map<ObjectId, V> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
    final Map<ObjectId, V> results = Maps.newHashMapWithExpectedSize(objectIds.size());
    if (versionCorrection.containsLatest()) {
      final Map<ObjectId, V> underlying = getUnderlying().get(objectIds, versionCorrection);
      for (ObjectId objectId : objectIds) {
        final V result = underlying.get(objectId);
        if (result != null) {
          results.put(objectId, cacheItem(result));
        }
      }
    } else {
      final Collection<ObjectId> misses = new ArrayList<ObjectId>(objectIds.size());
      final Map<ObjectId, UniqueId> lookups = Maps.newHashMapWithExpectedSize(objectIds.size());
      for (ObjectId objectId : objectIds) {
        final Pair<ObjectId, VersionCorrection> key = Pairs.of(objectId, versionCorrection);
        final Element e = _oidCache.get(key);
        if (e != null) {
          final UniqueId uid = (UniqueId) e.getObjectValue();
          lookups.put(objectId, uid);
        } else {
          misses.add(objectId);
        }
      }
      if (!lookups.isEmpty()) {
        final Map<UniqueId, V> underlying = get(lookups.values());
        for (Map.Entry<ObjectId, UniqueId> lookup : lookups.entrySet()) {
          final V result = underlying.get(lookup.getValue());
          if (result != null) {
            results.put(lookup.getKey(), cacheItem(result));
          }
        }
      }
      if (!misses.isEmpty()) {
        final Map<ObjectId, V> underlying = getUnderlying().get(misses, versionCorrection);
        for (ObjectId miss : misses) {
          V result = underlying.get(miss);
          if (result != null) {
            result = cacheItem(result);
            results.put(miss, result);
            _oidCache.put(new Element(Pairs.of(miss, versionCorrection), result.getUniqueId()));
          }
        }
      }
    }
    return results;
  }

  @Override
  public ChangeManager changeManager() {
    if (getUnderlying() instanceof ChangeProvider) {
      return ((ChangeProvider) getUnderlying()).changeManager();
    } else {
      return DummyChangeManager.INSTANCE;
    }
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache. It should not be part of a generic lifecycle method.
   */
  public void shutdown() {
    _manager.removeCache(_uidCacheName);
    _manager.removeCache(_oidCacheName);
  }

  //-------------------------------------------------------------------------
  protected V cacheItem(final V item) {
    if (item != null) {
      final V existing = _frontCacheByUID.putIfAbsent(item.getUniqueId(), item);
      if (existing != null) {
        return existing;
      } else {
        _uidCache.put(new Element(item.getUniqueId(), item));
        return item;
      }
    } else {
      return null;
    }
  }

  protected void cacheItems(final Collection<? extends V> items) {
    for (final V item : items) {
      cacheItem(item);
    }
  }
  
  protected void flush() {
    _uidCache.flush();
    _frontCacheByUID.clear();
    _oidCache.flush();
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getUnderlying() + "]";
  }

}
