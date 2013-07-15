/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.fudgemsg.FudgeMsg;
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
      final ViewComputationCacheKey cacheKey) {
    _underlying = underlying;
    _cacheManager = cacheManager;
    final String cacheName = cacheKey.toString();
    EHCacheUtils.addCache(cacheManager, cacheKey.toString());
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
  public FudgeMsg get(long identifier) {
    final Element cacheElement = getCache().get(identifier);
    if (cacheElement != null) {
      return (FudgeMsg) cacheElement.getObjectValue();
    }
    final FudgeMsg data = getUnderlying().get(identifier);
    getCache().put(new Element(identifier, data));
    return data;
  }

  @Override
  public void put(final long identifier, final FudgeMsg data) {
    getUnderlying().put(identifier, data);
    getCache().put(new Element(identifier, data));
  }

  @Override
  public String toString() {
    return "CachingFudgeMessageStore[" + getCache().getName() + "]";
  }

  @Override
  public Map<Long, FudgeMsg> get(Collection<Long> identifiers) {
    final Map<Long, FudgeMsg> result = new HashMap<Long, FudgeMsg>();
    final List<Long> missing = new ArrayList<Long>(identifiers.size());
    for (Long identifier : identifiers) {
      final Element cacheElement = getCache().get(identifier);
      if (cacheElement != null) {
        result.put(identifier, (FudgeMsg) cacheElement.getObjectValue());
      } else {
        missing.add(identifier);
      }
    }
    if (missing.isEmpty()) {
      return result;
    }
    if (missing.size() == 1) {
      final Long missingIdentifier = missing.get(0);
      final FudgeMsg data = getUnderlying().get(missingIdentifier);
      result.put(missingIdentifier, data);
      getCache().put(new Element(missingIdentifier, data));
    } else {
      final Map<Long, FudgeMsg> missingData = getUnderlying().get(missing);
      for (Map.Entry<Long, FudgeMsg> data : missingData.entrySet()) {
        result.put(data.getKey(), data.getValue());
        getCache().put(new Element(data.getKey(), data.getValue()));
      }
    }
    return result;
  }

  @Override
  public void put(final Map<Long, FudgeMsg> data) {
    getUnderlying().put(data);
    for (Map.Entry<Long, FudgeMsg> element : data.entrySet()) {
      getCache().put(new Element(element.getKey(), element.getValue()));
    }
  }

}
