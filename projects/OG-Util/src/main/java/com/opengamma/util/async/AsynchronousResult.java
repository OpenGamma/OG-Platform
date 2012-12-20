/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.async;

import com.opengamma.util.PublicAPI;

/**
 * Represents a result that may be produced by another thread and potentially allow the original calling thread to
 * perform another action in the meantime.
 * 
 * @param <T> type of the result
 */
@PublicAPI
public class AsynchronousResult<T> {

  private final T _result;
  private final RuntimeException _exception;

  /**
   * Creates a new instance.
   * 
   * @param result the result value, or null if {@code exception} is specified
   * @param exception the exception thrown, or null if a result was signaled
   */
  protected AsynchronousResult(final T result, final RuntimeException exception) {
    _result = result;
    _exception = exception;
  }

  /**
   * Returns the result or throws the exception that was signaled.
   * 
   * @return the result
   */
  public T getResult() {
    if (_exception != null) {
      throw _exception;
    } else {
      return _result;
    }
  }

}
