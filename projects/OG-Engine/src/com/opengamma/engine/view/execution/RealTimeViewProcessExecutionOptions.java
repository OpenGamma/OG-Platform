/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import javax.time.Instant;

/**
 * Implements the settings required for real-time view process execution.
 */
public final class RealTimeViewProcessExecutionOptions implements ViewProcessExecutionOptions {

  /**
   * Singleton instance
   */
  public static final RealTimeViewProcessExecutionOptions INSTANCE = new RealTimeViewProcessExecutionOptions();
  
  private static final String REAL_TIME_INPUT_DATA_SOURCE = "RealTime";
  
  private static ViewEvaluationTimeSequence s_timeSequence = new ViewEvaluationTimeSequence() {
    
    @Override
    public boolean isEmpty() {
      return false;
    }
    
    @Override
    public Instant getNextEvaluationTime() {
      return Instant.now();
    }
    
  };
  
  @Override
  public ViewEvaluationTimeSequence getEvaluationTimeSequence() {
    return s_timeSequence;
  }

  @Override
  public boolean isRunAsFastAsPossible() {
    return false;
  }

  @Override
  public Integer getMaxSuccessiveDeltaCycles() {
    return null;
  }

  @Override
  public String getInputDataSource() {
    return REAL_TIME_INPUT_DATA_SOURCE;
  }
  
  /**
   * Private constructor
   */
  private RealTimeViewProcessExecutionOptions() {
  }
  
  @Override
  public int hashCode() {
    return 1;
  }
  
  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    return (other instanceof RealTimeViewProcessExecutionOptions);
  }

}
