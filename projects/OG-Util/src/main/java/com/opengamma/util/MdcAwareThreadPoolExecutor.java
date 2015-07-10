/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.MDC;

/**
 * An extension of ThreadPoolExecutor that is aware of slf4j mapped diagnostic
 * logging (MDC). MDC allows per-thread, contextual information to be output
 * to logging files. This is a map that could hold a session id for a web
 * application or an identifier for a request from an external service. MDC
 * information is passed down to child threads when they are created but falls
 * down at the point where a thread-pool is used. In these cases no context
 * information is passed on and the information will be absent from the logs.
 *
 * This executor ensures that MDC information is copied from the calling
 * thread onto the worker thread and removed once the worker has completed
 * and is returned to the pool.
 *
 * Because access to the MDC is via static methods, it is only possible
 * to access the context applicable to the current thread i.e. when submitting
 * a job to the executor we have access to the calling thread's MDC, but no way
 * of accessing the worker's. Similarly, when the worker executes the job, we
 * have access to the worker's MDC but not the original caller's. To handle
 * this, the class records the MDC information from a thread when it submits
 * a job. When the job is executed, the MDC information is looked up and
 * copied to the worker.
 *
 * Note that this class overrides {@link #execute(Runnable)} which means
 * there is no need to override the rest of the methods defined in
 * {@link ExecutorService}
 */
public class MdcAwareThreadPoolExecutor extends ThreadPoolExecutor {

  /**
   * Map storing the jobs to be executed and the MDC information which was
   * held by the calling thread when the job was submitted. Note that,
   * depending on which method is used to submit a job, the key may not
   * be the actual job submitted (e.g. if a Callable is submitted, it
   * gets converted by the executor).
   */
  private final ConcurrentMap<Runnable, Map<String, String>> _diagnosticContexts = new ConcurrentHashMap<>();

  /**
   * Create a new instance with the specified thread factory. Note that
   * this will create an executor with the same defaults as if calling
   * {@link Executors#newCachedThreadPool(ThreadFactory)}
   *
   * @param factory the factory to use when the executor creates a new thread
   */
  public MdcAwareThreadPoolExecutor(ThreadFactory factory) {
    super(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), factory);
  }

  /**
   * Create a new instance.
   *
   * @param coreThreads the number of threads to keep in the pool, even
   * if they are idle, unless {@code allowCoreThreadTimeOut} is set
   * @param maxThreads the maximum number of threads to allow in the pool
   * @param keepAliveTime when the number of threads is greater than the
   * core, this is the maximum time that excess idle threads will wait for
   * new tasks before terminating.
   * @param timeUnit the time unit for the {@code keepAliveTime} argument
   * @param queue the queue to use for holding tasks before they are
   * executed. This queue will hold only the {@code Runnable} tasks submitted
   * by the {@code execute} method.
   * @param factory the factory to use when the executor creates a new thread
   */
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

    // This method is called by the worker thread before it executes the
    // specified task. Therefore we can insert the MDC information and it will
    // be available as the task is executed
    Map<String, String> contextMap = _diagnosticContexts.get(task);
    if (contextMap != null) {
      MDC.setContextMap(contextMap);
    }

    // As per documentation on {@link ThreadPoolExecutor#beforeExecute}, we should
    // call the super at the end of the method
    super.beforeExecute(t, task);
  }

  @Override
  protected void afterExecute(Runnable task, Throwable t) {

    // This method is called by the worker thread after it has executed the
    // specified task.

    // As per documentation on {@link ThreadPoolExecutor#afterExecute}, we should
    // call the super at the start of the method
    super.afterExecute(task, t);
    _diagnosticContexts.remove(task);

    // Clear the MDC information from the worker thread
    MDC.clear();
  }

  @Override
  public void execute(Runnable command) {
    recordDiagnosticContext(command);
    super.execute(command);
  }

  @Override
  public boolean remove(Runnable task) {
    boolean success = super.remove(task);
    // If remove fails then task has already been started and the
    // job will be removed when the task completes
    if (success) {
      _diagnosticContexts.remove(task);
    }
    return success;
  }

  @SuppressWarnings("unchecked")
  private void recordDiagnosticContext(Runnable task) {
    Map<String, String> contextMap = MDC.getCopyOfContextMap();
    if (contextMap != null) {
      _diagnosticContexts.put(task, contextMap);
    }
  }

}
