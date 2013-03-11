/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.exec.stats;

import java.util.concurrent.atomic.AtomicLong;

import org.threeten.bp.Instant;

import com.opengamma.id.UniqueId;

/**
 * Holds statistics about graph execution.
 */
public class GraphExecutionStatistics {

  private final UniqueId _viewProcessId;
  private final String _calcConfigName;
  private final AtomicLong _processedGraphs = new AtomicLong();
  private final AtomicLong _executedGraphs = new AtomicLong();
  private final AtomicLong _executedNodes = new AtomicLong();
  private final AtomicLong _executionTime = new AtomicLong();
  private final AtomicLong _actualTime = new AtomicLong();
  private final AtomicLong _processedJobs = new AtomicLong();
  private final AtomicLong _processedJobSize = new AtomicLong();
  private final AtomicLong _processedJobCycleCost = new AtomicLong();
  private final AtomicLong _processedJobDataCost = new AtomicLong();
  private volatile Instant _lastProcessedTime;
  private volatile Instant _lastExecutedTime;

  public GraphExecutionStatistics(final UniqueId viewProcessId, final String calcConfigName) {
    _viewProcessId = viewProcessId;
    _calcConfigName = calcConfigName;
  }

  public UniqueId getViewProcessId() {
    return _viewProcessId;
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

  public long getProcessedJobCycleCost() {
    return _processedJobCycleCost.get();
  }

  public long getProcessedJobDataCost() {
    return _processedJobDataCost.get();
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
      return (double) getProcessedJobSize() / (double) executions;
    } else {
      return 0;
    }
  }

  public double getAverageJobCycleCost() {
    final long executions = getProcessedGraphs();
    if (executions > 0) {
      return (double) getProcessedJobCycleCost() / (double) executions;
    } else {
      return 0;
    }
  }

  public double getAverageJobDataCost() {
    final long executions = getProcessedGraphs();
    if (executions > 0) {
      return (double) getProcessedJobDataCost() / (double) executions;
    } else {
      return 0;
    }
  }

  public void recordExecution(final int nodeCount, final long executionTime, final long duration) {
    _executedGraphs.incrementAndGet();
    _executedNodes.addAndGet(nodeCount);
    _executionTime.addAndGet(executionTime);
    _actualTime.addAndGet(duration);
    _lastExecutedTime = Instant.now();
  }

  public void recordProcessing(final int totalJobs, final double meanJobSize, double meanJobCycleCost, double meanJobIOCost) {
    if (Double.isNaN(meanJobCycleCost)) {
      meanJobCycleCost = getAverageJobCycleCost();
    }
    if (Double.isNaN(meanJobIOCost)) {
      meanJobIOCost = getAverageJobDataCost();
    }
    _processedGraphs.incrementAndGet();
    _processedJobs.addAndGet(totalJobs);
    _processedJobSize.addAndGet((long) meanJobSize);
    _processedJobCycleCost.addAndGet((long) meanJobCycleCost);
    _processedJobDataCost.addAndGet((long) meanJobIOCost);
    _lastProcessedTime = Instant.now();
  }

  public void reset() {
    _processedGraphs.set(0);
    _executedGraphs.set(0);
    _executedNodes.set(0);
    _executionTime.set(0);
    _actualTime.set(0);
    _processedJobs.set(0);
    _processedJobSize.set(0);
    _processedJobCycleCost.set(0);
    _processedJobDataCost.set(0);
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
    decay(_processedJobCycleCost, factor);
    decay(_processedJobDataCost, factor);
  }

  public GraphExecutionStatistics snapshot() {
    final GraphExecutionStatistics stats = new GraphExecutionStatistics(getViewProcessId(), getCalcConfigName());
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
    _processedJobCycleCost.set(other.getProcessedJobCycleCost());
    _processedJobDataCost.set(other.getProcessedJobDataCost());
  }

  public void delta(final GraphExecutionStatistics future) {
    _processedGraphs.set(future.getProcessedGraphs() - getProcessedGraphs());
    _executedGraphs.set(future.getExecutedGraphs() - getExecutedGraphs());
    _executedNodes.set(future.getExecutedNodes() - getExecutedNodes());
    _executionTime.set(future.getExecutionTime() - getExecutionTime());
    _actualTime.set(future.getActualTime() - getActualTime());
    _processedJobs.set(future.getProcessedJobs() - getProcessedJobs());
    _processedJobSize.set(future.getProcessedJobSize() - getProcessedJobSize());
    _processedJobCycleCost.set(future.getProcessedJobCycleCost() - getProcessedJobCycleCost());
    _processedJobDataCost.set(future.getProcessedJobDataCost() - getProcessedJobDataCost());
  }
}
