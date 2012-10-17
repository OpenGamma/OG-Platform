/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class PositionWeightFromNAVFunction extends AbstractFunction.NonCompiledInvoker {
  private final double _nav;

  public PositionWeightFromNAVFunction(final String nav) {
    Validate.notNull(nav, "nav");
    _nav = Double.parseDouble(nav);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
      final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Object fairValueObj = inputs.getValue(ValueRequirementNames.FAIR_VALUE);
    if (fairValueObj != null) {
      final double fairValue = (Double) fairValueObj;
      return Sets.newHashSet(new ComputedValue(new ValueSpecification(ValueRequirementNames.WEIGHT, target.toSpecification(), createValueProperties().get()), fairValue / _nav));
    }
    return null;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.singleton(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, target.toSpecification()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.WEIGHT, target.toSpecification(), createValueProperties().get()));
  }

  @Override
  public String getShortName() {
    return "PositionWeightFromNAV";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }
}
