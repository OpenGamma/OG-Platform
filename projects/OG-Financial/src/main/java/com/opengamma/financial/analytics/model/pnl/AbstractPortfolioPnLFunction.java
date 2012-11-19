/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE;
  }

  @Override
  protected ValueProperties.Builder createValueProperties() {
    final ValueProperties.Builder properties = super.createValueProperties();
    properties.withAny(ValuePropertyNames.CURRENCY);
    return properties;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PNL, target.toSpecification(), createValueProperties().get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final PortfolioNode node = target.getPortfolioNode();
    // TODO: We don't need the accumulated positions - the object identifiers only would suffice (don't need to go to both databases!)
    final Set<Position> allPositions = PositionAccumulator.getAccumulatedPositions(node);
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    final ValueProperties constraints;
    final Set<String> currencies = desiredValue.getConstraints().getValues(ValuePropertyNames.CURRENCY);
    if (currencies == null) {
      constraints = ValueProperties.withOptional(ValuePropertyNames.CURRENCY).get();
    } else if (currencies.isEmpty()) {
      if (desiredValue.getConstraints().isOptional(ValuePropertyNames.CURRENCY)) {
        constraints = ValueProperties.withOptional(ValuePropertyNames.CURRENCY).get();
      } else {
        constraints = ValueProperties.withAny(ValuePropertyNames.CURRENCY).get();
      }
    } else {
      constraints = ValueProperties.with(ValuePropertyNames.CURRENCY, currencies).get();
    }
    for (final Position position : allPositions) {
      requirements.add(new ValueRequirement(ValueRequirementNames.PNL, ComputationTargetType.POSITION, position.getUniqueId().toLatest(), constraints));
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    Set<String> currency = null;
    for (final ValueSpecification input : inputs.keySet()) {
      final Set<String> inputCurrency = input.getProperties().getValues(ValuePropertyNames.CURRENCY);
      if (inputCurrency != null) {
        if (inputCurrency.isEmpty()) {
          currency = Collections.emptySet();
        } else {
          if ((currency == null) || currency.isEmpty()) {
            currency = new HashSet<String>(inputCurrency);
          } else {
            final Iterator<String> itr = currency.iterator();
            while (itr.hasNext()) {
              if (!inputCurrency.contains(itr.next())) {
                itr.remove();
              }
            }
            if (currency.isEmpty()) {
              // Not got homogenous currencies on the inputs
              return null;
            }
          }
        }
      } else {
        if (currency != null) {
          // Not got full omission of currency information on the inputs
          return null;
        }
      }
    }
    final ValueProperties.Builder properties = createValueProperties();
    properties.withoutAny(ValuePropertyNames.CURRENCY);
    if (currency != null) {
      if (currency.isEmpty()) {
        properties.withAny(ValuePropertyNames.CURRENCY);
      } else {
        properties.with(ValuePropertyNames.CURRENCY, currency);
      }
    }
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PNL, target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    BigDecimal currentSum = BigDecimal.ZERO;
    for (final ComputedValue input : inputs.getAllValues()) {
      currentSum = MoneyCalculationUtils.add(currentSum, new BigDecimal(String.valueOf(input.getValue())));
    }
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueSpecification valueSpecification = new ValueSpecification(ValueRequirementNames.PNL, target.toSpecification(), desiredValue.getConstraints());
    final ComputedValue result = new ComputedValue(valueSpecification, currentSum.doubleValue());
    return Sets.newHashSet(result);
  }

}
