/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.async;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Represents the production of a result handle by another thread and potentially allow the original calling thread to perform another action in the meantime.
 * 
 * @param <T> type of the underlying result
 */
public class AsynchronousHandleOperation<T> extends AsynchronousOperation<AsynchronousHandle<T>> {

  /**
   * Creates a new instance.
   */
  public AsynchronousHandleOperation() {
  }

  protected AsynchronousHandleExecution createException() {
    return new AsynchronousHandleExecution(this);
  }

  /**
   * Returns control to the calling thread. If a result or exception has already been signaled, the result is returned or the exception thrown. If no result or exception is available, the checked
   * {@link AsynchronousHandleExecution} is thrown.
   * 
   * @return the result, if available
   * @throws AsynchronousHandleExecution if the result is not available
   */
  public T getHandleResult() throws AsynchronousHandleExecution {
    return getResultHandle().get();
  }

  /**
   * Returns control to the calling thread. If a result or exception has already been signaled, the handle to the result is returned or the exception thrown. If no result or exception is available,
   * the checked {@link AsynchronousHandleException} is thrown.
   * 
   * @return the result handle, if available
   * @throws AsynchronousHandleExecution if the result is not available
   */
  public AsynchronousHandle<T> getResultHandle() throws AsynchronousHandleExecution {
    try {
      return getResult();
    } catch (AsynchronousHandleExecution ex) {
      throw ex;
    } catch (AsynchronousExecution ex) {
      throw new AssertionError();
    }
  }

  public static <T> AsynchronousHandle<T> getResultHandle(final AsynchronousHandleExecution ex) {
    try {
      return ex.getResult();
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("interrupted", e);
    }
  }

  public static <T> T getHandleResult(AsynchronousHandleExecution ex) {
    do {
      try {
        return AsynchronousHandleOperation.<T>getResultHandle(ex).get();
      } catch (AsynchronousHandleExecution e) {
        ex = e;
      }
    } while (true);
  }

}
