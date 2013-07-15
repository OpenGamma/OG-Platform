/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.trigger;

/**
 * Trigger that indicates a fixed result once a time has passed.
 */
public class FixedTimeTrigger implements ViewCycleTrigger {

  private long _triggerTimeNanos = Long.MAX_VALUE;
  private ViewCycleTriggerResult _nextResult;
  
  @Override
  public ViewCycleTriggerResult query(long cycleTimeNanos) {
    if (cycleTimeNanos >= _triggerTimeNanos) {
      return _nextResult;
    }
    return ViewCycleTriggerResult.nothingUntil(_triggerTimeNanos);
  }

  @Override
  public void cycleTriggered(long cycleTimeNanos, ViewCycleType cycleType) {
  }
  
  public void reset() {
    _triggerTimeNanos = Long.MAX_VALUE;
    _nextResult = null;
  }
  
  public void set(long triggerTimeNanos, ViewCycleTriggerResult nextResult) {
    _triggerTimeNanos = triggerTimeNanos;
    _nextResult = nextResult;
  }

  @Override
  public String toString() {
    return "FixedTimeTrigger[triggerTimeNanos=" + _triggerTimeNanos + ", nextResult=" + _nextResult + "]";
  }

}
