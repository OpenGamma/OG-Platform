/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode.stats;

import org.threeten.bp.Instant;

import com.opengamma.util.ArgumentChecker;

/**
 * Records statistics about nodes.
 * <p>
 * This is run centrally to aggregate statistics.
 * The statistics recorded include the number of successful and unsuccessful jobs.
 * Old data is decayed to be less relevant.
 */
public class CalculationNodeStatistics {

  /**
   * The node id.
   */
  private final String _nodeId;
  /**
   * The number of successful jobs.
   */
  private long _successfulJobs;
  /**
   * The number of unsuccessful jobs.
   */
  private long _unsuccessfulJobs;
  /**
   * The number of job items.
   */
  private long _jobItems;
  /**
   * The execution time in nanoseconds.
   */
  private long _executionNanos;
  /**
   * The non-execution time in nanoseconds.
   */
  private long _nonExecutionNanos;
  /**
   * The last instant that a job was sent.
   */
  private Instant _lastJobInstant;

  /**
   * Creates an instance for a specific node.
   * 
   * @param nodeId  the node id, not null
   */
  public CalculationNodeStatistics(final String nodeId) {
    ArgumentChecker.notNull(nodeId, "nodeId");
    _nodeId = nodeId;
  }

  private CalculationNodeStatistics(final CalculationNodeStatistics other) {
    _nodeId = other.getNodeId();
    // the caller already holds the lock on the other object
    _successfulJobs = other._successfulJobs;
    _unsuccessfulJobs = other._unsuccessfulJobs;
    _executionNanos = other._executionNanos;
    _nonExecutionNanos = other._nonExecutionNanos;
    _lastJobInstant = other._lastJobInstant;
  }

  // -------------------------------------------------------------------------
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
   * @return the number of job items
   */
  public synchronized long getJobItems() {
    return _jobItems;
  }

  /**
   * Gets the number of successful jobs.
   * 
   * @return the number of successful jobs
   */
  public synchronized long getSuccessfulJobs() {
    return _successfulJobs;
  }

  /**
   * Gets the number of unsuccessful jobs.
   * 
   * @return the number of unsuccessful jobs
   */
  public synchronized long getUnsuccessfulJobs() {
    return _unsuccessfulJobs;
  }

  /**
   * Gets the execution time in nanoseconds.
   * 
   * @return the execution time
   */
  public synchronized long getExecutionTime() {
    return _executionNanos;
  }

  /**
   * Gets the non-execution time in nanoseconds. Non-execution time is the time from job dispatch to job completion
   * less the time the node reported as spent working on the job. It is a measure of overhead. A high non-execution
   * to execution ratio could indicate the jobs being dispatched are too small. High non-execution time could also
   * mean a large number of failed jobs; the entire duration of which is considered overhead as the job must be
   * repeated.
   * 
   * @return the non-execution time
   */
  public synchronized long getNonExecutionTime() {
    return _nonExecutionNanos;
  }

  /**
   * Gets the last instant a job ran.
   * 
   * @return the last job instant, null if no job has run
   */
  public synchronized Instant getLastJobTime() {
    return _lastJobInstant;
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the average execution time in seconds.
   * <p>
   * This method is for debugging only. A snapshot should be taken and then
   * analysis on the values and their relationships to each other be used.
   * 
   * @return the average execution time
   */
  public synchronized double getAverageExecutionTime() {
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
   * This method is for debugging only. A snapshot should be taken and then
   * analysis on the values and their relationships to each other be used.
   * 
   * @return the average non-execution time
   */
  public synchronized double getAverageNonExecutionTime() {
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
   * This method is for debugging only. A snapshot should be taken and then
   * analysis on the values and their relationships to each other be used.
   * 
   * @return the average number of job items
   */
  public synchronized double getAverageJobItems() {
    final long jobs = getSuccessfulJobs();
    if (jobs > 0) {
      return (double) getJobItems() / (double) jobs;
    } else {
      return 0;
    }
  }

  // -------------------------------------------------------------------------
  /**
   * Records a successful job.
   * 
   * @param jobItems  the number of job items
   * @param executionNanos  the execution time in nanoseconds
   * @param durationNanos  the duration in nanoseconds
   */
  public synchronized void recordSuccessfulJob(final int jobItems, final long executionNanos, final long durationNanos) {
    _successfulJobs++;
    _jobItems += jobItems;
    _executionNanos += executionNanos;
    _nonExecutionNanos += (durationNanos - executionNanos);
    _lastJobInstant = Instant.now();
  }

  /**
   * Records an unsuccessful job.
   * 
   * @param durationNanos  the duration in nanoseconds
   */
  public synchronized void recordUnsuccessfulJob(final long durationNanos) {
    _unsuccessfulJobs++;
    _nonExecutionNanos += durationNanos;
    _lastJobInstant = Instant.now();
  }

  // -------------------------------------------------------------------------
  /**
   * Resets the counters to zero.
   */
  public synchronized void reset() {
    _successfulJobs = 0;
    _unsuccessfulJobs = 0;
    _jobItems = 0;
    _executionNanos = 0;
    _nonExecutionNanos = 0;
  }

  /**
   * Decays the values by a specific factor.
   * 
   * @param factor  the factor to decay by
   */
  public synchronized void decay(final double factor) {
    _successfulJobs -= ((double) _successfulJobs * factor);
    _unsuccessfulJobs -= ((double) _unsuccessfulJobs * factor);
    _jobItems -= ((double) _jobItems * factor);
    _executionNanos -= ((double) _executionNanos * factor);
    _nonExecutionNanos -= ((double) _nonExecutionNanos * factor);
  }

  /**
   * Creates a snapshot of the current values.
   * 
   * @return a snapshot, not null
   */
  public synchronized CalculationNodeStatistics snapshot() {
    return new CalculationNodeStatistics(this);
  }

}
