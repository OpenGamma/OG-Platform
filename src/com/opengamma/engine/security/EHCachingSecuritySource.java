/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.RegisteredEventListeners;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.EHCacheUtils;

/**
 * A cache decorating a {@code SecuritySource}.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingSecuritySource implements SecuritySource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingSecuritySource.class);
  /** The single security cache key. */
  /* package for testing */ static final String SINGLE_SECURITY_CACHE = "single-security-cache";
  /** The multiple security cache key. */
  /* package for testing */ static final String MULTI_SECURITIES_CACHE = "multi-securities-cache";

  /**
   * The underlying cache.
   */
  private final SecuritySource _underlying;
  /**
   * The cache manager.
   */
  private final CacheManager _manager;
  /**
   * The single security cache.
   */
  private final Cache _uidCache;
  /**
   * The multiple security cache.
   */
  private final Cache _bundleCache;

  /**
   * Creates a cached security master.
   * @param underlying  the underlying security master, not null
   */
  public EHCachingSecuritySource(SecuritySource underlying) {
    ArgumentChecker.notNull(underlying, "Security Master");
    _underlying = underlying;
    CacheManager manager = EHCacheUtils.createCacheManager();
    EHCacheUtils.addCache(manager, SINGLE_SECURITY_CACHE);
    EHCacheUtils.addCache(manager, MULTI_SECURITIES_CACHE);
    _uidCache = EHCacheUtils.getCacheFromManager(manager, SINGLE_SECURITY_CACHE);
    _bundleCache = EHCacheUtils.getCacheFromManager(manager, MULTI_SECURITIES_CACHE);
    _manager = manager;
  }

  public EHCachingSecuritySource(SecuritySource underlying,
      int maxElementsInMemory,
      MemoryStoreEvictionPolicy memoryStoreEvictionPolicy,
      boolean overflowToDisk, String diskStorePath, boolean eternal,
      long timeToLiveSeconds, long timeToIdleSeconds, boolean diskPersistent,
      long diskExpiryThreadIntervalSeconds,
      RegisteredEventListeners registeredEventListeners) {
    ArgumentChecker.notNull(underlying, "Security Master");
    _underlying = underlying;
    CacheManager manager = EHCacheUtils.createCacheManager();
    EHCacheUtils.addCache(manager, SINGLE_SECURITY_CACHE, maxElementsInMemory,
        memoryStoreEvictionPolicy, overflowToDisk, diskStorePath, eternal,
        timeToLiveSeconds, timeToIdleSeconds, diskPersistent,
        diskExpiryThreadIntervalSeconds, registeredEventListeners);
    EHCacheUtils.addCache(manager, MULTI_SECURITIES_CACHE, maxElementsInMemory,
        memoryStoreEvictionPolicy, overflowToDisk, diskStorePath, eternal,
        timeToLiveSeconds, timeToIdleSeconds, diskPersistent,
        diskExpiryThreadIntervalSeconds, registeredEventListeners);
    _uidCache = EHCacheUtils.getCacheFromManager(manager, SINGLE_SECURITY_CACHE);
    _bundleCache = EHCacheUtils.getCacheFromManager(manager, MULTI_SECURITIES_CACHE);
    _manager = manager;
  }

  /**
   * Creates a cached security master.
   * @param underlying  the underlying security master, not null
   * @param manager  the cache manager, not null
   */
  public EHCachingSecuritySource(SecuritySource underlying, CacheManager manager) {
    ArgumentChecker.notNull(underlying, "Security Master");
    ArgumentChecker.notNull(manager, "CacheManager");
    _underlying = underlying;
    EHCacheUtils.addCache(manager, SINGLE_SECURITY_CACHE);
    EHCacheUtils.addCache(manager, MULTI_SECURITIES_CACHE);
    _uidCache = EHCacheUtils.getCacheFromManager(manager, SINGLE_SECURITY_CACHE);
    _bundleCache = EHCacheUtils.getCacheFromManager(manager, MULTI_SECURITIES_CACHE);
    _manager = manager;
  }

  /**
   * Creates a cached security master.
   * @param underlying  the underlying security master, not null
   * @param singleSecCache  the single security cache, not null
   * @param multiSecCache  the multiple security cache, not null
   */
  public EHCachingSecuritySource(SecuritySource underlying,
      Cache singleSecCache, Cache multiSecCache) {
    ArgumentChecker.notNull(underlying, "Security Master");
    ArgumentChecker.notNull(singleSecCache, "Single Security Cache");
    ArgumentChecker.notNull(multiSecCache, "Multi Security Cache");
    _underlying = underlying;
    CacheManager manager = EHCacheUtils.createCacheManager();
    EHCacheUtils.addCache(manager, singleSecCache);
    EHCacheUtils.addCache(manager, multiSecCache);
    _uidCache = EHCacheUtils.getCacheFromManager(manager, singleSecCache.getName());
    _bundleCache = EHCacheUtils.getCacheFromManager(manager, multiSecCache.getName());
    _manager = manager;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying security master.
   * @return the underlying security master, not null
   */
  public SecuritySource getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the cache manager.
   * @return the cache manager, not null
   */
  /* package for testing */ CacheManager getCacheManager() {
    return _manager;
  }

  //-------------------------------------------------------------------------
  @Override
  public synchronized Security getSecurity(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    Element e = _uidCache.get(uid);
    Security result = null;
    if (e != null) {
      Serializable value = e.getValue();
      if (value instanceof Security) {
        result = (Security) value;
        s_logger.debug("retrieved security: {} from single-security-cache", result);
      } else {
        s_logger.warn("returned object {} from single-security-cache not a Security", value);
      }
    } else {
      result = getUnderlying().getSecurity(uid);
      if (result != null) {
        _uidCache.put(new Element(uid, result));
      }
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Security> getSecurities(IdentifierBundle securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    Element e = _bundleCache.get(securityKey);
    Collection<Security> result = new HashSet<Security>();
    if (e != null) {
      Serializable value = e.getValue();
      if (value instanceof Set<?>) {
        result.addAll((Set<Security>) value);
      } else {
        s_logger.warn("returned object {} from cache is not a Set<Security>", value);
      }
    } else {
      result = getUnderlying().getSecurities(securityKey);
      if (result != null) {
        _bundleCache.put(new Element(securityKey, result));
        for (Security security : result) {
          _uidCache.put(new Element(security.getUniqueIdentifier(), security));
        }
      }
    }
    return result;
  }

  @Override
  public synchronized Security getSecurity(IdentifierBundle securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    Collection<Security> matched = getSecurities(securityKey);
    if (matched.isEmpty()) {
      return null;
    }
    return matched.iterator().next();
  }

  //-------------------------------------------------------------------------
  /**
   * Refreshes the value for the specified security key.
   * @param securityKey  the security key, not null
   */
  @SuppressWarnings("unchecked")
  public void refresh(Object securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    Element element = _bundleCache.get(securityKey);
    if (element != null) {
      Serializable value = element.getValue();
      if (value instanceof Collection<?>) {
        Collection<Security> securities = (Collection<Security>) value;
        for (Security sec : securities) {
          _uidCache.remove(sec.getUniqueIdentifier());
        }
      }
      _bundleCache.remove(securityKey);
    } else {
      _uidCache.remove(securityKey);
    }
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache.
   * It should not be part of a generic lifecycle method.
   */
  protected void shutdown() {
    _manager.removeCache(SINGLE_SECURITY_CACHE);
    _manager.removeCache(MULTI_SECURITIES_CACHE);
    _manager.shutdown();
  }

}
