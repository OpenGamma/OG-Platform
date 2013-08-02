/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFilter;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Creates a sub-graph based on a node filter. Unlike a direct application of the node filter a node is only included if the filter accepts it and all of its inputs are also marked for inclusion. A
 * node is excluded if the filter rejects it or any of its inputs are rejected. This will operate recursively, processing all nodes to the leaves of a graph.
 * <p>
 * The {@link DependencyGraph#subGraph} operation with a basic node filter is not suitable for incremental graph building as it can leave nodes in the sub-graph that have inputs that aren't in the
 * graph. Invalid nodes identified by the filter need to remove all the graph up to the terminal output root so that it can be rebuilt.
 * <p>
 * This filter contains internal state so should not be used by multiple threads. Although it implements {@link DependencyNodeFilter}, any subgraphing should be performed by calling the
 * {@link #subGraph} method which will correctly set up the internal state.
 */
public class SubGraphingFilter implements DependencyNodeFilter {

  private final DependencyNodeFilter _filter;
  private HashMap<DependencyNode, Boolean> _include;
  private Map<ValueSpecification, Set<ValueRequirement>> _terminalOutputs;
  private Set<ValueRequirement> _missingRequirements;

  public SubGraphingFilter(final DependencyNodeFilter filter) {
    ArgumentChecker.notNull(filter, "filter");
    _filter = filter;
  }

  protected DependencyNodeFilter getFilter() {
    return _filter;
  }

  /**
   * Tests if all of the nodes (and their input nodes) are included by the underlying filter.
   * 
   * @param nodes the nodes to test, not null
   * @return true if all of the nodes (and their inputs) are to be included, false otherwise
   */
  private boolean areIncluded(final Collection<DependencyNode> nodes) {
    boolean included = true;
    for (final DependencyNode node : nodes) {
      included &= isIncluded(node);
    }
    return included;
  }

  /**
   * Tests if a node, and all of its input nodes are included by the underlying filter.
   * 
   * @param node the node to test, not null
   * @return true if the node (and all of its inputs) are to be included, false otherwise
   */
  private boolean isIncluded(final DependencyNode node) {
    final Boolean included = _include.get(node);
    if (included == null) {
      if (getFilter().accept(node)) {
        if (areIncluded(node.getInputNodes())) {
          _include.put(node, Boolean.TRUE);
          return true;
        } else {
          _include.put(node, Boolean.FALSE);
          return false;
        }
      } else {
        _include.put(node, Boolean.FALSE);
        return false;
      }
    } else {
      return included.booleanValue();
    }
  }

  /**
   * Creates a sub-graph of this graph. If the filter would accept all of the nodes, the original graph is returned unchanged.
   * 
   * @param graph the graph to filter, not null
   * @param missingRequirements receives the original value requirements whose resolved value specifications were removed from the graph
   * @return the filtered graph, not null
   */
  public DependencyGraph subGraph(final DependencyGraph graph, final Set<ValueRequirement> missingRequirements) {
    final Collection<DependencyNode> nodes = graph.getDependencyNodes();
    final int size = nodes.size();
    if ((_include == null) || (_include.size() < size)) {
      _include = Maps.newHashMapWithExpectedSize(size);
    } else {
      _include.clear();
    }
    if (areIncluded(nodes)) {
      return graph;
    } else {
      _terminalOutputs = graph.getTerminalOutputs();
      _missingRequirements = missingRequirements;
      return graph.subGraph(this);
    }
  }

  // DependencyNodeFilter

  @Override
  public boolean accept(final DependencyNode node) {
    if (_include.get(node).booleanValue()) {
      return true;
    } else {
      if (node.hasTerminalOutputValues()) {
        for (final ValueSpecification output : node.getOutputValues()) {
          final Set<ValueRequirement> terminal = _terminalOutputs.get(output);
          if (terminal != null) {
            _missingRequirements.addAll(terminal);
          }
        }
      }
      return false;
    }
  }

}
