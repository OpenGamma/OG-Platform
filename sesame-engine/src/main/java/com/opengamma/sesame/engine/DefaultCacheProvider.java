/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import com.google.common.cache.Cache;
import com.opengamma.sesame.cache.CacheProvider;
import com.opengamma.sesame.cache.MethodInvocationKey;
import com.opengamma.util.ArgumentChecker;

/**
 * Immutable provider of a cache.
 */
public class DefaultCacheProvider implements CacheProvider {

  private final Cache<MethodInvocationKey, Object> _cache;

  /**
   * Creates a new instance which provides the supplied cache.
   *
   * @param cache the cache, not null
   */
  public DefaultCacheProvider(Cache<MethodInvocationKey, Object> cache) {
    _cache = ArgumentChecker.notNull(cache, "cache");
  }

  /**
   * @return the cache, not null
   */
  @Override
  public Cache<MethodInvocationKey, Object> get() {
    return _cache;
  }
}
