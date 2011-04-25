/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;

/**
 * 
 */
public class UnitPositionScalingFunction extends PropertyPreservingFunction {

  @Override
  protected String[] getPreservedProperties() {
    return new String[] {ValuePropertyNames.CURRENCY, ValuePropertyNames.CURVE, YieldCurveFunction.PROPERTY_FORWARD_CURVE, YieldCurveFunction.PROPERTY_FUNDING_CURVE };
  }

  private final String _requirementName;

  public UnitPositionScalingFunction(final String requirementName) {
    Validate.notNull(requirementName, "requirement name");
    _requirementName = requirementName;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ComputedValue value = inputs.getAllValues().iterator().next();
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(_requirementName, target.toSpecification()), getResultProperties(value.getSpecification()));
    return Sets.newHashSet(new ComputedValue(specification, value.getValue()));
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Position position = target.getPosition();
    final Security security = position.getSecurity();
    final ValueRequirement requirement = new ValueRequirement(_requirementName, ComputationTargetType.SECURITY, security.getUniqueId(), getInputConstraint(desiredValue));
    return Collections.singleton(requirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(new ValueSpecification(_requirementName, target.toSpecification(), getResultProperties()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification specification = new ValueSpecification(_requirementName, target.toSpecification(), getResultProperties(inputs.keySet().iterator().next()));
    return Collections.singleton(specification);
  }
  
  @Override
  public String getShortName() {
    return "UnitPositionScalingFunction";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

}
