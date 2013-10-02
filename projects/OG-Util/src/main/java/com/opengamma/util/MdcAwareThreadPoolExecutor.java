/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.MDC;

/**
 *
 */
public class MdcAwareThreadPoolExecutor extends ThreadPoolExecutor {

  private final ConcurrentMap<Runnable, Map> _diagnosticContexts = new ConcurrentHashMap<>();

  public MdcAwareThreadPoolExecutor(ThreadFactory factory) {
    super(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), factory);
  }

  public MdcAwareThreadPoolExecutor(int coreThreads,
                                    int maxThreads,
                                    long keepAliveTime,
                                    TimeUnit timeUnit,
                                    BlockingQueue<Runnable> queue,
                                    ThreadFactory factory) {
    super(coreThreads, maxThreads, keepAliveTime, timeUnit, queue, factory);
  }

  @Override
  protected void beforeExecute(Thread t, Runnable task) {

    Map contextMap = _diagnosticContexts.get(task);
    if (contextMap != null) {
      MDC.setContextMap(contextMap);
    }

    // As per documentation on {@link ThreadPoolExecutor#beforeExecute}, we should
    // call the super at the end of the method
    super.beforeExecute(t, task);
  }

  @Override
  protected void afterExecute(Runnable task, Throwable t) {
    // As per documentation on {@link ThreadPoolExecutor#afterExecute}, we should
    // call the super at the start of the method
    super.afterExecute(task, t);
    _diagnosticContexts.remove(task);
  }

  @Override
  public void execute(Runnable command) {
    recordDiagnosticContext(command);
    super.execute(command);
  }

  @Override
  protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
    RunnableFuture<T> runnableFuture = super.newTaskFor(runnable, value);
    recordDiagnosticContext(runnableFuture);
    return runnableFuture;
  }

  @Override
  protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
    RunnableFuture<T> runnableFuture = super.newTaskFor(callable);
    recordDiagnosticContext(runnableFuture);
    return runnableFuture;
  }

  @Override
  public boolean remove(Runnable task) {
    boolean success = super.remove(task);
    // If remove fails then task may already been started
    if (success) {
      _diagnosticContexts.remove(task);
    }
    return success;
  }

  private void recordDiagnosticContext(Runnable task) {
    Map contextMap = MDC.getCopyOfContextMap();
    if (contextMap != null) {
      _diagnosticContexts.put(task, contextMap);
    }
  }
}
