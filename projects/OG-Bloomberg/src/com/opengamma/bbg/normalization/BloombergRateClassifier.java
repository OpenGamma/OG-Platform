/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.normalization;

import java.util.Collections;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.loader.BloombergSecurityTypeResolver;
import com.opengamma.bbg.loader.SecurityType;
import com.opengamma.bbg.loader.SecurityTypeResolver;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * Classifies Bloomberg rates based on the security type to determine the necessary normalization factor.
 */
public class BloombergRateClassifier {

  private static final Logger s_logger = LoggerFactory.getLogger(BloombergRateClassifier.class);
  
  private static final String CACHE_KEY = "bbg-classifier-cache";
  private static final int CACHE_SIZE = 10000;
  
  private final SecurityTypeResolver _securityTypeResolver;
  private final Cache _cache;
  
  /**
   * Constructs an instance.
   * 
   * @param referenceDataProvider  the underlying reference data provider, not null
   * @param cacheManager  the cache manager, not null
   */
  public BloombergRateClassifier(ReferenceDataProvider referenceDataProvider, CacheManager cacheManager) {
    ArgumentChecker.notNull(referenceDataProvider, "referenceDataProvider");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _securityTypeResolver = new BloombergSecurityTypeResolver(referenceDataProvider);
    EHCacheUtils.addCache(cacheManager, CACHE_KEY, CACHE_SIZE, MemoryStoreEvictionPolicy.LRU, false, null, true, 0, 0, false, 0, null);
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, CACHE_KEY);
  }
  
  /**
   * Gets the normalization factor for a given security: 1, 100 or 10000.
   * <p>
   * The normalization factor is the multiple by which the market data is greater than the normalized value. 
   * 
   * @param buid  the BUID value of the security, not null
   * @return the normalization factor, or null if the security cannot be classified
   */
  public Integer getNormalizationFactor(String buid) {
    ArgumentChecker.notNull(buid, "buid");
    Element e = _cache.get(buid);
    if (e != null) {
      s_logger.debug("Obtained normalization factor for security " + buid + " from cache");
      return (Integer) e.getObjectValue();
    }
    try {
      Integer normalizationFactor = getNormalizationFactorCore(buid);
      s_logger.debug("Generated normalization factor {} for security {}", normalizationFactor, buid);
      e = new Element(buid, normalizationFactor);
      _cache.put(e);
      return normalizationFactor;
    } catch (Exception ex) {
      s_logger.warn("Error obtaining normalization factor for security " + buid, ex);
      throw new OpenGammaRuntimeException("Error obtaining normalization factor for security " + buid, ex);
    }
  }

  private Integer getNormalizationFactorCore(String buid) {
    ExternalIdBundle buidBundle = ExternalIdBundle.of(SecurityUtils.BLOOMBERG_BUID, buid);
    SecurityType securityType = _securityTypeResolver.getSecurityType(Collections.singleton(buidBundle)).get(buidBundle);
    if (securityType == null) {
      s_logger.warn("Unable to determine security type for BUID " + buid);
      return null;
    }
    switch (securityType) {
      case BASIS_SWAP:
      case FORWARD_CROSS:
        return 10000;
      case CASH:
      case FRA:
      case INTEREST_RATE_FUTURE:
      case IR_FUTURE_OPTION:
      case RATE:
      case SWAP:
      case VOLATILITY_QUOTE:
        return 100;
      default:
        return 1;
    }
  }
  
}
