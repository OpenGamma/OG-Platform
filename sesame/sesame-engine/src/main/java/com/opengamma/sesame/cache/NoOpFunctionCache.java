/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.cache;

import java.util.concurrent.Callable;

import com.opengamma.OpenGammaRuntimeException;

/**
 * {@code FunctionCache} implementation that doesn't do any caching and always invokes the value supplier to
 * get a value.
 * <p>
 * This is primarily for running functions in isolation and for testing.
 */
public class NoOpFunctionCache implements FunctionCache {

  /**
   * Always invokes {@code valueSupplier} to create a value. Performs no caching.
   *
   * @param key not used
   * @param valueSupplier calculates and returns a value
   * @param <T> type of the value
   * @return a value, created by invoking {@code valueSupplier}
   */
  @Override
  public <T> T get(CacheKey key, Callable<T> valueSupplier) {
    try {
      return valueSupplier.call();
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Failed to create value for cache", e);
    }
  }
}
