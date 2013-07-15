/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;

/**
 * Utility class for working with {@link VersionCorrection} instances.
 */
public final class VersionCorrectionUtils {

  private static final Logger s_logger = LoggerFactory.getLogger(VersionCorrectionUtils.class);

  /**
   * Listener for locking events.
   */
  public interface VersionCorrectionLockListener {

    /**
     * Called when the last lock on a version/correction pair is released.
     * 
     * @param unlocked the version/correction pair unlocked
     * @param locked the version/correction pairs still locked
     */
    void versionCorrectionUnlocked(VersionCorrection unlocked, Collection<VersionCorrection> locked);

  }

  private static final Map<VersionCorrection, AtomicInteger> s_locks = new HashMap<VersionCorrection, AtomicInteger>();

  private static final Set<VersionCorrectionLockListener> s_listeners = Sets.newSetFromMap(new MapMaker().weakKeys().<VersionCorrectionLockListener, Boolean>makeMap());

  private static final Map<Reference<Object>, VersionCorrection> s_autoLocks = new ConcurrentHashMap<Reference<Object>, VersionCorrection>();

  private static final ReferenceQueue<Object> s_autoUnlocks = new ReferenceQueue<Object>();

  /**
   * Prevents instantiation.
   */
  private VersionCorrectionUtils() {
  }

  /**
   * Acquires a lock on a version/correction pair. It is possible for other threads to determine whether there are any outstanding locks, or to execute actions when the last lock is released. Must be
   * paired with a call to {@link #unlock}.
   * 
   * @param versionCorrection the version/correction pair to lock, not null
   */
  public static void lock(final VersionCorrection versionCorrection) {
    synchronized (s_locks) {
      s_logger.info("Acquiring lock on {}", versionCorrection);
      AtomicInteger locked = s_locks.get(versionCorrection);
      if (locked == null) {
        locked = new AtomicInteger(1);
        s_locks.put(versionCorrection, locked);
        s_logger.debug("First lock acquired on {}", versionCorrection);
      } else {
        final int count = locked.incrementAndGet();
        s_logger.debug("Lock {} acquired on {}", count);
      }
    }
  }

  /**
   * Acquires a lock on a version/correction pair for the lifetime of the monitor object. It is possible for other threads to determine whether there are any outstanding locks, or to execute actions
   * when the last lock is released. Must be paired with a call to {@link #unlock}.
   * 
   * @param versionCorrection the version/correction pair to lock, not null
   * @param monitor the monitor object - the lock will be released when this falls out of scope, not null
   */
  public static void lockForLifetime(VersionCorrection versionCorrection, final Object monitor) {
    lock(versionCorrection);
    s_autoLocks.put(new PhantomReference<Object>(monitor, s_autoUnlocks), versionCorrection);
    Reference<? extends Object> ref = s_autoUnlocks.poll();
    while (ref != null) {
      versionCorrection = s_autoLocks.remove(ref);
      if (versionCorrection != null) {
        unlock(versionCorrection);
      }
      ref = s_autoUnlocks.poll();
    }
  }

  /**
   * Releases a lock on a version/correction pair. It is possible for other threads to determine whether there are any outstanding locks, or to execute actions when the last lock is released. Must be
   * paired with a call to {@link #unlock}.
   * 
   * @param versionCorrection the version/correction pair to lock, not null
   */
  public static void unlock(final VersionCorrection versionCorrection) {
    final Set<VersionCorrection> remaining;
    synchronized (s_locks) {
      s_logger.info("Releasing lock on {}", versionCorrection);
      AtomicInteger locked = s_locks.get(versionCorrection);
      if (locked == null) {
        s_logger.warn("{} not locked", versionCorrection);
        throw new IllegalStateException();
      }
      final int count = locked.decrementAndGet();
      if (count > 0) {
        s_logger.debug("Released lock on {}, {} remaining", versionCorrection, count);
        return;
      }
      assert count == 0;
      s_logger.debug("Last lock on {} released", versionCorrection);
      s_locks.remove(versionCorrection);
      remaining = new HashSet<VersionCorrection>(s_locks.keySet());
    }
    for (VersionCorrectionLockListener listener : s_listeners) {
      listener.versionCorrectionUnlocked(versionCorrection, remaining);
    }
  }

  public static void addVersionCorrectionLockListener(final VersionCorrectionLockListener listener) {
    s_listeners.add(listener);
  }

  public static void removeVersionCorrectionLockListener(final VersionCorrectionLockListener listener) {
    s_listeners.remove(listener);
  }

}
