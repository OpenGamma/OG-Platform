/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.PublicAPI;

/**
 * Provides operations for querying a dependency graph. These are designed in particular for remote use where the full
 * dependency graph is not available locally, but complex queries may need to be made. 
 */
@PublicAPI
public interface DependencyGraphExplorer {

  /**
   * Gets the graph used in the valuation
   * 
   * @return  the dependency graph
   */
  DependencyGraph getWholeGraph();
  
  /**
   * Gets a subgraph producing a given value.
   * <p>
   * Note that if the {@link ValueSpecification} originated from the results of executing the dependency graph then its
   * properties will uniquely identify the subgraph which produced the output; otherwise, the subgraph returned may
   * represent one of several producing the given output.
   * 
   * @param output  the output, not null
   * @return  a subgraph producing the output, or null if no such subgraph exists
   */
  DependencyGraph getSubgraphProducing(ValueSpecification output);  
}
