/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.concurrent.ConcurrentMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.google.common.collect.MapMaker;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A cached form of {@link ConventionSource}.
 */
public class EHCachingConventionSource implements ConventionSource {

  private static final String CONVENTION_CACHE_NAME = "convention";

  private final ConventionSource _underlying;
  private final CacheManager _cacheManager;

  private final Cache _conventionCache;

  private final ConcurrentMap<Object, Convention> _frontCache = new MapMaker().weakValues().makeMap();

  public EHCachingConventionSource(final ConventionSource underlying, final CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _underlying = underlying;
    _cacheManager = cacheManager;
    EHCacheUtils.addCache(cacheManager, CONVENTION_CACHE_NAME);
    _conventionCache = EHCacheUtils.getCacheFromManager(cacheManager, CONVENTION_CACHE_NAME);
  }

  protected ConventionSource getUnderlying() {
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

  protected Convention frontCache(final Convention convention) {
    final Convention existing = _frontCache.putIfAbsent(convention.getUniqueId(), convention);
    if (existing == null) {
      for (ExternalId identifier : convention.getExternalIdBundle()) {
        _frontCache.put(identifier, convention);
      }
      return convention;
    }
    return existing;
  }

  // ConventionSource

  @Override
  public Convention getConvention(final ExternalId identifier) {
    Convention convention = _frontCache.get(identifier);
    if (convention != null) {
      return convention;
    }
    final Element e = _conventionCache.get(identifier);
    if (e != null) {
      return frontCache((Convention) e.getObjectValue());
    }
    convention = getUnderlying().getConvention(identifier);
    if (convention == null) {
      return null;
    }
    final Convention front = frontCache(convention);
    if (front == convention) {
      _conventionCache.put(new Element(convention.getUniqueId(), convention));
      _conventionCache.put(new Element(identifier, convention));
      for (ExternalId eid : convention.getExternalIdBundle()) {
        if (!identifier.equals(eid)) {
          _conventionCache.put(new Element(eid, convention));
        }
      }
    }
    return front;
  }

  @Override
  public Convention getConvention(final ExternalIdBundle identifiers) {
    Convention convention;
    for (ExternalId identifier : identifiers) {
      convention = _frontCache.get(identifier);
      if (convention != null) {
        return convention;
      }
    }
    for (ExternalId identifier : identifiers) {
      final Element e = _conventionCache.get(identifier);
      if (e != null) {
        return frontCache((Convention) e.getObjectValue());
      }
    }
    convention = getUnderlying().getConvention(identifiers);
    if (convention == null) {
      return null;
    }
    final Convention front = frontCache(convention);
    if (front == convention) {
      _conventionCache.put(new Element(convention.getUniqueId(), convention));
      for (ExternalId eid : convention.getExternalIdBundle()) {
        _conventionCache.put(new Element(eid, convention));
      }
    }
    return front;
  }

  @Override
  public Convention getConvention(final UniqueId identifier) {
    Convention convention = _frontCache.get(identifier);
    if (convention != null) {
      return convention;
    }
    final Element e = _conventionCache.get(identifier);
    if (e != null) {
      return frontCache((Convention) e.getObjectValue());
    }
    convention = getUnderlying().getConvention(identifier);
    if (convention == null) {
      return null;
    }
    final Convention front = frontCache(convention);
    if (front == convention) {
      _conventionCache.put(new Element(identifier, convention));
      if (!convention.getUniqueId().equals(identifier)) {
        _conventionCache.put(new Element(convention.getUniqueId(), convention));
      }
      for (ExternalId eid : convention.getExternalIdBundle()) {
        _conventionCache.put(new Element(eid, convention));
      }
    }
    return front;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Convention> T getConvention(final Class<T> clazz, final ExternalId identifier) {
    final Convention convention = getConvention(identifier);
    if (convention == null) {
      return null;
    }
    if (clazz.isAssignableFrom(convention.getClass())) {
      return (T) convention;
    }
    throw new OpenGammaRuntimeException("Convention for " + identifier + " was not of expected type " + clazz);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Convention> T getConvention(final Class<T> clazz, final ExternalIdBundle identifiers) {
    final Convention convention = getConvention(identifiers);
    if (convention == null) {
      return null;
    }
    if (clazz.isAssignableFrom(convention.getClass())) {
      return (T) convention;
    }
    throw new OpenGammaRuntimeException("Convention for " + identifiers + " was not of expected type " + clazz);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Convention> T getConvention(final Class<T> clazz, final UniqueId identifier) {
    final Convention convention = getConvention(identifier);
    if (convention == null) {
      return null;
    }
    if (clazz.isAssignableFrom(convention.getClass())) {
      return (T) convention;
    }
    throw new OpenGammaRuntimeException("Convention for " + identifier + " was not of expected type " + clazz);
  }

}
