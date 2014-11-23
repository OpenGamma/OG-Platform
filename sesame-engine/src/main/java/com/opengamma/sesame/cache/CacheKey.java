/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.cache;

import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.opengamma.util.ArgumentChecker;

/**
 * Key for identifying values cached in a {@link FunctionCache}.
 * <p>
 * A key should uniquely identify an object, taking into account the fact that many objects can change
 * over time or if they are constructed using different data. For example, if a curve were cached the
 * valuation time should be included in the cache key, as the same curve built for a different valuation
 * time would not be the same object.
 * <p>
 * It is important to remember that the cache is shared across multiple views and multiple calculation
 * cycles. Therefore the objects in a cache key must create a key that is unique enough to identify
 * the exact version of an object that might appear in different views or other cycles.
 * <p>
 * Every key includes the type of the caller that put the value into the cache. This is to provide a kind of
 * namespace. It is quite possible for two classes to choose the same values for a cache keys by coincidence, not
 * necessarily for the same object. This would cause unintended results. Including the type of the object
 * that inserted the value greatly reduces the chances of this happening. It is assumed the function writer
 * will be aware of other uses of the cache in the same function, or can easily check.
 */
public final class CacheKey {

  /** The type of the object that placed the value in the cache. Provides a type of namespace for cache keys. */
  private final Class<?> _functionType;

  /** The objects that make up the identity of the object stored with the cache key. */
  private final ImmutableList<Object> _keys;

  private CacheKey(Class<?> functionType, ImmutableList<Object> keys) {
    _functionType = functionType;
    _keys = keys;
  }

  /**
   * Creates a key for a specified caller type and identifying keys.
   *
   * @param callerType the type of the object that inserted the value into the cache
   * @param key the first of the objects that make up the unique identifier of the cached value
   * @param keys any other objects that make up the unique identifier of the cached value
   * @return a cache key whose identity and hash code are derived from the key arguments
   */
  public static CacheKey of(Class<?> callerType, Object key, Object... keys) {
    ArgumentChecker.notNull(callerType, "callerType");
    ArgumentChecker.notNull(key, "key");

    ImmutableList.Builder<Object> builder = ImmutableList.builder();
    ImmutableList<Object> keyList = builder.add(key).add(keys).build();
    return new CacheKey(callerType, keyList);
  }

  /**
   * Creates a key for a specified caller and identifying keys.
   *
   * @param caller the object that inserted the value into the cache
   * @param key the first of the objects that make up the unique identifier of the cached value
   * @param keys any other objects that make up the unique identifier of the cached value
   * @return a cache key whose identity and hash code are derived from the key arguments
   */
  public static CacheKey of(Object caller, Object key, Object... keys) {
    ArgumentChecker.notNull(caller, "caller");
    ArgumentChecker.notNull(key, "key");

    ImmutableList.Builder<Object> builder = ImmutableList.builder();
    ImmutableList<Object> keyList = builder.add(key).add(keys).build();
    return new CacheKey(caller.getClass(), keyList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_functionType, _keys);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    CacheKey other = (CacheKey) obj;
    return Objects.equals(this._functionType, other._functionType) && Objects.deepEquals(this._keys, other._keys);
  }
}
