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
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.MarketDataAliasingFunction;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.marketdata.availability.MarketDataNotSatisfiableException;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.lazy.LazyComputationTargetResolver;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.async.BlockingOperation;
import com.opengamma.util.tuple.Triple;

/* package */final class GetFunctionsStep extends ResolveTask.State {

  private static final Logger s_logger = LoggerFactory.getLogger(GetFunctionsStep.class);

  private static final ParameterizedFunction MARKET_DATA_SOURCING_FUNCTION = createParameterizedFunction(MarketDataSourcingFunction.INSTANCE);
  private static final ParameterizedFunction RELABELLING_FUNCTION = createParameterizedFunction(MarketDataAliasingFunction.INSTANCE);

  private static ParameterizedFunction createParameterizedFunction(final CompiledFunctionDefinition function) {
    return new ParameterizedFunction(function, function.getFunctionDefinition().getDefaultParameters());
  }

  public GetFunctionsStep(final ResolveTask task) {
    super(task);
  }

  private static final ComputationTargetReferenceVisitor<Object> s_getTargetValue = new ComputationTargetReferenceVisitor<Object>() {

    @Override
    public Object visitComputationTargetRequirement(final ComputationTargetRequirement requirement) {
      return requirement.getIdentifiers();
    }

    @Override
    public Object visitComputationTargetSpecification(final ComputationTargetSpecification specification) {
      // If the object couldn't be resolved, then the rogue UID is the best we can present
      return specification.getUniqueId();
    }

  };

  /**
   * Removes any optional flags on constraints. If the constraint is optional and the provider produced no value, then the constraint is removed. If the provider produced values for the constraint,
   * and an intersection exists, then the intersection is used. Otherwise the maximal value set is used to satisfy the original constraint.
   * 
   * @param constraints the requested constraints, not null
   * @param properties the provider's value specification properties, not null
   * @return the builder for constraints
   */
  private static ValueProperties.Builder intersectOptional(final ValueProperties constraints, final ValueProperties properties) {
    ValueProperties.Builder builder = constraints.copy();
    // TODO: [PLAT-3446] Return the builder immediately to recreate the observed fault
    for (String property : constraints.getProperties()) {
      final Set<String> providerValues = properties.getValues(property);
      if (providerValues != null) {
        // Remove optional flag and take intersection if there is one
        builder.notOptional(property);
        final Set<String> constrainedValues = constraints.getValues(property);
        if (!providerValues.isEmpty()) {
          if (constrainedValues.isEmpty()) {
            builder.with(property, providerValues);
          } else {
            final Set<String> intersection = Sets.intersection(constrainedValues, providerValues);
            if (!intersection.isEmpty()) {
              builder.withoutAny(property).with(property, intersection);
            }
          }
        }
      } else {
        if (constraints.isOptional(property)) {
          // Constraint is optional, remove
          builder.withoutAny(property);
        }
      }
    }
    return builder;
  }

  @Override
  protected boolean run(final GraphBuildingContext context) {
    final ValueRequirement requirement = getValueRequirement();
    boolean missing = false;
    ValueSpecification marketDataSpec = null;
    ComputationTargetSpecification targetSpec = null;
    ComputationTarget target = null;
    Object targetValue = null;
    BlockingOperation.off();
    try {
      targetSpec = getTargetSpecification(context);
      if (targetSpec != null) {
        target = LazyComputationTargetResolver.resolve(context.getCompilationContext().getComputationTargetResolver(), targetSpec);
        if (target != null) {
          targetValue = target.getValue();
        } else {
          targetValue = requirement.getTargetReference().accept(s_getTargetValue);
        }
      } else {
        targetValue = requirement.getTargetReference().accept(s_getTargetValue);
      }
      marketDataSpec = context.getMarketDataAvailabilityProvider().getAvailability(targetSpec, targetValue, requirement);
    } catch (final BlockingOperation e) {
      return false;
    } catch (final MarketDataNotSatisfiableException e) {
      missing = true;
    } finally {
      BlockingOperation.on();
    }
    if (marketDataSpec != null) {
      s_logger.info("Found live data for {}", requirement);
      marketDataSpec = context.simplifyType(marketDataSpec);
      if (targetSpec == null) {
        // The system resolver did not produce a target that we can monitor, so use the MDAP supplied value
        targetSpec = marketDataSpec.getTargetSpecification();
      }
      ResolvedValue resolvedValue = createResult(marketDataSpec, MARKET_DATA_SOURCING_FUNCTION, Collections.<ValueSpecification>emptySet(), Collections.singleton(marketDataSpec));
      final ValueProperties constraints = requirement.getConstraints();
      boolean constraintsSatisfied = constraints.isSatisfiedBy(marketDataSpec.getProperties());
      if ((requirement.getValueName() != marketDataSpec.getValueName()) || !targetSpec.equals(marketDataSpec.getTargetSpecification()) || !constraintsSatisfied) {
        // The specification returned by market data provision does not match the logical target; publish a substitute node
        context.declareProduction(resolvedValue);
        final ValueProperties properties;
        if (constraintsSatisfied) {
          // Just the target or value name that was a mismatch; use the provider properties
          properties = marketDataSpec.getProperties();
        } else {
          final Set<String> functionNames = constraints.getValues(ValuePropertyNames.FUNCTION);
          if (functionNames == null) {
            final Set<String> allProperties = constraints.getProperties();
            if (allProperties == null) {
              // Requirement made no constraints
              properties = ValueProperties.with(ValuePropertyNames.FUNCTION, MarketDataAliasingFunction.UNIQUE_ID).get();
            } else if (!allProperties.isEmpty()) {
              // Requirement made no constraint on function identifier
              properties = intersectOptional(constraints, marketDataSpec.getProperties()).with(ValuePropertyNames.FUNCTION, MarketDataAliasingFunction.UNIQUE_ID).get();
            } else {
              // Requirement used a nearly infinite property bundle that omitted a function identifier
              properties = constraints.copy().withAny(ValuePropertyNames.FUNCTION).get();
            }
          } else {
            if (functionNames.isEmpty()) {
              final Set<String> allProperties = constraints.getProperties();
              if (allProperties.isEmpty()) {
                // Requirement is for an infinite or nearly infinite property bundle. This is valid but may be indicative of an error
                properties = constraints;
              } else {
                // Requirement had a wild card for the function but is otherwise finite
                properties = intersectOptional(constraints, marketDataSpec.getProperties()).withoutAny(ValuePropertyNames.FUNCTION)
                    .with(ValuePropertyNames.FUNCTION, MarketDataAliasingFunction.UNIQUE_ID).get();
              }
            } else if (functionNames.size() == 1) {
              // Requirement is fully specified
              properties = intersectOptional(constraints, marketDataSpec.getProperties()).get();
            } else {
              // Requirement allowed a choice of function - pick one
              properties = intersectOptional(constraints, marketDataSpec.getProperties()).withoutAny(ValuePropertyNames.FUNCTION)
                  .with(ValuePropertyNames.FUNCTION, functionNames.iterator().next()).get();
            }
          }
        }
        final ValueSpecification relabelledSpec = MemoryUtils.instance(new ValueSpecification(requirement.getValueName(), targetSpec, properties));
        resolvedValue = createResult(relabelledSpec, RELABELLING_FUNCTION, Collections.singleton(marketDataSpec), Collections.singleton(relabelledSpec));
      }
      final ResolvedValueProducer producer = new SingleResolvedValueProducer(requirement, resolvedValue);
      final ResolvedValueProducer existing = context.declareTaskProducing(resolvedValue.getValueSpecification(), getTask(), producer);
      if (existing == producer) {
        context.declareProduction(resolvedValue);
        if (!pushResult(context, resolvedValue, true)) {
          throw new IllegalStateException(resolvedValue + " rejected by pushResult");
        }
      } else {
        producer.release(context);
        existing.addCallback(context, new ResolvedValueCallback() {

          @Override
          public void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
            if (pump != null) {
              pump.close(context);
            }
            if (!pushResult(context, resolvedValue, true)) {
              throw new IllegalStateException(resolvedValue + " rejected by pushResult");
            }
            // Leave in current state; will go to finished after being pumped
          }

          @Override
          public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
            storeFailure(failure);
            setTaskStateFinished(context);
          }

          @Override
          public void recursionDetected() {
            getTask().setRecursionDetected();
          }

        });
        existing.release(context);
      }
      // Leave in current state; will go to finished after being pumped
    } else {
      if (missing) {
        s_logger.info("Missing market data for {}", requirement);
        storeFailure(context.marketDataMissing(requirement));
        setTaskStateFinished(context);
      } else {
        if (target != null) {
          final GraphBuildingContext.ResolutionIterator existingResolutions = context.getResolutions(targetSpec, requirement.getValueName());
          if (existingResolutions != null) {
            setRunnableTaskState(new TargetDigestStep(getTask(), existingResolutions), context);
          } else {
            getFunctions(target, context, this);
          }
        } else {
          s_logger.info("No functions for unresolved target {}", requirement);
          storeFailure(context.couldNotResolve(requirement));
          setTaskStateFinished(context);
        }
      }
    }
    return true;
  }

  /**
   * Fetches all of the functions that can apply to the target, changing the state to then process that collection.
   * <p>
   * The collection returned from the function resolver must include normalized value specifications.
   * 
   * @param target the target to apply the functions to, not null
   * @param context the graph building context, not null
   * @param state the current task execution state, not null
   */
  protected static void getFunctions(final ComputationTarget target, final GraphBuildingContext context, final ResolveTask.State state) {
    final ValueRequirement requirement = state.getValueRequirement();
    final Iterator<Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>>> itr = context.getFunctionResolver().resolveFunction(requirement.getValueName(), target,
        requirement.getConstraints());
    if (itr.hasNext()) {
      s_logger.debug("Found functions for {}", requirement);
      state.setRunnableTaskState(new ResolvedFunctionStep(state.getTask(), itr), context);
    } else {
      s_logger.info("No functions for {}", requirement);
      state.storeFailure(context.noFunctions(requirement));
      state.setTaskStateFinished(context);
    }
  }

  @Override
  protected void pump(final GraphBuildingContext context) {
    // Only had one market data result so go to finished state
    setTaskStateFinished(context);
  }

  @Override
  public String toString() {
    return "GET_FUNCTIONS" + getObjectId();
  }

}
