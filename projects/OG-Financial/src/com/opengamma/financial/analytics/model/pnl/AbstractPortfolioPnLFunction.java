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
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.PortfolioEquityPnLFunction;
import com.opengamma.util.money.MoneyCalculationUtil;

/**
 * 
 */
public abstract class AbstractPortfolioPnLFunction extends AbstractFunction.NonCompiledInvoker {
  
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioEquityPnLFunction.class);

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    final PortfolioNode node = target.getPortfolioNode();
    final Set<Position> allPositions = PositionAccumulator.getAccumulatedPositions(node);
    BigDecimal currentSum = BigDecimal.ZERO;
    for (final Position position : allPositions) {
      final Object tradeValue = inputs.getValue(new ValueRequirement(ValueRequirementNames.PNL,
          ComputationTargetType.POSITION, position.getUniqueId()));
      currentSum = MoneyCalculationUtil.add(currentSum, new BigDecimal(String.valueOf(tradeValue)));
    }
    final ValueSpecification valueSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL, node), getUniqueId());
    final ComputedValue result = new ComputedValue(valueSpecification, currentSum);
    return Sets.newHashSet(result);
  }
  
  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final PortfolioNode node = target.getPortfolioNode();
      final Set<Position> allPositions = PositionAccumulator.getAccumulatedPositions(node);
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      for (Position position : allPositions) {
        requirements.add(new ValueRequirement(ValueRequirementNames.PNL, ComputationTargetType.POSITION, position.getUniqueId()));
      }
      return requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL, target.getPortfolioNode()),
        getUniqueId()));
    }
    return null;
  }
    
  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE;
  }

}
