/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.opengamma.util.ArgumentChecker;

/**
 * Wraps another {@link ExecutorService} instance, ensuring {@link ThreadLocalServiceContext} is initialized before
 * every task is run and that it is cleared after every task completes. {@link ThreadLocalServiceContext#init} is
 * called on the pooled thread using the {@link ServiceContext} returned by
 * {@link ThreadLocalServiceContext#getInstance()} on the thread that submits the task.
 */
public class ServiceContextAwareExecutorService implements ExecutorService {

  /** Used to run the tasks. */
  private final ExecutorService _delegateExecutor;

  /**
   * @param delegate the underlying {@link ExecutorService} used to execute tasks
   */
  public ServiceContextAwareExecutorService(ExecutorService delegate) {
    _delegateExecutor = ArgumentChecker.notNull(delegate, "delegate");
  }

  @Override
  public void shutdown() {
    _delegateExecutor.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    return _delegateExecutor.shutdownNow();
  }

  @Override
  public boolean isShutdown() {
    return _delegateExecutor.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return _delegateExecutor.isTerminated();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return _delegateExecutor.awaitTermination(timeout, unit);
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    return _delegateExecutor.submit(new ServiceContextAwareCallable<>(task));
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    return _delegateExecutor.submit(new ServiceContextAwareRunnable(task), result);
  }

  @Override
  public Future<?> submit(Runnable task) {
    return _delegateExecutor.submit(new ServiceContextAwareRunnable(task));
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
    return _delegateExecutor.invokeAll(wrapTasks(tasks));
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
    return _delegateExecutor.invokeAll(wrapTasks(tasks), timeout, unit);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
    return _delegateExecutor.invokeAny(wrapTasks(tasks));
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return _delegateExecutor.invokeAny(wrapTasks(tasks), timeout, unit);
  }

  @Override
  public void execute(Runnable command) {
    _delegateExecutor.execute(new ServiceContextAwareRunnable(command));
  }

  /**
   * @return the tasks wrapped in instances of {@link ServiceContextAwareCallable}.
   */
  private <T> List<Callable<T>> wrapTasks(Collection<? extends Callable<T>> tasks) {
    List<Callable<T>> taskList = new ArrayList<>(tasks.size());

    for (Callable<T> task : tasks) {
      taskList.add(new ServiceContextAwareCallable<>(task));
    }
    return taskList;
  }

  /**
   * {@link Callable} implementation that sets up and tears down thread local {@link ServiceContext} bindings around
   * a call to a delegate {@link Callable} instance.
   */
  private final class ServiceContextAwareCallable<T> implements Callable<T> {

    private final Callable<T> _delegateCallable;

    /** The service context used to initialize {@link ThreadLocalServiceContext} before task execution. */
    private final ServiceContext _serviceContext;

    private ServiceContextAwareCallable(Callable<T> delegateCallable) {
      _delegateCallable = delegateCallable;
      _serviceContext = ThreadLocalServiceContext.getInstance();
    }

    @Override
    public T call() throws Exception {
      try {
        ThreadLocalServiceContext.init(_serviceContext);
        return _delegateCallable.call();
      } finally {
        ThreadLocalServiceContext.init(null);
      }
    }
  }

  /**
   * {@link Runnable} implementation that sets up and tears down thread local {@link ServiceContext} bindings around
   * a call to a delegate {@link Runnable} instance.
   */
  private final class ServiceContextAwareRunnable implements Runnable {

    private final Runnable _delegateRunnable;

    /** The service context used to initialize {@link ThreadLocalServiceContext} before task execution. */
    private final ServiceContext _serviceContext;

    private ServiceContextAwareRunnable(Runnable delegateRunnable) {
      _delegateRunnable = delegateRunnable;
      _serviceContext = ThreadLocalServiceContext.getInstance();
    }

    @Override
    public void run() {
      try {
        ThreadLocalServiceContext.init(_serviceContext);
        _delegateRunnable.run();
      } finally {
        ThreadLocalServiceContext.init(null);
      }
    }
  }
}
