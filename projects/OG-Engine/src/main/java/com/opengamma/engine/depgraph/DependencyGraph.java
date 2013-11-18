/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.PublicAPI;

/**
 * Represents a directed, acyclic, graph of nodes describing how to execute a single configuration of a view to produce the required terminal outputs.
 * <p>
 * Instances should be immutable and thread-safe.
 */
@PublicAPI
public interface DependencyGraph {

  /**
   * Returns the name of the configuration this graph has been built for.
   * 
   * @return the configuration name, not null
   */
  String getCalculationConfigurationName();

  /**
   * Returns the number of nodes in the graph.
   * 
   * @return the number of nodes
   */
  int getSize();

  /**
   * Returns the number of root nodes in the graph.
   * 
   * @return the number of root nodes
   */
  int getRootCount();

  /**
   * Returns a root node from the graph.
   * 
   * @param index the index of the root node, from 0 (inclusive) to {@link #getRootCount} (exclusive).
   * @return the root node, not null
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  DependencyNode getRootNode(int index);

  /**
   * Returns the set of output values from the graph that are marked as terminal outputs. These are the result of the requested values that drove the graph construction and will not be pruned. Any
   * other output values in the graph are intermediate values required by the functions used to deliver the requested terminal outputs.
   * 
   * @return the map of terminal output values to the originally requested value requirements, not null and not containing null
   */
  Map<ValueSpecification, Set<ValueRequirement>> getTerminalOutputs();

  /**
   * Returns an iterator over all of the nodes in the graph. Each node will only be passed once but the order is arbitrary.
   * 
   * @return an iterator over all nodes in the graph, not null
   */
  Iterator<DependencyNode> nodeIterator();

}
