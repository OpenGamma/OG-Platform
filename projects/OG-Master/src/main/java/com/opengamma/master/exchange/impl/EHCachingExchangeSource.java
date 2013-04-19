/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

import net.sf.ehcache.CacheManager;

import com.opengamma.core.AbstractEHCachingSourceWithExternalBundle;
import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.id.ExternalId;

/**
 * A cache to optimize the results of {@code ExchangeSource}.
 */
public class EHCachingExchangeSource extends AbstractEHCachingSourceWithExternalBundle<Exchange, ExchangeSource> implements ExchangeSource {

  /**
   * Creates the cache around an underlying exchange source.
   * 
   * @param underlying the underlying data, not null
   * @param cacheManager the cache manager, not null
   */
  public EHCachingExchangeSource(final ExchangeSource underlying, final CacheManager cacheManager) {
    super(underlying, cacheManager);
  }

  @Override
  public Exchange getSingle(final ExternalId identifier) {
    final Exchange result = getUnderlying().getSingle(identifier);
    cacheItem(result);
    return result;
  }

}
