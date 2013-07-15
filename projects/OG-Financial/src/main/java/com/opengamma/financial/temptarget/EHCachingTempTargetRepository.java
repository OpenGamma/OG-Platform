/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.temptarget;

import java.util.concurrent.ConcurrentMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.springframework.context.Lifecycle;

import com.google.common.collect.MapMaker;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * EH Cache {@link TempTargetRepository} wrapper.
 */
public class EHCachingTempTargetRepository implements TempTargetRepository, Lifecycle {

  private static final String CACHE_NAME = "TempTargetRepository";

  private final TempTargetRepository _underlying;

  private final ConcurrentMap<UniqueId, TempTarget> _frontCache = new MapMaker().weakValues().makeMap();

  private final Cache _cache;

  private boolean _running;

  public EHCachingTempTargetRepository(final TempTargetRepository underlying, final CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
    EHCacheUtils.addCache(cacheManager, CACHE_NAME);
    // TODO: This needs to be "in-memory" only
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, CACHE_NAME);
  }

  protected TempTargetRepository getUnderlying() {
    return _underlying;
  }

  protected Cache getCache() {
    return _cache;
  }

  // TempTargetRepository

  @Override
  public TempTarget get(UniqueId identifier) {
    TempTarget cached = _frontCache.get(identifier);
    if (cached != null) {
      return cached;
    }
    cached = getUnderlying().get(identifier);
    final TempTarget existing = _frontCache.putIfAbsent(identifier, cached);
    if (existing != null) {
      return existing;
    }
    return cached;
  }

  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

  @Override
  public UniqueId locateOrStore(TempTarget target) {
    Element element = getCache().get(target);
    if (element != null) {
      return (UniqueId) element.getObjectValue();
    }
    final UniqueId uid = getUnderlying().locateOrStore(target);
    _frontCache.putIfAbsent(uid, target.withUniqueId(uid));
    getCache().put(new Element(target, uid));
    return uid;
  }

  // Lifecycle

  @Override
  public synchronized void start() {
    if (getUnderlying() instanceof Lifecycle) {
      ((Lifecycle) getUnderlying()).start();
    }
    _running = true;
  }

  @Override
  public synchronized void stop() {
    _running = false;
    if (getUnderlying() instanceof Lifecycle) {
      ((Lifecycle) getUnderlying()).stop();
    }
  }

  @Override
  public synchronized boolean isRunning() {
    if (!_running) {
      return false;
    }
    if (getUnderlying() instanceof Lifecycle) {
      return ((Lifecycle) getUnderlying()).isRunning();
    }
    return true;
  }

}
