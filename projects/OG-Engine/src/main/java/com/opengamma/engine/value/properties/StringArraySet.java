/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value.properties;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * An immutable set of strings, backed by an array.
 */
/* package */final class StringArraySet implements Set<String> {

  private final String[] _values;

  /**
   * Creates a new instance.
   * 
   * @param values the values backing the set. This class will use this object but not modify it.
   */
  public StringArraySet(final String[] values) {
    _values = values;
  }

  // Set

  @Override
  public int size() {
    return _values.length;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean contains(final Object o) {
    if (o == null) {
      return false;
    }
    for (String value : _values) {
      if (o.equals(value)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Iterator<String> iterator() {
    return new Iterator<String>() {

      private int _index;

      @Override
      public boolean hasNext() {
        return _index < _values.length;
      }

      @Override
      public String next() {
        return _values[_index++];
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

  @Override
  public Object[] toArray() {
    final Object[] result = new Object[_values.length];
    System.arraycopy(_values, 0, result, 0, _values.length);
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T[] toArray(T[] a) {
    if (a.length < _values.length) {
      a = (T[]) Array.newInstance(a.getClass().getComponentType(), _values.length);
    }
    System.arraycopy(_values, 0, a, 0, _values.length);
    return a;
  }

  @Override
  public boolean add(final String e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(final Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    for (Object o : c) {
      if (!contains(o)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean addAll(final Collection<? extends String> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  // Object

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append('{');
    for (int i = 0; i < _values.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(_values[i]);
    }
    sb.append('}');
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int hc = 0;
    for (String value : _values) {
      hc += value.hashCode();
    }
    return hc;
  }

  @SuppressWarnings({"rawtypes", "unchecked" })
  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Set)) {
      return false;
    }
    return containsAll((Set) o) && ((Set) o).containsAll(this);
  }

}
