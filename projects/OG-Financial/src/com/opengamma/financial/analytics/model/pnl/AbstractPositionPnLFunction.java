/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
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
import com.opengamma.util.money.MoneyCalculationUtil;

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
      currentSum = MoneyCalculationUtil.add(currentSum, new BigDecimal(String.valueOf(tradeValue)));
    }
    final ValueSpecification valueSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL, position), getUniqueIdentifier());
    final ComputedValue result = new ComputedValue(valueSpecification, currentSum);
    return Sets.newHashSet(result);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final Position position = target.getPosition();
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      for (Trade trade : position.getTrades()) {
        requirements.add(new ValueRequirement(ValueRequirementNames.PNL, ComputationTargetType.TRADE, trade.getUniqueId()));
      }
      return requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL, target.getPosition()), getUniqueIdentifier()));
    }
    return null;
  }
  
}
