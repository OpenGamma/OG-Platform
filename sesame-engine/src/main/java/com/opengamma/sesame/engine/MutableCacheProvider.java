/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.concurrent.FutureTask;

import com.google.common.cache.Cache;
import com.opengamma.sesame.cache.CacheProvider;
import com.opengamma.sesame.cache.MethodInvocationKey;
import com.opengamma.util.ArgumentChecker;

/**
 * Mutable holder for a cache.
 * <p>
 * This allows the view to set the cache at the start of each calculation cycle and the cache proxy to
 * have access to the view's current cache. This is necessary because the graph and the cache proxy have
 * the same lifetime as the view, but the cache's lifetime can be as short as a single cycle.
 */
public class MutableCacheProvider implements CacheProvider {

  private Cache<MethodInvocationKey, FutureTask<Object>> _cache;

  /**
   * Creates an empty instance. {@link #set(Cache)} must be called before {@link #get()} is used.
   */
  public MutableCacheProvider() {
  }

  /**
   * Creates a new instance which provides the supplied cache.
   *
   * @param cache the cache, not null
   */
  public MutableCacheProvider(Cache<MethodInvocationKey, FutureTask<Object>> cache) {
    _cache = ArgumentChecker.notNull(cache, "cache");
  }

  /**
   * Sets the cache. Package private so it can only be used by {@link View}.
   *
   * @param cache the cache, not null
   */
  void set(Cache<MethodInvocationKey, FutureTask<Object>> cache) {
    _cache = ArgumentChecker.notNull(cache, "cache");
  }

  /**
   * @return the cache, not null
   */
  @Override
  public Cache<MethodInvocationKey, FutureTask<Object>> get() {
    if (_cache == null) {
      throw new IllegalStateException("No cache has been set");
    }
    return _cache;
  }
}
