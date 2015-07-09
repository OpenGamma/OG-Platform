/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableMap;
import com.opengamma.OpenGammaRuntimeException;

/**
 * Cache implementation that does no caching and always invokes the value loader to generate a result.
 * <p>
 * The only supported method is {@link #get}. All other methods throw {@code UnsupportedOperationException}.
 */
class NoOpCache implements Cache<Object, Object> {

  @Nullable
  @Override
  public Object getIfPresent(Object key) {
    throw new UnsupportedOperationException("getIfPresent not supported");
  }

  @Override
  public Object get(Object key, Callable<?> valueLoader) throws ExecutionException {
    try {
      return valueLoader.call();
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Failed to create value for cache", e);
    }
  }

  @Override
  public ImmutableMap<Object, Object> getAllPresent(Iterable<?> keys) {
    throw new UnsupportedOperationException("getAllPresent not supported");
  }

  @Override
  public void put(Object key, Object value) {
    throw new UnsupportedOperationException("put not supported");
  }

  @Override
  public void putAll(Map<?, ?> m) {
    throw new UnsupportedOperationException("putAll not supported");
  }

  @Override
  public void invalidate(Object key) {
    throw new UnsupportedOperationException("invalidate not supported");
  }

  @Override
  public void invalidateAll(Iterable<?> keys) {
    throw new UnsupportedOperationException("invalidateAll not supported");
  }

  @Override
  public void invalidateAll() {
    throw new UnsupportedOperationException("invalidateAll not supported");
  }

  @Override
  public long size() {
    throw new UnsupportedOperationException("size not supported");
  }

  @Override
  public CacheStats stats() {
    throw new UnsupportedOperationException("stats not supported");
  }

  @Override
  public ConcurrentMap<Object, Object> asMap() {
    throw new UnsupportedOperationException("asMap not supported");
  }

  @Override
  public void cleanUp() {
    throw new UnsupportedOperationException("cleanUp not supported");
  }
}
