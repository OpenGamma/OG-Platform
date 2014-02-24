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
   * Gets the maximum time to wait, in milliseconds, for market data to become available when
   * {@link ViewExecutionFlags#AWAIT_MARKET_DATA} is in use.
   * 
   * @return the maximum time to wait for market data to become available, null to use a default timeout.
   *         If less than or equal to zero, the effect is not to wait at all.
   */
  Long getMarketDataTimeoutMillis();

  /**
   * Gets the default execution options. If the cycles in the execution sequence omit values from their execution options they will typically inherit these defaults
   * 
   * @return the default execution options, null if not specified
   */
  ViewCycleExecutionOptions getDefaultExecutionOptions();
 
}
