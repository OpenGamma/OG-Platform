/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A cache wrapping a {@link SecurityRuleProvider}.
 */
public class EHCachingSecurityRuleProvider implements SecurityRuleProvider {
  
  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingSecurityRuleProvider.class);
  
  private final SecurityRuleProvider _underlying;
  private final Cache _cache;
  
  /**
   * Constructs an instance with an in-memory cache.
   * 
   * @param underlying  the underlying security rule provider, not null
   * @param cacheManager  the cache manager, not null
   * @param cacheName  the name of the cache, not null
   * @param maxElementsInMemory  the maximum number of security rules to cache
   */
  public EHCachingSecurityRuleProvider(SecurityRuleProvider underlying, CacheManager cacheManager, String cacheName, int maxElementsInMemory) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    ArgumentChecker.notNull(cacheName, "cacheName");
    _underlying = underlying;
    EHCacheUtils.addCache(cacheManager, cacheName, maxElementsInMemory, MemoryStoreEvictionPolicy.LRU, false, null, true, 0, 0, false, 0, null);
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, cacheName);
  }
  
  @Override
  public NormalizationRule getRule(String securityUniqueId) {
    Element e = _cache.get(securityUniqueId);
    if (e != null) {
      s_logger.debug("Obtained normalization rule for security " + securityUniqueId + " from cache");
      return (NormalizationRule) e.getObjectValue();
    }
    try {
      NormalizationRule rule = _underlying.getRule(securityUniqueId);
      s_logger.debug("Obtained normalization rule for security {} from underlying provider", securityUniqueId);
      e = new Element(securityUniqueId, rule);
      _cache.put(e);
      return rule;
    } catch (Exception ex) {
      // Don't attempt to cache exceptions as:
      //   a) they will cause the subscription to fail so the cache will be of little use
      //   b) if the error can be fixed at runtime then the user may try again and expect success
      s_logger.warn("Error obtaining normalization rule for security " + securityUniqueId, ex);
      throw new OpenGammaRuntimeException("Error obtaining normalization rule for security " + securityUniqueId, ex);
    }
  }

}
