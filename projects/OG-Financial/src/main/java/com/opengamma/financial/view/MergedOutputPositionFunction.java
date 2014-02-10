/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewCalculationConfiguration.MergedOutput;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class MergedOutputPositionFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(MergedOutputPositionFunction.class);

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return ImmutableSet.of(ValueSpecification.of(ValueRequirementNames.MERGED_OUTPUT, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final String mergedOutputName = desiredValue.getConstraint(ValuePropertyNames.NAME);
    final ViewCalculationConfiguration calcConfig = context.getViewCalculationConfiguration();
    final MergedOutput mergedOutput = calcConfig.getMergedOutput(mergedOutputName);
    if (mergedOutput == null) {
      return null;
    }
    final Set<ValueRequirement> requirements = new HashSet<>();
    for (final Pair<String, ValueProperties> requirement : mergedOutput.getPortfolioRequirements()) {
      final String valueName = requirement.getFirst();
      final ValueProperties constraints = requirement.getSecond().copy().with(ValuePropertyNames.NAME, mergedOutputName).withOptional(ValuePropertyNames.NAME).get();
      requirements.add(new ValueRequirement(valueName, target.toSpecification(), constraints));
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    if (inputs.size() == 0) {
      return null;
    }
    if (inputs.size() > 1) {
      s_logger.error("Expected requirements for merged output to be mutually exclusive, but multiple resolved successfully: " + inputs);
      return null;
    }
    final ValueRequirement inputRequirement = Iterables.getOnlyElement(inputs.values());
    final ValueSpecification inputSpec = Iterables.getOnlyElement(inputs.keySet());
    final ValueProperties properties = getResultProperties(inputRequirement.getConstraint(ValuePropertyNames.NAME), inputSpec);
    return ImmutableSet.of(ValueSpecification.of(ValueRequirementNames.MERGED_OUTPUT, target.toSpecification(), properties));
  }

  private ValueProperties getResultProperties(final String mergedOutputName, final ValueSpecification inputSpec) {
    return inputSpec.getProperties().copy()
        .with(ValuePropertyNames.NAME, mergedOutputName)
        .withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final ComputedValue result = Iterables.getOnlyElement(inputs.getAllValues());
    final ValueSpecification valueSpec = ValueSpecification.of(ValueRequirementNames.MERGED_OUTPUT, target.toSpecification(), desiredValue.getConstraints());
    return ImmutableSet.of(new ComputedValue(valueSpec, result.getValue()));
  }

  @Override
  public boolean canHandleMissingRequirements() {
    return true;
  }

}
