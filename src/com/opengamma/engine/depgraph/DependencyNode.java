/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * A single node in a {@link DependencyGraph}. A node represents
 * a particular invocation of a particular function at runtime, producing
 * certain values.
 */
public class DependencyNode {
  private final FunctionDefinition _functionDefinition;
  private final ComputationTarget _computationTarget;
  private final Set<ValueRequirement> _inputRequirements = new HashSet<ValueRequirement>();
  private final Set<ValueSpecification> _outputValues = new HashSet<ValueSpecification>();
  /**
   * The final output values that cannot be stripped from the {@link #_outputValues} set no matter
   * whether there are no dependent nodes.
   */
  private final Set<ValueSpecification> _terminalOutputValues = new HashSet<ValueSpecification>();
  private final Set<DependencyNode> _inputNodes = new HashSet<DependencyNode>();
  private final Map<ValueRequirement, ValueSpecification> _requirementMapping =
    new HashMap<ValueRequirement, ValueSpecification>();
  private final Set<DependencyNode> _dependentNodes = new HashSet<DependencyNode>();
  
  public DependencyNode(
      FunctionCompilationContext context,
      FunctionDefinition functionDefinition,
      ComputationTarget target) {
    ArgumentChecker.notNull(functionDefinition, "Function Definition");
    ArgumentChecker.notNull(target, "Computation Target");
    if (functionDefinition.getTargetType() != target.getType()) {
      throw new IllegalArgumentException(
          "Provided function of type " + functionDefinition.getTargetType()
          + " but target of type " + target.getType());
    }
    
    _functionDefinition = functionDefinition;
    _computationTarget = target;
    _inputRequirements.addAll(_functionDefinition.getRequirements(context, _computationTarget));
    _outputValues.addAll(_functionDefinition.getResults(context, _computationTarget));
  }
  
  public void addInputNode(DependencyNode inputNode) {
    ArgumentChecker.notNull(inputNode, "Input Node");
    _inputNodes.add(inputNode);
    inputNode.addDependentNode(this); // note how we rely on the yucky class-scope encapsulation of private
  }
 
  private void addDependentNode(DependencyNode dependentNode) {
    ArgumentChecker.notNull(dependentNode, "Dependent Node");
    _dependentNodes.add(dependentNode);
  }
  
  public Set<DependencyNode> getDependentNodes() {
    return Collections.unmodifiableSet(_dependentNodes);
  }
  
  public Set<DependencyNode> getInputNodes() {
    return Collections.unmodifiableSet(_inputNodes);
  }
  
  public Set<ValueSpecification> getOutputValues() {
    return Collections.unmodifiableSet(_outputValues);
  }
  
  public Set<ValueRequirement> getInputRequirements() {
    return Collections.unmodifiableSet(_inputRequirements);
  }
  
  /**
   * @return the functionDefinition
   */
  public FunctionDefinition getFunctionDefinition() {
    return _functionDefinition;
  }

  /**
   * @return the computationTarget
   */
  public ComputationTarget getComputationTarget() {
    return _computationTarget;
  }

  public void addRequirementMapping(ValueRequirement inputRequirement, ValueSpecification actualValue) {
    ArgumentChecker.notNull(inputRequirement, "Input Value Requirement");
    ArgumentChecker.notNull(actualValue, "Actual Value Specification");
    assert inputNodeProduces(actualValue);
    _requirementMapping.put(inputRequirement, actualValue);
  }
  
  public ValueSpecification getMappedRequirement(ValueRequirement inputRequirement) {
    return _requirementMapping.get(inputRequirement);
  }
  
  protected boolean inputNodeProduces(ValueSpecification value) {
    for (DependencyNode inputNode : _inputNodes) {
      if (inputNode.getOutputValues().contains(value)) {
        return true;
      }
    }
    return false;
  }
  
  public ValueSpecification satisfiesRequirement(ValueRequirement requirement) {
    if (requirement.getTargetSpecification().getType() != getComputationTarget().getType()) {
      return null;
    }
    if (!ObjectUtils.equals(requirement.getTargetSpecification().getUniqueIdentifier(), getComputationTarget().getUniqueIdentifier())) {
      return null;
    }
    for (ValueSpecification outputSpec : _outputValues) {
      if (ObjectUtils.equals(outputSpec.getRequirementSpecification(), requirement)) {
        return outputSpec;
      }
    }
    return null;
  }
  
  public Set<ValueSpecification> removeUnnecessaryOutputs() {
    Set<ValueSpecification> unnecessaryOutputs = new HashSet<ValueSpecification>();
    for (ValueSpecification outputSpec : _outputValues) {
      if (_terminalOutputValues.contains(outputSpec)) {
        continue;
      }
      boolean isUsed = false;
      for (DependencyNode dependantNode : _dependentNodes) {
        if (dependantNode.getInputRequirements().contains(outputSpec.getRequirementSpecification())) {
          isUsed = true;
          break;
        }
      }
      if (!isUsed) {
        unnecessaryOutputs.add(outputSpec);
      }
    }
    _outputValues.removeAll(unnecessaryOutputs);
    return unnecessaryOutputs;
  }
  
  public void addTerminalOutputValue(ValueSpecification terminalOutput) {
    _terminalOutputValues.add(terminalOutput);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("DependencyNode[");
    sb.append(getFunctionDefinition().getShortName());
    sb.append(" on ");
    sb.append(getComputationTarget().toSpecification());
    sb.append("]");
    return sb.toString();
  }
  
}
