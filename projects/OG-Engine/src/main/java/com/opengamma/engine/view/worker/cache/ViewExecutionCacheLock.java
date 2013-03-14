/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.cache;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.MapMaker;
import com.opengamma.util.WeakInstanceCache;

/**
 * Provides locks that correspond to the view execution cache keys.
 * <p>
 * Once a key has returned a lock, all the while that lock is in scope any keys that are equal will return the same lock instance.
 */
public final class ViewExecutionCacheLock {

  // No harm in sharing the canonicalized forms with other instances.
  private static final WeakInstanceCache<ViewExecutionCacheKey> s_keys = new WeakInstanceCache<ViewExecutionCacheKey>();

  // The actual lock objects are per instance
  private final ConcurrentMap<ViewExecutionCacheKey, Lock> _locks = new MapMaker().weakKeys().makeMap();

  public Lock get(final ViewExecutionCacheKey cacheKey) {
    final ViewExecutionCacheKey normalized = s_keys.get(cacheKey);
    Lock lock = _locks.get(normalized);
    if (lock == null) {
      lock = new ReentrantLock();
      Lock previous = _locks.putIfAbsent(normalized, lock);
      if (previous != null) {
        lock = previous;
      }
    }
    return lock;
  }

}
