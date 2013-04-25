/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.property.UnitProperties;
import com.opengamma.id.UniqueId;

/**
 * 
 */
public abstract class AbstractWeightFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * The property name that describes the value used to construct the weight. The full set of output properties will include those of the target and its parent node.
   */
  public static final String VALUE_PROPERTY_NAME = "Value";

  /**
   * Default to the generic value, for example FAIR_VALUE or PRESENT_VALUE depending on the asset classes.
   */
  private static final String DEFAULT_VALUE_NAME = ValueRequirementNames.VALUE;

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.WEIGHT, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    Set<String> values = constraints.getValues(VALUE_PROPERTY_NAME);
    final String inputValue;
    if ((values == null) || values.isEmpty()) {
      inputValue = DEFAULT_VALUE_NAME;
    } else if (values.size() == 1) {
      inputValue = values.iterator().next();
    } else {
      return null;
    }
    // Propogate the desired value constraints onto the requirements, removing those specific to this function and adding a unit homogeneity clause
    final ValueProperties.Builder requirementConstraintsBuilder = constraints.copy().withoutAny(VALUE_PROPERTY_NAME);
    for (String unit : UnitProperties.unitPropertyNames()) {
      values = constraints.getValues(unit);
      if (values == null) {
        // Unit was not specified on the output, but we specify it on the inputs so we can check homogeneity to ensure the division is valid
        requirementConstraintsBuilder.withOptional(unit);
      }
    }
    // Request value on the value and parent
    final ValueProperties requirementConstraints = requirementConstraintsBuilder.get();
    return ImmutableSet.of(new ValueRequirement(inputValue, getValueTarget(target), requirementConstraints), new ValueRequirement(inputValue, getParentTarget(target), requirementConstraints));
  }

  protected abstract ComputationTargetSpecification getValueTarget(final ComputationTarget target);

  protected abstract ComputationTargetReference getParentTarget(final ComputationTarget target);

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    ValueSpecification inputValue = null;
    ValueSpecification inputParent = null;
    final UniqueId value = target.getUniqueId();
    for (ValueSpecification input : inputs.keySet()) {
      if (value.equals(input.getTargetSpecification().getUniqueId())) {
        assert inputValue == null;
        inputValue = input;
      } else {
        assert inputParent == null;
        inputParent = input;
      }
    }
    final ValueProperties rawResultProperties = inputValue.getProperties().intersect(inputParent.getProperties());
    final ValueProperties.Builder resultPropertiesBuilder = rawResultProperties.copy();
    for (String unit : UnitProperties.unitPropertyNames()) {
      final Set<String> valueUnits = inputValue.getProperties().getValues(unit);
      final Set<String> parentUnits = inputParent.getProperties().getValues(unit);
      if (valueUnits != null) {
        if (parentUnits != null) {
          if (rawResultProperties.getValues(unit) != null) {
            // The operation is a division, so there are no units on the result
            resultPropertiesBuilder.withoutAny(unit);
          } else {
            // No common intersection between parent and value properties for this unit
            return null;
          }
        } else {
          // Parent did not include the same units as the value
          return null;
        }
      } else {
        if (parentUnits != null) {
          // Value did not include the same units as the parent
          return null;
        }
      }
    }
    resultPropertiesBuilder.withoutAny(VALUE_PROPERTY_NAME).with(VALUE_PROPERTY_NAME, inputValue.getValueName());
    resultPropertiesBuilder.withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId());
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.WEIGHT, target.toSpecification(), resultPropertiesBuilder.get()));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    double parentValue = 0;
    double targetValue = 0;
    final UniqueId targetIdentifier = target.getUniqueId();
    for (final ComputedValue c : inputs.getAllValues()) {
      if (targetIdentifier.equals(c.getSpecification().getTargetSpecification().getUniqueId())) {
        targetValue = (Double) c.getValue();
      } else {
        parentValue = (Double) c.getValue();
      }
    }
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.WEIGHT, target.toSpecification(), desiredValue.getConstraints()),
        targetValue / parentValue));
  }

}
