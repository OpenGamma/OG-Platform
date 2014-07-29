/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroup;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * Base class for steps that are based on iterating through a collection of candidate functions.
 * 
 * @param <T> the root iteration type
 */
/* package */abstract class FunctionIterationStep extends ResolveTask.State {

  private static final Logger s_logger = LoggerFactory.getLogger(FunctionIterationStep.class);

  public abstract static class IterationBaseStep extends ResolveTask.State {

    public IterationBaseStep(ResolveTask task) {
      super(task);
    }

    /**
     * Attempts a function application.
     * <p>
     * The {@code resolvedOutput} value must be normalized.
     * 
     * @param context the graph building context, not null
     * @param resolvedOutput the provisional resolved value specification, not null
     * @param resolvedFunction the function to apply, containing the definition, satisfying maximal specification, and all maximal output specifications
     */
    protected void functionApplication(final GraphBuildingContext context, final ValueSpecification resolvedOutput,
        final Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> resolvedFunction) {
      final Pair<ResolveTask[], ResolvedValueProducer[]> existing = context.getTasksProducing(resolvedOutput);
      if (existing == null) {
        final ResolvedValue existingValue = context.getProduction(resolvedOutput);
        if (existingValue == null) {
          // We're going to work on producing
          s_logger.debug("Creating producer for {}", resolvedOutput);
          setRunnableTaskState(new FunctionApplicationStep(getTask(), this, resolvedFunction, resolvedOutput), context);
        } else {
          // Value has already been produced
          s_logger.debug("Using existing production of {}", resolvedOutput);
          final ResolveTask.State state = new ExistingProductionStep(getTask(), this, resolvedFunction, resolvedOutput);
          if (setTaskState(state)) {
            if (!pushResult(context, existingValue, false)) {
              s_logger.debug("Production not accepted - rescheduling");
              state.setRunnableTaskState(this, context);
            }
          }
        }
      } else {
        // Other tasks are working on it, or have already worked on it
        s_logger.debug("Delegating to existing producers for {}", resolvedOutput);
        final ExistingResolutionsStep state = new ExistingResolutionsStep(getTask(), this, resolvedFunction, resolvedOutput);
        if (setTaskState(state)) {
          ResolvedValueProducer singleTask = null;
          AggregateResolvedValueProducer aggregate = null;
          // Must not introduce a loop (checking parent resolve tasks isn't sufficient) so only use "finished" tasks.
          final ResolveTask[] existingTasks = existing.getFirst();
          final ResolvedValueProducer[] existingProducers = existing.getSecond();
          for (int i = 0; i < existingTasks.length; i++) {
            if (existingTasks[i].isFinished()) {
              // Can use this task without creating a loop
              if (singleTask == null) {
                singleTask = existingProducers[i];
                singleTask.addRef(); // There is already an open count from when we got the producers
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
              // Other threads haven't progressed to completion - try and produce the value ourselves
              s_logger.debug("No suitable delegate found - creating producer");
              state.setRunnableTaskState(new FunctionApplicationStep(getTask(), this, resolvedFunction, resolvedOutput), context);
            }
          }
        }
      }
    }

    /**
     * Returns the desired value for the production. This might be a more constrained form than the value requirement being satisfied for graph construction.
     * 
     * @return the desired value, not null
     */
    protected abstract ValueRequirement getDesiredValue();

    /**
     * Resolves the output values declared by a function at late resolution against the current requirement. The one that satisfies the requirement is composed, added to the set, and returned. All
     * other output specifications are added to the output set unchanged.
     * <p>
     * The returned specification must be normalized.
     * 
     * @param context the graph building context, not null
     * @param newOutputValues the output values returned by the function, not null
     * @param resolvedOutputValues the composed output values, not null
     * @return the satisfying resolved output, or null if none satisfy
     */
    protected abstract ValueSpecification getResolvedOutputs(GraphBuildingContext context, Set<ValueSpecification> newOutputValues, Set<ValueSpecification> resolvedOutputValues);

    /**
     * For debugging/diagnostic reporting.
     */
    protected abstract void reportResult();

    @Override
    protected void pump(final GraphBuildingContext context) {
      // No-op; happens if a worker "finishes" a function application PumpingState and it progresses to the next natural
      // state in advance of the pump from the abstract value producer. See PumpingState.finished for an explanation
    }

  }

  private final IterationBaseStep _base;

  public FunctionIterationStep(final ResolveTask task, final IterationBaseStep base) {
    super(task);
    _base = base;
  }

  protected IterationBaseStep getIterationBase() {
    return _base;
  }

  protected Collection<FunctionExclusionGroup> getFunctionExclusion(final GraphBuildingContext context, final CompiledFunctionDefinition function) {
    final FunctionExclusionGroups groups = context.getFunctionExclusionGroups();
    if (groups == null) {
      return null;
    }
    final FunctionExclusionGroup functionExclusion = groups.getExclusionGroup(function.getFunctionDefinition());
    if (functionExclusion == null) {
      return getTask().getFunctionExclusion();
    }
    final Collection<FunctionExclusionGroup> parentExclusion = getTask().getFunctionExclusion();
    if (parentExclusion != null) {
      return groups.withExclusion(parentExclusion, functionExclusion);
    } else {
      return Collections.singleton(functionExclusion);
    }
  }

  @Override
  protected boolean run(final GraphBuildingContext context) {
    if (setTaskState(getIterationBase())) {
      return getIterationBase().run(context);
    } else {
      return true;
    }
  }

}
