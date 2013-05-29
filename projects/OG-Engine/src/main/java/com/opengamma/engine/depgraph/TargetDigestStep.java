/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.function.resolver.ResolutionRule;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.test.Profiler;
import com.opengamma.util.tuple.Triple;

/**
 * Calculates the target digest and considers existing nodes for a pattern to use for direct application.
 * <p>
 * Note that this step will produce unexpected dependency graphs if functions in the repository have applicability rules or behaviors that differ for two targets with the same digest. Care should be
 * taken when enabling this algorithm.
 */
/* package */final class TargetDigestStep extends FunctionIterationStep.IterationBaseStep {

  private static final Logger s_logger = LoggerFactory.getLogger(TargetDigestStep.class);

  private static final Profiler s_profilerSuccess = Profiler.create(TargetDigestStep.class, "success");
  private static final Profiler s_profilerWaste = Profiler.create(TargetDigestStep.class, "waste");

  private GraphBuildingContext.ResolutionIterator _resolutions;
  private long _timeSpent;
  private ValueRequirement _desiredValue;

  public TargetDigestStep(final ResolveTask task, final GraphBuildingContext.ResolutionIterator resolutions) {
    super(task);
    _resolutions = resolutions;
  }

  @Override
  protected boolean run(final GraphBuildingContext context) {
    final ValueRequirement requirement = getValueRequirement();
    final ComputationTarget target = getComputationTarget(context);
    if (_resolutions != null) {
      final long startTime = System.nanoTime();
      try {
        final ValueProperties constraints = requirement.getConstraints();
        do {
          final ValueProperties properties = _resolutions.getValueProperties();
          if (constraints.isSatisfiedBy(properties)) {
            s_logger.info("Trying digest resolution {} for {}", properties, requirement);
            final ParameterizedFunction function = _resolutions.getFunction();
            final CompiledFunctionDefinition functionDef = function.getFunction();
            if (!functionDef.getTargetType().isCompatible(target.getType())) {
              s_logger.debug("Function {} type is not compatible with {}", functionDef, target);
              continue;
            }
            final ComputationTarget adjustedTarget = ResolutionRule.adjustTarget(functionDef.getTargetType(), target);
            if (!functionDef.canApplyTo(context.getCompilationContext(), adjustedTarget)) {
              s_logger.debug("Function {} cannot be applied to {}", functionDef, target);
              continue;
            }
            Collection<ValueSpecification> results = functionDef.getResults(context.getCompilationContext(), adjustedTarget);
            if ((results == null) || results.isEmpty()) {
              s_logger.debug("Function {} applied to {} produced no results", functionDef, target);
              continue;
            }
            final ValueProperties composedConstraints;
            if (constraints.getValues(ValuePropertyNames.FUNCTION) != null) {
              composedConstraints = properties.compose(constraints);
            } else {
              // Note: some functions on OG-Financial do stupid things with their desired value if it constrains the function identifier.
              // it is easier to take action here rather than work through to fix them.
              composedConstraints = properties.withoutAny(ValuePropertyNames.FUNCTION).compose(constraints);
            }
            ValueRequirement composedRequirement = null;
            ValueSpecification matchedResult = null;
            ValueSpecification resolvedOutput = null;
            for (ValueSpecification result : results) {
              // value names are interned
              if (requirement.getValueName() == result.getValueName()) {
                if (composedConstraints.isSatisfiedBy(result.getProperties())) {
                  if (composedConstraints == constraints) {
                    composedRequirement = requirement;
                  } else {
                    composedRequirement = new ValueRequirement(requirement.getValueName(), requirement.getTargetReference(), composedConstraints);
                  }
                  matchedResult = context.simplifyType(result);
                  resolvedOutput = matchedResult.compose(composedRequirement);
                  if (resolvedOutput != matchedResult) {
                    resolvedOutput = context.simplifyType(resolvedOutput);
                    s_logger.debug("Composed original output of {} to {}", matchedResult, resolvedOutput);
                  }
                  break;
                }
              }
            }
            if (resolvedOutput == null) {
              s_logger.debug("Requirement {} not satisfied by results {}", requirement, results);
              continue;
            }
            s_logger.info("Inferred requirement {} for {}", composedRequirement, requirement);
            _desiredValue = composedRequirement;
            if (!_resolutions.hasNext()) {
              _resolutions = null;
            }
            functionApplication(context, resolvedOutput, Triple.of(function, matchedResult, context.simplifyTypes(results)));
            return true;
          } else {
            s_logger.debug("Ignoring digest resolution {} for {}", properties, requirement);
          }
        } while (_resolutions.hasNext());
      } finally {
        _timeSpent += System.nanoTime() - startTime;
      }
      s_profilerWaste.tick(_timeSpent);
    }
    if (s_logger.isDebugEnabled()) {
      s_logger.debug("No more digest resolutions for {} ({}us wasted)", requirement, (double) _timeSpent / 1e3);
    }
    GetFunctionsStep.getFunctions(target, context, this);
    return true;
  }

  @Override
  protected ValueRequirement getDesiredValue() {
    return _desiredValue;
  }

  @Override
  protected ValueSpecification getResolvedOutputs(final GraphBuildingContext context, final Set<ValueSpecification> newOutputValues, final Set<ValueSpecification> resolvedOutputValues) {
    final ValueRequirement desiredValue = getDesiredValue();
    final ValueProperties originalConstraints = getValueRequirement().getConstraints();
    ValueSpecification resolvedOutput = null;
    for (ValueSpecification outputValue : newOutputValues) {
      if ((resolvedOutput == null) && (desiredValue.getValueName() == outputValue.getValueName()) && desiredValue.getConstraints().isSatisfiedBy(outputValue.getProperties()) &&
          originalConstraints.isSatisfiedBy(outputValue.getProperties())) {
        resolvedOutput = context.simplifyType(outputValue.compose(desiredValue));
        s_logger.debug("Raw output {} resolves to {}", outputValue, resolvedOutput);
        resolvedOutputValues.add(resolvedOutput);
      } else {
        resolvedOutputValues.add(context.simplifyType(outputValue));
      }
    }
    return resolvedOutput;
  }

  @Override
  public String toString() {
    return "TARGET_DIGEST" + getObjectId();
  }

  @Override
  protected void reportResult() {
    s_logger.debug("Successful digest resolution of {} via {}", getValueRequirement(), getDesiredValue());
    s_profilerSuccess.tick(_timeSpent);
    _timeSpent = 0;
  }

}
