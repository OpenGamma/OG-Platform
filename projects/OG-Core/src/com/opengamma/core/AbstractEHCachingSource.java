/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import static com.google.common.collect.Maps.newHashMap;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import javax.time.Instant;
import javax.time.calendar.Period;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.map.HashDeepMap2;
import com.opengamma.util.map.Map2;
import com.opengamma.util.map.SoftValueHashMap2;
import com.opengamma.util.tuple.Pair;

/**
 * A cache decorating a {@code FinancialSecuritySource}.
 * <p>
 * The cache is implemented using {@code EHCache}.
 * 
 * @param <V> the type returned by the source
 * @param <S> the source
 */
public abstract class AbstractEHCachingSource<V extends UniqueIdentifiable, S extends Source<V> & ChangeProvider>  // CSIGNORE
    implements Source<V>, ChangeProvider {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractEHCachingSource.class);

  /** The oid cache key. */
  private final String _oidCacheName = getClass().getName() + "-oid-cache";
  /** The uid cache key. */
  private final String _uidCacheName = getClass().getName() + "-uid-cache";

  /**
   * EHCache doesn't like being hammered repeatedly for the same objects. Also, if the window of objects being requested is bigger than the in memory window then new objects get created as the on-disk
   * values get deserialized. The solution is to maintain a soft referenced buffer so that all the while the objects we have previously returned are in use we won't requery EHCache for them.
   */
  private final ConcurrentMap<UniqueId, V> _frontCache = new MapMaker().weakValues().makeMap();

  /**
   * EHCache doesn't like being hammered repeatedly for the same objects. Also, if the window of objects being requested is bigger than the in memory window then new objects get created as the on-disk
   * values get deserialized. The solution is to maintain a soft referenced buffer so that all the while the objects we have previously returned are in use we won't requery EHCache for them.
   */
  private final Map2<Object, VersionCorrection, V> _frontCache2 = new SoftValueHashMap2<Object, VersionCorrection, V>();

  private final HashDeepMap2<ObjectId, UniqueId, V> _frontCache3 = new HashDeepMap2<ObjectId, UniqueId, V>() {
    protected MapMaker mapMaker() {
      return super.mapMaker().softValues();
    }
  };

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
   * Listens for changes in the underlying security source.
   */
  private final ChangeListener _changeListener;
  /**
   * The local change manager.
   */
  private final ChangeManager _changeManager;

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
    _changeManager = new BasicChangeManager();
    _changeListener = new ChangeListener() {
      @Override
      public void entityChanged(ChangeEvent event) {
        final ObjectId oid = event.getObjectId();
        final Instant versionFrom = event.getVersionFrom();
        final Instant versionTo = event.getVersionTo();
        cleanCaches(oid, versionFrom, versionTo);
        _changeManager.entityChanged(event.getType(), event.getObjectId(), event.getVersionFrom(), event.getVersionTo(), event.getVersionInstant());
      }
    };
    underlying.changeManager().addChangeListener(_changeListener);
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

  /**
   * Gets the front cache.
   *
   * @return the front cache, not null
   */
  protected ConcurrentMap<UniqueId, V> getFrontCache() {
    return _frontCache;
  }

  /**
   * Gets the front cache.
   *
   * @return the front cache, not null
   */
  protected Map2<Object, VersionCorrection, V> getFrontCache2() {
    return _frontCache2;
  }

  /**
   * Gets the oid cache.
   *
   * @return the oid cache, not null
   */
  protected Cache getOidCache() {
    return _oidCache;
  }

  /**
   * Gets the uid cache.
   *
   * @return the uid cache, not null
   */
  protected Cache getUidCache() {
    return _uidCache;
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public V get(UniqueId uid) {
    ArgumentChecker.notNull(uid, "uid");
    V result = _frontCache.get(uid);
    if (result != null) {
      return result;
    }
    Element e = _uidCache.get(uid);
    if (e != null) {
      result = (V) e.getValue();
      s_logger.debug("retrieved object: {} from uid-cache", result);
      final V existing = _frontCache.putIfAbsent(uid, result);
      if (existing != null) {
        return existing;
      } else {
        return result;
      }
    } else {
      result = getUnderlying().get(uid);
      final V existing = _frontCache.putIfAbsent(uid, result);
      if (existing != null) {
        return existing;
      } else {
        _uidCache.put(new Element(uid, result));
        return result;
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected Map<VersionCorrection, V> getObjectIdCacheEntry(final ObjectId objectId) {
    final Element e = _oidCache.get(objectId);
    if (e != null) {
      if (e.getObjectValue() instanceof Map<?, ?>) {
        return (Map<VersionCorrection, V>) e.getObjectValue();
      }
    }
    return null;
  }

  @Override
  public V get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    V result = _frontCache2.get(objectId, versionCorrection);
    if (result != null) {
      return result;
    }
    Map<VersionCorrection, V> items = getObjectIdCacheEntry(objectId);
    if (items != null) {
      result = items.get(versionCorrection);
      if (result != null) {
        final V existing = _frontCache2.putIfAbsent(objectId, versionCorrection, result);
        if (existing != null) {
          return existing;
        }
      }
    }
    if (result == null) {
      result = getUnderlying().get(objectId, versionCorrection);
      if (result != null) {
        final V existing = _frontCache2.putIfAbsent(objectId, versionCorrection, result);
        if (existing != null) {
          return existing;
        }
        _uidCache.put(new Element(result.getUniqueId(), result));
      }
      final Map<VersionCorrection, V> newitems = new HashMap<VersionCorrection, V>();
      synchronized (this) {
        items = getObjectIdCacheEntry(objectId);
        if (items != null) {
          newitems.putAll(items);
        }
        newitems.put(versionCorrection, result);
        _oidCache.put(new Element(objectId, newitems));
      }
    }
    return result;
  }

  static final Period s_versionCorrectionBucketSize = Period.ofDays(1); //This is an attempt to avoid thrashing if we get conflicting queries
  static final BigInteger s_versionCorrectionBucketSizeInNanos = BigInteger.valueOf(s_versionCorrectionBucketSize.totalNanosWith24HourDays());
  static final int s_versionCorrectionBucketSizeInSeconds = (int) Period.ofDays(1).totalSecondsWith24HourDays();
  static final BigInteger s_latestBucket = BigInteger.valueOf(-1);

  @Override
  public Map<UniqueId, V> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, V> result = newHashMap();
    for (UniqueId uniqueId : uniqueIds) {
      try {
        V object = get(uniqueId);
        result.put(uniqueId, object);
      } catch (DataNotFoundException ex) {
        // do nothing
      }
    }
    return result;
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache. It should not be part of a generic lifecycle method.
   */
  public void shutdown() {
    _underlying.changeManager().removeChangeListener(_changeListener);
    _manager.removeCache(_uidCacheName);
    _manager.removeCache(_oidCacheName);
  }

  //-------------------------------------------------------------------------
  protected void cleanCaches(ObjectId oid, Instant versionFrom, Instant versionTo) {
    _frontCache3.remove(oid);
    
    // Note: this is very inefficient
    for (Pair<Object, VersionCorrection> key : _frontCache2.keySet()) {
      if (key.getFirst() instanceof ObjectId) {
        ObjectId objectId = (ObjectId) key.getFirst();
        VersionCorrection vc = key.getSecond();
        if (objectId.equals(oid) && vc.getCorrectedTo() == null && !vc.getVersionAsOf().isBefore(versionFrom) && !vc.getVersionAsOf().isAfter(versionTo)) {
          _frontCache2.remove(key);
        }
      }
    }
    
    for (Object key : _uidCache.getKeys()) {
      if (key instanceof UniqueId && ((UniqueId) key).getObjectId().equals(oid)) {
        _uidCache.remove(key);
        _frontCache.remove(key);
      }
    }
    
    // Destroy all version/correction cached values for the object
    _oidCache.remove(oid);
  }

  protected void cacheItems(Collection<V> items) {
    for (V security : items) {
      _uidCache.put(new Element(security.getUniqueId(), security));
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getUnderlying() + "]";
  }

}
