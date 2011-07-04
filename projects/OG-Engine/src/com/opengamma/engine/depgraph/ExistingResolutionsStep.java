/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/* package */final class ExistingResolutionsStep extends FunctionApplicationStep implements ResolvedValueCallback {

  private static final Logger s_logger = LoggerFactory.getLogger(ExistingResolutionsStep.class);

  private final DependencyGraphBuilder _builder;
  private ResolutionPump _pump;

  public ExistingResolutionsStep(final ResolveTask task, final Iterator<Pair<ParameterizedFunction, ValueSpecification>> nextFunctions, final DependencyGraphBuilder builder,
      final ParameterizedFunction function, final ValueSpecification originalOutput, final ValueSpecification resolvedOutput) {
    super(task, nextFunctions, function, originalOutput, resolvedOutput);
    _builder = builder;
  }

  @Override
  public void failed(final ValueRequirement value) {
    _pump = null;
    // All existing resolutions have been completed, so now try the actual application
    setRunnableTaskState(new FunctionApplicationStep(getTask(), getFunctions(), getFunction(), getOriginalOutput(), getResolvedOutput()), _builder);
  }

  @Override
  public void resolved(final ValueRequirement valueRequirement, final ResolvedValue value, final ResolutionPump pump) {
    pushResult(value);
    _pump = pump;
  }

  @Override
  protected void pump() {
    if (_pump == null) {
      // Either pump called twice for a resolve, called before the first resolve, or after failed
      throw new IllegalStateException();
    } else {
      s_logger.debug("Pumping underlying delegate");
      _pump.pump();
      _pump = null;
    }
  }

  @Override
  public String toString() {
    return "EXISTING_RESOLUTIONS";
  }

}
