/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFunction;
import com.opengamma.engine.depgraph.impl.DependencyGraphImpl;
import com.opengamma.engine.depgraph.impl.DependencyNodeFunctionImpl;
import com.opengamma.engine.depgraph.impl.DependencyNodeImpl;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Trivial dependency graph builder for use in test cases.
 */
public class TestDependencyGraphBuilder {

  /**
   * Represents a partially built node.
   */
  public final class NodeBuilder {

    private final DependencyNodeFunction _function;
    private final ComputationTargetSpecification _target;
    private final Set<ValueSpecification> _inputs = new HashSet<ValueSpecification>();
    private final Set<ValueSpecification> _outputs = new HashSet<ValueSpecification>();

    private NodeBuilder(final DependencyNodeFunction function, final ComputationTargetSpecification target) {
      _function = function;
      _target = target;
    }

    public void addInput(final ValueSpecification value) {
      _inputs.add(value);
    }

    private ValueSpecification createOutput(final String name) {
      return new ValueSpecification(name, _target, ValueProperties.with(ValuePropertyNames.FUNCTION, _function.getFunctionId()).get());
    }

    public ValueSpecification addOutput(final String name) {
      final ValueSpecification output = createOutput(name);
      addOutput(output);
      return output;
    }

    public void addOutput(final ValueSpecification value) {
      assert _target.equals(value.getTargetSpecification());
      _outputs.add(value);
    }

    public ValueSpecification addTerminalOutput(final String name) {
      final ValueSpecification output = createOutput(name);
      final ValueRequirement requirement = new ValueRequirement(name, output.getTargetSpecification(), output.getProperties().withoutAny(ValuePropertyNames.FUNCTION));
      addTerminalOutput(output, requirement);
      return output;
    }

    public void addTerminalOutput(final ValueSpecification value, final ValueRequirement requirement) {
      addOutput(value);
      _terminals.put(value, Collections.singleton(requirement));
    }

  }

  private final String _calcConfig;
  private final List<NodeBuilder> _nodes = new ArrayList<NodeBuilder>();
  private final Map<ValueSpecification, Set<ValueRequirement>> _terminals = new HashMap<ValueSpecification, Set<ValueRequirement>>();

  public TestDependencyGraphBuilder(final String calcConfig) {
    _calcConfig = calcConfig;
  }

  public NodeBuilder getNode(final int index) {
    return _nodes.get(index);
  }

  public NodeBuilder addNode(final DependencyNodeFunction function, final ComputationTargetSpecification target) {
    final NodeBuilder n = new NodeBuilder(function, target);
    _nodes.add(n);
    return n;
  }

  public NodeBuilder addNode(final String function, final ComputationTargetSpecification target) {
    return addNode(DependencyNodeFunctionImpl.of(function, EmptyFunctionParameters.INSTANCE), target);
  }

  public NodeBuilder addNode(final FunctionDefinition function, final ComputationTargetSpecification target) {
    return addNode(DependencyNodeFunctionImpl.of(function.getUniqueId(), function.getDefaultParameters()), target);
  }

  public NodeBuilder addNode(final MockFunction function) {
    return addNode(function, function.getTarget().toSpecification());
  }

  public void addTerminalOutput(final ValueSpecification valueSpecification, final Set<ValueRequirement> valueRequirements) {
    _terminals.put(valueSpecification, valueRequirements);
  }

  private DependencyNode buildNode(final ValueSpecification valueSpec, final Map<ValueSpecification, NodeBuilder> builders, final Map<ValueSpecification, DependencyNode> nodes) {
    DependencyNode node = nodes.get(valueSpec);
    if (node == null) {
      final NodeBuilder builder = builders.get(valueSpec);
      if (builder == null) {
        throw new IllegalArgumentException();
      }
      node = buildNode(builder, builders, nodes);
      for (ValueSpecification output : builder._outputs) {
        nodes.put(output, node);
      }
    }
    return node;
  }

  private DependencyNode buildNode(final NodeBuilder builder, final Map<ValueSpecification, NodeBuilder> builders, final Map<ValueSpecification, DependencyNode> nodes) {
    final Map<ValueSpecification, DependencyNode> inputs = new HashMap<ValueSpecification, DependencyNode>();
    for (ValueSpecification input : builder._inputs) {
      inputs.put(input, buildNode(input, builders, nodes));
    }
    return new DependencyNodeImpl(builder._function, builder._target, builder._outputs, inputs);
  }

  public DependencyGraph buildGraph() {
    final Map<ValueSpecification, NodeBuilder> nodeBuilders = new HashMap<ValueSpecification, NodeBuilder>();
    final Map<ValueSpecification, DependencyNode> nodes = new HashMap<ValueSpecification, DependencyNode>();
    final Set<ValueSpecification> intermediates = new HashSet<ValueSpecification>();
    for (NodeBuilder node : _nodes) {
      for (ValueSpecification output : node._outputs) {
        nodeBuilders.put(output, node);
      }
      intermediates.addAll(node._inputs);
    }
    final Collection<DependencyNode> roots = new ArrayList<DependencyNode>();
    nodeLoop: for (NodeBuilder node : _nodes) { //CSIGNORE
      for (ValueSpecification output : node._outputs) {
        if (intermediates.contains(output)) {
          continue nodeLoop;
        }
      }
      roots.add(buildNode(node, nodeBuilders, nodes));
    }
    return new DependencyGraphImpl(_calcConfig, roots, _nodes.size(), _terminals);
  }

}
