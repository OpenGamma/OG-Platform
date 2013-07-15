/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.trigger;

/**
 * Trigger that ensures a cycle is always eligible to run.
 */
public class RunAsFastAsPossibleTrigger implements ViewCycleTrigger {

  @Override
  public ViewCycleTriggerResult query(long cycleTimeNanos) {
    // Just force a delta cycle; other triggers should be responsible for changing the delta into a full cycle
    return new ViewCycleTriggerResult(ViewCycleEligibility.FORCE, ViewCycleType.DELTA);
  }

  @Override
  public void cycleTriggered(long cycleTimeNanos, ViewCycleType cycleType) {
  }

  @Override
  public String toString() {
    return "RunAsFastAsPossibleTrigger[]";
  }

}
