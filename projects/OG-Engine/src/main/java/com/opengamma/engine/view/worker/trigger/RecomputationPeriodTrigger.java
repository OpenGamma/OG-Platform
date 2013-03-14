/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.trigger;

import com.google.common.base.Supplier;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * Trigger that honours the minimum and maximum recomputation periods in a view definition.
 */
public class RecomputationPeriodTrigger implements ViewCycleTrigger {

  private static final long NANOS_PER_MILLISECOND = 1000000;

  private final Supplier<ViewDefinition> _viewDefinition;

  private long _eligibleForDeltaComputationFromNanos = Long.MIN_VALUE;
  private long _deltaComputationRequiredByNanos = Long.MIN_VALUE;
  private long _eligibleForFullComputationFromNanos = Long.MIN_VALUE;
  private long _fullComputationRequiredByNanos = Long.MIN_VALUE;

  public RecomputationPeriodTrigger(Supplier<ViewDefinition> viewDefinition) {
    ArgumentChecker.notNull(viewDefinition, "viewComputationJob");
    _viewDefinition = viewDefinition;
  }

  @Override
  public ViewCycleTriggerResult query(long cycleTimeNanos) {
    if (_fullComputationRequiredByNanos < cycleTimeNanos) {
      return new ViewCycleTriggerResult(ViewCycleEligibility.FORCE, ViewCycleType.FULL);
    }
    if (_deltaComputationRequiredByNanos < cycleTimeNanos) {
      return new ViewCycleTriggerResult(ViewCycleEligibility.FORCE, ViewCycleType.DELTA);
    }

    long nanosWhenRequired = Math.min(_fullComputationRequiredByNanos, _deltaComputationRequiredByNanos);
    if (_eligibleForFullComputationFromNanos < cycleTimeNanos) {
      return new ViewCycleTriggerResult(ViewCycleEligibility.ELIGIBLE, ViewCycleType.FULL, nanosWhenRequired);
    }
    if (_eligibleForDeltaComputationFromNanos < cycleTimeNanos) {
      return new ViewCycleTriggerResult(ViewCycleEligibility.ELIGIBLE, ViewCycleType.DELTA, nanosWhenRequired);
    }

    long nanosWhenEligible = Math.min(_eligibleForDeltaComputationFromNanos, _eligibleForFullComputationFromNanos);
    return ViewCycleTriggerResult.preventUntil(nanosWhenEligible);
  }

  @Override
  public void cycleTriggered(long cycleTimeNanos, ViewCycleType cycleType) {
    updateComputationTimes(cycleTimeNanos, cycleType == ViewCycleType.DELTA);
  }

  @Override
  public String toString() {
    return "RecomputationPeriodTrigger[eligibleForDeltaFrom=" + _eligibleForDeltaComputationFromNanos +
        ", deltaRequiredBy=" + _deltaComputationRequiredByNanos + ", eligibleForFullFrom=" + _eligibleForFullComputationFromNanos +
        ", _fullRequiredBy=" + _fullComputationRequiredByNanos + "]";
  }

  //-------------------------------------------------------------------------
  private ViewDefinition getViewDefinition() {
    return _viewDefinition.get();
  }

  private void updateComputationTimes(long currentNanos, boolean deltaOnly) {
    _eligibleForDeltaComputationFromNanos = getUpdatedTime(currentNanos, getViewDefinition().getMinDeltaCalculationPeriod(), 0);
    _deltaComputationRequiredByNanos = getUpdatedTime(currentNanos, getViewDefinition().getMaxDeltaCalculationPeriod(), Long.MAX_VALUE);

    if (!deltaOnly) {
      _eligibleForFullComputationFromNanos = getUpdatedTime(currentNanos, getViewDefinition().getMinFullCalculationPeriod(), 0);
      _fullComputationRequiredByNanos = getUpdatedTime(currentNanos, getViewDefinition().getMaxFullCalculationPeriod(), Long.MAX_VALUE);
    }
  }

  private long getUpdatedTime(long currentNanos, Long computationPeriod, long nullEquivalent) {
    if (computationPeriod == null) {
      return nullEquivalent;
    }
    long result = currentNanos + NANOS_PER_MILLISECOND * computationPeriod;
    // Check for overflow
    return result < currentNanos ? Long.MAX_VALUE : result;
  }

}
