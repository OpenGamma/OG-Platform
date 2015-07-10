/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import static com.opengamma.engine.value.ValueRequirementNames.CURRENCY_PAIRS;

import java.util.Collections;
import java.util.Set;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Function that returns the notional for a security. If the
 */
public class NotionalFunction extends AbstractFunction.NonCompiledInvoker {
  /** Property indicating whether this security has been bought or sold. */
  public static final String PROPERTY_BUY = "Buy";
  /** Indicates whether the notional should be negated */
  public static final String NEGATIVE = "Negative";
  /** Indicates whether the notional is of the correct sign */
  public static final String POSITIVE = "Positive";

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final CurrencyPairs currencyPairs = (CurrencyPairs) inputs.getValue(CURRENCY_PAIRS);
    SecuritySource securitySource = executionContext.getSecuritySource();
    final CurrencyAmount ca = FinancialSecurityUtils.getNotional(target.getSecurity(), currencyPairs, securitySource);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.NOTIONAL, target.toSpecification(), desiredValue.getConstraints().copy().get());
    if (desiredValue.getConstraint(PROPERTY_BUY).equals(NEGATIVE)) {
      return Collections.singleton(new ComputedValue(spec, ca.multipliedBy(-1)));
    }
    return Collections.singleton(new ComputedValue(spec, ca));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FINANCIAL_SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return context.getViewCalculationConfiguration() != null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Set<String> buy = context.getViewCalculationConfiguration().getDefaultProperties().getValues(PROPERTY_BUY);
    final String buyProperty = ((buy == null) || !buy.contains(NEGATIVE)) ? POSITIVE : NEGATIVE;
    final ValueProperties properties = createValueProperties().with(PROPERTY_BUY, buyProperty).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.NOTIONAL, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties properties = ValueProperties.builder().with(CurrencyPairsFunction.CURRENCY_PAIRS_NAME, CurrencyPairs.DEFAULT_CURRENCY_PAIRS).get();
    return Collections.singleton(new ValueRequirement(CURRENCY_PAIRS, ComputationTargetSpecification.NULL, properties));
  }

}
