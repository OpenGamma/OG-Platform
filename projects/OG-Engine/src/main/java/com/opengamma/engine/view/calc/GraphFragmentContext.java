/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.engine.view.calcnode.JobResultReceiver;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.async.Cancelable;

/**
 * State shared among all fragments of a dependency graph for execution. Also implements
 * the {@link JobResultReceiver} interface to coordinate responses and try to support
 * cancellation of an executing graph.
 */
/* package */class GraphFragmentContext implements JobResultReceiver {

  private static final Logger s_logger = LoggerFactory.getLogger(GraphFragmentContext.class);

  private static final AtomicInteger s_nextObjectId = new AtomicInteger();

  private final int _objectId = s_nextObjectId.incrementAndGet();
  private final AtomicInteger _graphFragmentIdentifiers = new AtomicInteger();
  private final long _functionInitializationTimestamp;
  private final VersionCorrection _resolverVersionCorrection;
  private final AtomicLong _executionTime = new AtomicLong();
  private final MultipleNodeExecutor _executor;
  private final DependencyGraph _graph;
  // TODO: don't need the full spec in the keys here -- just the job identifier will do
  private final Map<CalculationJobSpecification, Cancelable> _cancels = new ConcurrentHashMap<CalculationJobSpecification, Cancelable>();
  private Map<CalculationJobSpecification, GraphFragment<?>> _job2fragment;
  private volatile boolean _cancelled;
  private final Queue<ExecutionResult> _executionResultQueue;

  protected static <K, V> ConcurrentMap<K, V> createMap(int numElements) {
    return new ConcurrentHashMap<K, V>((numElements << 2) / 3);
  }

  public GraphFragmentContext(final MultipleNodeExecutor executor, final DependencyGraph graph, final Queue<ExecutionResult> executionResultQueue) {
    _executor = executor;
    _graph = graph;
    _functionInitializationTimestamp = executor.getFunctionInitId();
    _resolverVersionCorrection = executor.getResolverVersionCorrection();
    _executionResultQueue = executionResultQueue;
  }

  public MultipleNodeExecutor getExecutor() {
    return _executor;
  }

  public DependencyGraph getGraph() {
    return _graph;
  }

  public Queue<ExecutionResult> getExecutionResultQueue() {
    return _executionResultQueue;
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

  public void registerCallback(final CalculationJobSpecification jobspec, final GraphFragment<?> fragment) {
    _job2fragment.put(jobspec, fragment);
  }

  @Override
  public void resultReceived(final CalculationJobResult result) {
    _cancels.remove(result.getSpecification());
    final GraphFragment<?> fragment = _job2fragment.remove(result.getSpecification());
    if (fragment != null) {
      // Put result into the queue
      getExecutionResultQueue().offer(new ExecutionResult(Collections.unmodifiableList(fragment.getNodes()), result));
      fragment.resultReceived(this, result);
      // Mark nodes as good or bad - the result items are in the same order as the request items (the dependency nodes)
      final Iterator<CalculationJobResultItem> itrResult = result.getResultItems().iterator();
      final Iterator<DependencyNode> itrNode = fragment.getNodes().iterator();
      while (itrResult.hasNext()) {
        assert itrNode.hasNext();
        final CalculationJobResultItem resultItem = itrResult.next();
        final DependencyNode node = itrNode.next();
        if (resultItem.isFailed()) {
          getExecutor().markFailed(node);
        } else {
          getExecutor().markExecuted(node);
        }
      }
      assert !itrNode.hasNext();
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

  public VersionCorrection getResolverVersionCorrection() {
    return _resolverVersionCorrection;
  }

  protected PrintStream openDebugStream(final String name) {
    try {
      final String fileName = System.getProperty("java.io.tmpdir") + File.separatorChar + name + _objectId + ".txt";
      return new PrintStream(new FileOutputStream(fileName));
    } catch (IOException e) {
      s_logger.error("Can't open debug file", e);
      return System.out;
    }
  }

}
