/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.lambdava.streams;

class FilledStream<T> extends AbstractStream<T> {

  private T _head;
  private StreamI<T> _rest;

  protected FilledStream(T head, StreamI<T> rest) {
    _head = head;
    _rest = rest;
  }

  @Override
  public T head() {
    return _head;
  }

  @Override
  public StreamI<T> rest() {
    return _rest;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public int count() {
    return 1 + rest().count();
  }
}
