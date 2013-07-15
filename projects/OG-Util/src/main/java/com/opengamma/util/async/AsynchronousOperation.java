/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.async;

import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.NamedThreadPoolFactory;

/**
 * Represents the production of a result by another thread and potentially allow the original calling thread to
 * perform another action in the meantime.
 *
 * @param <T> type of the result
 */
public final class AsynchronousOperation<T> {

  private static final ScheduledExecutorService s_timeouts = createTimeoutExecutor();

  private final Class<T> _type;
  private AsynchronousResult<T> _result;
  private ResultListener<T> _listener;

  private static ScheduledExecutorService createTimeoutExecutor() {
    final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    executor.setKeepAliveTime(60, TimeUnit.SECONDS);
    executor.allowCoreThreadTimeOut(true);
    executor.setThreadFactory(new NamedThreadPoolFactory("AsynchronousOperation-Timeout"));
    return executor;
  }

  /**
   * Creates a new instance.
   */
  private AsynchronousOperation(final Class<T> type) {
    _type = type;
  }

  /**
   * Creates a new instance typed for the given class.
   *
   * @param <T> the type of the result
   * @param type the class of the result, never null
   * @return the new instance, never null
   */
  public static <T> AsynchronousOperation<T> create(final Class<T> type) {
    return new AsynchronousOperation<T>(type);
  }

  /**
   * Creates a new instance typed for a set.
   *
   * @param <T> the type within the set
   * @return the new instance, never null
   */
  @SuppressWarnings({"unchecked", "rawtypes" })
  public static <T> AsynchronousOperation<Set<T>> createSet() {
    return new AsynchronousOperation(Set.class);
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
   * Returns the type of the result.
   *
   * @return the type of the result, never null
   */
  protected Class<T> getResultType() {
    return _type;
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
      throw new AsynchronousExecution(this);
    } else {
      return result.getResult();
    }
  }

  /**
   * Declares a timeout on an asynchronous operation. The {@link Cancelable#cancel} callback is made after the timeout period
   * unless the handle returned by the timeout is itself canceled.
   *
   * @param cancelation the user callback, not null
   * @param timeoutMillis the timeout period in milliseconds
   * @return a cancellation handle for the timeout
   */
  public static Cancelable timeout(final Cancelable cancelation, final int timeoutMillis) {
    ArgumentChecker.notNull(cancelation, "cancelation");
    ArgumentChecker.notNegativeOrZero(timeoutMillis, "timeoutMillis");
    final ScheduledFuture<?> future = s_timeouts.schedule(new Runnable() {
      @Override
      public void run() {
        cancelation.cancel(true);
      }
    }, timeoutMillis, TimeUnit.MILLISECONDS);
    return new Cancelable() {
      @Override
      public boolean cancel(final boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
      }
    };
  }

  /**
   * Returns the result (or throws the signaled exception), blocking the caller until it is available. This is equivalent to {@link AsynchronousExecution#getResult} but wraps any
   * {@link InterruptedException} in a {@link OpenGammaRuntimeException}.
   *
   * @param <T> type of the result
   * @param ex the caught exception
   * @return the result
   */
  @SuppressWarnings("unchecked")
  public static <T> T getResult(final AsynchronousExecution ex) {
    try {
      return (T) ex.getResult();
    } catch (final InterruptedException e) {
      throw new OpenGammaRuntimeException("interrupted", e);
    }
  }

}
