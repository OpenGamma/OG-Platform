/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import org.apache.http.util.LangUtils;

/**
 * An immutable pair consisting of an {@code int} and {@code double}.
 * <p>
 * The class provides direct access to the primitive types and implements
 * the relevant fastutil interface.
 *
 * @author jim
 */
public class IntObjectPair<T> extends Pair<Integer, T> implements Int2ObjectMap.Entry<T> {

  /** The first element. */
  private final int _first;
  /** The second element. */
  private final T _second;

  /**
   * Constructor.
   * @param first  the first element
   * @param second  the second element
   */
  public IntObjectPair(final int first, final T second) {
    _first = first;
    _second = second;
  }

  //-------------------------------------------------------------------------
  @Override
  public Integer getFirst() {
    return _first;
  }

  @Override
  public T getSecond() {
    return _second;
  }

  public int getFirstInt() {
    return _first;
  }

  public T getSecondObject() {
    return _second;
  }

  //-------------------------------------------------------------------------
  @Override
  public int getIntKey() {
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
    if (obj instanceof IntObjectPair) {
      final IntObjectPair<T> other = (IntObjectPair<T>) obj;
      return this.getFirstInt() == other.getFirstInt() && LangUtils.equals(this.getSecond(), other.getSecond());
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    // see Map.Entry API specification
    return getFirstInt() ^ _second.hashCode();
  }

}
