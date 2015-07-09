/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * The default implementation of {@link FunctionCache} which uses a Guava cache.
 * <p>
 * If the user clears the cache, a new cache is created and the existing cache is discarded at the end of the
 * calculation cycle. Therefore the cache is looked up every time it is used to ensure this class always
 * uses the current cache.
 */
public class DefaultFunctionCache implements FunctionCache {

  /** Provides the cache. The cache is looked up every time it is used. */
  private final CacheProvider _cacheProvider;

  /**
   * @param cacheProvider provides the cache. The cache is looked up every time it is used
   */
  public DefaultFunctionCache(CacheProvider cacheProvider) {
    _cacheProvider = ArgumentChecker.notNull(cacheProvider, "cacheProvider");
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(CacheKey key, Callable<T> valueSupplier) {
    try {
      return (T) _cacheProvider.get().get(key, valueSupplier);
    } catch (ExecutionException e) {
      throw new OpenGammaRuntimeException("Failed to create value for cache", e);
    }
  }
}
