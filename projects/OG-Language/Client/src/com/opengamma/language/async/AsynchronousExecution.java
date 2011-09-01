/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.async;

/**
 * Indicates that the result of the original call is being prepared asynchronously. A callback object can be
 * registered with the exception instance to get the result when it becomes available.
 */
public class AsynchronousExecution extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private final AsynchronousOperation<?> _operation;

  /* package */AsynchronousExecution(final AsynchronousOperation<?> operation) {
    _operation = operation;
  }

  protected AsynchronousExecution(final AsynchronousExecution copyFrom) {
    this(copyFrom.getOperation());
  }

  @SuppressWarnings("unchecked")
  private <T> AsynchronousOperation<T> getOperation() {
    return (AsynchronousOperation<T>) _operation;
  }

  /**
   * Sets the listener that will be notified when a result (or exception) is available. If the result is already available
   * the calling thread will invoke the listener, otherwise the thread that signals the result (or exception) will invoke
   * the listener.
   * 
   * @param <T> type of the result
   * @param resultListener the listener
   */
  public <T> void setResultListener(final ResultListener<T> resultListener) {
    this.<T>getOperation().setResultListener(resultListener);
  }

  /**
   * Returns the result (or throws the signaled exception), blocking the caller until it is available.
   * 
   * @param <T> type of the result
   * @return the result
   * @throws InterruptedException if interrupted waiting for the result
   */
  public <T> T getResult() throws InterruptedException {
    return this.<T>getOperation().waitForResult();
  }

}
