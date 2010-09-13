/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.view.calc.stats;

import java.util.concurrent.atomic.AtomicLong;

import javax.time.Instant;

/**
 * Holds statistics about graph execution.
 */
public class GraphExecutionStatistics {

  private final String _viewName;
  private final String _calcConfigName;
  private final AtomicLong _processedGraphs = new AtomicLong();
  private final AtomicLong _executedGraphs = new AtomicLong();
  private final AtomicLong _executedNodes = new AtomicLong();
  private final AtomicLong _executionTime = new AtomicLong();
  private final AtomicLong _actualTime = new AtomicLong();
  private final AtomicLong _processedJobs = new AtomicLong();
  private final AtomicLong _processedJobSize = new AtomicLong();
  private final AtomicLong _processedJobCost = new AtomicLong();
  private volatile Instant _lastProcessedTime;
  private volatile Instant _lastExecutedTime;

  public GraphExecutionStatistics(final String viewName, final String calcConfigName) {
    _viewName = viewName;
    _calcConfigName = calcConfigName;
  }

  public String getViewName() {
    return _viewName;
  }

  public String getCalcConfigName() {
    return _calcConfigName;
  }

  public long getProcessedGraphs() {
    return _processedGraphs.get();
  }

  public long getExecutedGraphs() {
    return _executedGraphs.get();
  }

  public long getExecutedNodes() {
    return _executedNodes.get();
  }

  public long getExecutionTime() {
    return _executionTime.get();
  }

  public long getActualTime() {
    return _actualTime.get();
  }

  public long getProcessedJobs() {
    return _processedJobs.get();
  }

  public long getProcessedJobSize() {
    return _processedJobSize.get();
  }

  public long getProcessedJobCost() {
    return _processedJobCost.get();
  }

  public Instant getLastProcessedTime() {
    return _lastProcessedTime;
  }

  public Instant getLastExecutedTime() {
    return _lastExecutedTime;
  }

  public double getAverageGraphSize() {
    final long executions = getExecutedGraphs();
    if (executions > 0) {
      return (double) getExecutedNodes() / (double) executions;
    } else {
      return 0;
    }
  }

  public double getAverageExecutionTime() {
    final long executions = getExecutedGraphs();
    if (executions > 0) {
      return (double) getExecutionTime() / (double) executions / 1e9;
    } else {
      return 0;
    }
  }

  public double getAverageActualTime() {
    final long executions = getExecutedGraphs();
    if (executions > 0) {
      return (double) getActualTime() / (double) executions / 1e9;
    } else {
      return 0;
    }
  }

  public double getAverageJobSize() {
    final long executions = getProcessedGraphs();
    if (executions > 0) {
      return (double) getProcessedJobCost() / (double) executions;
    } else {
      return 0;
    }
  }

  public double getAverageJobCost() {
    final long executions = getProcessedGraphs();
    if (executions > 0) {
      return (double) getProcessedJobCost() / (double) executions;
    } else {
      return 0;
    }
  }

  public void recordExecution(final int nodeCount, final long executionTime, final long duration) {
    _executedGraphs.incrementAndGet();
    _executedNodes.addAndGet(nodeCount);
    _executionTime.addAndGet(executionTime);
    _actualTime.addAndGet(duration);
    _lastExecutedTime = Instant.nowSystemClock();
  }

  public void recordProcessing(final int totalJobs, final double meanJobSize, final double meanJobCycleCost) {
    _processedGraphs.incrementAndGet();
    _processedJobs.addAndGet(totalJobs);
    _processedJobSize.addAndGet((long) meanJobSize);
    _processedJobCost.addAndGet((long) meanJobCycleCost);
    _lastProcessedTime = Instant.nowSystemClock();
  }

  public void reset() {
    _processedGraphs.set(0);
    _executedGraphs.set(0);
    _executedNodes.set(0);
    _executionTime.set(0);
    _actualTime.set(0);
    _processedJobs.set(0);
    _processedJobSize.set(0);
    _processedJobCost.set(0);
  }

  private static void decay(final AtomicLong value, final double factor) {
    value.addAndGet(-(long) ((double) value.get() * factor));
  }

  public void decay(final double factor) {
    decay(_processedGraphs, factor);
    decay(_executedGraphs, factor);
    decay(_executedNodes, factor);
    decay(_executionTime, factor);
    decay(_actualTime, factor);
    decay(_processedJobs, factor);
    decay(_processedJobSize, factor);
    decay(_processedJobCost, factor);
  }

  public GraphExecutionStatistics snapshot() {
    final GraphExecutionStatistics stats = new GraphExecutionStatistics(getViewName(), getCalcConfigName());
    stats.snapshot(this);
    return stats;
  }

  public void snapshot(final GraphExecutionStatistics other) {
    _processedGraphs.set(other.getProcessedGraphs());
    _executedGraphs.set(other.getExecutedGraphs());
    _executedNodes.set(other.getExecutedNodes());
    _executionTime.set(other.getExecutionTime());
    _actualTime.set(other.getActualTime());
    _processedJobs.set(other.getProcessedJobs());
    _processedJobSize.set(other.getProcessedJobSize());
    _processedJobCost.set(other.getProcessedJobCost());
  }

  public void delta(final GraphExecutionStatistics future) {
    _processedGraphs.set(future.getProcessedGraphs() - getProcessedGraphs());
    _executedGraphs.set(future.getExecutedGraphs() - getExecutedGraphs());
    _executedNodes.set(future.getExecutedNodes() - getExecutedNodes());
    _executionTime.set(future.getExecutionTime() - getExecutionTime());
    _actualTime.set(future.getActualTime() - getActualTime());
    _processedJobs.set(future.getProcessedJobs() - getProcessedJobs());
    _processedJobSize.set(future.getProcessedJobSize() - getProcessedJobSize());
    _processedJobCost.set(future.getProcessedJobCost() - getProcessedJobCost());
  }
}
