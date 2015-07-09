/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.cache;

import java.util.concurrent.Callable;

/**
 * A simple cache which allows functions to explicitly cache values they calculate.
 * <p>
 * If a function needs to cache a value it should declare a {@code FunctionCache} field and constructor
 * parameter and the engine will inject one. The cache is automatically created and supplied by
 * the engine, no configuration is required to make a cache available.
 */
public interface FunctionCache {

  /**
   * Returns a value from the cache, calculating it by invoking the supplier if no value is available.
   * <p>
   * If the cache contains a value for the key it is returned immediately. If there is no value, the
   * cache invokes {@code valueSupplier} to calculate one. The calculated value is put into the cache
   * and returned. If a value is already being calculated for the key on a different thread, this method
   * blocks until it is available and returns it. This ensures each value is only calculated once.
   * <p>
   * Example usage:
   * <p>
   * Java 7
   * <pre>
   *   CacheKey key = ...;
   *
   *   Foo foo = cache.get(key, new Callable&lt;Foo&gt;() {
   *     {@literal @}Override
   *     public String call() {
   *       return fooFunction.createFoo();
   *     }
   *   });
   * </pre>
   * Java 8
   * <pre>
   *   CacheKey key = ...;
   *
   *   Foo foo = cache.get(key, () -> fooFunction.createFoo());
   * </pre>
   * If there is a value in the cache for the key and its type is not compatible with the return type of
   * {@code valueSupplier} a {@code ClassCastException} will be thrown.
   *
   * @param key the key that uniquely identifies the value in the cache
   * @param valueSupplier calculates and returns a value if there isn't one available in the cache
   * @param <T> type of the cached value
   * @return a value from the cache for the key, created by calling the supplier if it isn't available
   * @throws ClassCastException if there is a value in the cache whose type is not compatible with the
   *   return type of {@code valueSupplier}
   */
  <T> T get(CacheKey key, Callable<T> valueSupplier);
}
