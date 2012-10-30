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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.PositionAccumulator;
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
import com.opengamma.util.money.MoneyCalculationUtils;

/**
 * 
 */
public abstract class AbstractPortfolioPnLFunction extends AbstractFunction.NonCompiledInvoker {

  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractPortfolioPnLFunction.class);

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    final PortfolioNode node = target.getPortfolioNode();
    final Set<Position> allPositions = PositionAccumulator.getAccumulatedPositions(node);
    BigDecimal currentSum = BigDecimal.ZERO;
    for (final Position position : allPositions) {
      final Object tradeValue = inputs.getValue(new ValueRequirement(ValueRequirementNames.PNL,
          ComputationTargetType.POSITION, position.getUniqueId()));
      currentSum = MoneyCalculationUtils.add(currentSum, new BigDecimal(String.valueOf(tradeValue)));
    }
    ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueSpecification valueSpecification = new ValueSpecification(ValueRequirementNames.PNL, target.toSpecification(), desiredValue.getConstraints());
    final ComputedValue result = new ComputedValue(valueSpecification, currentSum.doubleValue());
    return Sets.newHashSet(result);
  }

  private ValueProperties extractCurrencyProperty(ValueRequirement desiredValue) {
    return ValueProperties.with(ValuePropertyNames.CURRENCY, desiredValue.getConstraint(ValuePropertyNames.CURRENCY)).get();
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final PortfolioNode node = target.getPortfolioNode();
    final Set<Position> allPositions = PositionAccumulator.getAccumulatedPositions(node);
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    for (Position position : allPositions) {
      requirements.add(new ValueRequirement(ValueRequirementNames.PNL, ComputationTargetType.POSITION, position.getUniqueId(), extractCurrencyProperty(desiredValue)));
    }
    return requirements;
  }

  @Override
  protected ValueProperties.Builder createValueProperties() {
    final ValueProperties.Builder properties = super.createValueProperties();
    properties.withAny(ValuePropertyNames.CURRENCY);
    return properties;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PNL, target.toSpecification(), createValueProperties().get()));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE;
  }

}
