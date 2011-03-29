/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

/**
 * Encapsulates the way in which a view should be executed.
 */
public interface ViewProcessExecutionOptions {
  
  /**
   * Gets the evaluation time sequence.
   * 
   * @return the evaluation time sequence, not null
   */
  ViewEvaluationTimeSequence getEvaluationTimeSequence();
  
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
   * Gets the maximum number of delta cycles following a full computation cycles.
   * 
   * @return the maximum number of delta cycles following a full computation cycle, or {@code null} for no limit
   */
  Integer getMaxSuccessiveDeltaCycles();
  
  /**
   * Gets the name of the input data source
   * 
   * @return the name of the input data source
   */
  String getInputDataSource();
  
}
