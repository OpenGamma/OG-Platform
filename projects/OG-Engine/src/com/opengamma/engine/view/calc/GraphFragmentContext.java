/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobItem;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.engine.view.calcnode.JobResultReceiver;
import com.opengamma.engine.view.calcnode.stats.FunctionCostsPerConfiguration;
import com.opengamma.engine.view.calcnode.stats.FunctionInvocationStatistics;
import com.opengamma.util.Cancelable;

/* package */class GraphFragmentContext implements JobResultReceiver {

  private static final Logger s_logger = LoggerFactory.getLogger(GraphFragmentContext.class);

  private final AtomicInteger _graphFragmentIdentifiers = new AtomicInteger();
  private final long _functionInitializationTimestamp;
  private final AtomicLong _executionTime = new AtomicLong();
  private MultipleNodeExecutor _executor;
  private final DependencyGraph _graph;
  private final Map<CalculationJobItem, DependencyNode> _item2node;
  private final FunctionCostsPerConfiguration _functionCost;
  private final Map<CalculationJobSpecification, Cancelable> _cancels = new ConcurrentHashMap<CalculationJobSpecification, Cancelable>();
  private Map<ValueSpecification, Boolean> _sharedCacheValues;
  private Map<CalculationJobSpecification, GraphFragment> _job2fragment;
  private volatile boolean _cancelled;
  private final BlockingQueue<CalculationJobResult> _calcJobResultQueue;

  public GraphFragmentContext(final MultipleNodeExecutor executor, final DependencyGraph graph, final BlockingQueue<CalculationJobResult> calcJobResultQueue) {
    _executor = executor;
    _graph = graph;
    final int hashSize = (graph.getSize() * 4) / 3;
    _item2node = new ConcurrentHashMap<CalculationJobItem, DependencyNode>(hashSize);
    _sharedCacheValues = new ConcurrentHashMap<ValueSpecification, Boolean>();
    for (ValueSpecification specification : graph.getTerminalOutputSpecifications()) {
      _sharedCacheValues.put(specification, Boolean.TRUE);
    }
    _functionCost = executor.getFunctionCosts().getStatistics(graph.getCalculationConfigurationName());
    _functionInitializationTimestamp = executor.getFunctionInitId();
    _calcJobResultQueue = calcJobResultQueue;
  }

  /**
   * Resets state so that the plan can be executed again. This is not valid after a cancellation.
   */
  public boolean reset(final MultipleNodeExecutor executor) {
    if (_cancelled) {
      s_logger.warn("Was cancelled - can't reset for re-execution");
      return false;
    }
    // sanity checks
    if (!_item2node.isEmpty()) {
      s_logger.warn("{} elements in item2node map - can't reset for re-execution ({})", _item2node.size(), _item2node);
      return false;
    }
    if (!_job2fragment.isEmpty()) {
      s_logger.warn("{} elements in job2fragment map - can't reset for re-execution ({})", _job2fragment.size(), _job2fragment);
      return false;
    }
    if (!_cancels.isEmpty()) {
      s_logger.warn("{} elements in cancellation set - can't reset for re-execution ({})", _cancels.size(), _cancels);
      return false;
    }
    _executionTime.set(0);
    _executor = executor;
    return true;
  }

  public MultipleNodeExecutor getExecutor() {
    return _executor;
  }

  public DependencyGraph getGraph() {
    return _graph;
  }

  public BlockingQueue<CalculationJobResult> getCalculationJobResultQueue() {
    return _calcJobResultQueue;
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

  public void freeSharedCacheValues() {
    _sharedCacheValues = null;
  }

  public void allocateFragmentMap(final int size) {
    _job2fragment = new ConcurrentHashMap<CalculationJobSpecification, GraphFragment>((size * 4) / 3);
  }

  public void registerCallback(final CalculationJobSpecification jobspec, final GraphFragment fragment) {
    _job2fragment.put(jobspec, fragment);
  }

  public FunctionInvocationStatistics getFunctionStatistics(final CompiledFunctionDefinition function) {
    return _functionCost.getStatistics(function.getFunctionDefinition().getUniqueId());
  }

  @Override
  public void resultReceived(final CalculationJobResult result) {
    _cancels.remove(result.getSpecification());
    final GraphFragment fragment = _job2fragment.remove(result.getSpecification());
    if (fragment != null) {
      // Put result into the queue
      getCalculationJobResultQueue().offer(result);
      fragment.resultReceived(result);
      // Mark nodes as good or bad
      for (CalculationJobResultItem item : result.getResultItems()) {
        DependencyNode node = _item2node.remove(item.getItem());
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
      _cancels.put(job.getSpecification(), getExecutor().dispatchJob(job, this));
      if (!_job2fragment.containsKey(job.getSpecification())) {
        if (_cancels.remove(job.getSpecification()) != null) {
          s_logger.debug("Removed cancellation handle on fast job execution of {}", job.getSpecification());
        }
      }
    }
  }

  public boolean isCancelled() {
    return _cancelled;
  }

  /**
   * Attempts to cancel jobs. This is a best efforts as there is no synchronization with dispatchJob and so it is
   * possible some jobs may be left to complete or fail.
   */
  public void cancelAll(final boolean mayInterrupt) {
    _cancelled = true;
    for (Cancelable cancel : _cancels.values()) {
      cancel.cancel(mayInterrupt);
    }
  }

  public long getFunctionInitializationTimestamp() {
    return _functionInitializationTimestamp;
  }

}
