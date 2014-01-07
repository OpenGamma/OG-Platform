/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
import java.util.Set;

import com.opengamma.core.position.Trade;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
public class NotionalForTradeFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final CurrencyPairs currencyPairs = OpenGammaExecutionContext.getCurrencyPairsSource(executionContext).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final Trade trade = target.getTrade();
    final CurrencyAmount ca = FinancialSecurityUtils.getNotional(trade.getSecurity(), currencyPairs, executionContext.getSecuritySource()).multipliedBy(trade.getQuantity().doubleValue());
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.NOTIONAL, target.toSpecification(), createValueProperties().get());
    return Collections.singleton(new ComputedValue(spec, ca));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.NOTIONAL, target.toSpecification(), createValueProperties().get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.emptySet();
  }

}
