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
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * A generic value renaming function. A single instance can be used for changing the name of multiple, mutually exclusive (for a given target) values.
 */
public class ValueRenamingFunction extends AbstractFunction.NonCompiledInvoker {

  private final Set<String> _valueNamesToChange;
  private final String _newValueName;
  private final ComputationTargetType _targetType;
  private final ValueProperties _additionalConstraints;

  /**
   * Constructs an instance.
   * 
   * @param valueNamesToChange the set of mutually exclusive value names (for a given target) which the function will change, not null or empty
   * @param newValueName the new name for any matching value, not null
   * @param targetType the computation target type for which the function will apply, not null
   */
  public ValueRenamingFunction(final Set<String> valueNamesToChange, final String newValueName, final ComputationTargetType targetType) {
    this(valueNamesToChange, newValueName, targetType, null);
  }

  /**
   * Constructs an instance.
   * 
   * @param valueNamesToChange the set of mutually exclusive value names (for a given target) which the function will change, not null or empty
   * @param newValueName the new name for any matching value, not null
   * @param targetType the computation target type for which the function will apply, not null
   * @param additionalConstraints additional constraints to set on the origin requirement, null for none
   */
  public ValueRenamingFunction(final Set<String> valueNamesToChange, final String newValueName, final ComputationTargetType targetType, final ValueProperties additionalConstraints) {
    ArgumentChecker.notNull(valueNamesToChange, "valueNamesToChange");
    ArgumentChecker.notEmpty(valueNamesToChange, "valueNamesToChange");
    ArgumentChecker.notNull(newValueName, "newValueName");
    ArgumentChecker.notNull(targetType, "targetType");
    _valueNamesToChange = valueNamesToChange;
    _newValueName = newValueName;
    _targetType = targetType;
    if ((additionalConstraints == null) || ValueProperties.none().equals(additionalConstraints)) {
      _additionalConstraints = null;
    } else {
      _additionalConstraints = additionalConstraints;
    }
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    Set<ComputedValue> result = new HashSet<>();
    Object prevValue = null;
    for (ComputedValue inputValue : inputs.getAllValues()) {
      Object value = inputValue.getValue();
      if (prevValue == null) {
        prevValue = value;
      } else if (!value.equals(prevValue)) {
        throw new OpenGammaRuntimeException("Attempted to rename two unequal values with the same name: " + _newValueName);
      }
      ValueSpecification outputSpec = getOutputSpec(inputValue.getSpecification());
      result.add(new ComputedValue(outputSpec, value));
    }
    return result;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return _targetType;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return true;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return ImmutableSet.of(new ValueSpecification(_newValueName, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    final ValueProperties constraints;
    if (_additionalConstraints == null) {
      constraints = desiredValue.getConstraints();
    } else {
      final ValueProperties.Builder constraintsBuilder = desiredValue.getConstraints().copy();
      for (String constraint : _additionalConstraints.getProperties()) {
        Set<String> values = _additionalConstraints.getValues(constraint);
        if (values.isEmpty()) {
          if (_additionalConstraints.isOptional(constraint)) {
            constraintsBuilder.withOptional(constraint);
          } else {
            constraintsBuilder.withAny(constraint);
          }
        } else {
          constraintsBuilder.with(constraint, values);
          if (_additionalConstraints.isOptional(constraint)) {
            constraintsBuilder.withOptional(constraint);
          }
        }
      }
      constraints = constraintsBuilder.get();
    }
    for (String possibleInputValueName : _valueNamesToChange) {
      result.add(new ValueRequirement(possibleInputValueName, desiredValue.getTargetReference(), constraints));
    }
    return result;
  }

  @Override
  public boolean canHandleMissingRequirements() {
    return _valueNamesToChange.size() > 1;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
    if (inputs.size() != 1) {
      final Set<ValueSpecification> result = new HashSet<>();
      for (ValueSpecification spec : inputs.keySet()) {
        result.add(getOutputSpec(spec));
      }
      return result;
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
