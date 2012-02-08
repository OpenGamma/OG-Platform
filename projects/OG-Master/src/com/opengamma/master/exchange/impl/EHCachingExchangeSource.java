/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

import java.util.Collection;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A cache to optimize the results of {@code ExchangeSource}.
 */
public class EHCachingExchangeSource implements ExchangeSource {

  /**
   * Cache key for exhanges.
   */
  private static final String EXCHANGE_EXTERNAL_ID_CACHE = "exchange.externalId";

  /**
   * The cache manager.
   */
  private final CacheManager _cacheManager;
  /**
   * The result cache.
   */
  private final Cache _exchangeExternalIdCache;

  private final ExchangeSource _underlying;

  private Integer _ttl;

  /**
   * Creates the cache around an underlying exchange source.
   * 
   * @param underlying  the underlying data, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingExchangeSource(final ExchangeSource underlying, final CacheManager cacheManager) {
    _underlying = underlying;
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _cacheManager = cacheManager;
    EHCacheUtils.addCache(cacheManager, EXCHANGE_EXTERNAL_ID_CACHE);
    _exchangeExternalIdCache = EHCacheUtils.getCacheFromManager(cacheManager, EXCHANGE_EXTERNAL_ID_CACHE);
  }

  public void setTTL(final Integer ttl) {
    _ttl = ttl;
  }

  public CacheManager getCacheManager() {
    return _cacheManager;
  }
  
  @Override
  public Exchange getExchange(UniqueId uniqueId) {
    return _underlying.getExchange(uniqueId);
  }

  @Override
  public Exchange getExchange(ObjectId objectId, VersionCorrection versionCorrection) {
    return _underlying.getExchange(objectId, versionCorrection);
  }

  @Override
  public Collection<? extends Exchange> getExchanges(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    return _underlying.getExchanges(bundle, versionCorrection);
  }

  @Override
  public Exchange getSingleExchange(ExternalId identifier) {
    Element element = _exchangeExternalIdCache.get(identifier);
    if (element != null) {
      return (Exchange) element.getValue();
    }
    
    Exchange underlying = _underlying.getSingleExchange(identifier);
    element = new Element(identifier, underlying);
    if (_ttl != null) {
      element.setTimeToLive(_ttl);
    }
    _exchangeExternalIdCache.put(element);
    return underlying;
  }

  @Override
  public Exchange getSingleExchange(ExternalIdBundle identifierBundle) {
    return _underlying.getSingleExchange(identifierBundle);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + _underlying + "]";
  }

}
