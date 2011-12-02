/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.PositionAccumulator;
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
import com.opengamma.util.money.MoneyCalculationUtils;

/**
 * 
 */
public abstract class AbstractPortfolioDailyPnLFunction extends AbstractFunction.NonCompiledInvoker {

  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractPortfolioDailyPnLFunction.class);

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    final PortfolioNode node = target.getPortfolioNode();
    final Set<Position> allPositions = PositionAccumulator.getAccumulatedPositions(node);
    BigDecimal currentSum = BigDecimal.ZERO;
    for (final Position position : allPositions) {
      final Object tradeValue = inputs.getValue(new ValueRequirement(ValueRequirementNames.DAILY_PNL,
          ComputationTargetType.POSITION, position.getUniqueId()));
      currentSum = MoneyCalculationUtils.add(currentSum, new BigDecimal(String.valueOf(tradeValue)));
    }
    ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueSpecification valueSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.DAILY_PNL, node, extractCurrencyProperty(desiredValue)), getUniqueId());
    final ComputedValue result = new ComputedValue(valueSpecification, currentSum.doubleValue());
    return Sets.newHashSet(result);
  }

  private ValueProperties extractCurrencyProperty(ValueRequirement desiredValue) {
    return ValueProperties.with(ValuePropertyNames.CURRENCY, desiredValue.getConstraint(ValuePropertyNames.CURRENCY)).get();
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final PortfolioNode node = target.getPortfolioNode();
      final Set<Position> allPositions = PositionAccumulator.getAccumulatedPositions(node);
      final ValueProperties constraints = extractCurrencyProperty(desiredValue);
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      for (Position position : allPositions) {
        requirements.add(new ValueRequirement(ValueRequirementNames.DAILY_PNL, ComputationTargetType.POSITION, position.getUniqueId(), constraints));
      }
      return requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.DAILY_PNL, target.getPortfolioNode(), ValueProperties.withAny(ValuePropertyNames.CURRENCY).get()),
          getUniqueId()));
    }
    return null;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE;
  }

}
