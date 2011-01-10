/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * Caches binary objects on top of another binary data store. This is an in-memory cache.
 */
public class CachingBinaryDataStore implements BinaryDataStore {

  private static final Logger s_logger = LoggerFactory.getLogger(CachingBinaryDataStore.class);

  private final BinaryDataStore _underlying;
  private final CacheManager _cacheManager;
  private final Cache _cache;

  public CachingBinaryDataStore(final BinaryDataStore underlying, final CacheManager cacheManager, final ViewComputationCacheKey cacheKey, final int maxCachedElements) {
    _underlying = underlying;
    _cacheManager = cacheManager;
    final String cacheName = cacheKey.toString();
    EHCacheUtils.addCache(cacheManager, cacheKey.toString(), maxCachedElements, MemoryStoreEvictionPolicy.LFU, false, null, true, 0, 0, false, 0, null);
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, cacheName);
  }

  protected BinaryDataStore getUnderlying() {
    return _underlying;
  }

  protected CacheManager getCacheManager() {
    return _cacheManager;
  }

  protected Cache getCache() {
    return _cache;
  }

  @Override
  public void delete() {
    s_logger.info("Delete on {}", this);
    getCacheManager().removeCache(getCache().getName());
    getUnderlying().delete();
  }

  @Override
  public byte[] get(long identifier) {
    s_logger.info("Get {} on {}", identifier, this);
    final Element cacheElement = getCache().get(identifier);
    if (cacheElement != null) {
      s_logger.debug("Cache hit for {} on {}", identifier, this);
      return (byte[]) cacheElement.getObjectValue();
    }
    s_logger.debug("Cache miss for {} on {}", identifier, this);
    final byte[] data = getUnderlying().get(identifier);
    getCache().put(new Element(identifier, data));
    return data;
  }

  @Override
  public void put(final long identifier, final byte[] data) {
    s_logger.info("Put {} on {}", identifier, this);
    getUnderlying().put(identifier, data);
    getCache().put(new Element(identifier, data));
  }

  @Override
  public String toString() {
    return "CachingBinaryDataStore[" + getCache().getName() + "]";
  }

  @Override
  public Map<Long, byte[]> get(Collection<Long> identifiers) {
    final Map<Long, byte[]> result = new HashMap<Long, byte[]>();
    final List<Long> missing = new ArrayList<Long>(identifiers.size());
    for (Long identifier : identifiers) {
      final Element cacheElement = getCache().get(identifier);
      if (cacheElement != null) {
        result.put(identifier, (byte[]) cacheElement.getObjectValue());
      } else {
        missing.add(identifier);
      }
    }
    if (missing.isEmpty()) {
      return result;
    }
    if (missing.size() == 1) {
      final Long missingIdentifier = missing.get(0);
      final byte[] data = getUnderlying().get(missingIdentifier);
      result.put(missingIdentifier, data);
      getCache().put(new Element(missingIdentifier, data));
    } else {
      final Map<Long, byte[]> missingData = getUnderlying().get(missing);
      for (Map.Entry<Long, byte[]> data : missingData.entrySet()) {
        result.put(data.getKey(), data.getValue());
        getCache().put(new Element(data.getKey(), data.getValue()));
      }
    }
    return result;
  }

  @Override
  public void put(final Map<Long, byte[]> data) {
    getUnderlying().put(data);
    for (Map.Entry<Long, byte[]> element : data.entrySet()) {
      getCache().put(new Element(element.getKey(), element.getValue()));
    }
  }

}
