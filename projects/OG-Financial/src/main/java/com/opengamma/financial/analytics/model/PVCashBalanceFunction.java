/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.cash.CashBalanceSecurity;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Function that produces present values for cash balances. The present value
 * is defined to be equal to the notional. 
 */
public class PVCashBalanceFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final CashBalanceSecurity security = (CashBalanceSecurity) target.getSecurity();
    final double pv = security.getAmount();
    final ValueProperties properties = createValueProperties()
        .with(CURRENCY, security.getCurrency().getCode())
        .get();
    final ValueSpecification spec = new ValueSpecification(PRESENT_VALUE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, pv));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.CASH_BALANCE_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .with(CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode())
        .get();
    return Collections.singleton(new ValueSpecification(PRESENT_VALUE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.emptySet();
  }

}
