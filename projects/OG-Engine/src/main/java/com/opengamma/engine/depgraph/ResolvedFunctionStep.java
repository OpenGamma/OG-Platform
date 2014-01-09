/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroup;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.test.Profiler;
import com.opengamma.util.tuple.Triple;

/* package */class ResolvedFunctionStep extends FunctionIterationStep.IterationBaseStep {

  private static final Logger s_logger = LoggerFactory.getLogger(ResolvedFunctionStep.class);

  private final Iterator<Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>>> _functions;

  public ResolvedFunctionStep(final ResolveTask task, final Iterator<Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>>> functions) {
    super(task);
    assert functions != null;
    _functions = functions;
  }

  protected Iterator<Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>>> getFunctions() {
    return _functions;
  }

  @Override
  protected boolean run(final GraphBuildingContext context) {
    if (!getFunctions().hasNext()) {
      s_logger.info("No more functions for {}", getValueRequirement());
      setTaskStateFinished(context);
      return true;
    }
    final Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> resolvedFunction = getFunctions().next();
    final Collection<FunctionExclusionGroup> groups = getTask().getFunctionExclusion();
    if (groups != null) {
      final FunctionExclusionGroups util = context.getFunctionExclusionGroups();
      final FunctionExclusionGroup exclusion = util.getExclusionGroup(resolvedFunction.getFirst().getFunction().getFunctionDefinition());
      if ((exclusion != null) && util.isExcluded(exclusion, groups)) {
        s_logger.debug("Ignoring {} from exclusion group {}", resolvedFunction, exclusion);
        getTask().setRecursionDetected();
        setRunnableTaskState(this, context);
        return true;
      }
    }
    s_logger.debug("Considering {} for {}", resolvedFunction, getValueRequirement());
    final ValueSpecification originalOutput = resolvedFunction.getSecond();
    ValueSpecification resolvedOutput = originalOutput.compose(getValueRequirement());
    if (resolvedOutput != originalOutput) {
      resolvedOutput = context.simplifyType(resolvedOutput);
      s_logger.debug("Composed original output of {} to {}", originalOutput, resolvedOutput);
    }
    functionApplication(context, resolvedOutput, resolvedFunction);
    return true;
  }

  @Override
  protected ValueRequirement getDesiredValue() {
    return getValueRequirement();
  }

  @Override
  protected ValueSpecification getResolvedOutputs(final GraphBuildingContext context, final Set<ValueSpecification> newOutputValues, final Set<ValueSpecification> resolvedOutputValues) {
    final ValueRequirement desiredValue = getValueRequirement();
    ValueSpecification resolvedOutput = null;
    for (ValueSpecification outputValue : newOutputValues) {
      if ((resolvedOutput == null) && (desiredValue.getValueName() == outputValue.getValueName()) && desiredValue.getConstraints().isSatisfiedBy(outputValue.getProperties())) {
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
    return "RESOLVED_FUNCTION" + getObjectId();
  }

  private static final Profiler s_profiler = Profiler.create(ResolvedFunctionStep.class);

  @Override
  protected void reportResult() {
    s_profiler.tick();
  }

}
