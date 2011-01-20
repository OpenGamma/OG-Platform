/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode.stats;

import java.util.concurrent.atomic.AtomicLong;

import javax.time.Instant;

import com.opengamma.util.ArgumentChecker;

/**
 * Records statistics about nodes.
 * <p>
 * This is run centrally to aggregate statistics.
 * The statistics recorded include the number of successful and unsuccessful jobs.
 * Old data is decayed to be less relevant.
 * <p>
 * This class exhibits complex behavior in a multi-threaded environment.
 * Each piece of state should be considered to be an independent value.
 * This class effectively provides a convenient holder for multiple values.
 * Taking two values and using them together in any way is inadvisable.
 * (The average method do exactly that, but are intended for debugging).
 * In general, the values returned are estimates, and should be treated as such.
 */
public class CalculationNodeStatistics {

  /**
   * The node id.
   */
  private final String _nodeId;
  /**
   * The number of successful jobs.
   */
  private final AtomicLong _successfulJobs = new AtomicLong();
  /**
   * The number of unsuccessful jobs.
   */
  private final AtomicLong _unsuccessfulJobs = new AtomicLong();
  /**
   * The number of job items.
   */
  private final AtomicLong _jobItems = new AtomicLong();
  /**
   * The execution time in nanoseconds.
   */
  private final AtomicLong _executionNanos = new AtomicLong();
  /**
   * The non-execution time in nanoseconds.
   */
  private final AtomicLong _nonExecutionNanos = new AtomicLong();
  /**
   * The last instant that a job was sent.
   */
  private volatile Instant _lastJobInstant;

  /**
   * Creates an instance for a specific node.
   * 
   * @param nodeId  the node id, not null
   */
  public CalculationNodeStatistics(final String nodeId) {
    ArgumentChecker.notNull(nodeId, "nodeId");
    _nodeId = nodeId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the node id.
   * 
   * @return the node id, not null
   */
  public String getNodeId() {
    return _nodeId;
  }

  /**
   * Gets the number of job items.
   * 
   * @return the number of job items, not null
   */
  public long getJobItems() {
    return _jobItems.get();
  }

  /**
   * Gets the number of successful jobs.
   * 
   * @return the number of successful jobs, not null
   */
  public long getSuccessfulJobs() {
    return _successfulJobs.get();
  }

  /**
   * Gets the number of unsuccessful jobs.
   * 
   * @return the number of unsuccessful jobs, not null
   */
  public long getUnsuccessfulJobs() {
    return _unsuccessfulJobs.get();
  }

  /**
   * Gets the execution time in nanoseconds.
   * 
   * @return the execution time, not null
   */
  public long getExecutionTime() {
    return _executionNanos.get();
  }

  /**
   * Gets the non-execution time in nanoseconds.
   * 
   * @return the non-execution time, not null
   */
  public long getNonExecutionTime() {
    return _nonExecutionNanos.get();
  }

  /**
   * Gets the last instant a job ran.
   * 
   * @return the last job instant, null if no job has run
   */
  public Instant getLastJobTime() {
    return _lastJobInstant;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the average execution time in seconds.
   * <p>
   * This method calls two state methods internally without synchonization,
   * thus the calculated result should probably only be used for debugging.
   * 
   * @return the average execution time
   */
  public double getAverageExecutionTime() {
    final long jobs = getSuccessfulJobs();
    if (jobs > 0) {
      return (double) getExecutionTime() / (double) jobs / 1e9;
    } else {
      return 0;
    }
  }

  /**
   * Gets the average non-execution time in seconds.
   * <p>
   * This method calls two state methods internally without synchonization,
   * thus the calculated result should probably only be used for debugging.
   * 
   * @return the average non-execution time
   */
  public double getAverageNonExecutionTime() {
    final long jobs = getSuccessfulJobs();
    if (jobs > 0) {
      return (double) getNonExecutionTime() / (double) jobs / 1e9;
    } else {
      return 0;
    }
  }

  /**
   * Gets the average number of job items.
   * <p>
   * This method calls two state methods internally without synchonization,
   * thus the calculated result should probably only be used for debugging.
   * 
   * @return the average number of job items
   */
  public double getAverageJobItems() {
    final long jobs = getSuccessfulJobs();
    if (jobs > 0) {
      return (double) getJobItems() / (double) jobs;
    } else {
      return 0;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Records a successful job.
   * 
   * @param jobItems  the number of job items
   * @param executionNanos  the execution time in nanoseconds
   * @param durationNanos  the duration in nanoseconds
   */
  public void recordSuccessfulJob(final int jobItems, final long executionNanos, final long durationNanos) {
    _successfulJobs.incrementAndGet();
    _jobItems.addAndGet(jobItems);
    _executionNanos.addAndGet(executionNanos);
    _nonExecutionNanos.addAndGet(durationNanos - executionNanos);
    _lastJobInstant = Instant.now();
  }

  /**
   * Records an unsuccessful job.
   * 
   * @param durationNanos  the duration in nanoseconds
   */
  public void recordUnsuccessfulJob(final long durationNanos) {
    _unsuccessfulJobs.incrementAndGet();
    _nonExecutionNanos.addAndGet(durationNanos);
    _lastJobInstant = Instant.now();
  }

  //-------------------------------------------------------------------------
  /**
   * Resets the counters to zero.
   */
  public void reset() {
    _successfulJobs.set(0);
    _unsuccessfulJobs.set(0);
    _jobItems.set(0);
    _executionNanos.set(0);
    _nonExecutionNanos.set(0);
  }

  /**
   * Decays the values by a specific factor.
   * 
   * @param factor  the factor to decay by
   */
  public void decay(final double factor) {
    decay(_successfulJobs, factor);
    decay(_unsuccessfulJobs, factor);
    decay(_jobItems, factor);
    decay(_executionNanos, factor);
    decay(_nonExecutionNanos, factor);
  }

  private static void decay(final AtomicLong value, final double factor) {
    value.addAndGet(-(long) ((double) value.get() * factor));
  }

  /**
   * Creates a snapshot of the current values.
   * 
   * @return a snapshot, not null
   */
  public CalculationNodeStatistics snapshot() {
    final CalculationNodeStatistics stats = new CalculationNodeStatistics(getNodeId());
    stats.snapshot(this);
    return stats;
  }

  private void snapshot(final CalculationNodeStatistics other) {
    _successfulJobs.set(other.getSuccessfulJobs());
    _unsuccessfulJobs.set(other.getUnsuccessfulJobs());
    _jobItems.set(other.getJobItems());
    _executionNanos.set(other.getExecutionTime());
    _nonExecutionNanos.set(other.getNonExecutionTime());
  }

  /**
   * Converts this instance into a delta to another instance.
   * 
   * @param future  the later instance
   */
  public void delta(final CalculationNodeStatistics future) {
    _successfulJobs.set(future.getSuccessfulJobs() - getSuccessfulJobs());
    _unsuccessfulJobs.set(future.getUnsuccessfulJobs() - getUnsuccessfulJobs());
    _jobItems.set(future.getJobItems() - getJobItems());
    _executionNanos.set(future.getExecutionTime() - getExecutionTime());
    _nonExecutionNanos.set(future.getNonExecutionTime() - getNonExecutionTime());
  }

}
