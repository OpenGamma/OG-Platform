/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.async;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Indicates that the result of the original call is being prepared asynchronously. A callback object can be registered with the exception instance to get a handle to the result when it becomes
 * available.
 */
public class AsynchronousHandleExecution extends AsynchronousExecution {

  private static final long serialVersionUID = 1L;

  protected AsynchronousHandleExecution(final AsynchronousHandleOperation<?> operation) {
    super(operation);
  }

  /**
   * Registers the result listener for the underlying object. A stub listener is registered so that when the handle is returned, it is expanded to the underlying object.
   * 
   * @param resultListener the result listener for the underlying object
   * @param <T> the underlying result type
   */
  @Override
  public <T> void setResultListener(final ResultListener<T> resultListener) {
    setResultHandleListener(new ResultListener<AsynchronousHandle<T>>() {
      @Override
      public void operationComplete(final AsynchronousResult<AsynchronousHandle<T>> result) {
        AsynchronousResult<T> underlyingResult;
        try {
          underlyingResult = new AsynchronousResult<T>(result.getResult().get(), null);
        } catch (AsynchronousHandleExecution e) {
          underlyingResult = new AsynchronousResult<T>(AsynchronousHandleOperation.<T>getHandleResult(e), null);
        } catch (Exception ex) {
          underlyingResult = new AsynchronousResult<T>(null, new OpenGammaRuntimeException("caught exception", ex));
        }
        resultListener.operationComplete(underlyingResult);
      }
    });
  }

  /**
   * Registers the result listener for the handle object.
   * 
   * @param resultListener the result listener
   * @param <T> the underlying result type
   */
  public <T> void setResultHandleListener(final ResultListener<AsynchronousHandle<T>> resultListener) {
    super.<AsynchronousHandle<T>>setResultListener(resultListener);
  }

  /**
   * Returns the underlying result object.
   * 
   * @return the object
   * @param <T> the underlying result type
   */
  @Override
  public <T> T getResult() throws InterruptedException {
    try {
      return this.<T>getResultHandle().get();
    } catch (AsynchronousHandleExecution ex) {
      return AsynchronousHandleOperation.<T>getHandleResult(ex);
    }
  }

  /**
   * Returns the result handle object.
   * 
   * @return the handle object
   * @param <T> the underlying result type
   */
  public <T> AsynchronousHandle<T> getResultHandle() throws InterruptedException {
    return super.<AsynchronousHandle<T>>getResult();
  }

}
