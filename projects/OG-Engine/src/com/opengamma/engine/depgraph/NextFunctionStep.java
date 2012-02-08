/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.depgraph.DependencyGraphBuilder.GraphBuildingContext;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/* package */class NextFunctionStep extends ResolveTask.State {

  private static final Logger s_logger = LoggerFactory.getLogger(NextFunctionStep.class);

  private final Iterator<Pair<ParameterizedFunction, ValueSpecification>> _functions;

  public NextFunctionStep(final ResolveTask task, final Iterator<Pair<ParameterizedFunction, ValueSpecification>> functions) {
    super(task);
    assert functions != null;
    _functions = functions;
  }

  protected Iterator<Pair<ParameterizedFunction, ValueSpecification>> getFunctions() {
    return _functions;
  }

  @Override
  protected void run(final GraphBuildingContext context) {
    if (!getFunctions().hasNext()) {
      s_logger.info("No more functions for {}", getValueRequirement());
      setTaskStateFinished(context);
      return;
    }
    final Pair<ParameterizedFunction, ValueSpecification> resolvedFunction = getFunctions().next();
    s_logger.debug("Considering {} for {}", resolvedFunction, getValueRequirement());
    final ValueSpecification originalOutput = resolvedFunction.getSecond();
    final ValueSpecification resolvedOutput = originalOutput.compose(getValueRequirement());
    final Map<ResolveTask, ResolvedValueProducer> existingTasks = context.getTasksProducing(resolvedOutput);
    if (existingTasks.isEmpty()) {
      // We're going to work on producing
      s_logger.debug("Creating producer for {} (original={})", resolvedOutput, originalOutput);
      final FunctionApplicationStep state = new FunctionApplicationStep(getTask(), getFunctions(), resolvedFunction.getFirst(), originalOutput, resolvedOutput);
      setRunnableTaskState(state, context);
    } else {
      // Other tasks are working on it, or have already worked on it
      s_logger.debug("Delegating to existing producers for {} (original={})", resolvedOutput, originalOutput);
      final ExistingResolutionsStep state = new ExistingResolutionsStep(getTask(), getFunctions(), resolvedFunction.getFirst(), originalOutput, resolvedOutput);
      setTaskState(state);
      ResolvedValueProducer singleTask = null;
      AggregateResolvedValueProducer aggregate = null;
      // Must not to introduce a loop (checking parent resolve tasks isn't sufficient) so only use "finished" tasks.
      for (Map.Entry<ResolveTask, ResolvedValueProducer> existingTask : existingTasks.entrySet()) {
        if (existingTask.getKey().isFinished()) {
          // Can use this task without creating a loop
          if (singleTask == null) {
            singleTask = existingTask.getValue();
            singleTask.addRef();
          } else {
            if (aggregate == null) {
              aggregate = new AggregateResolvedValueProducer(getValueRequirement());
              aggregate.addProducer(context, singleTask);
              singleTask.release(context);
            }
            aggregate.addProducer(context, existingTask.getValue());
          }
        }
        // Only the values are ref-counted
        existingTask.getValue().release(context);
      }
      if (aggregate != null) {
        aggregate.addCallback(context, state);
        aggregate.start(context);
        aggregate.release(context);
      } else {
        if (singleTask != null) {
          singleTask.addCallback(context, state);
          singleTask.release(context);
        } else {
          state.failed(context, getValueRequirement(), ResolutionFailure.recursiveRequirement(getValueRequirement()));
        }
      }
    }
  }

  @Override
  protected boolean isActive() {
    // Won't do anything unless {@link #run} is called
    return false;
  }

  @Override
  public String toString() {
    return "NEXT_FUNCTION" + getObjectId();
  }

}
