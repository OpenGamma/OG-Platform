/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;


/**
 * Provides the market price for the security of a position as a value on the position
 */
public class InterestRateFutureOptionMarketUnderlyingPriceFunction extends AbstractFunction.NonCompiledInvoker {
  
  private SecuritySource _securitySource;
  
  @Override
  public void init(final FunctionCompilationContext context) {
    _securitySource = OpenGammaCompilationContext.getSecuritySource(context);
  }
  
  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs,
      ComputationTarget target, Set<ValueRequirement> desiredValues) {
    Double marketValue = (Double) inputs.getValue(getRequirement(target));
    return Collections.singleton(new ComputedValue(getSpecification(target), marketValue));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.POSITION) {
      return false;
    }
    return target.getPositionOrTrade().getSecurity() instanceof IRFutureOptionSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    ValueRequirement valueRequirement = getRequirement(target);
    return Collections.singleton(valueRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    ValueSpecification spec = getSpecification(target);
    return Collections.singleton(spec);
  }

  private ValueSpecification getSpecification(ComputationTarget target) {
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getPosition().getSecurity());
    ValueProperties valueProperties;
    if (ccy == null) {
      valueProperties = createValueProperties().get();
    } else {
      valueProperties = createValueProperties().with(ValuePropertyNames.CURRENCY, ccy.getCode()).get();
    }
    return new ValueSpecification(new ValueRequirement(ValueRequirementNames.UNDERLYING_MARKET_PRICE,
        target.getPosition(), valueProperties), getUniqueId());
  }
  
  private ValueRequirement getRequirement(ComputationTarget target) {
    final IRFutureOptionSecurity irfo = (IRFutureOptionSecurity) target.getPositionOrTrade().getSecurity();
    final ExternalId underlyingID = irfo.getUnderlyingId();
    final Security underlyingSec = _securitySource.getSingle(ExternalIdBundle.of(underlyingID));
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, underlyingSec);
  }

}
