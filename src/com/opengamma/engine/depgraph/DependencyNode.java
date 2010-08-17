/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * A single node in a {@link DependencyGraph}. A node represents
 * a particular invocation of a particular function at runtime, producing
 * certain values.
 * <p>
 * The same DependencyNode can belong to multiple DependencyGraphs
 * due to the possibility of sub-graphing.
 */
public class DependencyNode {
  
  // BELOW: COMPLETELY IMMUTABLE VARIABLES 
  
  private final FunctionDefinition _functionDefinition;
  private final ComputationTarget _computationTarget;
  private final Set<ValueSpecification> _inputValues;
  
  // COMPLETELY IMMUTABLE VARIABLES END
  
  // BELOW: EVEN THOUGH VARIABLE ITSELF IS FINAL, CONTENTS ARE MUTABLE.
  
  private final Set<DependencyNode> _inputNodes = new HashSet<DependencyNode>();
  private final Set<DependencyNode> _dependentNodes = new HashSet<DependencyNode>();
  
  private final Set<ValueSpecification> _outputValues;
  
  /**
   * The final output values that cannot be stripped from the {@link #_outputValues} set no matter
   * whether there are no dependent nodes.
   */
  private final Set<ValueSpecification> _terminalOutputValues = new HashSet<ValueSpecification>();
  
  // MUTABLE CONTENTS VARIABLES END
  
  public DependencyNode(FunctionDefinition functionDefinition,
      ComputationTarget target,
      Set<DependencyNode> inputNodes,
      Set<ValueSpecification> inputValues,
      Set<ValueSpecification> outputValues) {
    ArgumentChecker.notNull(functionDefinition, "Function Definition");
    ArgumentChecker.notNull(target, "Computation Target");
    if (functionDefinition.getTargetType() != target.getType()) {
      throw new IllegalArgumentException(
          "Provided function of type " + functionDefinition.getTargetType()
          + " but target of type " + target.getType());
    }
    ArgumentChecker.notNull(inputNodes, "Input nodes");
    ArgumentChecker.notNull(inputValues, "Input values");
    ArgumentChecker.notNull(outputValues, "Output values");
    
    _functionDefinition = functionDefinition;
    _computationTarget = target;
    _inputValues = new HashSet<ValueSpecification>(inputValues);
    _outputValues = new HashSet<ValueSpecification>(outputValues);
    
    for (DependencyNode inputNode : inputNodes) {
      addInputNode(inputNode);      
    }
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
  
  public Set<ValueSpecification> getTerminalOutputValues() {
    return Collections.unmodifiableSet(_terminalOutputValues);
  }
  
  public Set<ValueRequirement> getOutputRequirements() {
    Set<ValueRequirement> outputRequirements = new HashSet<ValueRequirement>();
    for (ValueSpecification outputValue : getOutputValues()) {
      outputRequirements.add(outputValue.getRequirementSpecification());
    }
    return outputRequirements;
  }
  
  public Set<ValueSpecification> getInputValues() {
    return Collections.unmodifiableSet(_inputValues);
  }
  
  public Set<ValueRequirement> getInputRequirements() {
    Set<ValueRequirement> inputRequirements = new HashSet<ValueRequirement>();
    for (ValueSpecification outputValue : getInputValues()) {
      inputRequirements.add(outputValue.getRequirementSpecification());
    }
    return inputRequirements;
  }
  
  /**
   * @return The returned {@code ValueRequirements} only tell you what the function
   * of this <i>this</i> node requires. They tell you nothing about the 
   * functions of any child nodes. 
   */
  public Set<ValueRequirement> getRequiredLiveData() {
    return _functionDefinition.getRequiredLiveData();
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

  public ValueSpecification resolveInput(ValueRequirement requirement) {
    for (ValueSpecification inputSpec : getInputValues()) {
      if (ObjectUtils.equals(inputSpec.getRequirementSpecification(), requirement)) {
        return inputSpec;
      }
    }
    return null;
  }
  
  public ValueSpecification resolveOutput(ValueRequirement requirement) {
    for (ValueSpecification outputSpec : getOutputValues()) {
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
        if (dependantNode.getInputValues().contains(outputSpec)) {
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
  
  /**
   * Marks an output as terminal, meaning that it cannot be pruned. If this node already belongs to a graph, use
   * {@link DependencyGraph#addTerminalOutputValue(ValueSpecification)}. 
   * 
   * @param terminalOutput  the output to mark as terminal
   */
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
