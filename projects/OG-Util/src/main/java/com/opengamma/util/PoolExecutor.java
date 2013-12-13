/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

/**
 * Implementation of {@link Executor} that allows jobs to run in a group with a single consumer receiving results for them.
 * <p>
 * The maximum number of additional threads is limited, but the thread which submitted jobs may temporarily join the pool to allow its tasks to complete.
 */
public class PoolExecutor implements Executor, Lifecycle {

  private static final Logger s_logger = LoggerFactory.getLogger(PoolExecutor.class);

  /**
   * Callback interface for receiving results of a pooled execution.
   */
  public interface CompletionListener<T> {

    void success(T result);

    void failure(Throwable error);

  }

  /**
   * Implementation of a {@link ExecutorService} that is associated with a group.
   */
  public class Service<T> implements Executor {

    private final AtomicInteger _pending = new AtomicInteger();
    private final CompletionListener<T> _listener;
    private volatile boolean _shutdown;
    private boolean _joining;

    protected Service(final CompletionListener<T> listener) {
      s_logger.info("Created thread pool service {}", this);
      _listener = listener;
    }

    protected void decrementAndNotify() {
      if (_pending.decrementAndGet() == 0) {
        synchronized (this) {
          if (_joining) {
            notifyAll();
          }
        }
      }
    }

    protected void postResult(final T result) {
      if ((_listener != null) && !_shutdown) {
        s_logger.debug("Result available from {} - {} remaining", this, _pending);
        _listener.success(result);
      } else {
        s_logger.debug("Discarding result from {} - {} remaining", this, _pending);
      }
    }

    protected void postException(final Throwable error) {
      if ((_listener != null) && !_shutdown) {
        s_logger.debug("Error available from {} - {} remaining", this, _pending);
        _listener.failure(error);
      } else {
        s_logger.debug("Discarding result from {} - {} remaining", this, _pending);
      }
    }

    /**
     * Submits a job for execution, posting the result when it completes.
     * <p>
     * This must not be used after {@link #shutdown} or {@link #join} have been called.
     * 
     * @param command the job to execute, not null
     * @param result the result to post
     */
    public void execute(final Runnable command, final T result) {
      _pending.incrementAndGet();
      PoolExecutor.this.execute(new ExecuteRunnable<T>(this, command, result));
    }

    /**
     * Submits a job for execution, posting its result when it completes.
     * <p>
     * This must not be used after {@link #shutdown} or {@link #join} have been called.
     * 
     * @param command the job to execute, not null
     */
    public void execute(final Callable<T> command) {
      _pending.incrementAndGet();
      PoolExecutor.this.execute(new ExecuteCallable<T>(this, command));
    }

    /**
     * Discards any outstanding jobs. This will return immediately; to wait for jobs to be discarded or completed, call {@link #join} afterwards.
     */
    public synchronized void shutdown() {
      s_logger.info("Shutting down {}", this);
      if (_shutdown) {
        return;
      }
      _shutdown = true;
      if (_joining) {
        notifyAll();
      }
      final Iterator<Runnable> itrQueue = getQueue().iterator();
      while (itrQueue.hasNext()) {
        final Runnable entry = itrQueue.next();
        if (entry instanceof Execute) {
          final Execute<?> execute = (Execute<?>) entry;
          if ((execute._service == this) && execute.markExecuted()) {
            s_logger.debug("Discarding {}", execute);
            _pending.decrementAndGet();
            itrQueue.remove();
          }
        }
      }
    }

    /**
     * Waits for all submitted jobs to complete. This thread may execute one or more of the submitted jobs.
     */
    public void join() throws InterruptedException {
      s_logger.info("Joining");
      Execute<?> inline = null;
      try {
        Iterator<Runnable> itrQueue = null;
        do {
          synchronized (this) {
            _joining = true;
            try {
              if (_pending.get() == 0) {
                s_logger.info("No pending tasks");
                _shutdown = true;
                return;
              } else {
                if ((itrQueue == null) || !itrQueue.hasNext()) {
                  itrQueue = getQueue().iterator();
                }
                while (itrQueue.hasNext()) {
                  final Runnable entry = itrQueue.next();
                  if (entry instanceof Execute) {
                    final Execute<?> execute = (Execute<?>) entry;
                    if ((execute._service == this) && execute.markExecuted()) {
                      s_logger.debug("Inline execution of {}", execute);
                      itrQueue.remove();
                      inline = execute;
                      break;
                    }
                  }
                }
                if (inline == null) {
                  s_logger.info("No inline executions available, waiting for {} remaining tasks", _pending);
                  wait();
                }
              }
            } finally {
              _joining = false;
            }
          }
          if (inline != null) {
            inline.runImpl();
            inline = null;
          }
        } while (true);
      } finally {
        if (inline != null) {
          getQueue().add(inline);
        }
      }
    }

    // Executor

    /**
     * Submit a job for execution to the group. This is the same as calling {@link #execute(Runnable,Object)}.
     * <p>
     * This must not be used after {@link #shutdown} or {@link #join} have been called.
     * 
     * @param command the job to execute, not null
     */
    @Override
    public void execute(final Runnable command) {
      execute(command, null);
    }

    // Object

    @Override
    public String toString() {
      return Integer.toHexString(hashCode());
    }

  }

  private abstract static class Execute<T> implements Runnable {

    private final Service<T> _service;
    private final AtomicBoolean _executed = new AtomicBoolean();

    protected Execute(final Service<T> service) {
      _service = service;
    }

    public boolean markExecuted() {
      return !_executed.getAndSet(true);
    }

    protected abstract T callImpl() throws Throwable;

    protected void runImpl() {
      try {
        s_logger.debug("Executing {}", this);
        _service.postResult(callImpl());
      } catch (Throwable t) {
        _service.postException(t);
      } finally {
        _service.decrementAndNotify();
      }
    }

    @Override
    public void run() {
      if (_service._shutdown) {
        return;
      }
      if (markExecuted()) {
        runImpl();
      } else {
        s_logger.debug("Already executed or cancelled {}", this);
      }
    }

    @Override
    public String toString() {
      return _service.toString();
    }

  }

  private static final class ExecuteRunnable<T> extends Execute<T> {

    private final Runnable _runnable;
    private final T _result;

    public ExecuteRunnable(final Service<T> service, final Runnable runnable, final T result) {
      super(service);
      ArgumentChecker.notNull(runnable, "runnable");
      _runnable = runnable;
      _result = result;
    }

    @Override
    protected T callImpl() {
      _runnable.run();
      return _result;
    }

    @Override
    public String toString() {
      return super.toString() + "/" + _runnable;
    }

  }

  private static final class ExecuteCallable<T> extends Execute<T> {

    private final Callable<T> _callable;

    public ExecuteCallable(final Service<T> service, final Callable<T> callable) {
      super(service);
      ArgumentChecker.notNull(callable, "callable");
      _callable = callable;
    }

    @Override
    protected T callImpl() throws Throwable {
      return _callable.call();
    }

    @Override
    public String toString() {
      return super.toString() + "/" + _callable;
    }

  }

  private static final ThreadLocal<Reference<PoolExecutor>> s_instance = new ThreadLocal<Reference<PoolExecutor>>();
  private final Reference<PoolExecutor> _me = new WeakReference<PoolExecutor>(this);
  private final BlockingQueue<Runnable> _queue = new LinkedBlockingQueue<Runnable>();
  private final ThreadPoolExecutor _underlying;

  private static final class ExecutorThread extends Thread {

    private final Reference<PoolExecutor> _owner;

    private ExecutorThread(final Reference<PoolExecutor> owner, final ThreadGroup group, final Runnable runnable, final String threadName, final int stackSize) {
      super(group, runnable, threadName, stackSize);
      _owner = owner;
    }

    @Override
    public void run() {
      s_instance.set(_owner);
      super.run();
    }

  }

  private static final class ExecutorThreadFactory extends NamedThreadPoolFactory {

    private final Reference<PoolExecutor> _owner;

    private ExecutorThreadFactory(final Reference<PoolExecutor> owner, final String name) {
      super(name, true);
      _owner = owner;
    }

    @Override
    protected Thread createThread(final ThreadGroup group, final Runnable runnable, final String threadName, final int stackSize) {
      return new ExecutorThread(_owner, group, runnable, threadName, stackSize);
    }

  }

  /**
   * Creates a new execution pool with the given (maximum) number of threads.
   * <p>
   * This can be created with no threads. Tasks submitted will never be executed unless they arrive from a pool and another thread then joins that pool to complete its execution.
   * 
   * @param maxThreads the maximum number of threads to put in the pool
   * @param name the diagnostic name to use for the pool
   */
  public PoolExecutor(final int maxThreads, final String name) {
    if (maxThreads > 0) {
      ThreadFactory factory = new ExecutorThreadFactory(_me, name);
      _underlying = new MdcAwareThreadPoolExecutor(maxThreads, maxThreads, 60, TimeUnit.SECONDS, _queue, factory);
      _underlying.allowCoreThreadTimeOut(true);
    } else {
      _underlying = null;
    }
  }

  @Override
  protected void finalize() {
    if (_underlying != null) {
      _underlying.shutdown();
    }
  }

  protected BlockingQueue<Runnable> getQueue() {
    return _queue;
  }

  /**
   * Creates a service group with a listener to handle results from that group.
   * 
   * @param <T> the result type for jobs submitted to the group
   * @param listener the listener to receive results from jobs in the group, or null if the results are not wanted
   * @return the service group to submit further jobs to
   */
  public <T> Service<T> createService(CompletionListener<T> listener) {
    return new Service<T>(listener);
  }

  public ExecutorService asService() {
    return _underlying;
  }

  /**
   * Registers an instance with the current thread, returning the previously registered instance (if any).
   * 
   * @param instance the instance to register, or null for none
   * @return the previously registered instance, or null for none
   */
  public static PoolExecutor setInstance(final PoolExecutor instance) {
    Reference<PoolExecutor> previous = s_instance.get();
    if (instance != null) {
      s_instance.set(instance._me);
    } else {
      s_instance.set(null);
    }
    if (previous != null) {
      return previous.get();
    } else {
      return null;
    }
  }

  /**
   * Returns the instance registered with the current thread, if any.
   * 
   * @return the registered instance, or null for none
   */
  public static PoolExecutor instance() {
    Reference<PoolExecutor> executor = s_instance.get();
    if (executor != null) {
      return executor.get();
    } else {
      return null;
    }
  }

  // Executor

  /**
   * Submits a job to the underlying execution pool.
   * 
   * @param command the job to execute, not null
   */
  @Override
  public void execute(final Runnable command) {
    s_logger.debug("Submitting {}", command);
    if (_underlying != null) {
      _underlying.execute(command);
    } else {
      getQueue().add(command);
    }
  }

  // Lifecycle

  /**
   * Dummy {@link Lifecycle#start} method; this object is implicitly started at construction and it is not possible to restart it after a {@link #stop} request.
   */
  @Override
  public void start() {
    if (!isRunning()) {
      throw new IllegalStateException("Can't restart service after explicit stop");
    }
  }

  @Override
  public void stop() {
    _me.clear();
    if (_underlying != null) {
      _underlying.shutdown();
    }
  }

  @Override
  public boolean isRunning() {
    return _me.get() != null;
  }

}
