/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class PositionWeightFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final Object fairValueObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, position));
    if (fairValueObject == null) {
      throw new NullPointerException("Could not get fair value for position " + position);
    }
    final double fairValue = (Double) fairValueObject;
    Object portfolioValueObject = null;
    final Collection<ComputedValue> computed = inputs.getAllValues();
    //TODO this needs to be removed when we can get the portfolio structure from the execution context [ENG-236]
    for (final ComputedValue c : computed) {
      if (c.getSpecification().getTargetSpecification().getType() == ComputationTargetType.PORTFOLIO_NODE
          && c.getSpecification().getValueName().equals(ValueRequirementNames.FAIR_VALUE)) {
        portfolioValueObject = c.getValue();
      }
    }
    if (portfolioValueObject == null) {
      throw new NullPointerException("Could not get fair value for portfolio ");
    }
    return Collections.singleton(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.WEIGHT, position), getUniqueId()),
        fairValue / (Double) portfolioValueObject));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final Position position = target.getPosition();
      return Sets.newHashSet(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, position),
          new ValueRequirement(ValueRequirementNames.FAIR_VALUE, context.getPortfolioStructure().getRootPortfolioNode(position)));
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.WEIGHT, target.getPosition()), getUniqueId()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "PositionWeight";
  }
}
