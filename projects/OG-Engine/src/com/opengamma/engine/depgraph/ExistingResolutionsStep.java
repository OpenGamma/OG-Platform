/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.depgraph.DependencyGraphBuilder.GraphBuildingContext;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/* package */final class ExistingResolutionsStep extends FunctionApplicationStep implements ResolvedValueCallback {

  private static final Logger s_logger = LoggerFactory.getLogger(ExistingResolutionsStep.class);

  private ResolutionPump _pump;

  public ExistingResolutionsStep(final ResolveTask task, final Iterator<Pair<ParameterizedFunction, ValueSpecification>> nextFunctions, final ParameterizedFunction function,
      final ValueSpecification originalOutput, final ValueSpecification resolvedOutput) {
    super(task, nextFunctions, function, originalOutput, resolvedOutput);
  }

  @Override
  public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
    s_logger.debug("Failed to resolve {} from {}", value, this);
    storeFailure(failure);
    synchronized (this) {
      _pump = null;
    }
    // All existing resolutions have been completed, so now try the actual application
    setRunnableTaskState(new FunctionApplicationStep(getTask(), getFunctions(), getFunction(), getOriginalOutput(), getResolvedOutput()), context);
  }

  @Override
  public void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue value, final ResolutionPump pump) {
    s_logger.debug("Resolved {} from {}", value, this);
    synchronized (this) {
      _pump = pump;
    }
    if (!pushResult(context, value)) {
      synchronized (this) {
        assert _pump == pump;
        _pump = null;
      }
      context.pump(pump);
    }
  }

  @Override
  protected void pump(final GraphBuildingContext context) {
    final ResolutionPump pump;
    synchronized (this) {
      pump = _pump;
      _pump = null;
    }
    if (pump == null) {
      // Either pump called twice for a resolve, called before the first resolve, or after failed
      throw new IllegalStateException();
    } else {
      s_logger.debug("Pumping underlying delegate");
      context.pump(pump);
    }
  }

  @Override
  public String toString() {
    return "EXISTING_RESOLUTIONS" + getObjectId();
  }

}
