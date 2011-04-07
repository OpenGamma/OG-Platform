/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/**
 * State required by the {@link DependencyGraphBuilder} to iterate over and backtrack through
 * alternative resolutions to node requirements.
 */
/* package */final class ResolutionState {

  /**
   * 
   */
  public static final class Node {

    private final ValueSpecification _valueSpecification;
    private final DependencyNode _dependencyNode;
    private final ParameterizedFunction _parameterizedFunction;
    private Collection<ResolutionState> _inputStates;

    private Node(final ValueSpecification valueSpecification, final DependencyNode dependencyNode) {
      _valueSpecification = valueSpecification;
      _dependencyNode = dependencyNode;
      _parameterizedFunction = null;
    }

    private Node(final ValueSpecification valueSpecification, final ParameterizedFunction parameterizedFunction, final DependencyNode dependencyNode) {
      _valueSpecification = valueSpecification;
      _parameterizedFunction = parameterizedFunction;
      _dependencyNode = dependencyNode;
      dependencyNode.setFunction(parameterizedFunction);
      dependencyNode.addOutputValue(valueSpecification);
    }

    public ValueSpecification getValueSpecification() {
      return _valueSpecification;
    }

    public DependencyNode getDependencyNode() {
      return _dependencyNode;
    }

    public ParameterizedFunction getParameterizedFunction() {
      return _parameterizedFunction;
    }

    public Collection<ResolutionState> getInputStates() {
      return _inputStates;
    }

    public void dimInputState(final int size) {
      _inputStates = new ArrayList<ResolutionState>(size);
    }

    public void addInputState(final ResolutionState inputState) {
      _inputStates.add(inputState);
    }

    private boolean removeDeepest() {
      if ((_inputStates == null) || _inputStates.isEmpty()) {
        return false;
      }
      for (ResolutionState inputState : _inputStates) {
        if (inputState.removeDeepest()) {
          return true;
        }
      }
      return false;
    }

    @Override
    public String toString() {
      return ReflectionToStringBuilder.toString(this);
    }

  }

  public abstract static class LazyPopulator {

    protected abstract boolean more();

  }

  private final ValueRequirement _valueRequirement;
  private final List<Node> _nodes = new LinkedList<Node>();
  private Pair<DependencyNode, ValueSpecification> _lastValid;
  private LazyPopulator _lazyPopulator;

  public ResolutionState(final ValueRequirement valueRequirement) {
    _valueRequirement = valueRequirement;
  }

  public ResolutionState(final ValueRequirement valueRequirement, final ValueSpecification valueSpecification, final ParameterizedFunction parameterizedFunction, final DependencyNode dependencyNode) {
    this(valueRequirement);
    final Node node = new Node(valueSpecification, parameterizedFunction, dependencyNode);
    node._inputStates = Collections.emptySet();
    _nodes.add(node);
  }

  public void addExistingNodes(final Collection<Pair<DependencyNode, ValueSpecification>> nodes) {
    for (Pair<DependencyNode, ValueSpecification> node : nodes) {
      addExistingNode(node.getFirst(), node.getSecond());
    }
  }

  public void addExistingNode(final DependencyNode node, final ValueSpecification resolvedOutput) {
    _nodes.add(new Node(resolvedOutput, node));
  }

  public void addFunction(final ValueSpecification valueSpecification, final ParameterizedFunction parameterizedFunction, final DependencyNode node) {
    _nodes.add(new Node(valueSpecification, parameterizedFunction, node));
  }

  public boolean isEmpty() {
    if (_nodes.isEmpty()) {
      if (_lazyPopulator != null) {
        if (_lazyPopulator.more()) {
          assert !_nodes.isEmpty();
          return false;
        } else {
          _lazyPopulator = null;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  public boolean isSingle() {
    if (_nodes.size() == 1) {
      if (_lazyPopulator != null) {
        if (_lazyPopulator.more()) {
          assert _nodes.size() > 1;
          return false;
        } else {
          _lazyPopulator = null;
        }
      }
    }
    return _nodes.size() == 1;
  }

  public Node getFirst() {
    return _nodes.get(0);
  }

  public void removeFirst() {
    _nodes.remove(0);
  }

  public boolean removeDeepest() {
    if (isEmpty()) {
      return false;
    }
    Node node = getFirst();
    if (node.removeDeepest()) {
      return true;
    }
    if (isSingle()) {
      return false;
    }
    removeFirst();
    return true;
  }

  public ValueRequirement getValueRequirement() {
    return _valueRequirement;
  }

  public void setLastValid(final Pair<DependencyNode, ValueSpecification> lastValid) {
    _lastValid = lastValid;
  }

  public Pair<DependencyNode, ValueSpecification> getLastValid() {
    if (_lastValid == null) {
      throw new UnsatisfiableDependencyGraphException(getValueRequirement(), "No last valid")
          .addState("ResolutionState", this);
    }
    return _lastValid;
  }

  public void setLazyPopulator(final LazyPopulator lazyPopulator) {
    _lazyPopulator = lazyPopulator;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this);
  }

}
