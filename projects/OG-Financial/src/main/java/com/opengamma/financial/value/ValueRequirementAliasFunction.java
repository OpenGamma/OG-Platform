/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.value;

import java.util.Map;
import java.util.Set;

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
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * A generic function for exposing a particular value requirement under a different name. This may allow the value to be delivered through a heavily constrained value requirement, but output through a
 * simple value requirement name.
 */
public class ValueRequirementAliasFunction extends AbstractFunction.NonCompiledInvoker {

  // REVIEW 2013-04-12 Andrew - This is a poor implementation that doesn't propogate constraints particularly satisfactorily; use ValueRenamingFunction instead

  private final String _aliasedValueRequirementName;
  private final String _inputRequirementName;
  private final ValueProperties _inputConstraints;
  private final Set<String> _preservedProperties;
  private final ComputationTargetType _targetType;

  /**
   * Constructs an instance.
   * 
   * @param aliasedValueRequirementName the value requirement name under which to expose the input value, not null
   * @param inputRequirementName the input value requirement name, not null
   * @param inputConstraints the static input value requirement constraints, not null
   * @param preservedProperties the properties preserved on the output which may be constrained, not null
   * @param targetType the function target type, not null
   */
  public ValueRequirementAliasFunction(String aliasedValueRequirementName, String inputRequirementName,
      ValueProperties inputConstraints, Set<String> preservedProperties, ComputationTargetType targetType) {
    ArgumentChecker.notNull(aliasedValueRequirementName, "aliasedValueRequirementName");
    ArgumentChecker.notNull(inputRequirementName, "inputRequirementName");
    ArgumentChecker.notNull(inputConstraints, "inputConstraints");
    ArgumentChecker.notNull(preservedProperties, "preservedProperties");
    ArgumentChecker.notNull(targetType, "targetType");
    _aliasedValueRequirementName = aliasedValueRequirementName;
    _inputRequirementName = inputRequirementName;
    _inputConstraints = inputConstraints;
    _preservedProperties = preservedProperties;
    _targetType = targetType;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return _targetType;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    final Builder builder = createValueProperties();
    for (String preservedProperty : _preservedProperties) {
      builder.withAny(preservedProperty);
    }
    return ImmutableSet.of(new ValueSpecification(_aliasedValueRequirementName, target.toSpecification(), builder.get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    return ImmutableSet.of(createValueRequirement(target, desiredValue));
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification inputSpec = Iterables.getOnlyElement(inputs.keySet());
    final Builder builder = createValueProperties();
    appendPreservedProperties(inputSpec.getProperties(), builder);
    return ImmutableSet.of(new ValueSpecification(_aliasedValueRequirementName, target.toSpecification(), builder.get()));
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final Object result = inputs.getValue(_inputRequirementName);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    return ImmutableSet.of(new ComputedValue(new ValueSpecification(desiredValue.getValueName(), target.toSpecification(), desiredValue.getConstraints()), result));
  }

  private ValueRequirement createValueRequirement(ComputationTarget target, ValueRequirement desiredValue) {
    final Builder constraints = _inputConstraints.copy();
    appendPreservedProperties(desiredValue.getConstraints(), constraints);
    return new ValueRequirement(_inputRequirementName, target.toSpecification(), constraints.get());
  }

  private void appendPreservedProperties(ValueProperties from, final Builder to) {
    Set<String> sourceProperties = from.getProperties();
    if (sourceProperties == null) {
      return;
    }
    for (String constraintName : sourceProperties) {
      if (!_preservedProperties.contains(constraintName)) {
        continue;
      }
      Set<String> constraintValues = from.getValues(constraintName);
      if (constraintValues.isEmpty()) {
        to.withAny(constraintName);
      } else {
        to.with(constraintName, constraintValues);
      }
    }
  }

}
