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

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionAccumulator;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

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
 *
 * @author kirk
 */
public class SummingFunction
extends AbstractFunction
implements FunctionInvoker {
  private final String _requirementName;
  
  public SummingFunction(String requirementName) {
    ArgumentChecker.checkNotNull(requirementName, "Requirement name");
    _requirementName = requirementName;
  }

  /**
   * @return the requirementName
   */
  public String getRequirementName() {
    return _requirementName;
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext,
      FunctionInputs inputs, ComputationTarget target,
      Set<ValueRequirement> desiredValues) {
    PortfolioNode node = target.getPortfolioNode();
    Set<Position> allPositions = PositionAccumulator.getAccumulatedPositions(node);
    Object currentSum = null;
    for(Position position : allPositions) {
      Object positionValue = inputs.getValue(new ValueRequirement(_requirementName, ComputationTargetType.POSITION, position.getIdentityKey()));
      currentSum = addValue(currentSum, positionValue);
    }
    ComputedValue computedValue = new ComputedValue(
        new ValueSpecification(
            new ValueRequirement(_requirementName, ComputationTargetType.MULTIPLE_POSITIONS, node.getIdentityKey())),
        currentSum);
    return Collections.singleton(computedValue);
  }
  
  protected Object addValue(Object previousSum, Object currentValue) {
    if(previousSum == null) {
      return currentValue;
    }
    if(previousSum.getClass() != currentValue.getClass()) {
      throw new IllegalArgumentException("Inputs have different value types for requirement " + _requirementName);
    }
    if(currentValue instanceof Double) {
      Double previousDouble = (Double) previousSum;
      return previousDouble + (Double) currentValue;
    } else if(currentValue instanceof BigDecimal) {
      BigDecimal previousDecimal = (BigDecimal) previousSum;
      return previousDecimal.add((BigDecimal) currentValue);
    }
    throw new IllegalArgumentException("Can only add Doubles and BigDecimal right now.");
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return target.getType() == ComputationTargetType.MULTIPLE_POSITIONS;
  }
  
  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target) {
    PortfolioNode node = target.getPortfolioNode();
    Set<Position> allPositions = PositionAccumulator.getAccumulatedPositions(node);
    Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    for(Position position : allPositions) {
      requirements.add(new ValueRequirement(_requirementName, ComputationTargetType.POSITION, position.getIdentityKey()));
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context,
      ComputationTarget target,
      Set<ValueRequirement> requirements) {
    PortfolioNode node = target.getPortfolioNode();
    ValueSpecification result = new ValueSpecification(
        new ValueRequirement(_requirementName, ComputationTargetType.MULTIPLE_POSITIONS, node.getIdentityKey()));
    return Collections.singleton(result);
  }

  @Override
  public String getShortName() {
    return "Sum(" + _requirementName + ")";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.MULTIPLE_POSITIONS;
  }

}
