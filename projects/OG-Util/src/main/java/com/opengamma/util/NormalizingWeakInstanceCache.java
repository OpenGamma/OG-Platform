/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Reduction of common object values to single instances via a normalization callback.
 * 
 * @param <T> object type to reduce
 */
public abstract class NormalizingWeakInstanceCache<T> extends WeakInstanceCache<T> {

  protected T getImpl(final WeakHashMap<T, WeakReference<T>> data, final T value) {
    synchronized (data) {
      final WeakReference<T> canonRef = data.get(value);
      if (canonRef != null) {
        final T canonValue = canonRef.get();
        if (canonValue != null) {
          return canonValue;
        }
      }
    }
    return super.getImpl(data, normalize(value));
  }

  protected abstract T normalize(final T value);

}
