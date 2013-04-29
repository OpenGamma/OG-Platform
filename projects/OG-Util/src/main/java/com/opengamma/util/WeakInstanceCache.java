/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import com.opengamma.util.async.AbstractHousekeeper;

/**
 * Reduction of common object values to single instances.
 * 
 * @param <T> object type to reduce
 */
public class WeakInstanceCache<T> {

  private static final class Housekeeper extends AbstractHousekeeper<WeakInstanceCache<?>> {

    protected Housekeeper(final WeakInstanceCache<?> target) {
      super(target);
    }

    @Override
    protected boolean housekeep(final WeakInstanceCache<?> target) {
      target.gc();
      return true;
    }

  }

  /**
   * Use a pool of buckets in the way that the concurrent hash works to try and reduce collisions on the monitor.
   */
  private static final int BUCKETS = 1024;

  @SuppressWarnings("unchecked")
  private final WeakHashMap<T, WeakReference<T>>[] _data = new WeakHashMap[BUCKETS];

  public WeakInstanceCache() {
    for (int i = 0; i < BUCKETS; i++) {
      _data[i] = new WeakHashMap<T, WeakReference<T>>();
    }
    (new Housekeeper(this)).start();
  }

  protected T getImpl(final WeakHashMap<T, WeakReference<T>> data, final T value) {
    synchronized (data) {
      final WeakReference<T> canonRef = data.get(value);
      if (canonRef != null) {
        final T canonValue = canonRef.get();
        if (canonValue != null) {
          return canonValue;
        }
      }
      data.put(value, new WeakReference<T>(value));
      return value;
    }
  }

  public T get(final T value) {
    final WeakHashMap<T, WeakReference<T>> data = _data[value.hashCode() & (BUCKETS - 1)];
    return getImpl(data, value);
  }

  protected void gc() {
    for (int i = 0; i < BUCKETS; i++) {
      _data[i].size();
    }
  }

}
