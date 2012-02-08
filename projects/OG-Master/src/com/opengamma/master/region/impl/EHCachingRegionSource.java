/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

import java.util.Collection;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.RegisteredEventListeners;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A cache decorating a {@code RegionSource}.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingRegionSource implements RegionSource {
  
  
  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingRegionSource.class);
  /**
   * The cache name.
   */
  private static final String CACHE_NAME = "RegionCache";
  /**
   * The cache manager.
   */
  private final CacheManager _cacheManager;

  /**
   * The underlying source.
   */
  private final RegionSource _underlying;
  /**
   * The cache.
   */
  private final Cache _cache;
  
  /**
   * The time to live.
   */
  private Integer _ttl;

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying source, not null
   * @param cacheManager  the cache manager, not null
   * @param maxElementsInMemory  cache configuration
   * @param memoryStoreEvictionPolicy  cache configuration
   * @param overflowToDisk  cache configuration
   * @param diskStorePath  cache configuration
   * @param eternal  cache configuration
   * @param timeToLiveSeconds  cache configuration
   * @param timeToIdleSeconds  cache configuration
   * @param diskPersistent  cache configuration
   * @param diskExpiryThreadIntervalSeconds  cache configuration
   * @param registeredEventListeners  cache configuration
   */
  public EHCachingRegionSource(
      final RegionSource underlying, final CacheManager cacheManager, final int maxElementsInMemory,
      final MemoryStoreEvictionPolicy memoryStoreEvictionPolicy, final boolean overflowToDisk, final String diskStorePath,
      final boolean eternal, final long timeToLiveSeconds, final long timeToIdleSeconds, final boolean diskPersistent,
      final long diskExpiryThreadIntervalSeconds, final RegisteredEventListeners registeredEventListeners) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _underlying = underlying;
    EHCacheUtils.addCache(cacheManager, CACHE_NAME, maxElementsInMemory, memoryStoreEvictionPolicy, overflowToDisk, diskStorePath,
        eternal, timeToLiveSeconds, timeToIdleSeconds, diskPersistent, diskExpiryThreadIntervalSeconds, registeredEventListeners);
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, CACHE_NAME);
    _cacheManager = cacheManager;
  }

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying source, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingRegionSource(RegionSource underlying, CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "Cache Manager");
    _underlying = underlying;
    EHCacheUtils.addCache(cacheManager, CACHE_NAME);
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, CACHE_NAME);
    _cacheManager = cacheManager;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying source.
   * 
   * @return the underlying source, not null
   */
  public RegionSource getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the cache manager.
   * 
   * @return the cache manager, not null
   */
  public CacheManager getCacheManager() {
    return _cache.getCacheManager();
  }
  
  /**
   * Gets the ttl.
   * @return the ttl
   */
  public Integer getTtl() {
    return _ttl;
  }

  /**
   * Sets the ttl.
   * @param ttl  the ttl
   */
  public void setTtl(Integer ttl) {
    _ttl = ttl;
  }

  @Override
  public Region getRegion(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    Region result = null;
    if (uniqueId.isLatest()) {
      result = _underlying.getRegion(uniqueId);
      s_logger.debug("Caching region {}", result);
      _cache.put(new Element(uniqueId, result));
    } else {
      Element element = _cache.get(uniqueId); 
      if (element != null) {
        s_logger.debug("Cache hit on {}", uniqueId);
        if (element.getValue() instanceof Region) {
          result = (Region) element.getValue();
        }
      } else {
        s_logger.debug("Cache miss on {}", uniqueId);
        result = _underlying.getRegion(uniqueId);
        s_logger.debug("Caching region {}", result);
        _cache.put(new Element(uniqueId, result));
      }
    }
    return result;
  }

  @Override
  public Region getRegion(ObjectId objectId, VersionCorrection versionCorrection) {
    RegionSearchRequest request = new RegionSearchRequest();
    request.setVersionCorrection(versionCorrection);
    request.addObjectId(objectId);
    
    Region result = null;
    Element element = _cache.get(request);
    if (element != null) {
      s_logger.debug("Cache hit on {}", request);
      result = (Region) element.getValue();
    } else {
      s_logger.debug("Cache miss on {}", request);
      result = _underlying.getRegion(objectId, versionCorrection);
      s_logger.debug("Caching regions {}", result);
      _cache.put(new Element(request, result));
      if (result != null) {
        _cache.put(new Element(result.getUniqueId(), result));
      }
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<? extends Region> getRegions(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    RegionSearchRequest request = new RegionSearchRequest(bundle);
    request.setVersionCorrection(versionCorrection);
    
    Element element = _cache.get(request);
    Collection<? extends Region> result = null;
    if (element != null) {
      s_logger.debug("Cache hit on {}", request);
      result = (Collection<? extends Region>) element.getValue();
    } else {
      s_logger.debug("Cache miss on {}", request);
      result = _underlying.getRegions(bundle, versionCorrection);
      s_logger.debug("Caching regions {}", result);
      element = new Element(request, result);
      if (_ttl != null) {
        element.setTimeToLive(_ttl);
      }
      _cache.put(element);
      if (result != null) {
        for (Region region : result) {
          _cache.put(new Element(region.getUniqueId(), region));
        }
      }
    }
    return result;
  }

  @Override
  public Region getHighestLevelRegion(ExternalId externalId) {
    return getHighestLevelRegion(ExternalIdBundle.of(externalId));
  }

  @Override
  public Region getHighestLevelRegion(ExternalIdBundle bundle) {
    Region result = null;
    Element element = _cache.get(bundle);
    if (element != null) {
      s_logger.debug("Cache hit on {}", bundle);
      result = (Region) element.getValue();
    } else {
      s_logger.debug("Cache miss on {}", bundle);
      result = _underlying.getHighestLevelRegion(bundle);
      s_logger.debug("Caching regions {}", result);
      element = new Element(bundle, result);
      if (_ttl != null) {
        element.setTimeToLive(_ttl);
      }
      _cache.put(element);
      if (result != null) {
        _cache.put(new Element(result.getUniqueId(), result));
      }
    }
    return result;
  }
  
  /**
   * Call this at the end of a unit test run to clear the state of EHCache.
   * It should not be part of a generic lifecycle method.
   */
  protected void shutdown() {
    _cacheManager.removeCache(CACHE_NAME);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getUnderlying() + "]";
  }

}
