/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.async;

import java.util.concurrent.ExecutorService;


/**
 * Signals production of a result for a {@link AsynchronousResult} instance.
 * 
 * @param <T> type of the result
 */
public class ResultCallback<T> {

  private final AsynchronousOperation<T> _operation;

  /* package */ResultCallback(final AsynchronousOperation<T> operation) {
    _operation = operation;
  }

  protected ResultCallback(final ResultCallback<T> copyFrom) {
    this(copyFrom.getOperation());
  }

  private AsynchronousOperation<T> getOperation() {
    return _operation;
  }

  /**
   * Passes the result back to the original caller. Note that the calling thread may be used to execute a callback handler
   * which may in turn block or perform other actions. If this will be a problem, the caller should use an {@link ExecutorService}
   * or other source of threads to make the notification.
   * 
   * @param result the result value
   */
  public void setResult(final T result) {
    getOperation().setResult(result);
  }

  /**
   * Passes an exception back to the original caller. Note that the calling thread may be used to execute a callback handler
   * which may in turn block or perform other actions. If this will be a problem, the caller should use an {@link ExecutorService}
   * or other source of threads to make the notification.
   * 
   * @param exception the exception that should be thrown
   */
  public void setException(final RuntimeException exception) {
    getOperation().setException(exception);
  }

}
