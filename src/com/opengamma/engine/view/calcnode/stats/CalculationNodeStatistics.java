/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode.stats;

import java.util.concurrent.atomic.AtomicLong;

import javax.time.Instant;

/**
 * Holds statistics about a node. Flesh out with additional data necessary to satisfy [ENG-57] and related tasks.
 */
public class CalculationNodeStatistics {

  private final String _nodeId;
  private final AtomicLong _successfulJobs = new AtomicLong();
  private final AtomicLong _unsuccessfulJobs = new AtomicLong();
  private final AtomicLong _jobItems = new AtomicLong();
  private final AtomicLong _jobCycleCost = new AtomicLong();
  private final AtomicLong _executionTime = new AtomicLong();
  private final AtomicLong _nonExecutionTime = new AtomicLong();
  private volatile Instant _lastJobTime;

  public CalculationNodeStatistics(final String nodeId) {
    _nodeId = nodeId;
  }

  public String getNodeId() {
    return _nodeId;
  }

  public long getSuccessfulJobs() {
    return _successfulJobs.get();
  }

  public long getJobItems() {
    return _jobItems.get();
  }

  public long getJobCycleCost() {
    return _jobCycleCost.get();
  }

  public long getUnsuccessfulJobs() {
    return _unsuccessfulJobs.get();
  }

  public long getExecutionTime() {
    return _executionTime.get();
  }

  public long getNonExecutionTime() {
    return _nonExecutionTime.get();
  }

  public Instant getLastJobTime() {
    return _lastJobTime;
  }

  public double getAverageExecutionTime() {
    final long jobs = getSuccessfulJobs();
    if (jobs > 0) {
      return (double) getExecutionTime() / (double) jobs / 1e9;
    } else {
      return 0;
    }
  }

  public double getAverageNonExecutionTime() {
    final long jobs = getSuccessfulJobs();
    if (jobs > 0) {
      return (double) getNonExecutionTime() / (double) jobs / 1e9;
    } else {
      return 0;
    }
  }

  public double getAverageJobItems() {
    final long jobs = getSuccessfulJobs();
    if (jobs > 0) {
      return (double) getJobItems() / (double) jobs;
    } else {
      return 0;
    }
  }

  public double getAverageJobCycleCost() {
    final long jobs = getSuccessfulJobs();
    if (jobs > 0) {
      return (double) getJobCycleCost() / (double) jobs;
    } else {
      return 0;
    }
  }

  public void recordSuccessfulJob(final int jobItems, final int jobCycleCost, final long executionTime, final long duration) {
    _successfulJobs.incrementAndGet();
    _jobItems.addAndGet(jobItems);
    _jobCycleCost.addAndGet(jobCycleCost);
    _executionTime.addAndGet(executionTime);
    _nonExecutionTime.addAndGet(duration - executionTime);
    _lastJobTime = Instant.nowSystemClock();
  }

  public void recordUnsuccessfulJob(final long duration) {
    _unsuccessfulJobs.incrementAndGet();
    _nonExecutionTime.addAndGet(duration);
    _lastJobTime = Instant.nowSystemClock();
  }

  public void reset() {
    _successfulJobs.set(0);
    _unsuccessfulJobs.set(0);
    _jobItems.set(0);
    _jobCycleCost.set(0);
    _executionTime.set(0);
    _nonExecutionTime.set(0);
  }

  private static void decay(final AtomicLong value, final double factor) {
    value.addAndGet(-(long) ((double) value.get() * factor));
  }

  public void decay(final double factor) {
    decay(_successfulJobs, factor);
    decay(_unsuccessfulJobs, factor);
    decay(_jobItems, factor);
    decay(_jobCycleCost, factor);
    decay(_executionTime, factor);
    decay(_nonExecutionTime, factor);
  }

  public CalculationNodeStatistics snapshot() {
    final CalculationNodeStatistics stats = new CalculationNodeStatistics(getNodeId());
    stats.snapshot(this);
    return stats;
  }

  public void snapshot(final CalculationNodeStatistics other) {
    _successfulJobs.set(other.getSuccessfulJobs());
    _unsuccessfulJobs.set(other.getUnsuccessfulJobs());
    _jobItems.set(other.getJobItems());
    _jobCycleCost.set(other.getJobCycleCost());
    _executionTime.set(other.getExecutionTime());
    _nonExecutionTime.set(other.getNonExecutionTime());
  }

  public void delta(final CalculationNodeStatistics future) {
    _successfulJobs.set(future.getSuccessfulJobs() - getSuccessfulJobs());
    _unsuccessfulJobs.set(future.getUnsuccessfulJobs() - getUnsuccessfulJobs());
    _jobItems.set(future.getJobItems() - getJobItems());
    _jobCycleCost.set(future.getJobCycleCost() - getJobCycleCost());
    _executionTime.set(future.getExecutionTime() - getExecutionTime());
    _nonExecutionTime.set(future.getNonExecutionTime() - getNonExecutionTime());
  }

}
