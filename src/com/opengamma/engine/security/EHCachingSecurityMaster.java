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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;


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
  
  // Constructor options:
  // - Just like it is now
  // - With a CacheManager provided
  // - With both Caches provided
  /** 
   * @param underlying
   */
  public EHCachingSecurityMaster(SecurityMaster underlying) {
    ArgumentChecker.checkNotNull(underlying, "Security Master");
    _underlying = underlying; 
    CacheManager manager = CacheManager.create();
    if (!manager.cacheExists(SINGLE_SECURITY_CACHE)) {
      manager.addCache(SINGLE_SECURITY_CACHE);
    }
    if (!manager.cacheExists(MULTI_SECURITIES_CACHE)) {
      manager.addCache(MULTI_SECURITIES_CACHE);
    }
    _singleSecurityCache = manager.getCache(SINGLE_SECURITY_CACHE);
    _multiSecuritiesCache = manager.getCache(MULTI_SECURITIES_CACHE);
    _manager = manager;
  }
  
  public EHCachingSecurityMaster(SecurityMaster underlying, CacheManager manager) {
    ArgumentChecker.checkNotNull(underlying, "Security Master");
    ArgumentChecker.checkNotNull(manager, "CacheManager");
    _underlying = underlying; 
    if (!manager.cacheExists(SINGLE_SECURITY_CACHE)) {
      manager.addCache(SINGLE_SECURITY_CACHE);
    }
    if (!manager.cacheExists(MULTI_SECURITIES_CACHE)) {
      manager.addCache(MULTI_SECURITIES_CACHE);
    }
    _singleSecurityCache = manager.getCache(SINGLE_SECURITY_CACHE);
    _multiSecuritiesCache = manager.getCache(MULTI_SECURITIES_CACHE);
    _manager = manager;
  }
  
  public EHCachingSecurityMaster(SecurityMaster underlying, Cache singleSecCache, Cache multiSecCache) {
    ArgumentChecker.checkNotNull(underlying, "Security Master");
    ArgumentChecker.checkNotNull(singleSecCache, "Single Security Cache");
    ArgumentChecker.checkNotNull(multiSecCache, "Multi Security Cache");
    _underlying = underlying; 
    CacheManager manager = CacheManager.create();
    manager.addCache(singleSecCache);
    manager.addCache(multiSecCache);
    _singleSecurityCache = singleSecCache;
    _multiSecuritiesCache = multiSecCache;
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
  public Collection<Security> getSecurities(SecurityKey secKey) {
    Element e = _multiSecuritiesCache.get(secKey);
    if (e != null) {
      Set<Security> result = new HashSet<Security>();
      Serializable value = e.getValue();
      if (isSetOfSecurityKeys(value)) {
        Set<SecurityKey> keys = (Set<SecurityKey>)value;
        for (SecurityKey key : keys) {
          result.add(getSecurity(key));
        }
      } else {
        s_logger.warn("returned object {} from cache is not a Set<SecurityKey> type", value);
      }
      return result;
    } else {
      // REVIEW kirk 2009-10-26 -- There's a major concurrency problem here if the instance
      // or the cache is shared. Need to resolve that.
      Set<SecurityKey> keys = new HashSet<SecurityKey>();
      Collection<Security> securities = getUnderlying().getSecurities(secKey);
      if (securities != null && !securities.isEmpty()) {
        for (Security security : securities) {
          SecurityKey key = security.getIdentityKey();
          _singleSecurityCache.put(new Element(key, security));
          keys.add(key);
        }
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
      Set<?> elements = (Set<?>) value;
      for (Object e : elements) {
        if (!(e instanceof SecurityKey)) {
          return false;
        }
      }
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
        sec = (Security)value;
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
   * refresh value  for given key 
   */
  public void refresh(String key) { 
    _singleSecurityCache.remove(key); 
    _multiSecuritiesCache.remove(key);
  }
   
  /**
   * to call eventually  when your application is  exiting  
   */
  public void shutdown() { 
    _manager.removeCache(SINGLE_SECURITY_CACHE);
    _manager.removeCache(MULTI_SECURITIES_CACHE);
    _manager.shutdown(); 
  } 

}
