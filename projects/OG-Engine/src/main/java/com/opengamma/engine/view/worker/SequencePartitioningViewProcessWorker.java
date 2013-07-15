/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.impl.ViewProcessContext;

/**
 * Implementation of {@link ViewProcessWorker} for partitioning a sequence and delegating to other workers to handle each partition.
 */
public class SequencePartitioningViewProcessWorker implements ViewProcessWorker, ViewProcessWorkerContext {

  private static final Logger s_logger = LoggerFactory.getLogger(SequencePartitioningViewProcessWorker.class);

  private final ViewProcessWorkerFactory _delegate;
  private final ViewProcessWorkerContext _context;
  private final EnumSet<ViewExecutionFlags> _executionFlags;
  private final Integer _maxSuccessiveDeltaCycles;
  private final ViewCycleExecutionSequence _sequence;
  private final ViewCycleExecutionOptions _defaultExecutionOptions;
  private final Queue<ViewProcessWorker> _workers = new LinkedList<ViewProcessWorker>();
  private volatile ViewDefinition _viewDefinition;
  private int _partition;
  private boolean _terminated;
  private int _spawnedWorkerCount;
  private int _spawnedCycleCount;
  private int _spawnedWorkers;
  private int _trigger;

  public SequencePartitioningViewProcessWorker(final ViewProcessWorkerFactory delegate, final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions,
      final ViewDefinition viewDefinition, final int partition, final int maxWorkers) {
    _delegate = delegate;
    _context = context;
    _executionFlags = EnumSet.copyOf(executionOptions.getFlags());
    _maxSuccessiveDeltaCycles = executionOptions.getMaxSuccessiveDeltaCycles();
    _defaultExecutionOptions = executionOptions.getDefaultExecutionOptions();
    _sequence = executionOptions.getExecutionSequence();
    _viewDefinition = viewDefinition;
    _partition = partition;
    _trigger = maxWorkers;
    if (!_executionFlags.remove(ViewExecutionFlags.WAIT_FOR_INITIAL_TRIGGER)) {
      // Kick off first batch of workers
      triggerCycle();
    }
  }

  private ViewProcessWorkerFactory getDelegate() {
    return _delegate;
  }

  private ViewProcessWorkerContext getWorkerContext() {
    return _context;
  }

  private EnumSet<ViewExecutionFlags> getExecutionFlags() {
    return _executionFlags;
  }

  private ViewCycleExecutionOptions getDefaultExecutionOptions() {
    return _defaultExecutionOptions;
  }

  private Integer getMaxSuccessiveDeltaCycles() {
    return _maxSuccessiveDeltaCycles;
  }

  private ViewExecutionOptions getExecutionOptions(ViewCycleExecutionSequence newSequence) {
    return new ExecutionOptions(newSequence, getExecutionFlags(), getMaxSuccessiveDeltaCycles(), getDefaultExecutionOptions());
  }

  private ViewCycleExecutionSequence getSequence() {
    return _sequence;
  }

  private ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }

  private int getPartitionSize() {
    return _partition;
  }

  private synchronized void spawnWorker() {
    ViewCycleExecutionSequence sequence = getSequence();
    final int partitionSize = getPartitionSize();
    final List<ViewCycleExecutionOptions> partition = new ArrayList<ViewCycleExecutionOptions>(partitionSize);
    for (int i = 0; i < partitionSize; i++) {
      final ViewCycleExecutionOptions step = sequence.poll(getDefaultExecutionOptions());
      if (step != null) {
        partition.add(step);
      } else {
        break;
      }
    }
    if (partition.isEmpty()) {
      s_logger.info("No more cycles to execute");
    } else {
      final int firstCycle = _spawnedCycleCount;
      _spawnedCycleCount += partition.size();
      s_logger.info("Spawning worker {} for {} cycles {} - {}", new Object[] {++_spawnedWorkerCount, getWorkerContext(), firstCycle, _spawnedCycleCount });
      ViewProcessWorker delegate = getDelegate().createWorker(this, getExecutionOptions(new ArbitraryViewCycleExecutionSequence(partition)), getViewDefinition());
      _workers.add(delegate);
      _spawnedWorkers++;
    }
  }

  // ViewProcessWorker

  @Override
  public synchronized boolean triggerCycle() {
    if (_trigger == 0) {
      s_logger.debug("Ignoring triggerCycle on run-as-fast-as-possible sequence");
      return false;
    }
    while (_trigger > 0) {
      spawnWorker();
      _trigger--;
    }
    return true;
  }

  @Override
  public boolean requestCycle() {
    s_logger.debug("Ignoring requestCycle on run-as-fast-as-possible sequence");
    return false;
  }

  @Override
  public void updateViewDefinition(ViewDefinition viewDefinition) {
    // This is not a good state of affairs as the caller has little or no control or knowledge over the sequence we're working on
    s_logger.warn("View definition updated on run-as-fast-as-possible sequence");
    _viewDefinition = viewDefinition;
    Collection<ViewProcessWorker> delegates;
    synchronized (this) {
      delegates = new ArrayList<ViewProcessWorker>(_workers);
    }
    for (ViewProcessWorker delegate : delegates) {
      delegate.updateViewDefinition(_viewDefinition);
    }
  }

  @Override
  public void terminate() {
    Collection<ViewProcessWorker> delegates;
    synchronized (this) {
      _terminated = true;
      delegates = new ArrayList<ViewProcessWorker>(_workers);
    }
    for (ViewProcessWorker delegate : delegates) {
      delegate.terminate();
    }
  }

  @Override
  public void join() throws InterruptedException {
    Collection<ViewProcessWorker> delegates;
    synchronized (this) {
      delegates = new ArrayList<ViewProcessWorker>(_workers);
    }
    for (ViewProcessWorker delegate : delegates) {
      delegate.join();
    }
  }

  @Override
  public boolean join(long timeout) throws InterruptedException {
    Collection<ViewProcessWorker> delegates;
    final long maxTime = System.nanoTime() + (timeout * 1000000);
    long time;
    do {
      synchronized (this) {
        delegates = new ArrayList<ViewProcessWorker>(_workers);
      }
      for (ViewProcessWorker delegate : delegates) {
        time = (maxTime - System.nanoTime()) / 1000000;
        if (time <= 0) {
          return false;
        }
        if (!delegate.join(time)) {
          return false;
        }
      }
      if (isTerminated()) {
        return true;
      }
      time = (maxTime - System.nanoTime()) / 1000000;
    } while (time > 0);
    return false;
  }

  /**
   * Tests whether at least one of the workers in the buffer is still active. This will also housekeep the buffer of workers, removing any that have terminated.
   * 
   * @return true if the worker buffer is empty, or all return true from {@code isTerminated} (which leaves the buffer empty)
   */
  @Override
  public boolean isTerminated() {
    ViewProcessWorker worker;
    synchronized (this) {
      worker = _workers.peek();
    }
    while (worker != null) {
      if (!worker.isTerminated()) {
        return false;
      }
      synchronized (this) {
        ViewProcessWorker worker2 = _workers.poll();
        if (worker2 == worker) {
          s_logger.debug("Removing completed worker from head of queue");
          worker = worker2;
        } else if (worker2 == null) {
          s_logger.debug("Completed worker already removed from queue");
          break;
        } else {
          s_logger.debug("Concurrent worker queue access");
          _workers.add(worker2);
          worker = _workers.peek();
        }
      }
    }
    return true;
  }

  @Override
  public void forceGraphRebuild() {
    Collection<ViewProcessWorker> delegates;
    synchronized (this) {
      delegates = new ArrayList<>(_workers);
    }
    for (ViewProcessWorker delegate : delegates) {
      delegate.forceGraphRebuild();
    }
  }

  // ViewProcessWorkerContext

  @Override
  public ViewProcessContext getProcessContext() {
    return getWorkerContext().getProcessContext();
  }

  @Override
  public void viewDefinitionCompiled(ViewExecutionDataProvider dataProvider, CompiledViewDefinitionWithGraphs compiled) {
    s_logger.debug("View definition compiled");
    getWorkerContext().viewDefinitionCompiled(dataProvider, compiled);
  }

  @Override
  public void viewDefinitionCompilationFailed(Instant compilationTime, Exception exception) {
    s_logger.debug("View definition compilation failed");
    getWorkerContext().viewDefinitionCompilationFailed(compilationTime, exception);
  }

  @Override
  public void cycleStarted(ViewCycleMetadata cycleMetadata) {
    s_logger.debug("Cycle started");
    getWorkerContext().cycleStarted(cycleMetadata);
  }

  @Override
  public void cycleFragmentCompleted(ViewComputationResultModel result, ViewDefinition viewDefinition) {
    s_logger.debug("Cycle fragment completed");
    getWorkerContext().cycleFragmentCompleted(result, viewDefinition);
  }

  @Override
  public void cycleCompleted(ViewCycle cycle) {
    s_logger.debug("Cycle completed");
    getWorkerContext().cycleCompleted(cycle);
  }

  @Override
  public void cycleExecutionFailed(ViewCycleExecutionOptions options, Exception exception) {
    s_logger.debug("Cycle execution failed");
    getWorkerContext().cycleExecutionFailed(options, exception);
  }

  @Override
  public void workerCompleted() {
    s_logger.debug("Worker completed");
    final boolean finished;
    synchronized (this) {
      finished = (--_spawnedWorkers) == 0;
      if (!_terminated) {
        spawnWorker();
      }
    }
    // isTerminated will housekeep the queue for us, but may not return TRUE as the worker that called us might not be considered terminated yet
    isTerminated();
    if (finished) {
      getWorkerContext().workerCompleted();
    }
  }

  // Object

  @Override
  public String toString() {
    return "Partition[" + getWorkerContext() + "]";
  }

}
