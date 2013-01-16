/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.async;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;

import com.opengamma.util.PublicAPI;

/**
 * Indicates that the result of the original call is being prepared asynchronously. A callback object can be
 * registered with the exception instance to get the result when it becomes available.
 */
@PublicAPI
public final class AsynchronousExecution extends Exception {

  /**
   * This class is not serializable.
   */
  private static final long serialVersionUID = 0L;

  /**
   * This class is not serializable.
   */
  private void writeObject(final ObjectOutputStream output) throws IOException {
    throw new NotSerializableException();
  }

  private final AsynchronousOperation<?> _operation;

  protected AsynchronousExecution(final AsynchronousOperation<?> operation) {
    _operation = operation;
  }

  @SuppressWarnings("unchecked")
  private <T> AsynchronousOperation<T> getOperation() {
    return (AsynchronousOperation<T>) _operation;
  }

  /**
   * Sets the listener that will be notified when a result (or exception) is available. If the result is already available the calling thread will invoke the listener, otherwise the thread that
   * signals the result (or exception) will invoke the listener.
   * 
   * @param <T> type of the result, as returned by {@link #getResultType}.
   * @param resultListener the listener
   */
  public <T> void setResultListener(final ResultListener<T> resultListener) {
    this.<T>getOperation().setResultListener(resultListener);
  }

  /**
   * Returns the result (or throws the signaled exception), blocking the caller until it is available.
   * 
   * @param <T> type of the result, as returned by {@link #getResultType}.
   * @return the result
   * @throws InterruptedException if interrupted waiting for the result
   */
  public <T> T getResult() throws InterruptedException {
    return this.<T>getOperation().waitForResult();
  }

  /**
   * Returns the type of the result that the operation will produce. Normally this will be the return type of the original method, but there may be times when an alternative class is preferable.
   *
   * @return the class of the result, as declared when the corresponding {@link AsynchronousOperation} was created, not null
   */
  public Class<?> getResultType() {
    return getOperation().getResultType();
  }

}
