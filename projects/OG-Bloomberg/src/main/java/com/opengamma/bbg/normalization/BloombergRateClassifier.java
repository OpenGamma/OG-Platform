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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.loader.BloombergEquityScaleResolver;
import com.opengamma.bbg.loader.BloombergFXForwardScaleResolver;
import com.opengamma.bbg.loader.BloombergSecurityTypeResolver;
import com.opengamma.bbg.loader.SecurityType;
import com.opengamma.bbg.loader.SecurityTypeResolver;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * Classifies Bloomberg rates based on the security type to determine the necessary normalization factor.
 */
public class BloombergRateClassifier {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergRateClassifier.class);

  private static final String CACHE_KEY = "bbg-classifier-cache";
  
  private final SecurityTypeResolver _securityTypeResolver;
  private final BloombergFXForwardScaleResolver _fwdScaleResolver;
  private final Cache _cache;
  private final ExternalScheme _bbgScheme;

  private final BloombergEquityScaleResolver _equityScaleResolver;
  
  /**
   * Constructs an instance.
   * 
   * @param referenceDataProvider  the underlying reference data provider, not null
   * @param cacheManager  the cache manager, not null
   * @param bbgScheme the scheme that should be used to subscribe, not null
   */
  public BloombergRateClassifier(ReferenceDataProvider referenceDataProvider, CacheManager cacheManager, ExternalScheme bbgScheme) {
    ArgumentChecker.notNull(referenceDataProvider, "referenceDataProvider");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    ArgumentChecker.notNull(bbgScheme, "bbgScheme");
    _securityTypeResolver = new BloombergSecurityTypeResolver(referenceDataProvider);
    _fwdScaleResolver = new BloombergFXForwardScaleResolver(referenceDataProvider, bbgScheme);
    _equityScaleResolver = new BloombergEquityScaleResolver(referenceDataProvider, bbgScheme);
    EHCacheUtils.addCache(cacheManager, CACHE_KEY);
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, CACHE_KEY);
    _bbgScheme = bbgScheme;
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
    ExternalIdBundle buidBundle = ExternalIdBundle.of(_bbgScheme, buid);
    SecurityType securityType = _securityTypeResolver.getSecurityType(Collections.singleton(buidBundle)).get(buidBundle);
    if (securityType == null) {
      s_logger.warn("Unable to determine security type for BUID " + buid);
      return null;
    }
    switch (securityType) {
      case BASIS_SWAP:
        return 10000;
      case FORWARD_CROSS:
      case FX_FORWARD:        
        Integer bbgFwdScale = _fwdScaleResolver.getBloombergFXForwardScale(Collections.singleton(buidBundle)).get(buidBundle);
        return getForwardScale(bbgFwdScale);
      case BILL:
      case BOND:
      case CASH:
      case CREDIT_DEFAULT_SWAP:
      case FRA:
      case INTEREST_RATE_FUTURE:
      case BOND_FUTURE:
      case IR_FUTURE_OPTION:
      case BOND_FUTURE_OPTION:
      case RATE:
      case INDEX:
      case SWAP:
      case VOLATILITY_QUOTE:
      case CD:
      case INFLATION_SWAP:
        return 100;
      case EQUITY_FUTURE:
        return 1;
      case INDEX_FUTURE:
        return 1;
      case EQUITY:
        Integer equityScale = _equityScaleResolver.getBloombergEquityScale(Collections.singleton(buidBundle)).get(buidBundle);
        return equityScale;
      default:
        return 1;
    }
  }
  
  private Integer getForwardScale(int bbgFwdScale) {
    switch(bbgFwdScale) {
      case 0:
        return 1;
      case 1:
        return 10;
      case 2: 
        return 100;
      case 3: 
        return 1000;
      case 4:
        return 10000;
      default:
        s_logger.warn("Unable to handle forward scale {}", bbgFwdScale);
        return null;
    }
  }
}
