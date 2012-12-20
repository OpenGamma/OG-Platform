/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.debug;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;

/**
 * Profiler tool for timing calls to a given code fragment.
 */
public final class Profiler {

  // TODO: This is in the wrong place; should it be in Util?

  private static final Logger s_logger = LoggerFactory.getLogger(Profiler.class);
  private static final Collection<Profiler> s_profilers = Sets.newSetFromMap(new MapMaker().weakKeys().<Profiler, Boolean>makeMap());
  private static volatile boolean s_enabled;

  private final String _name;
  private final AtomicInteger _lock = new AtomicInteger();
  private final AtomicLong _time = new AtomicLong();
  private final AtomicInteger _operations = new AtomicInteger();
  private volatile boolean _snapshotPending;
  private double _snapshotTime;
  private int _snapshotOperations;

  private Profiler(final String name) {
    _name = name;
  }

  public void begin() {
    if (!isEnabled()) {
      return;
    }
    do {
      int lock = _lock.get();
      if (lock == 0) {
        if (_lock.compareAndSet(0, 1)) {
          break;
        }
      } else if (lock > 0) {
        if (_lock.compareAndSet(lock, lock + 1)) {
          break;
        }
      }
    } while (true);
    _time.addAndGet(-System.nanoTime());
  }

  public void end() {
    if (!isEnabled()) {
      return;
    }
    _time.addAndGet(System.nanoTime());
    _operations.incrementAndGet();
    final int lock = _lock.decrementAndGet();
    if (lock == 0) {
      if (_snapshotPending) {
        snapshot();
      }
    } else if (lock < 0) {
      // This happens if the profiling was enabled between the calls to begin and end. Other threads see it as if
      // the object is now locked for a snapshot so clear the values.
      _time.set(0);
      _operations.set(0);
    }
  }

  private synchronized void snapshot() {
    if (!_lock.compareAndSet(0, -1)) {
      // Can't snapshot; currently locked
      _snapshotPending = true;
      return;
    }
    _snapshotPending = false;
    _snapshotTime += (double) _time.getAndSet(0) / 1e6;
    _snapshotOperations += _operations.getAndSet(0);
    assert _lock.get() == -1;
    _lock.set(0);
  }

  public static synchronized Profiler create(final String name) {
    final Profiler profiler = new Profiler(name);
    s_profilers.add(profiler);
    return profiler;
  }

  public static synchronized Profiler create(final Class<?> clazz) {
    return create(clazz.getName());
  }

  public static synchronized Profiler create(final Class<?> clazz, final String name) {
    return create(clazz.getName() + '%' + name);
  }

  private static void insertNoClash(final Map<String, Object[]> report, final Object[] arg, int dot) {
    final String key = (String) arg[0];
    while (dot >= 0) {
      final String substring = key.substring(dot + 1);
      if (report.containsKey(substring)) {
        final Object[] clash = report.remove(substring);
        final int xdot = key.length() - substring.length() - 2;
        insertNoClash(report, clash, key.lastIndexOf('.', xdot));
        dot = key.lastIndexOf('.', dot - 1);
      } else {
        report.put(substring, arg);
        return;
      }
    }
    report.put(key, arg);
  }

  private static synchronized void printProfilers() {
    final Map<String, Object[]> report = new HashMap<String, Object[]>();
    for (Profiler profiler : s_profilers) {
      profiler.snapshot();
      final Object[] arg = new Object[] {profiler._name, profiler._snapshotOperations, profiler._snapshotTime };
      insertNoClash(report, arg, profiler._name.lastIndexOf('.'));
    }
    for (Map.Entry<String, Object[]> entry : report.entrySet()) {
      final Object[] values = entry.getValue();
      values[0] = entry.getKey();
      s_logger.info("{} - {} in {}ms", values);
    }
    s_logger.debug("{} active profiler instances", s_profilers.size());
  }

  public static synchronized void enable(final long period) {
    if (isEnabled()) {
      s_logger.warn("Already enabled");
      return;
    }
    s_enabled = true;
    final Thread t = new Thread() {
      @Override
      public void run() {
        do {
          try {
            Thread.sleep(period);
          } catch (InterruptedException e) {
            return;
          }
          printProfilers();
        } while (true);
      }
    };
    t.setName(Profiler.class.getSimpleName());
    t.setDaemon(false);
    t.start();
  }

  public static boolean isEnabled() {
    return s_enabled;
  }

}
