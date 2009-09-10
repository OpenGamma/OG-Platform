package com.opengamma.math.interpolation;

import com.opengamma.math.MathException;

/**
 * 
 * @author emcleod
 * 
 * @param <T>
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

  public T getErrorEstimate() throws MathException {
    if (_error == null) {
      throw new MathException("Error was not calculated for this interpolation result");
    }
    return _error;
  }
}
