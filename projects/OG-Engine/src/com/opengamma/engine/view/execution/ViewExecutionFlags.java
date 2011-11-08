/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.util.PublicAPI;

/**
 * Represents options which can apply to a {@link ViewExecutionOptions} instance. These are not necessarily mutually
 * compatible; incorrect combinations may result in execution errors or unexpected behaviour.
 */
@PublicAPI
public enum ViewExecutionFlags {

  /**
   * Indicates that all market data should be present before a cycle is allowed to run.
   */
  AWAIT_MARKET_DATA,
  
  /**
   * Indicates that a computation cycle should be triggered whenever market data inputs change.
   */
  TRIGGER_CYCLE_ON_MARKET_DATA_CHANGED,
  
  /**
   * Indicates that a computation cycle should be triggered after a certain time period has elapsed since the last
   * cycle, as configured in the {@link ViewDefinition}.
   */
  TRIGGER_CYCLE_ON_TIME_ELAPSED,
  
  /**
   * Indicates that the execution sequence should proceed as fast as possible, ignoring any minimum elapsed time
   * between cycles specified in the view definition, and possibly executing cycles concurrently.
   */
  RUN_AS_FAST_AS_POSSIBLE,

  /**
   * Indicates that the execution sequence should wait for an initial trigger; e.g. market data changes (if
   * {@link #TRIGGER_CYCLE_ON_MARKET_DATA_CHANGED} is set), a time elapse (if {@link #TRIGGER_CYCLE_ON_TIME_ELAPSED}
   * is set), or a manual trigger.
   */
  WAIT_FOR_INITIAL_TRIGGER,

  /**
   * Indicates that the view definition should be compiled but not executed.
   */
  COMPILE_ONLY,

  /**
   * Indicates that the results should be stored in batch database.
   */
  BATCH;
    
}
