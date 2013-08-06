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
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    BigDecimal currentSum = BigDecimal.ZERO;
    for (ComputedValue input : inputs.getAllValues()) {
      currentSum = MoneyCalculationUtils.add(currentSum, new BigDecimal(String.valueOf(input.getValue())));
    }
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueSpecification valueSpecification = new ValueSpecification(ValueRequirementNames.PNL, target.toSpecification(), desiredValue.getConstraints());
    final ComputedValue result = new ComputedValue(valueSpecification, currentSum.doubleValue());
    return Sets.newHashSet(result);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final PortfolioNode node = target.getPortfolioNode();
    final Set<Position> allPositions = PositionAccumulator.getAccumulatedPositions(node);
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    final Set<String> currencies = desiredValue.getConstraints().getValues(ValuePropertyNames.CURRENCY);
    final ValueProperties constraints;
    if ((currencies == null) || currencies.isEmpty()) {
      constraints = ValueProperties.withAny(ValuePropertyNames.CURRENCY).get();
    } else {
      constraints = ValueProperties.with(ValuePropertyNames.CURRENCY, currencies).get();
    }
    for (final Position position : allPositions) {
      requirements.add(new ValueRequirement(ValueRequirementNames.PNL, ComputationTargetType.POSITION, position.getUniqueId(), constraints));
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PNL, target.toSpecification(), createValueProperties().withAny(ValuePropertyNames.CURRENCY).get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    Set<String> currencies = null;
    for (final ValueSpecification input : inputs.keySet()) {
      final Set<String> inputCurrencies = input.getProperties().getValues(ValuePropertyNames.CURRENCY);
      if ((inputCurrencies != null) && !inputCurrencies.isEmpty()) {
        if (currencies == null) {
          currencies = new HashSet<String>(inputCurrencies);
        } else {
          currencies.retainAll(inputCurrencies);
          if (currencies.isEmpty()) {
            // Inputs aren't in a common currency
            return null;
          }
        }
      }
    }
    if (currencies == null) {
      return getResults(context, target);
    } else {
      return Collections.singleton(new ValueSpecification(ValueRequirementNames.PNL, target.toSpecification(), createValueProperties().with(ValuePropertyNames.CURRENCY, currencies).get()));
    }
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE;
  }

}
