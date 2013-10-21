/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.cache;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.threeten.bp.Instant;

import com.google.common.collect.MapMaker;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.WeakInstanceCache;
import com.opengamma.util.map.HashMap2;
import com.opengamma.util.map.Map2;
import com.opengamma.util.map.WeakValueHashMap2;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Provides locks that correspond to the view execution cache keys.
 * <p>
 * Once a key has returned a lock, all the while that lock is in scope any keys that are equal will return the same lock instance.
 */
public final class ViewExecutionCacheLock {

  // No harm in sharing the canonicalized forms with other instances.
  private static final WeakInstanceCache<ViewExecutionCacheKey> s_keys = new WeakInstanceCache<ViewExecutionCacheKey>();

  private static final class Locks {

    private final Lock _broad = new ReentrantLock();
    private final Map2<VersionCorrection, Instant, Lock> _fine = new WeakValueHashMap2<VersionCorrection, Instant, Lock>(HashMap2.STRONG_KEYS);

  }

  // The actual lock objects are per instance
  private final ConcurrentMap<ViewExecutionCacheKey, Locks> _locks = new MapMaker().weakKeys().makeMap();

  private Locks getOrCreateLocks(final ViewExecutionCacheKey cacheKey) {
    final ViewExecutionCacheKey normalized = s_keys.get(cacheKey);
    Locks locks = _locks.get(normalized);
    if (locks == null) {
      locks = new Locks();
      final Locks previous = _locks.putIfAbsent(normalized, locks);
      if (previous != null) {
        locks = previous;
      }
    }
    return locks;
  }

  /**
   * Acquires the broad lock for the view compilation.
   * 
   * @param cacheKey the broad key that summarizes the view definition and market data providers, not null
   * @return the lock instance, not null
   */
  public Lock get(final ViewExecutionCacheKey cacheKey) {
    return getOrCreateLocks(cacheKey)._broad;
  }

  /**
   * Acquires the broad and finer grained locks for a specific compilation of a view
   * 
   * @param cacheKey the broad key that summarizes the view definition and market data providers, not null
   * @param valuationTime an indicative valuation time for any compilation, not null
   * @param resolverVersionCorrection the target resolver version/correction timestamp, not null
   * @return the lock instances, not null. The first element in the pair is the broad lock, the second is the finer lock
   */
  public Pair<Lock, Lock> get(final ViewExecutionCacheKey cacheKey, final Instant valuationTime, final VersionCorrection resolverVersionCorrection) {
    final Locks locks = getOrCreateLocks(cacheKey);
    Lock lock = locks._fine.get(resolverVersionCorrection, valuationTime);
    if (lock == null) {
      lock = new ReentrantLock();
      final Lock previous = locks._fine.putIfAbsent(resolverVersionCorrection, valuationTime, lock);
      if (previous != null) {
        lock = previous;
      }
    }
    return Pairs.of(locks._broad, lock);
  }

}
