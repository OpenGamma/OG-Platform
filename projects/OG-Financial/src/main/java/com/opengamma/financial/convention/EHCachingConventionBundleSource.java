/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.concurrent.ConcurrentMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.google.common.collect.MapMaker;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A cached form of {@link ConventionBundleSource}.
 */
public class EHCachingConventionBundleSource implements ConventionBundleSource {

  private static final String CONVENTION_CACHE_NAME = "conventionBundle";

  private final ConventionBundleSource _underlying;
  private final CacheManager _cacheManager;

  private final Cache _conventionCache;

  private final ConcurrentMap<Object, ConventionBundle> _frontCache = new MapMaker().weakValues().makeMap();

  public EHCachingConventionBundleSource(final ConventionBundleSource underlying, final CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _underlying = underlying;
    _cacheManager = cacheManager;
    EHCacheUtils.addCache(cacheManager, CONVENTION_CACHE_NAME);
    _conventionCache = EHCacheUtils.getCacheFromManager(cacheManager, CONVENTION_CACHE_NAME);
  }

  protected ConventionBundleSource getUnderlying() {
    return _underlying;
  }

  protected CacheManager getCacheManager() {
    return _cacheManager;
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache. It should not be part of a generic lifecycle method.
   */
  protected void shutdown() {
    getCacheManager().removeCache(CONVENTION_CACHE_NAME);
  }

  /**
   * For use by test methods only to control the front cache.
   */
  /* package */void emptyFrontCache() {
    _frontCache.clear();
  }

  /**
   * For use by test methods only to control the EH cache.
   */
  /* package */void emptyEHCache() {
    EHCacheUtils.clear(getCacheManager(), CONVENTION_CACHE_NAME);
  }

  protected ConventionBundle frontCache(final ConventionBundle bundle) {
    final ConventionBundle existing = _frontCache.putIfAbsent(bundle.getUniqueId(), bundle);
    if (existing == null) {
      if (bundle.getIdentifiers() != null) {
        _frontCache.put(bundle.getIdentifiers(), bundle);
      }
      return bundle;
    }
    return existing;
  }

  // ConventionBundleSource

  @Override
  public ConventionBundle getConventionBundle(final ExternalId identifier) {
    ConventionBundle bundle = _frontCache.get(identifier);
    if (bundle != null) {
      return bundle;
    }
    final Element e = _conventionCache.get(identifier);
    if (e != null) {
      bundle = frontCache((ConventionBundle) e.getObjectValue());
      _frontCache.put(identifier, bundle);
      return bundle;
    }
    bundle = getUnderlying().getConventionBundle(identifier);
    if (bundle == null) {
      // TODO: Is it worth caching the misses?
      return null;
    }
    bundle = frontCache(bundle);
    _conventionCache.put(new Element(identifier, bundle));
    _frontCache.put(identifier, bundle);
    return bundle;
  }

  @Override
  public ConventionBundle getConventionBundle(final ExternalIdBundle identifiers) {
    ConventionBundle bundle = _frontCache.get(identifiers);
    if (bundle != null) {
      return bundle;
    }
    final Element e = _conventionCache.get(identifiers);
    if (e != null) {
      bundle = frontCache((ConventionBundle) e.getObjectValue());
      _frontCache.put(identifiers, bundle);
      return bundle;
    }
    bundle = getUnderlying().getConventionBundle(identifiers);
    if (bundle == null) {
      // TODO: is it worth caching the misses?
      return null;
    }
    bundle = frontCache(bundle);
    _conventionCache.put(new Element(identifiers, bundle));
    _frontCache.put(identifiers, bundle);
    return bundle;
  }

  @Override
  public ConventionBundle getConventionBundle(final UniqueId identifier) {
    ConventionBundle bundle = _frontCache.get(identifier);
    if (bundle != null) {
      return bundle;
    }
    final Element e = _conventionCache.get(identifier);
    if (e != null) {
      return frontCache((ConventionBundle) e.getObjectValue());
    }
    bundle = getUnderlying().getConventionBundle(identifier);
    if (bundle == null) {
      // TODO: is it worth caching the misses?
      return null;
    }
    bundle = frontCache(bundle);
    _conventionCache.put(new Element(identifier, bundle));
    return bundle;
  }

}
