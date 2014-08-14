/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.function.scenarios.curvedata;

import com.google.common.cache.CacheBuilder;
import com.opengamma.sesame.cache.CacheProvider;
import com.opengamma.sesame.cache.MethodInvocationKey;
import com.opengamma.sesame.engine.DefaultCacheProvider;

/**
 * Helper methods for tests in sesame-function
 */
public class FunctionTestUtils {

  private static final long MAX_CACHE_ENTRIES = 10_000;

  private FunctionTestUtils() {
  }

  /**
   * @return a cache provider configured for use with the engine
   */
  public static CacheProvider createCacheProvider() {
    return new DefaultCacheProvider(createCacheBuilder().<MethodInvocationKey, Object>build());
  }

  public static CacheBuilder<Object, Object> createCacheBuilder() {
    int nProcessors = Runtime.getRuntime().availableProcessors();
    int concurrencyLevel = nProcessors * 8;
    return CacheBuilder.newBuilder().maximumSize(MAX_CACHE_ENTRIES).softValues().concurrencyLevel(concurrencyLevel);
  }
}
