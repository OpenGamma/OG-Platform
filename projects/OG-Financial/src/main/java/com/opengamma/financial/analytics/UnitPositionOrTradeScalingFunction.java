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
import com.opengamma.core.security.Security;
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

/**
 * Takes as input the result of a function that acts on ComputationTargetType.SECURITY, applies unit scaling ( * 1.0 ) and outputs the result for ComputationTargetType.POSITION_OR_TRADE.
 */
public class UnitPositionOrTradeScalingFunction extends AbstractFunction.NonCompiledInvoker {

  private final String _requirementName;

  public UnitPositionOrTradeScalingFunction(final String requirementName) {
    Validate.notNull(requirementName, "requirement name");
    _requirementName = requirementName;
  }

  @Override
  public String getShortName() {
    return "UnitPositionScalingFunction";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION_OR_TRADE;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(new ValueSpecification(_requirementName, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Security security = target.getPositionOrTrade().getSecurity();
    final ValueProperties constraints = desiredValue.getConstraints().withoutAny(ValuePropertyNames.FUNCTION);
    final ValueRequirement requirement = new ValueRequirement(_requirementName, ComputationTargetType.SECURITY, security.getUniqueId(), constraints);
    return Collections.singleton(requirement);
  }

  private ValueProperties getResultProperties(final ValueSpecification input) {
    return input.getProperties().copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification specification = new ValueSpecification(_requirementName, target.toSpecification(), getResultProperties(inputs.keySet().iterator().next()));
    return Collections.singleton(specification);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement outputValue = desiredValues.iterator().next();
    final ComputedValue inputValue = inputs.getAllValues().iterator().next();
    final ValueSpecification specification = new ValueSpecification(_requirementName, target.toSpecification(), outputValue.getConstraints());
    return Sets.newHashSet(new ComputedValue(specification, inputValue.getValue()));
  }

}
