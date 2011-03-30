/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

/**
 * Encapsulates settings affecting the overall execution of a view process.
 */
public interface ViewExecutionOptions {
  
  /**
   * The real-time input data source
   */
  String REAL_TIME_DATA_SOURCE = "RealTime";
  
  /**
   * Gets the cycle execution sequence.
   * 
   * @return the cycle execution sequence, not null
   */
  ViewCycleExecutionSequence getExecutionSequence();
  
  /**
   * Indicates whether the view should run as fast as possible, perhaps faster than any constraints specified in the
   * {@link ViewDefinition}, and perhaps running multiple computation cycles concurrently.
   * <p>
   * This might make sense for batch jobs where the evaluation times are independent of the current time.
   * 
   * @return {@code true} if the view should run as fast as possible, {@code false} otherwise.
   */
  boolean isRunAsFastAsPossible();
  
  /**
   * Gets whether live data ticks can trigger computation cycles. This requires support for subscriptions from the
   * {@link LiveDataSnapshotProvider}.
   * <p>
   * This setting may be ignored if other settings would cause computation cycles to run even faster.
   * 
   * @return {@code true} if live data ticks can trigger computation cycles, {@code false} otherwise.
   */
  boolean isLiveDataTriggerEnabled();
  
  /**
   * Gets the maximum number of delta cycles following a full computation cycles.
   * 
   * @return the maximum number of delta cycles following a full computation cycle, or {@code null} for no limit
   */
  Integer getMaxSuccessiveDeltaCycles();
  
}
