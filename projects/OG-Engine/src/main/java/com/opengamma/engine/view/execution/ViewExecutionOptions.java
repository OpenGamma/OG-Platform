/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import java.util.EnumSet;

import com.opengamma.id.VersionCorrection;

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
   * @return the set of flags, not null
   */
  EnumSet<ViewExecutionFlags> getFlags();
  
  /**
   * Gets the maximum number of delta cycles following a full computation cycles.
   * 
   * @return the maximum number of delta cycles following a full computation cycle, null for no limit
   */
  Integer getMaxSuccessiveDeltaCycles();
    
  /**
   * Gets the default execution options.
   * 
   * @return the default execution options, null if not specified
   */
  ViewCycleExecutionOptions getDefaultExecutionOptions();
 
  /**
   * Gets the version-correction to apply during execution.
   * <p>
   * The version-correction affects the data used by the running view process. If the version-correction has either the
   * version or correction set to 'latest' then the view process will track real-time changes to any data on which it
   * depends. 
   * 
   * @return the version-correction, not null
   */
  VersionCorrection getVersionCorrection();
  
}
