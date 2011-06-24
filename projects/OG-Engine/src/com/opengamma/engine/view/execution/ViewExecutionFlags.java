/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import com.opengamma.engine.view.ViewDefinition;

/**
 * Represents options which can apply to a {@link ViewExecutionOptions} instance. These are not necessarily mutually
 * compatible; incorrect combinations may result in execution errors or unexpected behaviour.
 */
public enum ViewExecutionFlags {

  /**
   * Indicates that a computation cycle should be triggered whenever live data inputs change. For example, this could
   * be caused by a market data tick or an alteration to a snapshot.
   */
  TRIGGER_CYCLE_ON_LIVE_DATA_CHANGED,
  
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
   * Indicates that the view definition should be compiled but not executed.
   */
  COMPILE_ONLY;
    
}
