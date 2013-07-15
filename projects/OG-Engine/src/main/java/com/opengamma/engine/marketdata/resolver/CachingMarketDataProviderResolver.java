/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.resolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.map.HashMap2;
import com.opengamma.util.map.Map2;
import com.opengamma.util.map.WeakValueHashMap2;

/**
 * Caching wrapper around {@link MarketDataProviderResolver} - this can be used when resolvers will create new instances each time when the call with the same user and specification could share the
 * same instance.
 */
public class CachingMarketDataProviderResolver implements MarketDataProviderResolver {

  private static final Logger s_logger = LoggerFactory.getLogger(CachingMarketDataProviderResolver.class);

  private final MarketDataProviderResolver _underlying;
  private final Map2<UserPrincipal, MarketDataSpecification, MarketDataProvider> _cache = new WeakValueHashMap2<UserPrincipal, MarketDataSpecification, MarketDataProvider>(HashMap2.STRONG_KEYS);

  public CachingMarketDataProviderResolver(final MarketDataProviderResolver underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  protected MarketDataProviderResolver getUnderlying() {
    return _underlying;
  }

  protected Map2<UserPrincipal, MarketDataSpecification, MarketDataProvider> getCache() {
    return _cache;
  }

  protected MarketDataProvider resolveCached(final UserPrincipal marketDataUser, final MarketDataSpecification snapshotSpec) {
    s_logger.debug("Looking up {} for {}", snapshotSpec, marketDataUser);
    return getCache().get(marketDataUser, snapshotSpec);
  }

  protected MarketDataProvider resolveUnderlying(final UserPrincipal marketDataUser, final MarketDataSpecification snapshotSpec) {
    s_logger.debug("Resolving {} for {}", snapshotSpec, marketDataUser);
    return getUnderlying().resolve(marketDataUser, snapshotSpec);
  }

  protected MarketDataProvider updateCache(final UserPrincipal marketDataUser, final MarketDataSpecification snapshotSpec, final MarketDataProvider provider) {
    final MarketDataProvider existing = getCache().putIfAbsent(marketDataUser, snapshotSpec, provider);
    if (existing == null) {
      s_logger.debug("Stored cache entry of {} for {}", snapshotSpec, marketDataUser);
      return provider;
    } else {
      s_logger.debug("Using existing cached entry of {} for {}", snapshotSpec, marketDataUser);
      return existing;
    }
  }

  // MarketDataProviderResolver

  @Override
  public MarketDataProvider resolve(final UserPrincipal marketDataUser, final MarketDataSpecification snapshotSpec) {
    MarketDataProvider provider = resolveCached(marketDataUser, snapshotSpec);
    if (provider != null) {
      s_logger.info("Already resolved {} for {}", snapshotSpec, marketDataUser);
      return provider;
    }
    provider = resolveUnderlying(marketDataUser, snapshotSpec);
    s_logger.info("Resolved {} for {}", snapshotSpec, marketDataUser);
    return updateCache(marketDataUser, snapshotSpec, provider);
  }

}
