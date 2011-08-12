/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.depgraph.DependencyGraphBuilderPLAT1049.GraphBuildingContext;

/* package */final class ResolveTargetStep extends ResolveTask.State {

  private static final Logger s_logger = LoggerFactory.getLogger(ResolveTargetStep.class);

  public ResolveTargetStep(final ResolveTask task) {
    super(task);
  }

  @Override
  protected void run(final GraphBuildingContext context) {
    final ComputationTargetResolver targetResolver = context.getTargetResolver();
    final ComputationTarget target = targetResolver.resolve(getValueRequirement().getTargetSpecification());
    if (target == null) {
      s_logger.warn("Couldn't resolve target for {}", getValueRequirement());
      context.exception(new UnsatisfiableDependencyGraphException(ResolutionFailure.couldNotResolve(getValueRequirement())));
      setTaskStateFinished(context);
    } else {
      s_logger.debug("Resolved target {}", getValueRequirement().getTargetSpecification());
      final ResolveTask task = getTask();
      task.setComputationTarget(target);
      setRunnableTaskState(new GetFunctionsStep(task), context);
    }
  }

  @Override
  protected boolean isActive() {
    // No background state
    return false;
  }

  @Override
  public String toString() {
    return "RESOLVE" + getObjectId();
  }

}
