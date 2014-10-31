/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.graph;

import java.util.Objects;

/**
 * ID for a function instance in the engine.
 * <p>
 * Function IDs are used in the engine cache to create the cache key used for storing calculated values. If
 * two functions are identical it is safe to assume they will return the same value when invoked
 * with the same set of arguments. Therefore identical functions are allocated the same function ID so
 * they can use each other's values in the cache.
 */
public final class FunctionId {

  private final int _id;

  private FunctionId(int id) {
    _id = id;
  }

  /**
   * Creates a new ID with the given underlying integer ID.
   *
   * @param id the underlying ID
   * @return a {@code FunctionId} wrapping the specified integer ID
   */
  public static FunctionId of(int id) {
    return new FunctionId(id);
  }

  @Override
  public int hashCode() {
    return _id;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final FunctionId other = (FunctionId) obj;
    return Objects.equals(this._id, other._id);
  }

  @Override
  public String toString() {
    return "FunctionId [_id=" + _id + "]";
  }
}
