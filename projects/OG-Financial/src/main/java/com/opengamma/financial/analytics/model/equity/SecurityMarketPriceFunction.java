/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.Collections;
import java.util.Set;

import com.opengamma.core.security.Security;
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
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.MarketSecurityVisitor;
import com.opengamma.util.money.Currency;

/**
 * Provides the market price for the security of a position as a value on the position. <p>
 * See also {@link SecurityMarkCurrentFunction}
 */
public class SecurityMarketPriceFunction extends AbstractFunction.NonCompiledInvoker {

  private static MarketSecurityVisitor s_judgeOfMarketSecurities = new MarketSecurityVisitor();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
      final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final double marketValue = (Double) inputs.getValue(MarketDataRequirementNames.MARKET_VALUE);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.SECURITY_MARKET_PRICE, target.toSpecification(), desiredValue.getConstraints()), marketValue));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION_OR_TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getPositionOrTrade().getSecurity() instanceof FinancialSecurity)) {
      return false;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getPositionOrTrade().getSecurity();
    return security.accept(s_judgeOfMarketSecurities);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.singleton(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, target.getPositionOrTrade().getSecurity().getUniqueId()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getPositionOrTrade().getSecurity());
    ValueProperties valueProperties;
    if (ccy == null) {
      valueProperties = createValueProperties().get();
    } else {
      valueProperties = createValueProperties().with(ValuePropertyNames.CURRENCY, ccy.getCode()).get();
    }
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.SECURITY_MARKET_PRICE, target.toSpecification(), valueProperties));
  }

}
