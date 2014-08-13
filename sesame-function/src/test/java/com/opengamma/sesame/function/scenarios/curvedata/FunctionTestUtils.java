/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.function.scenarios.curvedata;

import java.util.concurrent.FutureTask;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.opengamma.sesame.cache.CacheProvider;
import com.opengamma.sesame.cache.MethodInvocationKey;
import com.opengamma.sesame.engine.MutableCacheProvider;

/**
 * Helper methods for tests in sesame-function
 */
public class FunctionTestUtils {

  private static final long MAX_CACHE_ENTRIES = 100_000;

  private FunctionTestUtils() {
  }

  /**
   * @return a cache provider configured for use with the engine
   */
  public static CacheProvider createCacheProvider() {
    int concurrencyLevel = Runtime.getRuntime().availableProcessors() + 2;
    Cache<MethodInvocationKey, FutureTask<Object>> cache =
        CacheBuilder.newBuilder()
                    .maximumSize(MAX_CACHE_ENTRIES)
                    .concurrencyLevel(concurrencyLevel)
                    .build();
    return new MutableCacheProvider(cache);
  }

  public static CacheBuilder<Object, Object> createCacheBuilder() {
    int concurrencyLevel = Runtime.getRuntime().availableProcessors() + 2;
    return CacheBuilder.newBuilder().maximumSize(MAX_CACHE_ENTRIES).concurrencyLevel(concurrencyLevel);
  }
}
