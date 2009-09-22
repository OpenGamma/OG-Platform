/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import com.opengamma.math.MathException;

/**
 * Contains the results of an interpolation - the interpolated value, and the
 * estimated error of that value (if applicable).
 * 
 * @author emcleod
 * 
 * @param <T>
 *          The type of the interpolated value and the error.
 */

public class InterpolationResult<T> {
  private final T _result;
  private T _error;

  public InterpolationResult(T result) {
    _result = result;
  }

  public InterpolationResult(T result, T error) {
    _result = result;
    _error = error;
  }

  public T getResult() {
    return _result;
  }

  public T getErrorEstimate() {
    if (_error == null) {
      throw new MathException("Error was not calculated for this interpolation result");
    }
    return _error;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_error == null) ? 0 : _error.hashCode());
    result = prime * result + ((_result == null) ? 0 : _result.hashCode());
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    InterpolationResult other = (InterpolationResult) obj;
    if (_error == null) {
      if (other._error != null)
        return false;
    } else if (!_error.equals(other._error))
      return false;
    if (_result == null) {
      if (other._result != null)
        return false;
    } else if (!_result.equals(other._result))
      return false;
    return true;
  }
}
