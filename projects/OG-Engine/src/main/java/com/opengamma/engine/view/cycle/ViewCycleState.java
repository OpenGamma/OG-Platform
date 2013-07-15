/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cycle;

/**
 * Enumerates the states of a {@link ViewCycle}.
 */
public enum ViewCycleState {
  
  /**
   * The computation cycle is new and awaiting execution.
   */
  AWAITING_EXECUTION,
  
  /**
   * The computation cycle is being executed.
   */
  EXECUTING,
  
  /**
   * The computation cycle was once executing, but this was interrupted. Results may be present but should not be used.
   */
  EXECUTION_INTERRUPTED,
  
  /**
   * The computation cycle has finished executing and may be queried for results.
   */
  EXECUTED,
  
  /**
   * The computation cycle has been destroyed, releasing any associated resources. It is no longer queryable for
   * results and should be discarded.
   */
  DESTROYED
  
}
