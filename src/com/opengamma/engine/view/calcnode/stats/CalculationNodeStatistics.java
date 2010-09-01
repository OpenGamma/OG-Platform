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

  public void recordSuccessfulJob(final int jobItems, final int jobCycleCost, final long executionTime, final long duration) {
    _successfulJobs.incrementAndGet();
    _jobItems.addAndGet((long) jobItems);
    _jobCycleCost.addAndGet((long) jobCycleCost);
    _executionTime.addAndGet(executionTime);
    _nonExecutionTime.addAndGet(duration - executionTime);
    _lastJobTime = Instant.nowSystemClock();
  }

  public void recordUnsuccessfulJob(final long duration) {
    _unsuccessfulJobs.incrementAndGet();
    _nonExecutionTime.addAndGet(duration);
    _lastJobTime = Instant.nowSystemClock();
  }

}
