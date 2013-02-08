/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.lambdava.streams;

import java.util.Iterator;

class EmptyStream<T> extends AbstractStream<T> {
  protected EmptyStream() {
  }
  
  private static EmptyStream<?> EMPTY = new EmptyStream<Object>(); 

  public static <T> EmptyStream<T> empty() {
    return (EmptyStream<T>) EMPTY;
  }

  @Override
  public T head() {
    return null;
  }

  @Override
  public StreamI<T> rest() {
    return empty();
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public int count() {
    return 0;
  }

  @Override
  public String toString() {
    return "[]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return true;
    if (!(o instanceof Iterable)) return false;
    Iterable that = (Iterable) o;
    Iterator it = that.iterator();

    return !it.hasNext();
  }


  @Override
  public int hashCode() {    
    return 31;
  }
}

