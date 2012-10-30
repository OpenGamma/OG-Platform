/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
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
import com.opengamma.financial.analytics.SumUtils;
import com.opengamma.util.money.MoneyCalculationUtils;

/**
 * 
 */
public abstract class AbstractPortfolioDailyPnLFunction extends AbstractFunction.NonCompiledInvoker {

  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractPortfolioDailyPnLFunction.class);

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    BigDecimal currentSum = BigDecimal.ZERO;
    ValueProperties currentProperties = null;
    for (ComputedValue value : inputs.getAllValues()) {
      currentSum = MoneyCalculationUtils.add(currentSum, new BigDecimal(String.valueOf(value.getValue())));
      currentProperties = SumUtils.addProperties(currentProperties, value.getSpecification().getProperties());
    }
    if (currentProperties == null) {
      return Collections.emptySet();
    }
    for (ValueSpecification valueSpec : inputs.getMissingValues()) {
      currentProperties = SumUtils.addProperties(currentProperties, valueSpec.getProperties());
    }
    currentProperties = currentProperties.copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
    final ValueSpecification valueSpecification = new ValueSpecification(ValueRequirementNames.DAILY_PNL, target.toSpecification(), currentProperties);
    final ComputedValue result = new ComputedValue(valueSpecification, currentSum.doubleValue());
    return Sets.newHashSet(result);
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final String currency = desiredValue.getConstraint(ValuePropertyNames.CURRENCY);
    if (currency == null) {
      return null;
    }
    final PortfolioNode node = target.getPortfolioNode();
    final Set<Position> allPositions = PositionAccumulator.getAccumulatedPositions(node);
    final ValueProperties constraints = ValueProperties.with(ValuePropertyNames.CURRENCY, currency).get();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    for (Position position : allPositions) {
      requirements.add(new ValueRequirement(ValueRequirementNames.DAILY_PNL, ComputationTargetType.POSITION, position.getUniqueId(), constraints));
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.DAILY_PNL, target.getPortfolioNode(), ValueProperties.withAny(ValuePropertyNames.CURRENCY).get()),
        getUniqueId()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    ValueProperties currentProperties = null;
    for (ValueSpecification input : inputs.keySet()) {
      currentProperties = SumUtils.addProperties(currentProperties, input.getProperties());
    }
    if (currentProperties == null) {
      return null;
    }
    currentProperties = currentProperties.copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.DAILY_PNL, target.toSpecification(), currentProperties));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE;
  }

}
