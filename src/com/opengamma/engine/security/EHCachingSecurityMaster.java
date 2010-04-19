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
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.RegisteredEventListeners;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.EHCacheUtils;

/**
 * 
 * 
 * @author yomi
 */
public class EHCachingSecurityMaster implements SecurityMaster {
  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingSecurityMaster.class);
  public static String SINGLE_SECURITY_CACHE = "single-security-cache";
  public static String MULTI_SECURITIES_CACHE = "multi-securities-cache";

  private final SecurityMaster _underlying;
  private final CacheManager _manager;
  private final Cache _singleSecurityCache;
  private final Cache _multiSecuritiesCache;

  public EHCachingSecurityMaster(SecurityMaster underlying) {
    ArgumentChecker.checkNotNull(underlying, "Security Master");
    _underlying = underlying;
    CacheManager manager = EHCacheUtils.createCacheManager();
    EHCacheUtils.addCache(manager, SINGLE_SECURITY_CACHE);
    EHCacheUtils.addCache(manager, MULTI_SECURITIES_CACHE);
    _singleSecurityCache = EHCacheUtils.getCacheFromManager(manager, SINGLE_SECURITY_CACHE);
    _multiSecuritiesCache = EHCacheUtils.getCacheFromManager(manager, MULTI_SECURITIES_CACHE);
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
    CacheManager manager = EHCacheUtils.createCacheManager();
    EHCacheUtils.addCache(manager, SINGLE_SECURITY_CACHE, maxElementsInMemory,
        memoryStoreEvictionPolicy, overflowToDisk, diskStorePath, eternal,
        timeToLiveSeconds, timeToIdleSeconds, diskPersistent,
        diskExpiryThreadIntervalSeconds, registeredEventListeners);
    EHCacheUtils.addCache(manager, MULTI_SECURITIES_CACHE, maxElementsInMemory,
        memoryStoreEvictionPolicy, overflowToDisk, diskStorePath, eternal,
        timeToLiveSeconds, timeToIdleSeconds, diskPersistent,
        diskExpiryThreadIntervalSeconds, registeredEventListeners);
    _singleSecurityCache = EHCacheUtils.getCacheFromManager(manager, SINGLE_SECURITY_CACHE);
    _multiSecuritiesCache = EHCacheUtils.getCacheFromManager(manager, MULTI_SECURITIES_CACHE);
    _manager = manager;
  }

  public EHCachingSecurityMaster(SecurityMaster underlying, CacheManager manager) {
    ArgumentChecker.checkNotNull(underlying, "Security Master");
    ArgumentChecker.checkNotNull(manager, "CacheManager");
    _underlying = underlying;
    EHCacheUtils.addCache(manager, SINGLE_SECURITY_CACHE);
    EHCacheUtils.addCache(manager, MULTI_SECURITIES_CACHE);
    _singleSecurityCache = EHCacheUtils.getCacheFromManager(manager, SINGLE_SECURITY_CACHE);
    _multiSecuritiesCache = EHCacheUtils.getCacheFromManager(manager, MULTI_SECURITIES_CACHE);
    _manager = manager;
  }

  public EHCachingSecurityMaster(SecurityMaster underlying,
      Cache singleSecCache, Cache multiSecCache) {
    ArgumentChecker.checkNotNull(underlying, "Security Master");
    ArgumentChecker.checkNotNull(singleSecCache, "Single Security Cache");
    ArgumentChecker.checkNotNull(multiSecCache, "Multi Security Cache");
    _underlying = underlying;
    CacheManager manager = EHCacheUtils.createCacheManager();
    EHCacheUtils.addCache(manager, singleSecCache);
    EHCacheUtils.addCache(manager, multiSecCache);
    _singleSecurityCache = EHCacheUtils.getCacheFromManager(manager, singleSecCache.getName());
    _multiSecuritiesCache = EHCacheUtils.getCacheFromManager(manager, multiSecCache.getName());
    _manager = manager;
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
  public Collection<Security> getSecurities(IdentifierBundle secKey) {
    Element e = _multiSecuritiesCache.get(secKey);
    Collection<Security> securities = new HashSet<Security>();
    if (e != null) {
      Serializable value = e.getValue();
      if (value instanceof Set<?>) {
        Set<Identifier> identityKeys = (Set<Identifier>) value;
        for (Identifier identityKey : identityKeys) {
          securities.add(getSecurity(identityKey));
        }
      } else {
        s_logger.warn("returned object {} from cache is not a Set<SecurityKey> type", value);
      }
    } else {
      Set<Identifier> identityKeys = new HashSet<Identifier>();
      securities = getUnderlying().getSecurities(secKey);
      if (securities != null && !securities.isEmpty()) {
        for (Security security : securities) {
          Identifier identityKey = security.getIdentityKey();
          _singleSecurityCache.put(new Element(identityKey, security));
          identityKeys.add(identityKey);
        }
        _multiSecuritiesCache.put(new Element(secKey, identityKeys));
      }
    }
    return securities;
  }

  @Override
  public Security getSecurity(IdentifierBundle secKey) {
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
   * refresh value for given security key
   */
  @SuppressWarnings("unchecked")
  public void refresh(Object secKey) {
    ArgumentChecker.checkNotNull(secKey, "Security Key");
    Element element = _multiSecuritiesCache.get(secKey);
    if (element != null) {
      Serializable value = element.getValue();
      if (value instanceof Set<?>) {
        Set<Identifier> keys = (Set<Identifier>) value;
        for (Identifier key : keys) {
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
  public Security getSecurity(Identifier identityKey) {
    Element e = _singleSecurityCache.get(identityKey);
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
      sec = getUnderlying().getSecurity(identityKey);
      if (sec != null) {
        _singleSecurityCache.put(new Element(identityKey, sec));
      }
    }
    return sec;
  }

}
