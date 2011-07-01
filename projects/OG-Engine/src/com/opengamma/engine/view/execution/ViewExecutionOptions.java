/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import java.util.EnumSet;

/**
 * Encapsulates settings affecting the overall execution of a view process.
 */
public interface ViewExecutionOptions {
  
  /**
   * Gets the cycle execution sequence.
   * 
   * @return the cycle execution sequence, not null
   */
  ViewCycleExecutionSequence getExecutionSequence();
  
  /**
   * Gets the set of flags defining aspects of the execution behaviour in relation to the execution options.
   * 
   * @return the set of flags, not {@code null}
   */
  EnumSet<ViewExecutionFlags> getFlags();
  
  /**
   * Gets the maximum number of delta cycles following a full computation cycles.
   * 
   * @return the maximum number of delta cycles following a full computation cycle, or {@code null} for no limit
   */
  Integer getMaxSuccessiveDeltaCycles();
    
  /**
   * Gets the default execution options.
   * 
   * @return the default execution options, or {@code null} if not specified
   */
  ViewCycleExecutionOptions getDefaultExecutionOptions();
  
}
