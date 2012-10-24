/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Collection;
import java.util.HashSet;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.AbstractEHCachingSourceWithExternalBundle;
import com.opengamma.core.security.Security;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A cache decorating a {@code FinancialSecuritySource}.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingFinancialSecuritySource
    extends AbstractEHCachingSourceWithExternalBundle<Security, FinancialSecuritySource>
    implements FinancialSecuritySource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingFinancialSecuritySource.class);

  /** The mulitple bonds cache key */
  /* package for testing */static final String MULTI_BONDS_CACHE = "multi-bonds-cache";

  /**
   * The bond cache.
   */
  private final Cache _bondCache;

  /**
   * Creates an instance over an underlying source specifying the cache manager.
   *
   * @param underlying  the underlying security source, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingFinancialSecuritySource(final FinancialSecuritySource underlying, final CacheManager cacheManager) {
    super(underlying, cacheManager);

    EHCacheUtils.addCache(cacheManager, MULTI_BONDS_CACHE);
    _bondCache = EHCacheUtils.getCacheFromManager(cacheManager, MULTI_BONDS_CACHE);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public Collection<Security> getBondsWithIssuerName(String issuerType) {
    ArgumentChecker.notNull(issuerType, "issuerType");
    Element e = _bondCache.get(issuerType);
    Collection<Security> result = new HashSet<Security>();
    if (e != null) {
      if (e.getObjectValue() instanceof Collection<?>) {
        result.addAll((Collection<Security>) e.getObjectValue());
      } else {
        s_logger.warn("returned object {} from bond cache is not a Collection<Security>", e.getObjectValue());
      }
    } else {
      result = getUnderlying().getBondsWithIssuerName(issuerType);
      if (result != null) {
        _bondCache.put(new Element(issuerType, result));
        cacheItems(result);
      }
    }
    return result;
  }

}
