/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.analytics.AnalyticFunction;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.security.Security;

/**
 * An individual node in any dependency graph.
 *
 * @author kirk
 */
public class DependencyNode {
  private final AnalyticFunction _function;
  private final Set<AnalyticValueDefinition<?>> _outputValues =
    new HashSet<AnalyticValueDefinition<?>>();
  private final Set<AnalyticValueDefinition<?>> _inputValues =
    new HashSet<AnalyticValueDefinition<?>>();
  private final Set<DependencyNode> _inputNodes =
    new HashSet<DependencyNode>();
  private final Map<AnalyticValueDefinition<?>, DependencyNode> _inputNodesByValue =
    new HashMap<AnalyticValueDefinition<?>, DependencyNode>();
  
  public DependencyNode(AnalyticFunction function, Security security) {
    if(function == null) {
      throw new NullPointerException("Must provide a function for this node.");
    }
    _function = function;
    _outputValues.addAll(function.getPossibleResults());
    _inputValues.addAll(function.getInputs(security));
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
   * @return the inputNodesByValue
   */
  public Map<AnalyticValueDefinition<?>, DependencyNode> getInputNodesByValue() {
    return _inputNodesByValue;
  }
  
  /**
   * @return the function
   */
  public AnalyticFunction getFunction() {
    return _function;
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
  
  public void addInputNode(AnalyticValueDefinition<?> satisfyingInput, DependencyNode inputNode) {
    if(satisfyingInput == null) {
      throw new NullPointerException("All input nodes must satisfy an input value required");
    }
    if(inputNode == null) {
      throw new NullPointerException("Must specify a function to produce the input.");
    }
    _inputNodes.add(inputNode);
    _inputNodesByValue.put(satisfyingInput, inputNode);
  }
}
