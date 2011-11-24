/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobItem;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.engine.view.calcnode.JobResultReceiver;
import com.opengamma.util.Cancelable;

/**
 * State shared among all fragments of a dependency graph for execution. Also implements
 * the {@link JobResultReceiver} interface to coordinate responses and try to support
 * cancellation of an executing graph.
 */
/* package */class GraphFragmentContext implements JobResultReceiver {

  private static final Logger s_logger = LoggerFactory.getLogger(GraphFragmentContext.class);

  private final AtomicInteger _graphFragmentIdentifiers = new AtomicInteger();
  private final long _functionInitializationTimestamp;
  private final AtomicLong _executionTime = new AtomicLong();
  private final MultipleNodeExecutor _executor;
  private final DependencyGraph _graph;
  private final Map<CalculationJobItem, DependencyNode> _item2node;
  private final Map<CalculationJobSpecification, Cancelable> _cancels = new ConcurrentHashMap<CalculationJobSpecification, Cancelable>();
  private Map<CalculationJobSpecification, GraphFragment<?, ?>> _job2fragment;
  private volatile boolean _cancelled;
  private final BlockingQueue<CalculationJobResult> _calcJobResultQueue;

  protected static <K, V> ConcurrentMap<K, V> createMap(int numElements) {
    return new ConcurrentHashMap<K, V>((numElements << 2) / 3);
  }

  public GraphFragmentContext(final MultipleNodeExecutor executor, final DependencyGraph graph, final BlockingQueue<CalculationJobResult> calcJobResultQueue) {
    _executor = executor;
    _graph = graph;
    _item2node = createMap(graph.getSize());
    _functionInitializationTimestamp = executor.getFunctionInitId();
    _calcJobResultQueue = calcJobResultQueue;
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

  public void allocateFragmentMap(final int size) {
    _job2fragment = createMap(size);
  }

  public void registerJobItem(final CalculationJobItem jobitem, final DependencyNode node) {
    _item2node.put(jobitem, node);
  }

  public void registerCallback(final CalculationJobSpecification jobspec, final GraphFragment<?, ?> fragment) {
    _job2fragment.put(jobspec, fragment);
  }

  @Override
  public void resultReceived(final CalculationJobResult result) {
    _cancels.remove(result.getSpecification());
    final GraphFragment<?, ?> fragment = _job2fragment.remove(result.getSpecification());
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

  public long getFunctionInitId() {
    return _functionInitializationTimestamp;
  }

}
