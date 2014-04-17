/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.futures;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Prototype - Takes {@link ValueRequirementNames#NET_MARKET_VALUE} as input requirement, 
 * and filters out all but those Security Types that are Equity based. <p>
 * <p>
 * Applies only to Equity Security Types
 */
public class EquityNetMarketValueFunction extends AbstractFunction.NonCompiledInvoker {

  private String getOutputName() {
    return ValueRequirementNames.EQUITY_NET_MARKET_VALUE;
  }
  
  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    Security security = target.getPositionOrTrade().getSecurity();
    if ((security instanceof EquitySecurity) || 
        (security instanceof EquityOptionSecurity) || 
        (security instanceof EquityIndexOptionSecurity)) {
      return true;
    }
    return false;
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    // Get Net Market Value requirement
    Double netMarketValue = null;
    final ComputedValue inputVal = inputs.getComputedValue(ValueRequirementNames.NET_MARKET_VALUE);
    if (inputVal != null) { // Ensure the value was successfully obtained
      netMarketValue = (Double) inputVal.getValue();
    } else {
      throw new OpenGammaRuntimeException("Did not satisfy requirement," + ValueRequirementNames.NET_MARKET_VALUE + ", for trade " + target.getPositionOrTrade().getUniqueId());
    }
    // 3. Create specification and return
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueSpecification valueSpecification = new ValueSpecification(desiredValue.getValueName(), target.toSpecification(), desiredValue.getConstraints());
    return Sets.newHashSet(new ComputedValue(valueSpecification, netMarketValue));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION_OR_TRADE;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(getOutputName() , target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    // inputs provide the properties of the required security greek. These we pass through to the position
    final ValueSpecification netMarketValueSpec = inputs.keySet().iterator().next();
    if (netMarketValueSpec.getValueName() != ValueRequirementNames.NET_MARKET_VALUE) {
      return null;
    }
    final Security security = target.getPositionOrTrade().getSecurity();
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    final ValueProperties properties = netMarketValueSpec.getProperties().copy()
        .withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId())
        .withoutAny(ValuePropertyNames.CURRENCY).with(ValuePropertyNames.CURRENCY, currency)
        .get();
    return Collections.singleton(new ValueSpecification(getOutputName() , target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    if (!desiredValue.getValueName().equals(getOutputName())) {
      return null;
    }
    return Collections.singleton(new ValueRequirement(ValueRequirementNames.NET_MARKET_VALUE, target.toSpecification(),
          desiredValue.getConstraints().withoutAny(ValuePropertyNames.FUNCTION)));
  }

}
