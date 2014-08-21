/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.function.Supplier;

/**
 * Abstract cache.
 * 
 * @param <A>  the first type
 * @param <B>  the second type
 */
public abstract class HierarhicalEHCache<A, B> {

  private final Cache _aCache;

  private final Cache _bCache;

  private final Cache _missedCache;

  private final String _cacheNameA = getCachePrefix() + "A_Cache";

  private final String _cacheNameB = getCachePrefix() + "B_Cache";

  private final String _missedCacheName = getCachePrefix() + "Missed_Cache";

  abstract String getCachePrefix();

  abstract Object extractKey(A key, B value);

  private long _timeout = 1000;

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(HierarhicalEHCache.class);

  public HierarhicalEHCache(CacheManager cacheManager) {
    EHCacheUtils.addCache(cacheManager, _cacheNameA);
    _aCache = EHCacheUtils.getCacheFromManager(cacheManager, _cacheNameA);

    EHCacheUtils.addCache(cacheManager, _cacheNameB);
    _bCache = EHCacheUtils.getCacheFromManager(cacheManager, _cacheNameB);

    EHCacheUtils.addCache(cacheManager, _missedCacheName);
    _missedCache = EHCacheUtils.getCacheFromManager(cacheManager, _missedCacheName);
  }

  public void setTimeout(long timeout) {
    _timeout = timeout;
  }

  public void markMissed(Object key) {
    _missedCache.put(new Element(key, null));
  }

  @SuppressWarnings("unchecked")
  public B deepInsert(A aKey, Object bKey, B value) {
    try {
      _bCache.tryWriteLockOnKey(bKey, _timeout);
      // reread the cached element
      Element bElement = _bCache.get(bKey);
      Map<Object, B> map;
      if (bElement != null) {
        map = (Map<Object, B>) bElement.getObjectValue();
      } else {
        map = new HashMap<>();
      }
      s_logger.debug(getCachePrefix() + ": Caching value {}", value);
      map.put(aKey, value);
      // reinsert the map into cache
      _bCache.put(new Element(bKey, map));
    } catch (InterruptedException e) {
      // interrupted so we will not store value in cache this time
    } finally {
      _bCache.releaseWriteLockOnKey(bKey);
    }
    return value;
  }

  private B deepInsertAndMarkMissed(A aKey, Supplier<B> closure) {
    if (closure == null) {
      return null;
    }
    B b = closure.get();
    if (b == null) {
      _missedCache.put(new Element(aKey, null));
      return null;
    } else {
      Object bKey = extractKey(aKey, b);
      deepInsert(aKey, bKey, b);
      _aCache.put(new Element(aKey, bKey));
      return b;
    }
  }

  @SuppressWarnings("unchecked")
  public B shallowInsert(Object bKey, B value) {
    try {
      _bCache.tryWriteLockOnKey(bKey, _timeout);
      // reread the cached element
      Element bElement = _bCache.get(bKey);
      Map<Object, B> map;
      if (bElement != null) {
        map = (Map<Object, B>) bElement.getObjectValue();
      } else {
        map = new HashMap<>();
      }
      s_logger.debug(getCachePrefix() + ": Caching value {}", value);
      map.put(bKey, value);
      // reinsert the map into cache
      _bCache.put(new Element(bKey, map));
    } catch (InterruptedException e) {
      // interrupted so we will not store value in cache this time
    } finally {
      _bCache.releaseWriteLockOnKey(bKey);
    }
    return value;
  }

  private B shallowInsertAndMarkMissed(Object bKey, Supplier<B> closure) {
    if (closure == null) {
      return null;
    }
    B b = closure.get();
    if (b == null) {
      _missedCache.put(new Element(bKey, null));
      return null;
    } else {
      shallowInsert(bKey, b);
      return b;
    }
  }

  public B get(A aKey, Supplier<B> closure) {
    if (_missedCache.isKeyInCache(aKey)) {
      s_logger.debug(getCachePrefix() + ": Caching miss on {}", aKey);
      return null;
    }
    Element aElement = _aCache.get(aKey);
    if (aElement != null) {
      Object bKey = aElement.getObjectValue();
      Element bElement = _bCache.get(bKey);
      if (bElement != null) {
        @SuppressWarnings("unchecked")
        Map<Object, B> map = (Map<Object, B>) bElement.getObjectValue();
        B value = map.get(aKey);
        if (value == null) {
          return deepInsertAndMarkMissed(aKey, closure);
        } else {
          return value;
        }
      }
    }
    return deepInsertAndMarkMissed(aKey, closure);
  }

  public B getBySecondKey(Object bKey, Supplier<B> closure) {
    if (_missedCache.isKeyInCache(bKey)) {
      s_logger.debug(getCachePrefix() + ": Caching miss on {}", bKey);
      return null;
    }
    Element bElement = _bCache.get(bKey);
    if (bElement != null) {
      @SuppressWarnings("unchecked")
      Map<Object, B> map = (Map<Object, B>) bElement.getObjectValue();
      B value = map.get(bKey);
      if (value == null) {
        return shallowInsertAndMarkMissed(bKey, closure);
      } else {
        return value;
      }
    }
    return shallowInsertAndMarkMissed(bKey, closure);
  }

  public void clear(Object bKey) {
    _bCache.remove(bKey);
    _missedCache.removeAll();
  }

  public void shutdown() {
    _aCache.getCacheManager().removeCache(_cacheNameA);
    _bCache.getCacheManager().removeCache(_cacheNameB);
    _missedCache.getCacheManager().removeCache(_missedCacheName);
  }
}
