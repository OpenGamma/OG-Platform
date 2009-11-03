/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.RegisteredEventListeners;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 * 
 * @author yomi
 */
public class EHCachingSecurityMaster implements SecurityMaster {
  private static final Logger s_logger = LoggerFactory
      .getLogger(EHCachingSecurityMaster.class);
  public static String SINGLE_SECURITY_CACHE = "single-security-cache";
  public static String MULTI_SECURITIES_CACHE = "multi-securities-cache";

  private final SecurityMaster _underlying;
  private final CacheManager _manager;
  private final Cache _singleSecurityCache;
  private final Cache _multiSecuritiesCache;

  public EHCachingSecurityMaster(SecurityMaster underlying) {
    ArgumentChecker.checkNotNull(underlying, "Security Master");
    _underlying = underlying;
    CacheManager manager = createCacheManager();
    addCache(manager, SINGLE_SECURITY_CACHE);
    addCache(manager, MULTI_SECURITIES_CACHE);
    _singleSecurityCache = getCacheFromManager(manager, SINGLE_SECURITY_CACHE);
    _multiSecuritiesCache = getCacheFromManager(manager, MULTI_SECURITIES_CACHE);
    _manager = manager;
  }

  public EHCachingSecurityMaster(SecurityMaster underlying,
      int maxElementsInMemory,
      MemoryStoreEvictionPolicy memoryStoreEvictionPolicy,
      boolean overflowToDisk, String diskStorePath, boolean eternal,
      long timeToLiveSeconds, long timeToIdleSeconds, boolean diskPersistent,
      long diskExpiryThreadIntervalSeconds,
      RegisteredEventListeners registeredEventListeners) {
    ArgumentChecker.checkNotNull(underlying, "Security Master");
    _underlying = underlying;
    CacheManager manager = createCacheManager();
    addCache(manager, SINGLE_SECURITY_CACHE, maxElementsInMemory,
        memoryStoreEvictionPolicy, overflowToDisk, diskStorePath, eternal,
        timeToLiveSeconds, timeToIdleSeconds, diskPersistent,
        diskExpiryThreadIntervalSeconds, registeredEventListeners);
    addCache(manager, MULTI_SECURITIES_CACHE, maxElementsInMemory,
        memoryStoreEvictionPolicy, overflowToDisk, diskStorePath, eternal,
        timeToLiveSeconds, timeToIdleSeconds, diskPersistent,
        diskExpiryThreadIntervalSeconds, registeredEventListeners);
    _singleSecurityCache = getCacheFromManager(manager, SINGLE_SECURITY_CACHE);
    _multiSecuritiesCache = getCacheFromManager(manager, MULTI_SECURITIES_CACHE);
    _manager = manager;
  }

  public EHCachingSecurityMaster(SecurityMaster underlying, CacheManager manager) {
    ArgumentChecker.checkNotNull(underlying, "Security Master");
    ArgumentChecker.checkNotNull(manager, "CacheManager");
    _underlying = underlying;
    addCache(manager, SINGLE_SECURITY_CACHE);
    addCache(manager, MULTI_SECURITIES_CACHE);
    _singleSecurityCache = getCacheFromManager(manager, SINGLE_SECURITY_CACHE);
    _multiSecuritiesCache = getCacheFromManager(manager, MULTI_SECURITIES_CACHE);
    _manager = manager;
  }

  public EHCachingSecurityMaster(SecurityMaster underlying,
      Cache singleSecCache, Cache multiSecCache) {
    ArgumentChecker.checkNotNull(underlying, "Security Master");
    ArgumentChecker.checkNotNull(singleSecCache, "Single Security Cache");
    ArgumentChecker.checkNotNull(multiSecCache, "Multi Security Cache");
    _underlying = underlying;
    CacheManager manager = createCacheManager();
    addCache(manager, singleSecCache);
    addCache(manager, multiSecCache);
    _singleSecurityCache = getCacheFromManager(manager, singleSecCache.getName());
    _multiSecuritiesCache = getCacheFromManager(manager, multiSecCache.getName());
    _manager = manager;
  }

  /**
   * @param manager
   * @return
   */
  protected CacheManager createCacheManager() {
    CacheManager manager = null;
    try {
      manager = CacheManager.create();
    } catch (CacheException e) {
      throw new OpenGammaRuntimeException("Unable to create CacheManager", e);
    }
    return manager;
  }

  /**
   * @param manager
   * @param cache
   */
  protected void addCache(CacheManager manager, Cache cache) {
    ArgumentChecker.checkNotNull(manager, "CacheManager");
    ArgumentChecker.checkNotNull(cache, "Cache");
    if (!manager.cacheExists(cache.getName())) {
      try {
        manager.addCache(cache);
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("Unable to add cache " + cache.getName(), e);
      }
    }

  }

  /**
   * @param manager
   * @param name
   * @param maxElementsInMemory
   * @param memoryStoreEvictionPolicy
   * @param overflowToDisk
   * @param diskStorePath
   * @param eternal
   * @param timeToLiveSeconds
   * @param timeToIdleSeconds
   * @param diskPersistent
   * @param diskExpiryThreadIntervalSeconds
   * @param registeredEventListeners
   */
  protected void addCache(CacheManager manager, String name,
      int maxElementsInMemory,
      MemoryStoreEvictionPolicy memoryStoreEvictionPolicy,
      boolean overflowToDisk, String diskStorePath, boolean eternal,
      long timeToLiveSeconds, long timeToIdleSeconds, boolean diskPersistent,
      long diskExpiryThreadIntervalSeconds,
      RegisteredEventListeners registeredEventListeners) {
    ArgumentChecker.checkNotNull(manager, "CacheManager");
    ArgumentChecker.checkNotNull(name, "CacheName");
    if (!manager.cacheExists(name)) {
      try {
        manager.addCache(new Cache(name, maxElementsInMemory,
            memoryStoreEvictionPolicy, overflowToDisk, diskStorePath, eternal,
            timeToLiveSeconds, timeToIdleSeconds, diskPersistent,
            diskExpiryThreadIntervalSeconds, registeredEventListeners));
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("Unable to create cache " + name, e);
      }
    }
  }

  /**
   * @param manager
   */
  protected void addCache(final CacheManager manager, final String name) {
    if (!manager.cacheExists(name)) {
      try {
        manager.addCache(name);
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("Unable to create cache " + name, e);
      }
    }
  }

  /**
   * @param manager
   * @param name
   * @return
   */
  protected Cache getCacheFromManager(CacheManager manager, String name) {
    ArgumentChecker.checkNotNull(manager, "CacheManager");
    ArgumentChecker.checkNotNull(name, "cache name");
    Cache cache = null;
    try {
      cache = manager.getCache(name);
    } catch (Exception e) {
      throw new OpenGammaRuntimeException(
          "Unable to retrieve from CacheManager, cache: " + name, e);
    }
    return cache;
  }

  /**
   * @return the underlying
   */
  public SecurityMaster getUnderlying() {
    return _underlying;
  }

  /*
   * @return the CacheManager
   */
  public CacheManager getCacheManager() {
    return _manager;
  }

  @Override
  public Set<String> getAllSecurityTypes() {
    return getUnderlying().getAllSecurityTypes();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Security> getSecurities(SecurityKey secKey) {
    Element e = _multiSecuritiesCache.get(secKey);
    if (e != null) {
      Set<Security> result = new HashSet<Security>();
      Serializable value = e.getValue();
      if (isSetOfSecurityKeys(value)) {
        Set<SecurityKey> keys = (Set<SecurityKey>) value;
        for (SecurityKey key : keys) {
          result.add(getSecurity(key));
        }
      } else {
        s_logger.warn("returned object {} from cache is not a Set<SecurityKey> type", value);
      }
      return result;
    } else {
      Set<SecurityKey> keys = new HashSet<SecurityKey>();
      Collection<Security> securities = getUnderlying().getSecurities(secKey);
      if (securities != null && !securities.isEmpty()) {
        // TODO kirk 2009-11-03 -- Yomi, fix this.
        /*
        for (Security security : securities) {
          SecurityKey key = security.getIdentityKey();
          _singleSecurityCache.put(new Element(key, security));
          keys.add(key);
        }
        */
        _multiSecuritiesCache.put(new Element(secKey, keys));
      }
      return securities;
    }
  }

  /**
   * @param value
   * @return
   */
  private boolean isSetOfSecurityKeys(Serializable value) {
    if (!(value instanceof Set<?>)) {
      return false;
    } else {
      // REVIEW kirk 2009-10-26 -- The following block is a candidate for an assertion.
      /*
       * Set<?> elements = (Set<?>) value;
       * for (Object e : elements) {
       * if (!(e instanceof SecurityKey)) {
       * return false;
       * }
       * }
       */
    }
    return true;
  }

  @Override
  public Security getSecurity(SecurityKey secKey) {
    Element e = _singleSecurityCache.get(secKey);
    Security sec = null;
    if (e != null) {
      Serializable value = e.getValue();
      if (value instanceof Security) {
        sec = (Security) value;
        s_logger.debug("retrieved security: {} from single-security-cache", sec);
      } else {
        s_logger.warn("returned object {} from single-security-cache not a security type", value);
      }
    } else {
      sec = getUnderlying().getSecurity(secKey);
      if (sec != null) {
        _singleSecurityCache.put(new Element(secKey, sec));
      }
    }
    return sec;
  }

  /**
   * refresh value for given key
   */
  @SuppressWarnings("unchecked")
  public void refresh(SecurityKey secKey) {
    Element element = _multiSecuritiesCache.get(secKey);
    if (element != null) {
      Serializable value = element.getValue();
      if (isSetOfSecurityKeys(value)) {
        Set<SecurityKey> keys = (Set<SecurityKey>) value;
        for (SecurityKey key : keys) {
          _singleSecurityCache.remove(key);
        }
      }
      _multiSecuritiesCache.remove(secKey);
    }
    _singleSecurityCache.remove(secKey);
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

  @Override
  public Security getSecurity(String identityKey) {
    throw new OpenGammaRuntimeException("Not yet implemented.");
  }

}
