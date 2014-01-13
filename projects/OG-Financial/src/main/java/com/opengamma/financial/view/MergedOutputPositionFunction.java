/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
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
  
  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return ImmutableSet.of(ValueSpecification.of(ValueRequirementNames.MERGED_OUTPUT, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    String mergedOutputName = desiredValue.getConstraint(ValuePropertyNames.NAME);
    ViewCalculationConfiguration calcConfig = context.getViewCalculationConfiguration();
    MergedOutput mergedOutput = calcConfig.getMergedOutput(mergedOutputName);
    if (mergedOutput == null) {
      return null;
    }
    Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    for (Pair<String, ValueProperties> requirement : mergedOutput.getPortfolioRequirements()) {
      String valueName = requirement.getFirst();
      ValueProperties constraints = requirement.getSecond().copy().with(ValuePropertyNames.NAME, mergedOutputName).withOptional(ValuePropertyNames.NAME).get();
      requirements.add(new ValueRequirement(valueName, target.toSpecification(), constraints));
    }
    return requirements;
  }
  
  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
    if (inputs.size() == 0) {
      return null;
    }
    if (inputs.size() > 1) {
      throw new OpenGammaRuntimeException("Expected requirements for merged output to be mutually exclusive, but multiple resolved successfully: " + inputs);
    }
    ValueRequirement inputRequirement = Iterables.getOnlyElement(inputs.values());
    ValueSpecification inputSpec = Iterables.getOnlyElement(inputs.keySet());
    ValueProperties properties = getResultProperties(inputRequirement.getConstraint(ValuePropertyNames.NAME), inputSpec);
    return ImmutableSet.of(ValueSpecification.of(ValueRequirementNames.MERGED_OUTPUT, target.toSpecification(), properties));
  }

  private ValueProperties getResultProperties(String mergedOutputName, ValueSpecification inputSpec) {
    return inputSpec.getProperties().copy()
        .with(ValuePropertyNames.NAME, mergedOutputName)
        .withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues); 
    ComputedValue result = Iterables.getOnlyElement(inputs.getAllValues());
    ValueSpecification valueSpec = ValueSpecification.of(ValueRequirementNames.MERGED_OUTPUT, target.toSpecification(), desiredValue.getConstraints());
    return ImmutableSet.of(new ComputedValue(valueSpec, result.getValue()));
  }

  @Override
  public boolean canHandleMissingRequirements() {
    return true;
  }
  
}
