/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class DummyPortfolioNodeFunction extends AbstractFunction.NonCompiledInvoker {
  private final String _valueRequirement;
  private final Double _value;

  public DummyPortfolioNodeFunction(final String valueRequirement, final String value) {
    _valueRequirement = valueRequirement;
    _value = Double.parseDouble(value);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
      final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Set<ComputedValue> result = new HashSet<ComputedValue>();
    final PortfolioNode node = target.getPortfolioNode();
    result.add(new ComputedValue(new ValueSpecification(new ValueRequirement(_valueRequirement, node),
        getUniqueId()), _value));
    return result;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.PORTFOLIO_NODE;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.<ValueRequirement>emptySet();
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
      final PortfolioNode node = target.getPortfolioNode();
      results.add(new ValueSpecification(new ValueRequirement(_valueRequirement, node), getUniqueId()));
      return results;
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "DummyPortfolioNodeModel";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE;
  }
}
