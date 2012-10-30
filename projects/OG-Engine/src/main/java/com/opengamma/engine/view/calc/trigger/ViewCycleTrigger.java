/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc.trigger;

/**
 * Represents a trigger condition for view cycles which will be queried whenever a view process is considering
 * whether to perform a cycle.
 */
public interface ViewCycleTrigger {

  /**
   * Queries the trigger.
   * 
   * @param cycleTimeNanos  the nano time to be associated with the cycle
   * @return the trigger result, not null
   */
  ViewCycleTriggerResult query(long cycleTimeNanos);

  /**
   * Called to indicate that a cycle has been triggered.
   * 
   * @param cycleTimeNanos  the nano time associated with the cycle, for elapsed time calculations with future cycles
   * @param cycleType  the type of cycle triggered, not null
   */
  void cycleTriggered(long cycleTimeNanos, ViewCycleType cycleType);
  
}
