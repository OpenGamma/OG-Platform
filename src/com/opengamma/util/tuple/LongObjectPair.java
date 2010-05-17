/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import org.apache.http.util.LangUtils;

/**
 * An immutable pair consisting of an {@code long} and {@code double}.
 * <p>
 * The class provides direct access to the primitive types and implements
 * the relevant fastutil longerface.
 *
 * @author jim
 */
public class LongObjectPair<T> extends Pair<Long, T> implements Long2ObjectMap.Entry<T> {

  /** The first element. */
  private final long _first;
  /** The second element. */
  private final T _second;

  /**
   * Constructor.
   * @param first  the first element
   * @param second  the second element
   */
  public LongObjectPair(final long first, final T second) {
    _first = first;
    _second = second;
  }

  //-------------------------------------------------------------------------
  @Override
  public Long getFirst() {
    return _first;
  }

  @Override
  public T getSecond() {
    return _second;
  }

  public long getFirstLong() {
    return _first;
  }

  public T getSecondObject() {
    return _second;
  }

  //-------------------------------------------------------------------------
  @Override
  public long getLongKey() {
    return _first;
  }

  @Override
  public T setValue(final T value) {
    throw new UnsupportedOperationException("Immutable");
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof LongObjectPair) {
      final LongObjectPair<T> other = (LongObjectPair<T>) obj;
      return this.getFirstLong() == other.getFirstLong() && LangUtils.equals(this.getSecond(), other.getSecond());
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    // see Map.Entry API specification
    return (int) (getFirstLong() ^ _second.hashCode());
  }

}
