/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

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
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
public class NotionalFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final CurrencyPairs currencyPairs = OpenGammaExecutionContext.getCurrencyPairsSource(executionContext).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    CurrencyAmount ca = FinancialSecurityUtils.getNotional(target.getSecurity(), currencyPairs);
    if (desiredValue.getConstraint("Buy").equals("Negative")) {
      ca = CurrencyAmount.of(ca.getCurrency(), -ca.getAmount());
    }
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.NOTIONAL, target.toSpecification(), desiredValue.getConstraints());
    return Collections.singleton(new ComputedValue(spec, ca));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FINANCIAL_SECURITY;
  }
  
  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    Set<String> buy = context.getViewCalculationConfiguration().getDefaultProperties().getValues("Buy");
    String buyProperty = ((buy == null) || !buy.contains("Negative")) ? "Positive" : "Negative";
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.NOTIONAL, target.toSpecification(), createValueProperties().with("Buy", buyProperty).get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.emptySet();
  }

}
