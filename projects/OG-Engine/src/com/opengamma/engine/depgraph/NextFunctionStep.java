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
  protected void run(final DependencyGraphBuilder builder) {
    if (!getFunctions().hasNext()) {
      s_logger.info("No more functions for {}", getValueRequirement());
      setTaskStateFinished();
      return;
    }
    final Pair<ParameterizedFunction, ValueSpecification> resolvedFunction = getFunctions().next();
    s_logger.debug("Considering {} for {}", resolvedFunction, getValueRequirement());
    final ValueSpecification originalOutput = resolvedFunction.getSecond();
    final ValueSpecification resolvedOutput = originalOutput.compose(getValueRequirement());
    final Map<ResolveTask, ResolvedValueProducer> existingTasks = builder.getTasksProducing(resolvedOutput);
    if (existingTasks.isEmpty()) {
      // We're going to work on producing
      final FunctionApplicationStep state = new FunctionApplicationStep(getTask(), getFunctions(), resolvedFunction.getFirst(), originalOutput, resolvedOutput);
      setRunnableTaskState(state, builder);
    } else {
      // Other tasks are working on it, or have already worked on it
      final AggregateResolvedValueProducer aggregate = new AggregateResolvedValueProducer(getValueRequirement());
      for (Map.Entry<ResolveTask, ResolvedValueProducer> existingTask : existingTasks.entrySet()) {
        if (getTask().hasParent(existingTask.getKey())) {
          // Can't use this task; there would be a loop
          continue;
        }
        aggregate.addProducer(existingTask.getValue());
      }
      aggregate.start();
      final ExistingResolutionsStep state = new ExistingResolutionsStep(getTask(), getFunctions(), builder, resolvedFunction.getFirst(), originalOutput, resolvedOutput);
      aggregate.addCallback(state);
      setTaskState(state);
    }
  }

  @Override
  public String toString() {
    return "NEXT_FUNCTION";
  }

}
