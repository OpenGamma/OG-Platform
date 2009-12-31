/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.Pair;

/**
 * 
 *
 * @author kirk
 */
public class NewDependencyGraph {
  private final ComputationTarget _computationTarget;
  private final Set<ValueSpecification> _outputValues = new HashSet<ValueSpecification>();
  private final List<NewDependencyNode> _dependencyNodes = new ArrayList<NewDependencyNode>();
  
  public NewDependencyGraph(ComputationTarget computationTarget) {
    ArgumentChecker.checkNotNull(computationTarget, "Computation target");
    _computationTarget = computationTarget;
  }

  /**
   * @return the computationTarget
   */
  public ComputationTarget getComputationTarget() {
    return _computationTarget;
  }
  
  public Set<ValueSpecification> getOutputValues() {
    return Collections.unmodifiableSet(_outputValues);
  }
  
  public Collection<NewDependencyNode> getDependencyNodes() {
    return Collections.unmodifiableList(_dependencyNodes);
  }
  
  public Pair<NewDependencyNode, ValueSpecification> getNodeProducing(ValueRequirement requirement) {
    for(NewDependencyNode depNode : _dependencyNodes) {
      ValueSpecification satisfyingSpec = depNode.satisfiesRequirement(requirement);
      if(satisfyingSpec != null) {
        return new Pair<NewDependencyNode, ValueSpecification>(depNode, satisfyingSpec);
      }
    }
    return null;
  }
  
  public void addDependencyNode(NewDependencyNode node) {
    _dependencyNodes.add(node);
    _outputValues.addAll(node.getOutputValues());
  }
  
  public Collection<NewDependencyNode> getNodes() {
    return Collections.unmodifiableCollection(_dependencyNodes);
  }

}
