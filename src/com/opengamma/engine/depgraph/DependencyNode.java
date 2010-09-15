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
import com.opengamma.engine.function.ParameterizedFunction;
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

  private final ComputationTarget _computationTarget;

  // COMPLETELY IMMUTABLE VARIABLES END

  private ParameterizedFunction _function;

  // BELOW: EVEN THOUGH VARIABLE ITSELF IS FINAL, CONTENTS ARE MUTABLE.

  private final Set<ValueSpecification> _inputValues = new HashSet<ValueSpecification>();
  private final Set<ValueSpecification> _outputValues = new HashSet<ValueSpecification>();

  private final Set<DependencyNode> _inputNodes = new HashSet<DependencyNode>();
  private final Set<DependencyNode> _dependentNodes = new HashSet<DependencyNode>();

  /**
   * The final output values that cannot be stripped from the {@link #_outputValues} set no matter
   * whether there are no dependent nodes.
   */
  private final Set<ValueSpecification> _terminalOutputValues = new HashSet<ValueSpecification>();

  // MUTABLE CONTENTS VARIABLES END

  public DependencyNode(ComputationTarget target) {
    ArgumentChecker.notNull(target, "Computation Target");
    _computationTarget = target;
  }

  public void addInputNodes(Set<DependencyNode> inputNodes) {
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

  public DependencyNode getDependentNode() {
    if (_dependentNodes.isEmpty()) {
      return null;
    } else if (_dependentNodes.size() > 1) {
      throw new IllegalStateException("More than one dependent node");
    } else {
      return _dependentNodes.iterator().next();
    }
  }

  public Set<DependencyNode> getInputNodes() {
    return Collections.unmodifiableSet(_inputNodes);
  }

  public void addOutputValues(Set<ValueSpecification> outputValues) {
    for (ValueSpecification outputValue : outputValues) {
      addOutputValue(outputValue);
    }
  }

  public void addOutputValue(ValueSpecification outputValue) {
    ArgumentChecker.notNull(outputValue, "Output value");
    _outputValues.add(outputValue);
  }

  /* package */void addInputValue(ValueSpecification inputValue) {
    ArgumentChecker.notNull(inputValue, "Input value");
    _inputValues.add(inputValue);
  }

  public Set<ValueSpecification> getOutputValues() {
    return Collections.unmodifiableSet(_outputValues);
  }

  public Set<ValueSpecification> getTerminalOutputValues() {
    return Collections.unmodifiableSet(_terminalOutputValues);
  }

  public Set<ValueRequirement> getOutputRequirements() {
    Set<ValueRequirement> outputRequirements = new HashSet<ValueRequirement>();
    for (ValueSpecification outputValue : _outputValues) {
      outputRequirements.add(outputValue.getRequirementSpecification());
    }
    return outputRequirements;
  }

  public Set<ValueSpecification> getInputValues() {
    return Collections.unmodifiableSet(_inputValues);
  }

  public boolean hasInputValue(final ValueSpecification specification) {
    return _inputValues.contains(specification);
  }

  public Set<ValueRequirement> getInputRequirements() {
    Set<ValueRequirement> inputRequirements = new HashSet<ValueRequirement>();
    for (ValueSpecification outputValue : _inputValues) {
      inputRequirements.add(outputValue.getRequirementSpecification());
    }
    return inputRequirements;
  }

  /**
   * @return The returned {@code ValueSpecifications} only tell you what the function
   * of this <i>this</i> node requires. They tell you nothing about the 
   * functions of any child nodes. 
   */
  public Set<ValueSpecification> getRequiredLiveData() {
    return _function.getFunction().getRequiredLiveData();
  }

  /**
   * @return the function
   */
  public ParameterizedFunction getFunction() {
    return _function;
  }

  /**
   * Uses default parameters to invoke the function. Useful in tests.
   * @param function Function to be invoked
   */
  public void setFunction(FunctionDefinition function) {
    setFunction(new ParameterizedFunction(function, function.getDefaultParameters()));
  }

  public void setFunction(ParameterizedFunction function) {
    ArgumentChecker.notNull(function, "Function");
    if (_function != null) {
      throw new IllegalStateException("The function was already set");
    }

    if (function.getFunction().getTargetType() != getComputationTarget().getType()) {
      throw new IllegalArgumentException("Provided function of type " + function.getFunction().getTargetType() + " but target of type " + getComputationTarget().getType());
    }

    _function = function;
  }

  /**
   * @return the computationTarget
   */
  public ComputationTarget getComputationTarget() {
    return _computationTarget;
  }

  public ValueSpecification resolveInput(ValueRequirement requirement) {
    for (ValueSpecification inputSpec : _inputValues) {
      if (ObjectUtils.equals(inputSpec.getRequirementSpecification(), requirement)) {
        return inputSpec;
      }
    }
    return null;
  }

  public ValueSpecification resolveOutput(ValueRequirement requirement) {
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
        if (dependantNode.hasInputValue(outputSpec)) {
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
    if (getFunction() != null) {
      sb.append(getFunction().getFunction().getShortName());
    } else {
      sb.append("<null function>");
    }
    sb.append(" on ");
    sb.append(getComputationTarget().toSpecification());
    sb.append("]");
    return sb.toString();
  }

}
