/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.value;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * A generic value renaming function. A single instance can be used for changing the name of multiple, mutually
 * exclusive (for a given target) values.
 */
public class ValueRenamingFunction extends AbstractFunction.NonCompiledInvoker {

  private final Set<String> _valueNamesToChange;
  private final String _newValueName;
  private final ComputationTargetType _targetType;
  
  /**
   * Constructs an instance.
   * 
   * @param valueNamesToChange  the set of mutually exclusive value names (for a given target) which the function will change, not null or empty
   * @param newValueName  the new name for any matching value, not null
   * @param targetType  the computation target type for which the function will apply, not null
   */
  public ValueRenamingFunction(final Set<String> valueNamesToChange, final String newValueName, final ComputationTargetType targetType) {
    ArgumentChecker.notNull(valueNamesToChange, "valueNamesToChange");
    ArgumentChecker.notEmpty(valueNamesToChange, "valueNamesToChange");
    ArgumentChecker.notNull(newValueName, "newValueName");
    ArgumentChecker.notNull(targetType, "targetType");
    _valueNamesToChange = valueNamesToChange;
    _newValueName = newValueName;
    _targetType = targetType;
  }
  
  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    ComputedValue inputValue = Iterables.getOnlyElement(inputs.getAllValues());
    ValueSpecification outputSpec = getOutputSpec(inputValue.getSpecification());
    return ImmutableSet.of(new ComputedValue(outputSpec, inputValue.getValue()));
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
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.VALUE, new ComputationTargetSpecification(_targetType, target.getUniqueId()), ValueProperties.all()));
  }
  
  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    for (String possibleInputValueName : _valueNamesToChange) {
      result.add(new ValueRequirement(possibleInputValueName, desiredValue.getTargetSpecification(), desiredValue.getConstraints()));
    }
    return result;
  }

  @Override
  public boolean canHandleMissingRequirements() {
    return true;
  }
  
  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
    if (inputs.isEmpty()) {
      throw new OpenGammaRuntimeException("Unable to satify any of " + _valueNamesToChange + " in order to rename to " + _newValueName + " for target " + target);
    }
    if (inputs.size() > 1) {
      throw new OpenGammaRuntimeException("Unable to uniquely map from one of " + _valueNamesToChange + " to " + _newValueName + " since multiple inputs satisfied: " + inputs);
    }
    ValueSpecification inputSpec = Iterables.getOnlyElement(inputs.keySet());
    return ImmutableSet.of(getOutputSpec(inputSpec));
  }

  protected ValueSpecification getOutputSpec(ValueSpecification inputSpec) {
    ValueProperties outputProperties = inputSpec.getProperties().copy()
        .withoutAny(ValuePropertyNames.FUNCTION)
        .with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
    return new ValueSpecification(_newValueName, inputSpec.getTargetSpecification(), outputProperties);
  }

}
