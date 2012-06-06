/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Reduction of common object values to single instances.
 * 
 * @param <T> object type to reduce
 */
public final class WeakInstanceCache<T> {

  private final WeakHashMap<T, WeakReference<T>> _data = new WeakHashMap<T, WeakReference<T>>();

  public synchronized T get(final T value) {
    final WeakReference<T> canonRef = _data.get(value);
    if (canonRef != null) {
      final T canonValue = canonRef.get();
      if (canonValue != null) {
        return canonValue;
      }
    }
    _data.put(value, new WeakReference<T>(value));
    return value;
  }

}
