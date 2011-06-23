/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.tuple.DoublesPair;

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
    return new String[] {ValuePropertyNames.CUBE,
                         ValuePropertyNames.CURRENCY,
                         ValuePropertyNames.CURVE,
                         ValuePropertyNames.CURVE_CURRENCY,
                         YieldCurveFunction.PROPERTY_FORWARD_CURVE,
                         YieldCurveFunction.PROPERTY_FUNDING_CURVE};
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
      final ValueRequirement requirement = new ValueRequirement(_requirementName, ComputationTargetType.POSITION, position.getUniqueId());
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
    } else if (currentValue instanceof DoubleLabelledMatrix1D) {
      final DoubleLabelledMatrix1D previousMatrix = (DoubleLabelledMatrix1D) previousSum;
      final DoubleLabelledMatrix1D currentMatrix = (DoubleLabelledMatrix1D) currentValue;
      return previousMatrix.add(currentMatrix);
    } else if (currentValue instanceof LocalDateLabelledMatrix1D) {
      final LocalDateLabelledMatrix1D previousMatrix = (LocalDateLabelledMatrix1D) previousSum;
      final LocalDateLabelledMatrix1D currentMatrix = (LocalDateLabelledMatrix1D) currentValue;
      return previousMatrix.add(currentMatrix);
    } else if (currentValue instanceof ZonedDateTimeLabelledMatrix1D) {
      final ZonedDateTimeLabelledMatrix1D previousMatrix = (ZonedDateTimeLabelledMatrix1D) previousSum;
      final ZonedDateTimeLabelledMatrix1D currentMatrix = (ZonedDateTimeLabelledMatrix1D) currentValue;
      return previousMatrix.add(currentMatrix);
    } else if (_requirementName.equals(ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY)) { //TODO this should probably not be done like this
      @SuppressWarnings("unchecked")
      final Map<String, List<DoublesPair>> previousMap = (Map<String, List<DoublesPair>>) previousSum;
      @SuppressWarnings("unchecked")
      final Map<String, List<DoublesPair>> currentMap = (Map<String, List<DoublesPair>>) currentValue;
      final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
      for (final String name : previousMap.keySet()) {
        final List<DoublesPair> temp = new ArrayList<DoublesPair>();
        for (final DoublesPair pair : previousMap.get(name)) {
          temp.add(pair);
        }
        if (currentMap.containsKey(name)) {
          for (final DoublesPair pair : currentMap.get(name)) {
            temp.add(pair);
          }
        }
        result.put(name, temp);
      }
      for (final String name : currentMap.keySet()) {
        if (!result.containsKey(name)) {
          final List<DoublesPair> temp = new ArrayList<DoublesPair>();
          for (final DoublesPair pair : currentMap.get(name)) {
            temp.add(pair);
          }
          result.put(name, temp);
        }
      }
    }
    throw new IllegalArgumentException("Can only add Doubles, BigDecimal, DoubleTimeSeries and LabelledMatrix1D (Double, LocalDate and ZonedDateTime), " +
        "or present value curve sensitivities right now.");
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
      requirements.add(new ValueRequirement(_requirementName, ComputationTargetType.POSITION, position.getUniqueId(), getInputConstraint(desiredValue)));
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueSpecification result = new ValueSpecification(_requirementName, target.toSpecification(), getResultProperties());
    return Collections.singleton(result);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification result = new ValueSpecification(_requirementName, target.toSpecification(), getCompositeSpecificationProperties(inputs.keySet()));
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
