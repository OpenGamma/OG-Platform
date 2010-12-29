/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.PositionAccumulator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.DoubleTimeSeries;

// REVIEW kirk 2010-01-02 -- This version aggregates from the leaf positions for all inputs.
// For non-linear aggregates and large portfolios, you'll want to use a more refined
// form which loads values from sub-nodes and positions, rather than just leaf positions.

/**
 * Able to sum a particular requirement name from a set of underlying
 * positions.
 * While in general we assume that basic linear aggregates will be performed in the
 * presentation layer on demand, this Function can be used to perform aggregate
 * in the engine.
 * In addition, it is an excellent demonstration of how to write portfolio-node-specific
 * functions.
 */
public class SummingFunction extends PropertyPreservingFunction {

  @Override
  protected String[] getPreservedProperties() {
    return new String[] {ValuePropertyNames.CURRENCY};
  }

  private final String _requirementName;

  public SummingFunction(final String requirementName) {
    ArgumentChecker.notNull(requirementName, "Requirement name");
    _requirementName = requirementName;
  }

  /**
   * @return the requirementName
   */
  public String getRequirementName() {
    return _requirementName;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final PortfolioNode node = target.getPortfolioNode();
    final Set<Position> allPositions = PositionAccumulator.getAccumulatedPositions(node);
    Object currentSum = null;
    for (final Position position : allPositions) {
      final ValueRequirement requirement = new ValueRequirement(_requirementName, ComputationTargetType.POSITION, position.getUniqueIdentifier());
      final Object positionValue = inputs.getValue(requirement);
      currentSum = addValue(currentSum, positionValue);
    }
    final ComputedValue computedValue = new ComputedValue(new ValueSpecification(_requirementName, target.toSpecification(), getCompositeValueProperties(inputs.getAllValues())), currentSum);
    return Collections.singleton(computedValue);
  }

  protected Object addValue(final Object previousSum, final Object currentValue) {
    if (previousSum == null) {
      return currentValue;
    }
    if (previousSum.getClass() != currentValue.getClass()) {
      throw new IllegalArgumentException("Inputs have different value types for requirement " + _requirementName);
    }
    if (currentValue instanceof Double) {
      final Double previousDouble = (Double) previousSum;
      return previousDouble + (Double) currentValue;
    } else if (currentValue instanceof BigDecimal) {
      final BigDecimal previousDecimal = (BigDecimal) previousSum;
      return previousDecimal.add((BigDecimal) currentValue);
    } else if (currentValue instanceof DoubleTimeSeries<?>) {
      final DoubleTimeSeries<?> previousTS = (DoubleTimeSeries<?>) previousSum;
      return previousTS.add((DoubleTimeSeries<?>) currentValue);
    }
    throw new IllegalArgumentException("Can only add Doubles and BigDecimal and DoubleTimeSeries right now.");
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.PORTFOLIO_NODE;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final PortfolioNode node = target.getPortfolioNode();
    final Set<Position> allPositions = PositionAccumulator.getAccumulatedPositions(node);
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    for (final Position position : allPositions) {
      requirements.add(new ValueRequirement(_requirementName, ComputationTargetType.POSITION, position.getUniqueIdentifier(), getInputConstraint(desiredValue)));
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueSpecification result = new ValueSpecification(_requirementName, target.toSpecification(), getResultProperties());
    return Collections.singleton(result);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Set<ValueSpecification> inputs) {
    final ValueSpecification result = new ValueSpecification(_requirementName, target.toSpecification(), getCompositeSpecificationProperties(inputs));
    return Collections.singleton(result);
  }

  @Override
  public String getShortName() {
    return "Sum(" + _requirementName + ")";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE;
  }

}
