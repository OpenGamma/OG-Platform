/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.async;

import java.util.concurrent.LinkedBlockingDeque;

import com.opengamma.util.ArgumentChecker;

/**
 * Represents the production of a result by another thread and potentially allow the original calling thread to
 * perform another action in the meantime.
 * 
 * @param <T> type of the result
 */
public class AsynchronousOperation<T> {

  private AsynchronousResult<T> _result;
  private ResultListener<T> _listener;

  /**
   * Creates a new instance.
   */
  public AsynchronousOperation() {
  }

  /**
   * Creates a callback object that can be used to post the result when it is available.
   * 
   * @return the callback object
   */
  public ResultCallback<T> getCallback() {
    return new ResultCallback<T>(this);
  }

  /**
   * Called when a result is available.
   * 
   * @param result the result value
   */
  protected void setResult(final T result) {
    setAsynchronousResult(new AsynchronousResult<T>(result, null));
  }

  /**
   * Called when an exception is available.
   * 
   * @param exception the exception
   */
  protected void setException(final RuntimeException exception) {
    ArgumentChecker.notNull(exception, "exception");
    setAsynchronousResult(new AsynchronousResult<T>(null, exception));
  }

  /**
   * Sets the result object, invoking the listener if one is registered.
   * 
   * @param result the signaled result object
   */
  protected void setAsynchronousResult(final AsynchronousResult<T> result) {
    final ResultListener<T> resultListener;
    synchronized (this) {
      if (_result != null) {
        throw new IllegalStateException();
      }
      _result = result;
      resultListener = _listener;
    }
    if (resultListener != null) {
      resultListener.operationComplete(result);
    }
  }

  /**
   * Called when a result listener has been registered with the exception.
   * 
   * @param resultListener the listener
   */
  protected void setResultListener(final ResultListener<T> resultListener) {
    ArgumentChecker.notNull(resultListener, "resultListener");
    final AsynchronousResult<T> result;
    synchronized (this) {
      if (_listener != null) {
        throw new IllegalStateException();
      }
      _listener = resultListener;
      result = _result;
    }
    if (result != null) {
      resultListener.operationComplete(result);
    }
  }

  /**
   * Called when the exception is blocking on the result.
   * 
   * @return the result
   * @throws InterruptedException if interrupted waiting for the result
   */
  protected T waitForResult() throws InterruptedException {
    final LinkedBlockingDeque<AsynchronousResult<T>> results = new LinkedBlockingDeque<AsynchronousResult<T>>();
    setResultListener(new ResultListener<T>() {
      @Override
      public void operationComplete(final AsynchronousResult<T> result) {
        results.add(result);
      }
    });
    return results.take().getResult();
  }

  /**
   * Creates the exception thrown by {@link #getResult}.
   * 
   * @return the exception to be thrown
   */
  protected AsynchronousExecution createException() {
    return new AsynchronousExecution(this);
  }

  /**
   * Returns control to the calling thread. If a result or exception has already been signaled, the
   * result is returned or the exception thrown. If no result or exception is available, the checked
   * {@link AsynchronousExecution} is thrown.
   * 
   * @return the result, if available
   * @throws AsynchronousExecution if the result is not available
   */
  public T getResult() throws AsynchronousExecution {
    AsynchronousResult<T> result;
    synchronized (this) {
      result = _result;
    }
    if (result == null) {
      throw createException();
    } else {
      return result.getResult();
    }
  }

}
