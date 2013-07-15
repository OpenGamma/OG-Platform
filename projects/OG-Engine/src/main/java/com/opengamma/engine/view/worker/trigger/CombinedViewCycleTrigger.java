/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.trigger;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Combines multiple {@link ViewCycleTrigger} instances and applies rules to form an overall trigger result.
 */
public class CombinedViewCycleTrigger implements ViewCycleTrigger {

  private static final Logger s_logger = LoggerFactory.getLogger(CombinedViewCycleTrigger.class);
  
  private List<ViewCycleTrigger> _triggers = new ArrayList<ViewCycleTrigger>();
  
  @Override
  public ViewCycleTriggerResult query(long cycleTimeNanos) {
    s_logger.debug("Querying triggers for cycle time {}", cycleTimeNanos);
    // Use a 'not bothered' result as the base which neither prevents nor forces a cycle and indicates no future state change
    ViewCycleTriggerResult mergedResult = new ViewCycleTriggerResult(ViewCycleEligibility.ELIGIBLE, ViewCycleType.DELTA, Long.MAX_VALUE);
    for (ViewCycleTrigger trigger : _triggers) {
      ViewCycleTriggerResult triggerResult = trigger.query(cycleTimeNanos);
      s_logger.debug("Trigger {} returned result {}", trigger, triggerResult);
      mergedResult = mergeTriggerResults(mergedResult, triggerResult);
    }
    return mergedResult;
  }

  @Override
  public void cycleTriggered(long cycleTimeNanos, ViewCycleType cycleType) {
    for (ViewCycleTrigger trigger : _triggers) {
      trigger.cycleTriggered(cycleTimeNanos, cycleType);
    }
  }
  
  //-------------------------------------------------------------------------
  public void addTrigger(ViewCycleTrigger trigger) {
    _triggers.add(trigger);
  }
  
  //-------------------------------------------------------------------------
  private ViewCycleTriggerResult mergeTriggerResults(ViewCycleTriggerResult a, ViewCycleTriggerResult b) {
    if (a == null) {
      return b;
    }
    if (b == null) {
      return a;
    }
    ViewCycleEligibility mergedCycleEligibility = ViewCycleEligibility.merge(a.getCycleEligibility(), b.getCycleEligibility());
    ViewCycleType mergedCycleType = ViewCycleType.merge(a.getCycleType(), b.getCycleType());
    Long mergedNextStateChangeNanos = mergeNextStateChangeNanos(a, b);
    return new ViewCycleTriggerResult(mergedCycleEligibility, mergedCycleType, mergedNextStateChangeNanos);
  }

  private Long mergeNextStateChangeNanos(ViewCycleTriggerResult a, ViewCycleTriggerResult b) {
    if (a.getNextStateChangeNanos() == null) {
      return b.getNextStateChangeNanos();
    }
    if (b.getNextStateChangeNanos() == null) {
      return a.getNextStateChangeNanos();
    }
    return Math.min(a.getNextStateChangeNanos(), b.getNextStateChangeNanos());
  }
  
}
