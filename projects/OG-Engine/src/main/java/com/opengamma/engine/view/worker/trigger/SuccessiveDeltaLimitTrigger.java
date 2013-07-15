/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.trigger;

/**
 * Trigger that applies a limit to the number of successive delta cycles before a full cycle is required.
 */
public class SuccessiveDeltaLimitTrigger implements ViewCycleTrigger {

  private final int _maxSuccessiveDeltas;
  private int _successiveDeltaCount;
  
  public SuccessiveDeltaLimitTrigger(int maxSuccessiveDeltas) {
    _maxSuccessiveDeltas = maxSuccessiveDeltas;
  }

  @Override
  public ViewCycleTriggerResult query(long cycleTimeNanos) {
    ViewCycleType type = _successiveDeltaCount >= _maxSuccessiveDeltas ? ViewCycleType.FULL : ViewCycleType.DELTA;
    return new ViewCycleTriggerResult(type);
  }

  @Override
  public void cycleTriggered(long cycleTimeNanos, ViewCycleType cycleType) {
    switch (cycleType) {
      case DELTA:
        _successiveDeltaCount++;
        break;
      case FULL:
        _successiveDeltaCount = 0;
        break;
    }
  }

  @Override
  public String toString() {
    return "SuccessiveDeltaLimitTrigger[maxSuccessiveDeltas=" + _maxSuccessiveDeltas + ", successiveDeltaCount=" + _successiveDeltaCount + "]";
  }

}
