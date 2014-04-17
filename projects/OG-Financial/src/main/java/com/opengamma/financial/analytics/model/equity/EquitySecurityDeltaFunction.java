/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.Collections;
import java.util.Set;

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
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Provides sensitivity of EquitySecurity price with respect to itself, i.e. always unity. This is essential in order to show aggregate position in this underlying in a derivatives portfolio.
 * 
 * @author casey
 */
public class EquitySecurityDeltaFunction extends AbstractFunction.NonCompiledInvoker {

  private String getValueRequirementName() {
    return ValueRequirementNames.DELTA;
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueSpecification valueSpecification = new ValueSpecification(getValueRequirementName(), target.toSpecification(), desiredValue.getConstraints());
    final ComputedValue result = new ComputedValue(valueSpecification, 1.0);
    return Sets.newHashSet(result);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.EQUITY_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), createValueProperties().get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    return Collections.emptySet();
  }

}
