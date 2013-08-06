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
import com.opengamma.financial.analytics.model.forex.FXUtils;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MoneyCalculationUtils;

/**
 *
 */
public abstract class AbstractPositionPnLFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    BigDecimal currentSum = BigDecimal.ZERO;
    for (final Trade trade : position.getTrades()) {
      final Object tradeValue = inputs.getValue(new ValueRequirement(ValueRequirementNames.PNL,
          ComputationTargetType.TRADE, trade.getUniqueId()));
      currentSum = MoneyCalculationUtils.add(currentSum, new BigDecimal(String.valueOf(tradeValue)));
    }
    return Sets.newHashSet(new ComputedValue(new ValueSpecification(ValueRequirementNames.PNL, target.toSpecification(), createValueProperties(position).get()), currentSum.doubleValue()));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getPosition().getSecurity();
    if (FXUtils.isFXSecurity(security)) {
      // Can't do FX securities with this because they don't have a single currency we can use
      return false;
    }
    return true;
  }

  protected ValueProperties.Builder createValueProperties(final Position position) {
    final ValueProperties.Builder properties = super.createValueProperties();
    final Currency ccy = FinancialSecurityUtils.getCurrency(position.getSecurity());
    if (ccy != null) {
      properties.with(ValuePropertyNames.CURRENCY, ccy.getCode());
    }
    return properties;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PNL, target.toSpecification(), createValueProperties(target.getPosition()).get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Position position = target.getPosition();
    final Currency currency = FinancialSecurityUtils.getCurrency(position.getSecurity());
    final ValueProperties constraints;
    if (currency != null) {
      constraints = ValueProperties.with(ValuePropertyNames.CURRENCY, currency.getCode()).get();
    } else {
      constraints = ValueProperties.none();
    }
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    for (final Trade trade : position.getTrades()) {
      requirements.add(new ValueRequirement(ValueRequirementNames.PNL, ComputationTargetType.TRADE, trade.getUniqueId(), constraints));
    }
    return requirements;
  }

}
