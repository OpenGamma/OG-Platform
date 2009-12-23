/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.analytics.AggregatePositionFunctionDefinition;
import com.opengamma.engine.analytics.FunctionDefinition;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.AnalyticValueDefinitionComparator;
import com.opengamma.engine.analytics.PositionFunctionDefinition;
import com.opengamma.engine.analytics.PrimitiveFunctionDefinition;
import com.opengamma.engine.analytics.SecurityFunctionDefinition;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.util.ArgumentChecker;

/**
 * An individual node in any dependency graph.
 *
 * @author kirk
 */
public class DependencyNode {
  private final ComputationTargetType _computationTargetType;
  private final Object _computationTarget;
  private final FunctionDefinition _function;
  private final Set<AnalyticValueDefinition<?>> _outputValues =
    new HashSet<AnalyticValueDefinition<?>>();
  private final Set<AnalyticValueDefinition<?>> _inputValues =
    new HashSet<AnalyticValueDefinition<?>>();
  private final Set<DependencyNode> _inputNodes =
    new HashSet<DependencyNode>();
  private final Map<AnalyticValueDefinition<?>, AnalyticValueDefinition<?>> _resolvedInputs =
    new HashMap<AnalyticValueDefinition<?>, AnalyticValueDefinition<?>>();
  
  @SuppressWarnings("unchecked")
  public DependencyNode(FunctionDefinition function, Object computationTarget) {
    ArgumentChecker.checkNotNull(function, "Analytic Function Definition");
    _function = function;
    _computationTargetType = function.getTargetType();
    _computationTarget = computationTarget;
    if(!ComputationTargetType.isCompatible(function.getTargetType(), computationTarget)) {
      throw new IllegalArgumentException("Computation target " + computationTarget + " not compatible with function's target type " + function.getTargetType());
    }
    switch(_computationTargetType) {
    case PRIMITIVE:
      PrimitiveFunctionDefinition primitiveFunction = (PrimitiveFunctionDefinition) function;
      _inputValues.addAll(primitiveFunction.getInputs());
      _outputValues.addAll(primitiveFunction.getPossibleResults());
      break;
    case SECURITY:
      SecurityFunctionDefinition securityFunction = (SecurityFunctionDefinition) function;
      _inputValues.addAll(securityFunction.getInputs((Security) computationTarget));
      _outputValues.addAll(securityFunction.getPossibleResults((Security) computationTarget));
      break;
    case POSITION:
      PositionFunctionDefinition positionFunction = (PositionFunctionDefinition) function;
      _inputValues.addAll(positionFunction.getInputs((Position) computationTarget));
      _outputValues.addAll(positionFunction.getPossibleResults((Position) computationTarget));
      break;
    case MULTIPLE_POSITIONS:
      AggregatePositionFunctionDefinition aggFunction = (AggregatePositionFunctionDefinition) function;
      _inputValues.addAll(aggFunction.getInputs((Collection) computationTarget));
      _outputValues.addAll(aggFunction.getPossibleResults((Collection) computationTarget));
      break;
    }
  }
  
  /**
   * @return the outputValues
   */
  public Set<AnalyticValueDefinition<?>> getOutputValues() {
    return _outputValues;
  }
  /**
   * @return the inputValues
   */
  public Set<AnalyticValueDefinition<?>> getInputValues() {
    return _inputValues;
  }
  /**
   * @return the inputNodes
   */
  public Set<DependencyNode> getInputNodes() {
    return _inputNodes;
  }
  
  /**
   * @return the function
   */
  public FunctionDefinition getFunction() {
    return _function;
  }

  /**
   * @return the computationTargetType
   */
  public ComputationTargetType getComputationTargetType() {
    return _computationTargetType;
  }

  /**
   * @return the computationTarget
   */
  public Object getComputationTarget() {
    return _computationTarget;
  }

  public void addOutputValues(Collection<AnalyticValueDefinition<?>> outputValues) {
    if(outputValues == null) {
      return;
    }
    _outputValues.addAll(outputValues);
  }

  public void addInputValues(Collection<AnalyticValueDefinition<?>> inputValues) {
    if(inputValues == null) {
      return;
    }
    _inputValues.addAll(inputValues);
  }
  
  public Map<AnalyticValueDefinition<?>, AnalyticValueDefinition<?>> getResolvedInputs() {
    return Collections.unmodifiableMap(_resolvedInputs);
  }
  
  public AnalyticValueDefinition<?> getResolvedInput(AnalyticValueDefinition<?> requiredInput) {
    return _resolvedInputs.get(requiredInput);
  }
  
  public AnalyticValueDefinition<?> getBestOutput(AnalyticValueDefinition<?> input) {
    for(AnalyticValueDefinition<?> outputValue: getOutputValues()) {
      if(AnalyticValueDefinitionComparator.matches(input, outputValue)) {
        return outputValue;
      }
    }
    return null;
  }
  
  public void addInputNode(AnalyticValueDefinition<?> satisfyingInput, DependencyNode inputNode) {
    if(satisfyingInput == null) {
      throw new NullPointerException("All input nodes must satisfy an input value required");
    }
    if(inputNode == null) {
      throw new NullPointerException("Must specify a function to produce the input.");
    }
    _inputNodes.add(inputNode);
    _resolvedInputs.put(satisfyingInput, inputNode.getBestOutput(satisfyingInput));
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("DependencyNode[");
    sb.append(getFunction().getShortName());
    sb.append(" (").append(getComputationTargetType()).append(")");
    if(getComputationTarget() != null) {
      sb.append(" on ").append(getComputationTarget());
    }
    sb.append("]");
    return sb.toString();
  }

}
