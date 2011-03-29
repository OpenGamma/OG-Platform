/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import org.fudgemsg.FudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * Caches Fudge message objects on top of another Fudge message store. This is an in-memory cache.
 */
public class CachingFudgeMessageStore implements FudgeMessageStore {

  private static final Logger s_logger = LoggerFactory.getLogger(CachingFudgeMessageStore.class);

  private final FudgeMessageStore _underlying;
  private final CacheManager _cacheManager;
  private final Cache _cache;

  public CachingFudgeMessageStore(final FudgeMessageStore underlying, final CacheManager cacheManager,
      final ViewComputationCacheKey cacheKey, final int maxCachedElements) {
    _underlying = underlying;
    _cacheManager = cacheManager;
    final String cacheName = cacheKey.toString();
    EHCacheUtils.addCache(cacheManager, cacheKey.toString(), maxCachedElements, MemoryStoreEvictionPolicy.LFU, false, null, true, 0, 0, false, 0, null);
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, cacheName);
  }

  protected FudgeMessageStore getUnderlying() {
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
  public FudgeFieldContainer get(long identifier) {
    final Element cacheElement = getCache().get(identifier);
    if (cacheElement != null) {
      return (FudgeFieldContainer) cacheElement.getObjectValue();
    }
    final FudgeFieldContainer data = getUnderlying().get(identifier);
    getCache().put(new Element(identifier, data));
    return data;
  }

  @Override
  public void put(final long identifier, final FudgeFieldContainer data) {
    getUnderlying().put(identifier, data);
    getCache().put(new Element(identifier, data));
  }

  @Override
  public String toString() {
    return "CachingFudgeMessageStore[" + getCache().getName() + "]";
  }

  @Override
  public Map<Long, FudgeFieldContainer> get(Collection<Long> identifiers) {
    final Map<Long, FudgeFieldContainer> result = new HashMap<Long, FudgeFieldContainer>();
    final List<Long> missing = new ArrayList<Long>(identifiers.size());
    for (Long identifier : identifiers) {
      final Element cacheElement = getCache().get(identifier);
      if (cacheElement != null) {
        result.put(identifier, (FudgeFieldContainer) cacheElement.getObjectValue());
      } else {
        missing.add(identifier);
      }
    }
    if (missing.isEmpty()) {
      return result;
    }
    if (missing.size() == 1) {
      final Long missingIdentifier = missing.get(0);
      final FudgeFieldContainer data = getUnderlying().get(missingIdentifier);
      result.put(missingIdentifier, data);
      getCache().put(new Element(missingIdentifier, data));
    } else {
      final Map<Long, FudgeFieldContainer> missingData = getUnderlying().get(missing);
      for (Map.Entry<Long, FudgeFieldContainer> data : missingData.entrySet()) {
        result.put(data.getKey(), data.getValue());
        getCache().put(new Element(data.getKey(), data.getValue()));
      }
    }
    return result;
  }

  @Override
  public void put(final Map<Long, FudgeFieldContainer> data) {
    getUnderlying().put(data);
    for (Map.Entry<Long, FudgeFieldContainer> element : data.entrySet()) {
      getCache().put(new Element(element.getKey(), element.getValue()));
    }
  }

}
