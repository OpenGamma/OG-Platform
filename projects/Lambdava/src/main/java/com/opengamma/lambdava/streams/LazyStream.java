/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.lambdava.streams;

import static java.lang.System.identityHashCode;

import com.opengamma.lambdava.tuple.Pair;

class LazyStream<T> extends AbstractStream<T> {

  private boolean _realized;
  private StreamI<T> _materialized;
  private Thunk<T> _thunk;


  public LazyStream(Thunk<T> thunk) {
    _thunk = thunk;
  }

  @Override
  public T head() {
    if (!_realized) {
      _realized = true;
      _materialized = _thunk.execute();
    }
    if (_materialized != null)
      return _materialized.head();
    else
      return null;
  }

  @Override
  public StreamI<T> rest() {
    if (!_realized) {
      _realized = true;
      _materialized = _thunk.execute();
    }
    if (_materialized != null)
      return _materialized.rest();
    else
      return Stream.empty();
  }

  @Override
  public boolean isEmpty() {
    if (!_realized) {
      _realized = true;
      _materialized = _thunk.execute();
    }
    if (_materialized != null)
      return _materialized.isEmpty();
    else
      return true;
  }

  @Override
  public int count() {
    if (!_realized) {
      _realized = true;
      _materialized = _thunk.execute();
    }
    if (_materialized != null)
      return _materialized.count();
    else
      return 0;
  }

  @Override
  public String toString() {
    if (!_realized) {
      return "LazyStream@" + identityHashCode(this) + " (not realized yet)";
    } else {
      if(_materialized == null)
        return "LazyStream@[]";
      else
        return "LazyStream@" + _materialized.toString();
    }
  }
}
