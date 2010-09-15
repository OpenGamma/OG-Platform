/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobItem;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.engine.view.calcnode.JobResultReceiver;
import com.opengamma.engine.view.calcnode.stats.FunctionCost;
import com.opengamma.engine.view.calcnode.stats.FunctionInvocationStatistics;
import com.opengamma.util.Cancellable;

/* package */class GraphFragmentContext implements JobResultReceiver {

  private final AtomicInteger _graphFragmentIdentifiers = new AtomicInteger();
  private final AtomicLong _executionTime = new AtomicLong();
  private final MultipleNodeExecutor _executor;
  private final DependencyGraph _graph;
  private final Map<CalculationJobItem, DependencyNode> _item2node;
  private final Map<ValueSpecification, Boolean> _sharedCacheValues;
  private final AtomicInteger _maxConcurrency = new AtomicInteger();
  private final FunctionCost.ForConfiguration _functionCost;
  private final Queue<Cancellable> _cancels = new ConcurrentLinkedQueue<Cancellable>();
  private Map<CalculationJobSpecification, GraphFragment> _job2fragment;
  private volatile boolean _cancelled;

  public GraphFragmentContext(final MultipleNodeExecutor executor, final DependencyGraph graph) {
    _executor = executor;
    _graph = graph;
    final int hashSize = (graph.getSize() * 4) / 3;
    _item2node = new ConcurrentHashMap<CalculationJobItem, DependencyNode>(hashSize);
    _sharedCacheValues = new ConcurrentHashMap<ValueSpecification, Boolean>();
    for (ValueSpecification specification : graph.getTerminalOutputValues()) {
      _sharedCacheValues.put(specification, Boolean.TRUE);
    }
    _functionCost = executor.getFunctionCost().getStatistics(graph.getCalcConfName());
  }

  public MultipleNodeExecutor getExecutor() {
    return _executor;
  }

  public DependencyGraph getGraph() {
    return _graph;
  }

  public int nextIdentifier() {
    return _graphFragmentIdentifiers.incrementAndGet();
  }

  public void addExecutionTime(final long duration) {
    _executionTime.addAndGet(duration);
  }

  public long getExecutionTime() {
    return _executionTime.get();
  }

  public Map<CalculationJobItem, DependencyNode> getItem2Node() {
    return _item2node;
  }

  public Map<ValueSpecification, Boolean> getSharedCacheValues() {
    return _sharedCacheValues;
  }

  public void allocateFragmentMap(final int size) {
    _job2fragment = new ConcurrentHashMap<CalculationJobSpecification, GraphFragment>((size * 4) / 3);
  }

  public void registerCallback(final CalculationJobSpecification jobspec, final GraphFragment fragment) {
    _job2fragment.put(jobspec, fragment);
  }

  public void collectMaxConcurrency(final int tailCount) {
    int maxCon = _maxConcurrency.get();
    while (tailCount > maxCon) {
      if (_maxConcurrency.compareAndSet(maxCon, tailCount)) {
        return;
      }
      maxCon = _maxConcurrency.get();
    }
  }

  public int getMaxConcurrency() {
    return _maxConcurrency.get();
  }

  public FunctionInvocationStatistics getFunctionStatistics(final FunctionDefinition function) {
    return _functionCost.getStatistics(function.getUniqueIdentifier());
  }

  @Override
  public void resultReceived(final CalculationJobResult result) {
    final GraphFragment fragment = _job2fragment.remove(result.getSpecification());
    if (fragment != null) {
      fragment.resultReceived(result);
      // Mark nodes as good or bad
      for (CalculationJobResultItem item : result.getResultItems()) {
        DependencyNode node = _item2node.get(item.getItem());
        if (node == null) {
          continue;
        }
        getExecutor().markExecuted(node);
        if (item.failed()) {
          getExecutor().markFailed(node);
        }
      }
    }
  }

  public void dispatchJob(final CalculationJob job) {
    if (!_cancelled) {
      _cancels.add(getExecutor().dispatchJob(job, this));
    }
  }

  public boolean isCancelled() {
    return _cancelled;
  }

  public void cancelAll(final boolean mayInterrupt) {
    _cancelled = true;
    Cancellable cancel = _cancels.poll();
    while (cancel != null) {
      cancel.cancel(mayInterrupt);
      cancel = _cancels.poll();
    }
  }

}
