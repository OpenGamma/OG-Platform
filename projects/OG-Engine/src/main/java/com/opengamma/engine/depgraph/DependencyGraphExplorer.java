/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.PublicAPI;

/**
 * Provides operations for querying a dependency graph. These are designed in particular for remote use where the full dependency graph is not available locally, but complex queries may need to be
 * made.
 */
@PublicAPI
public interface DependencyGraphExplorer {

  /**
   * Returns the name of the calculation configuration.
   * 
   * @return the calculation configuration name
   */
  String getCalculationConfigurationName();

  /**
   * Gets the graph used in the valuation
   * 
   * @return the dependency graph
   */
  DependencyGraph getWholeGraph();

  /**
   * Gets a subgraph producing a given value.
   * <p>
   * Note that if the {@link ValueSpecification} originated from the results of executing the dependency graph then its properties will uniquely identify the subgraph which produced the output;
   * otherwise, the subgraph returned may represent one of several producing the given output.
   * <p>
   * The graph returned will have a single root node that produces the requested output.
   * 
   * @param output the output, not null
   * @return a subgraph producing the output, or null if no such subgraph exists
   */
  DependencyGraphExplorer getSubgraphProducing(ValueSpecification output);

  /**
   * Gets a node producing a given value.
   * <p>
   * Note that this is similar to {@link #getSubgraphProducing}, returning what would be the root node for that graph but without the overhead of constructing the terminal output subset.
   * 
   * @param output the output, not null
   * @return the node that produces that output
   */
  DependencyNode getNodeProducing(ValueSpecification output);

  /**
   * Returns the terminal outputs, and the original value requirements, from the underlying graph.
   * 
   * @return the terminal output set from the graph.
   */
  Map<ValueSpecification, Set<ValueRequirement>> getTerminalOutputs();

  /**
   * Returns all of the computation targets present in the underlying graph.
   * 
   * @return the computation target set.
   */
  Set<ComputationTargetSpecification> getComputationTargets();

}
