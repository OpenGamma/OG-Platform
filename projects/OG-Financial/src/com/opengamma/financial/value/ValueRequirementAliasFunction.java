/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.value;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * A generic function for exposing a particular value requirement under a different name. This may allow the value to
 * be delivered through a heavily constrained value requirement, but output through a simple value requirement name.
 */
public class ValueRequirementAliasFunction extends AbstractFunction.NonCompiledInvoker {

  private final String _aliasedValueRequirementName;
  private final String _inputRequirementName;
  private final ValueProperties _inputConstraints;
  private final ComputationTargetType _targetType;
  
  /**
   * Constructs an instance.
   * 
   * @param aliasedValueRequirementName  the value requirement name under which to expose the input value, not null
   * @param inputRequirementName  the input value requirement name, not null
   * @param inputConstraints  the input value requirement constraints, not null
   * @param targetType  the function target type, not null
   */
  public ValueRequirementAliasFunction(String aliasedValueRequirementName, String inputRequirementName, ValueProperties inputConstraints, ComputationTargetType targetType) {
    ArgumentChecker.notNull(aliasedValueRequirementName, "aliasedValueRequirementName");
    ArgumentChecker.notNull(inputRequirementName, "inputRequirementName");
    ArgumentChecker.notNull(inputConstraints, "inputConstraints");
    ArgumentChecker.notNull(targetType, "targetType");
    _aliasedValueRequirementName = aliasedValueRequirementName;
    _inputRequirementName = inputRequirementName;
    _inputConstraints = inputConstraints;
    _targetType = targetType;
  }
  
  @Override
  public ComputationTargetType getTargetType() {
    return _targetType;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return target.getType() == _targetType;
  }
  
  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    return ImmutableSet.of(createValueRequirement(target));
  }
  
  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return ImmutableSet.of(createValueSpec(target));
  }
  
  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    Object result = inputs.getValue(createValueRequirement(target));
    return ImmutableSet.of(new ComputedValue(createValueSpec(target), result));
  }
  
  private ValueRequirement createValueRequirement(ComputationTarget target) {
    return new ValueRequirement(_inputRequirementName, target.toSpecification(), _inputConstraints);
  }
  
  private ValueSpecification createValueSpec(ComputationTarget target) {
    return new ValueSpecification(_aliasedValueRequirementName, target.toSpecification(), createValueProperties().get());
  }

}
