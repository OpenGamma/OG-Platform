/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroup;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.lambdava.tuple.Pair;
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
          setTaskState(new ExistingProductionStep(getTask(), this, resolvedFunction, resolvedOutput));
          if (!pushResult(context, existingValue, false)) {
            s_logger.debug("Production not accepted - rescheduling");
            setRunnableTaskState(this, context);
          }
        }
      } else {
        // Other tasks are working on it, or have already worked on it
        s_logger.debug("Delegating to existing producers for {}", resolvedOutput);
        final ExistingResolutionsStep state = new ExistingResolutionsStep(getTask(), this, resolvedFunction, resolvedOutput);
        setTaskState(state);
        ResolvedValueProducer singleTask = null;
        AggregateResolvedValueProducer aggregate = null;
        // Must not introduce a loop (checking parent resolve tasks isn't sufficient) so only use "finished" tasks.
        final ResolveTask[] existingTasks = existing.getFirst();
        final ResolvedValueProducer[] existingProducers = existing.getSecond();
        boolean recursion = false;
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
          } else if (!recursion && getTask().hasParent(existingTasks[i])) {
            recursion = true;
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
          } else if (recursion) {
            // Loop detected
            state.failed(context, getValueRequirement(), context.recursiveRequirement(getValueRequirement()));
          } else {
            // Other threads haven't progressed to completion, and a loop isn't obvious - try and produce the value ourselves
            s_logger.debug("No suitable delegate found - creating producer");
            setRunnableTaskState(new FunctionApplicationStep(getTask(), this, resolvedFunction, resolvedOutput), context);
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

    protected abstract ValueSpecification getResolvedOutputs(GraphBuildingContext context, Set<ValueSpecification> newOutputValues, Set<ValueSpecification> resolvedOutputValues);

    /**
     * For debugging/diagnostic reporting.
     */
    protected abstract void reportResult();

    @Override
    protected boolean isActive() {
      // Won't do anything unless {@link #tryRun} is called
      return false;
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

  protected Map<ComputationTargetSpecification, Set<FunctionExclusionGroup>> getFunctionExclusion(final GraphBuildingContext context, final CompiledFunctionDefinition function) {
    final Map<ComputationTargetSpecification, Set<FunctionExclusionGroup>> parentExclusion = getTask().getFunctionExclusion();
    if (parentExclusion != null) {
      final FunctionExclusionGroup functionExclusion = context.getFunctionExclusionGroups().getExclusionGroup(function.getFunctionDefinition());
      if (functionExclusion != null) {
        final ComputationTargetSpecification target = getTargetSpecification(context);
        Set<FunctionExclusionGroup> result = parentExclusion.get(target);
        if (result == null) {
          result = Collections.singleton(functionExclusion);
        } else {
          result = new HashSet<FunctionExclusionGroup>(result);
          result.add(functionExclusion);
        }
        final Map<ComputationTargetSpecification, Set<FunctionExclusionGroup>> result2 = new HashMap<ComputationTargetSpecification, Set<FunctionExclusionGroup>>(parentExclusion);
        result2.put(target, result);
        return result2;
      } else {
        return parentExclusion;
      }
    } else {
      final FunctionExclusionGroups groups = context.getFunctionExclusionGroups();
      if (groups != null) {
        final FunctionExclusionGroup functionExclusion = groups.getExclusionGroup(function.getFunctionDefinition());
        if (functionExclusion != null) {
          final ComputationTargetSpecification target = getTargetSpecification(context);
          return Collections.singletonMap(target, Collections.singleton(functionExclusion));
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
    return getIterationBase().run(context);
  }

}
