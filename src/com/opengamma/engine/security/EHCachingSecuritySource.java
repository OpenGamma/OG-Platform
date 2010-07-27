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
   * Creates an instance over an underlying source using a default cache manager.
   * @param underlying  the underlying security source, not null
   */
  public EHCachingSecuritySource(final SecuritySource underlying) {
    this(underlying, EHCacheUtils.createCacheManager());
  }

  /**
   * Creates an instance over an underlying source specifying the cache manager.
   * @param underlying  the underlying security source, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingSecuritySource(final SecuritySource underlying, final CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _underlying = underlying;
    EHCacheUtils.addCache(cacheManager, SINGLE_SECURITY_CACHE);
    EHCacheUtils.addCache(cacheManager, MULTI_SECURITIES_CACHE);
    _uidCache = EHCacheUtils.getCacheFromManager(cacheManager, SINGLE_SECURITY_CACHE);
    _bundleCache = EHCacheUtils.getCacheFromManager(cacheManager, MULTI_SECURITIES_CACHE);
    _manager = cacheManager;
  }

  /**
   * Creates an instance over an underlying source specifying the caches.
   * @param underlying  the underlying security source, not null
   * @param singleSecCache  the single security cache, not null
   * @param multiSecCache  the multiple security cache, not null
   */
  public EHCachingSecuritySource(SecuritySource underlying,
      Cache singleSecCache, Cache multiSecCache) {
    ArgumentChecker.notNull(underlying, "Security Master");
    ArgumentChecker.notNull(singleSecCache, "singleSecCache");
    ArgumentChecker.notNull(multiSecCache, "multiSecCache");
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
   * Gets the underlying source of securities.
   * @return the underlying source of securities, not null
   */
  protected SecuritySource getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the cache manager.
   * @return the cache manager, not null
   */
  protected CacheManager getCacheManager() {
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
      if (value instanceof Collection<?>) {
        result.addAll((Collection<Security>) value);
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
