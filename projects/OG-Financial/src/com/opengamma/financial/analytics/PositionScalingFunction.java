/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Able to scale values produced by the rest of the OG-Financial package.
 */
public class PositionScalingFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * Constraints to preserve from output to required input.
   */
  private static final String[] s_preserve = new String[] {ValuePropertyNames.CURRENCY};

  /**
   * Loose input constraints.
   */
  private static final ValueProperties s_inputConstraints;

  /**
   * Loose output properties.
   */
  private static final ValueProperties s_outputProperties;

  static {
    final ValueProperties.Builder builder = ValueProperties.builder();
    for (String preserve : s_preserve) {
      builder.withAny(preserve);
    }
    s_outputProperties = builder.get();
    for (String preserve : s_preserve) {
      builder.withOptional(preserve);
    }
    s_inputConstraints = builder.get();
  }

  private final String _requirementName;
  private ValueProperties _properties;

  public PositionScalingFunction(String requirementName) {
    ArgumentChecker.notNull(requirementName, "Requirement name");
    _requirementName = requirementName;
  }

  @Override
  public void setUniqueIdentifier(final String identifier) {
    super.setUniqueIdentifier(identifier);
    _properties = s_outputProperties.copy().with(ValuePropertyNames.FUNCTION, getUniqueIdentifier()).get();
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, final ValueRequirement desiredValue) {
    final Position position = target.getPosition();
    final Security security = position.getSecurity();
    // Remove the function identifier, otherwise preserve constraints
    final ValueProperties constraints = s_inputConstraints.compose(desiredValue.getConstraints());
    final ValueRequirement requirement = new ValueRequirement(_requirementName, ComputationTargetType.SECURITY, security.getUniqueIdentifier(), constraints);
    return Collections.singleton(requirement);
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    final ValueSpecification specification = new ValueSpecification(_requirementName, target.toSpecification(), _properties);
    return Collections.singleton(specification);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Set<ValueSpecification> inputs) {
    // Replace the function identifier, otherwise preserve input properties
    final ValueProperties inputProperties = inputs.iterator().next().getProperties();
    final ValueProperties outputProperties = inputProperties.copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueIdentifier()).get();
    final ValueSpecification specification = new ValueSpecification(_requirementName, target.toSpecification(), outputProperties);
    return Collections.singleton(specification);
  }

  @Override
  public String getShortName() {
    return "PositionScaling for " + _requirementName;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    Object value = inputs.getValue(_requirementName);
    ValueRequirement requirement = new ValueRequirement(_requirementName, target.toSpecification());
    ValueSpecification specification = new ValueSpecification(requirement, getUniqueIdentifier());
    ComputedValue scaledValue = null;
    if (value instanceof Double) {
      Double doubleValue = (Double) value;
      double quantity = target.getPosition().getQuantity().doubleValue();
      doubleValue *= quantity;
      scaledValue = new ComputedValue(specification, doubleValue);
    } else {
      scaledValue = new ComputedValue(specification, value);
    }
    return Collections.singleton(scaledValue);
  }

}
