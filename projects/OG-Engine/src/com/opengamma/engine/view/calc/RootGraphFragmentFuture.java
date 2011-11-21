/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;

/**
 * Implementation of the {@link Future} part of a root graph fragment.
 */
/* package */class RootGraphFragmentFuture implements Future<DependencyGraph> {

  private final GraphFragment<?, ?> _this;
  private final GraphExecutorStatisticsGatherer _statistics;
  private long _jobStarted;
  private boolean _done;

  public RootGraphFragmentFuture(final GraphFragment<?, ?> root, final GraphExecutorStatisticsGatherer statistics) {
    _this = root;
    _statistics = statistics;
    _jobStarted = System.nanoTime();
  }

  public synchronized void executed() {
    if (!isCancelled()) {
      _done = true;
      notifyAll();
      _statistics.graphExecuted(getContext().getGraph().getCalculationConfigurationName(), getContext().getGraph().getSize(), getContext().getExecutionTime(), System.nanoTime() - _jobStarted);
    }
  }

  protected GraphFragmentContext getContext() {
    return _this.getContext();
  }

  protected GraphFragment<?, ?> getThis() {
    return _this;
  }

  // Future

  @Override
  public synchronized boolean cancel(boolean mayInterruptIfRunning) {
    if (isDone()) {
      return false;
    }
    getContext().cancelAll(mayInterruptIfRunning);
    notifyAll();
    return true;
  }

  @Override
  public synchronized DependencyGraph get() throws InterruptedException, ExecutionException {
    while (!_done && !getContext().isCancelled()) {
      wait();
    }
    if (getContext().isCancelled()) {
      throw new CancellationException();
    }
    return getContext().getGraph();
  }

  @Override
  public synchronized DependencyGraph get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    final long tFinish = System.nanoTime() + unit.toNanos(timeout);
    while (!_done && !getContext().isCancelled() && (tFinish - System.nanoTime() > 0)) {
      wait(unit.toMillis(timeout));
    }
    if (getContext().isCancelled()) {
      throw new CancellationException();
    }
    if (!_done) {
      throw new TimeoutException();
    }
    return getContext().getGraph();
  }

  @Override
  public synchronized boolean isCancelled() {
    return getContext().isCancelled();
  }

  @Override
  public synchronized boolean isDone() {
    return isCancelled() || _done;
  }

}
