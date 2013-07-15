/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.value.MarketDataRequirementNames;
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
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Calculates the Value (or Dollar) Delta of a FutureSecurity. 
 * The value delta is defined as the Delta (dV/dS) multiplied by the spot, S. 
 * As dS/dS == 1, ValueDelta = S, the spot value of the security.
 * ValueDelta can be roughly described as the delta hedge of the position expressed in currency value. 
 * It indicates how much currency must be used in order to delta hedge a position. 
 */
public class FutureSecurityValueDeltaFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getSecurity() instanceof FutureSecurity) {
      return true;
    }
    return false;
  }
  
  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode())
        .get(); 
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.VALUE_DELTA, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    return Collections.singleton(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, getTargetType(), target.getUniqueId()));
  }
  
  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    ValueProperties properties = desiredValue.getConstraints().copy()
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode())
        .get();
    final ValueSpecification valueSpecification = new ValueSpecification(ValueRequirementNames.VALUE_DELTA, target.toSpecification(), properties);
    
    // Get Market Value
    final Object marketValueObject = inputs.getValue(MarketDataRequirementNames.MARKET_VALUE);
    if (marketValueObject == null) {
      throw new OpenGammaRuntimeException("Could not get market value");
    }
    final Double marketValue = (Double) marketValueObject;
    
    final ComputedValue result = new ComputedValue(valueSpecification, marketValue);
    return Collections.singleton(result);
  }
}
