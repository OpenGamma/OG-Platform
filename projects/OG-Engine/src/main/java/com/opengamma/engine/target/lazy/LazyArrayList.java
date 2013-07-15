/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

/**
 * Implements a lazily constructed list of objects backed by an array of source objects.
 */
/* package */abstract class LazyArrayList<T> extends AbstractList<T> {

  private final List<T> _raw;
  private final Object[] _resolved;

  public LazyArrayList(final List<T> raw) {
    _raw = raw;
    _resolved = new Object[raw.size()];
  }

  @Override
  public int size() {
    return _resolved.length;
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {

      private int _index;

      @Override
      public boolean hasNext() {
        return _index < _resolved.length;
      }

      @Override
      public T next() {
        return get(_index++);
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

  protected abstract T resolve(T object);

  @SuppressWarnings("unchecked")
  @Override
  public synchronized T get(final int index) {
    if (_resolved[index] == null) {
      _resolved[index] = resolve(_raw.get(index));
    }
    return (T) _resolved[index];
  }

}
