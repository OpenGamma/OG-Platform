/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An executor which will create one thread per requesting thread to service its requests
 */
public class ThreadPerThreadExecutor extends AbstractExecutorService {

  private final Set<ExecutorService> _delegatedExecutors = new CopyOnWriteArraySet<ExecutorService>();
  private final ThreadLocal<ExecutorService> _threadExecutor = new ThreadLocal<ExecutorService>();
  
  @Override
  public void shutdown() {
    for (ExecutorService executor : _delegatedExecutors) {
      executor.shutdown();
    }
  }

  @Override
  public List<Runnable> shutdownNow() {
    ArrayList<Runnable> ret = new ArrayList<Runnable>();
    for (ExecutorService executor : _delegatedExecutors) {
      ret.addAll(executor.shutdownNow());
    }
    return ret;
  }

  @Override
  public boolean isShutdown() {
    //NOTE: will return true if no work was executed, which is only kind of true 
    for (ExecutorService executor : _delegatedExecutors) {
      if (!executor.isShutdown()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean isTerminated() {
    //NOTE: will return true if no work was executed, which is only kind of true 
    for (ExecutorService executor : _delegatedExecutors) {
      if (!executor.isTerminated()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean awaitTermination(long timeoutX, TimeUnit unit) throws InterruptedException {
    long timeout = unit.toMillis(timeoutX);
    long start = System.currentTimeMillis();
    long end = start + timeout; 
    for (ExecutorService executor : _delegatedExecutors) {
      long allowed = end - System.currentTimeMillis();
      if (allowed <= 0 || !executor.awaitTermination(allowed, TimeUnit.MILLISECONDS)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void execute(Runnable command) {
    ExecutorService executor = _threadExecutor.get();
    if (executor == null) {
      executor = createExecutorService();
      _threadExecutor.set(executor);
      _delegatedExecutors.add(executor);
    }
    
    executor.execute(command);
  }

  private ExecutorService createExecutorService() {
    ExecutorService executor;
    //NOTE: this is quite like Executors.newSingleThreadExecutor(), except it times out
    executor = new ThreadPoolExecutor(0, 1,
        getKeepAliveTimeMillis(), TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<Runnable>(),
        getThreadFactory());
    return executor;
  }

  public long getKeepAliveTimeMillis() {
    long keepAliveTimeMillis = 10000L;
    return keepAliveTimeMillis;
  }

  private ThreadFactory getThreadFactory() {
    ThreadFactory threadFactory = Executors.defaultThreadFactory(); //TODO: from pool?
    return threadFactory;
  }

}
