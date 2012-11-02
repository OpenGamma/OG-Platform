/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import java.util.AbstractList;
import java.util.Iterator;

import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;

/**
 * Implements a lazily constructed list of objects held by the computation target specifications.
 */
/* package */abstract class TargetResolverList<T> extends AbstractList<T> {

  private final ComputationTargetResolver.AtVersionCorrection _resolver;
  private final ComputationTargetSpecification[] _specifications;
  private final Object[] _resolved;

  public TargetResolverList(final ComputationTargetResolver.AtVersionCorrection resolver, final ComputationTargetSpecification[] specifications) {
    _resolver = resolver;
    _specifications = specifications;
    _resolved = new Object[specifications.length];
  }

  protected ComputationTargetResolver.AtVersionCorrection getTargetResolver() {
    return _resolver;
  }

  protected abstract T createObject(ComputationTargetSpecification specification);

  @Override
  public int size() {
    return _specifications.length;
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {

      private int _index;

      @Override
      public boolean hasNext() {
        return _index < _specifications.length;
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

  @SuppressWarnings("unchecked")
  @Override
  public synchronized T get(final int index) {
    if (_resolved[index] == null) {
      _resolved[index] = createObject(_specifications[index]);
    }
    return (T) _resolved[index];
  }

}
