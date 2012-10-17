/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroup;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/* package */class NextFunctionStep extends ResolveTask.State {

  private static final Logger s_logger = LoggerFactory.getLogger(NextFunctionStep.class);

  private final Iterator<Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>>> _functions;

  public NextFunctionStep(final ResolveTask task, final Iterator<Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>>> functions) {
    super(task);
    assert functions != null;
    _functions = functions;
  }

  protected Iterator<Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>>> getFunctions() {
    return _functions;
  }

  protected Set<FunctionExclusionGroup> getFunctionExclusion(final GraphBuildingContext context, final CompiledFunctionDefinition function) {
    final Set<FunctionExclusionGroup> parentExclusion = getTask().getFunctionExclusion();
    if (parentExclusion != null) {
      final FunctionExclusionGroup functionExclusion = context.getFunctionExclusionGroups().getExclusionGroup(function.getFunctionDefinition());
      if (functionExclusion != null) {
        final Set<FunctionExclusionGroup> result = Sets.newHashSetWithExpectedSize(parentExclusion.size() + 1);
        result.addAll(parentExclusion);
        result.add(functionExclusion);
        return result;
      } else {
        return parentExclusion;
      }
    } else {
      final FunctionExclusionGroups groups = context.getFunctionExclusionGroups();
      if (groups != null) {
        final FunctionExclusionGroup functionExclusion = groups.getExclusionGroup(function.getFunctionDefinition());
        if (functionExclusion != null) {
          return Collections.singleton(functionExclusion);
        } else {
          return null;
        }
      } else {
        return null;
      }
    }
  }

  @Override
  protected boolean run(final GraphBuildingContext context) {
    if (!getFunctions().hasNext()) {
      s_logger.info("No more functions for {}", getValueRequirement());
      setTaskStateFinished(context);
      return true;
    }
    final Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> resolvedFunction = getFunctions().next();
    if (getTask().getFunctionExclusion() != null) {
      final FunctionExclusionGroup exclusion = context.getFunctionExclusionGroups().getExclusionGroup(resolvedFunction.getFirst().getFunction().getFunctionDefinition());
      if ((exclusion != null) && getTask().getFunctionExclusion().contains(exclusion)) {
        s_logger.debug("Ignoring {} from exclusion group {}", resolvedFunction, exclusion);
        setRunnableTaskState(this, context);
        return true;
      }
    }
    s_logger.debug("Considering {} for {}", resolvedFunction, getValueRequirement());
    final ValueSpecification originalOutput = resolvedFunction.getSecond();
    ValueSpecification resolvedOutput = originalOutput.compose(getValueRequirement());
    if (resolvedOutput != originalOutput) {
      resolvedOutput = context.simplifyType(resolvedOutput);
    }
    final Pair<ResolveTask[], ResolvedValueProducer[]> existing = context.getTasksProducing(resolvedOutput);
    if (existing == null) {
      final ResolvedValue existingValue = context.getProduction(resolvedOutput);
      if (existingValue == null) {
        // We're going to work on producing
        s_logger.debug("Creating producer for {} (original={})", resolvedOutput, originalOutput);
        final FunctionApplicationStep state = new FunctionApplicationStep(getTask(), getFunctions(), resolvedFunction, resolvedOutput);
        setRunnableTaskState(state, context);
      } else {
        // Value has already been produced
        s_logger.debug("Using existing production of {} (original={})", resolvedOutput, originalOutput);
        final ExistingProductionStep state = new ExistingProductionStep(getTask(), getFunctions(), resolvedFunction, resolvedOutput);
        setTaskState(state);
        if (!pushResult(context, existingValue, false)) {
          s_logger.debug("Production not accepted - rescheduling");
          setRunnableTaskState(this, context);
        }
      }
    } else {
      // Other tasks are working on it, or have already worked on it
      s_logger.debug("Delegating to existing producers for {} (original={})", resolvedOutput, originalOutput);
      final ExistingResolutionsStep state = new ExistingResolutionsStep(getTask(), getFunctions(), resolvedFunction, resolvedOutput);
      setTaskState(state);
      ResolvedValueProducer singleTask = null;
      AggregateResolvedValueProducer aggregate = null;
      // Must not to introduce a loop (checking parent resolve tasks isn't sufficient) so only use "finished" tasks.
      final ResolveTask[] existingTasks = existing.getFirst();
      final ResolvedValueProducer[] existingProducers = existing.getSecond();
      for (int i = 0; i < existingTasks.length; i++) {
        if (existingTasks[i].isFinished()) {
          // Can use this task without creating a loop
          if (singleTask == null) {
            singleTask = existingProducers[i];
            singleTask.addRef();
          } else {
            if (aggregate == null) {
              aggregate = new AggregateResolvedValueProducer(getValueRequirement());
              aggregate.addProducer(context, singleTask);
              singleTask.release(context);
            }
            aggregate.addProducer(context, existingProducers[i]);
          }
        }
        // Only the producers are ref-counted
        existingProducers[i].release(context);
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
          state.failed(context, getValueRequirement(), context.recursiveRequirement(getValueRequirement()));
        }
      }
    }
    return true;
  }

  @Override
  protected boolean isActive() {
    // Won't do anything unless {@link #tryRun} is called
    return false;
  }

  @Override
  public String toString() {
    return "NEXT_FUNCTION" + getObjectId();
  }

}
