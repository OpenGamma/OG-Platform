/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.organization.impl;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.AbstractEHCachingSource;
import com.opengamma.core.organization.Organization;
import com.opengamma.core.organization.OrganizationSource;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A cache decorating a {@code OrganizationSource}.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingOrganizationSource extends AbstractEHCachingSource<Organization, OrganizationSource> implements OrganizationSource {

  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingOrganizationSource.class);
  /**
   * The cache name.
   */
  private static final String RED_CACHE_NAME = "OrganizationCacheRED";

  /**
   * The cache name.
   */
  private static final String TICKER_CACHE_NAME = "OrganizationCacheTicker";

  /**
   * The RED cache.
   */
  private final Cache _redCache;
  
  /**
   * The Ticker cache.
   */
  private final Cache _tickerCache;
  
  private OrganizationSource _underling;

  /**
   * Creates an instance.
   * 
   * @param underlying the underlying source, not null
   * @param cacheManager the cache manager, not null
   */
  public EHCachingOrganizationSource(OrganizationSource underlying, CacheManager cacheManager) {
    super(underlying, cacheManager);
    EHCacheUtils.addCache(cacheManager, RED_CACHE_NAME);
    EHCacheUtils.addCache(cacheManager, TICKER_CACHE_NAME);
    _redCache = EHCacheUtils.getCacheFromManager(cacheManager, RED_CACHE_NAME);
    _tickerCache = EHCacheUtils.getCacheFromManager(cacheManager, TICKER_CACHE_NAME);
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache. It should not be part of a generic lifecycle method.
   */
  public void shutdown() {
    _redCache.getCacheManager().removeCache(RED_CACHE_NAME);
    _tickerCache.getCacheManager().removeCache(TICKER_CACHE_NAME);
  }
  
  @Override
  public Organization getOrganizationByRedCode(String redCode) {
    Element element = _redCache.get(redCode);
    if (element != null) {
      s_logger.debug("Cache hit on {}", redCode);
      return (Organization) element.getObjectValue();
    } else {
      s_logger.debug("Cache miss on {}", redCode);
      Organization organization = getUnderlying().getOrganizationByRedCode(redCode);
      _redCache.put(new Element(redCode, organization));
      if (organization != null && organization.getObligor() != null && organization.getObligor().getObligorTicker() != null) {
        // cross populate the ticker cache
        _tickerCache.put(new Element(organization.getObligor().getObligorTicker(), organization));
      }
      return organization;
    }
  }

  @Override
  public Organization getOrganizationByTicker(String ticker) {
    Element element = _tickerCache.get(ticker);
    if (element != null) {
      s_logger.debug("Cache hit on {}", ticker);
      return (Organization) element.getObjectValue();
    } else {
      s_logger.debug("Cache miss on {}", ticker);
      Organization organization = getUnderlying().getOrganizationByRedCode(ticker);
      _tickerCache.put(new Element(ticker, organization));
      if (organization != null && organization.getObligor() != null && organization.getObligor().getObligorTicker() != null) {
        // cross populate the ticker cache
        _redCache.put(new Element(organization.getObligor().getObligorREDCode(), organization));
      }
      return organization;
    }  
  }

}
