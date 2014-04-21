/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Triple;

/* package */final class ExistingResolutionsStep extends FunctionApplicationStep implements ResolvedValueCallback {

  private static final Logger s_logger = LoggerFactory.getLogger(ExistingResolutionsStep.class);

  private ResolutionPump _pump;

  /**
   * Creates a new instance.
   * <p>
   * The {@code resolvedOutput} parameter must be normalized.
   * 
   * @param task the resolve task this step is part of, not null
   * @param base the superclass data, not null
   * @param resolved the resolved function information, not null
   * @param resolvedOutput the provisional resolved value specification, not null
   */
  public ExistingResolutionsStep(final ResolveTask task, final FunctionIterationStep.IterationBaseStep base,
      final Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> resolved, final ValueSpecification resolvedOutput) {
    super(task, base, resolved, resolvedOutput);
  }

  @Override
  public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
    s_logger.debug("Failed to resolve {} from {}", value, this);
    // Don't store the failures from trying the existing ones; they might be for different value requirements that we've just piggy-backed onto
    // All existing resolutions have been completed, so now try the actual application
    setRunnableTaskState(new FunctionApplicationStep(getTask(), getIterationBase(), getResolved(), getResolvedOutput()), context);
  }

  @Override
  public void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue value, final ResolutionPump pump) {
    s_logger.debug("Resolved {} from {}", value, this);
    if (pump != null) {
      synchronized (this) {
        _pump = pump;
      }
      if (!pushResult(context, value, false)) {
        synchronized (this) {
          assert _pump == pump;
          _pump = null;
        }
        context.pump(pump);
      }
    } else {
      // We don't have a state to pump as this is the producer's last result, but this isn't the last we'll do
      synchronized (this) {
        _pump = ResolutionPump.Dummy.INSTANCE;
      }
      if (!pushResult(context, value, false)) {
        context.failed(this, valueRequirement, null);
      }
    }
  }

  @Override
  public void recursionDetected() {
    // No-op
  }

  @Override
  protected void pump(final GraphBuildingContext context) {
    final ResolutionPump pump;
    synchronized (this) {
      pump = _pump;
      _pump = null;
    }
    if (pump == null) {
      // Rogue pump -- see PumpingState.finished for an explanation
      return;
    }
    if (pump != ResolutionPump.Dummy.INSTANCE) {
      s_logger.debug("Pumping underlying delegate");
      context.pump(pump);
    } else {
      // All existing resolutions have been completed, so now try the actual application
      setRunnableTaskState(new FunctionApplicationStep(getTask(), getIterationBase(), getResolved(), getResolvedOutput()), context);
    }
  }

  @Override
  protected void discard(final GraphBuildingContext context) {
    final ResolutionPump pump;
    synchronized (this) {
      pump = _pump;
      _pump = null;
    }
    if (pump != null) {
      context.close(pump);
    }
  }

  @Override
  public String toString() {
    return "EXISTING_RESOLUTIONS" + getObjectId();
  }

}
