/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
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
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MoneyCalculationUtils;

/**
 * 
 */
public abstract class AbstractPositionPnLFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    Position position = target.getPosition();
    BigDecimal currentSum = BigDecimal.ZERO;
    for (final Trade trade : position.getTrades()) {
      final Object tradeValue = inputs.getValue(new ValueRequirement(ValueRequirementNames.PNL,
          ComputationTargetType.TRADE, trade.getUniqueId()));
      currentSum = MoneyCalculationUtils.add(currentSum, new BigDecimal(String.valueOf(tradeValue)));
    }
    Currency ccy = FinancialSecurityUtils.getCurrency(position.getSecurity());
    final ValueSpecification valueSpecification;
    if (ccy == null) {
      valueSpecification = new ValueSpecification(ValueRequirementNames.PNL, target.toSpecification(), createValueProperties().get());
    } else {
      valueSpecification = new ValueSpecification(ValueRequirementNames.PNL, target.toSpecification(), createValueProperties().get());
    }
    final ComputedValue result = new ComputedValue(valueSpecification, currentSum.doubleValue());
    return Sets.newHashSet(result);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }
  
  private ValueProperties extractCurrencyProperty(ValueRequirement desiredValue) {
    String currency = desiredValue.getConstraint(ValuePropertyNames.CURRENCY);
    if (currency == null) {
      return ValueProperties.none();
    } else {
      return ValueProperties.with(ValuePropertyNames.CURRENCY, currency).get();
    }
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final Position position = target.getPosition();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    for (Trade trade : position.getTrades()) {
      requirements.add(new ValueRequirement(ValueRequirementNames.PNL, ComputationTargetType.TRADE, trade.getUniqueId(), extractCurrencyProperty(desiredValue)));
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    final Security security = target.getPosition().getSecurity();
    if (FXUtils.isFXSecurity(security)) {
      return null;
    }
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getPosition().getSecurity());
    final ValueProperties.Builder properties = createValueProperties();
    if (ccy != null) {
      properties.with(ValuePropertyNames.CURRENCY, ccy.getCode());
    }
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PNL, target.toSpecification(), properties.get()));
  }
  
}
