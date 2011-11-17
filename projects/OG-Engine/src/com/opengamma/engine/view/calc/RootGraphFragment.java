/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.view.calcnode.CalculationJobResult;

/* package */class RootGraphFragment extends GraphFragment implements Future<Object> {

  private final GraphExecutorStatisticsGatherer _statistics;
  private long _jobStarted;
  private boolean _done;

  public RootGraphFragment(final GraphFragmentContext context, final GraphExecutorStatisticsGatherer statistics) {
    super(context);
    _statistics = statistics;
    _jobStarted = System.nanoTime();
  }

  public RootGraphFragment(final GraphFragmentContext context, final GraphExecutorStatisticsGatherer statistics, final Collection<DependencyNode> nodes) {
    super(context, nodes);
    _statistics = statistics;
  }

  @Override
  public synchronized void execute() {
    if (!isCancelled()) {
      _done = true;
      notifyAll();
      _statistics.graphExecuted(getContext().getGraph().getCalculationConfigurationName(), getContext().getGraph().getSize(), getContext().getExecutionTime(), System.nanoTime() - _jobStarted);
      getContext().freeSharedCacheValues();
      getContext().freeCalculationJobResultQueue();
      getContext().getExecutor().getCache().cacheExecutionPlan(getContext().getGraph(), this);
    }
  }

  /**
   * Only gets called if this was the only node created because the dep graph was
   * too small.
   */
  @Override
  public void resultReceived(final CalculationJobResult result) {
    super.resultReceived(result);
    execute();
  }

  public boolean reset(final MultipleNodeExecutor executor, final Set<GraphFragment> processed, final BlockingQueue<CalculationJobResult> results) {
    if (!getContext().reset(executor, results)) {
      return false;
    }
    if (!reset(processed)) {
      return false;
    }
    _jobStarted = System.nanoTime();
    _done = false;
    return true;
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
  public synchronized Object get() throws InterruptedException, ExecutionException {
    while (!_done && !getContext().isCancelled()) {
      wait();
    }
    if (getContext().isCancelled()) {
      throw new CancellationException();
    }
    return null;
  }

  @Override
  public synchronized Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    while (!_done && !getContext().isCancelled()) {
      wait(unit.toMillis(timeout));
    }
    if (getContext().isCancelled()) {
      throw new CancellationException();
    }
    return null;
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
