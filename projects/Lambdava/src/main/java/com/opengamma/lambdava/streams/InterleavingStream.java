/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.lambdava.streams;

/**
 * Stream interleaving two underlying streams
 * @param <T>
 */
class InterleavingStream<T> extends AbstractStream<T> {

  private StreamI<T> _left;
  private StreamI<T> _right;

  protected InterleavingStream(StreamI<T> left, StreamI<T> right) {
    _left = left;
    _right = right;
  }

  @Override
  public T head() {
    if (_left.isEmpty())
      return _right.head();
    else
      return _left.head();
  }

  @Override
  public StreamI<T> rest() {
    if (_left.isEmpty()) {
      return _right.rest();
    } else {            
      return new AppendingStream<T>(_right, _left.rest());      
    }
  }

  @Override
  public boolean isEmpty() {
    return _left.isEmpty() && _right.isEmpty();
  }

  @Override
  public int count() {
    return _left.count() + _right.count();
  }
}
