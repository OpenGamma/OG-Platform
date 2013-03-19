/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
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
 * Takes as input the result of a function that acts on ComputationTargetType.TRADE, applies unit scaling ( * 1.0 )
 * and outputs the result for ComputationTargetType.POSITION. <p>
 * Closely related to UnitPositionOrTradeScalingFunction but with different requirement target. 
 */
public class UnitPositionTradeScalingFunction extends AbstractFunction.NonCompiledInvoker {

  private final String _requirementName;

  public UnitPositionTradeScalingFunction(final String requirementName) {
    Validate.notNull(requirementName, "requirement name");
    _requirementName = requirementName;
  }

  @Override
  public String getShortName() {
    return "UnitPositionTradeScalingFunction";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return !target.getPosition().getTrades().isEmpty();
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(new ValueSpecification(_requirementName, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Position position = target.getPosition();
    final Collection<Trade> trades = position.getTrades();
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    final ValueProperties inputConstraint = desiredValue.getConstraints().withoutAny(ValuePropertyNames.FUNCTION);
    for (final Trade trade : trades) {
      result.add(new ValueRequirement(_requirementName, ComputationTargetType.TRADE, trade.getUniqueId(), inputConstraint));
    }
    return result;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    // Result properties are anything that was common to the input specifications
    ValueProperties common = null;
    for (ValueSpecification input : inputs.keySet()) {
      common = SumUtils.addProperties(common, input.getProperties());
    }
    if (common == null) {
      // Can't have been any inputs ... ?
      return null;
    }
    common = common.copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
    return Collections.singleton(new ValueSpecification(_requirementName, target.toSpecification(), common));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    ValueProperties common = null;
    Object scaledValue = null;
    for (ComputedValue value : inputs.getAllValues()) {
      common = SumUtils.addProperties(common, value.getSpecification().getProperties());
      scaledValue = SumUtils.addValue(scaledValue, value.getValue(), _requirementName);
    }
    common = common.copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
    final ValueSpecification specification = new ValueSpecification(_requirementName, target.toSpecification(), common);
    return Sets.newHashSet(new ComputedValue(specification, scaledValue));
  }

}
