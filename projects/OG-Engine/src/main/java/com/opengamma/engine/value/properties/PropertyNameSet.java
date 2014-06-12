/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value.properties;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An immutable set of property name strings, backed by the property hash array.
 */
public final class PropertyNameSet implements Set<String> {

  private final AbstractValueProperty[] _properties;

  private volatile int _size = -1;

  /**
   * Creates a new instance.
   * 
   * @param properties the values backing the set. This class will use this object but not modify it.
   */
  public PropertyNameSet(final AbstractValueProperty[] properties) {
    _properties = properties;
  }

  @Override
  public int size() {
    if (_size < 0) {
      int size = 0;
      for (AbstractValueProperty property : _properties) {
        for (; property != null; property = property.getNext()) {
          size++;
        }
      }
      _size = size;
    }
    return _size;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean contains(final Object o) {
    if ((o == null) || !(o instanceof String)) {
      return false;
    }
    final int hc = o.hashCode() & 0x7FFFFFFF;
    AbstractValueProperty property = _properties[hc % _properties.length];
    while (property != null) {
      if (property.getKey().equals(o)) {
        return true;
      }
      property = property.getNext();
    }
    return false;
  }

  @Override
  public Iterator<String> iterator() {
    return new Iterator<String>() {

      private int _index;
      private AbstractValueProperty _property;

      @Override
      public boolean hasNext() {
        while (_property == null) {
          if (_index < _properties.length) {
            _property = _properties[_index++];
          } else {
            return false;
          }
        }
        return true;
      }

      @Override
      public String next() {
        AbstractValueProperty property = _property;
        if (property == null) {
          while (property == null) {
            if (_index < _properties.length) {
              property = _properties[_index++];
            } else {
              throw new NoSuchElementException();
            }
          }
        }
        _property = property.getNext();
        return property.getKey();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

  @Override
  public Object[] toArray() {
    final int size = size();
    final Object[] result = new Object[size];
    int i = 0;
    for (AbstractValueProperty property : _properties) {
      for (; property != null; property = property.getNext()) {
        result[i++] = property.getKey();
      }
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T[] toArray(T[] a) {
    final int size = size();
    if (a.length < size) {
      a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
    }
    int i = 0;
    for (AbstractValueProperty property : _properties) {
      for (; property != null; property = property.getNext()) {
        a[i++] = (T) property.getKey();
      }
    }
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

}
