/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import java.util.Map;
import java.util.Set;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Evaluates a dependency graph.
 */
public interface DependencyGraphExecutor {

  /**
   * Evaluates a dependency graph.
   * <p>
   * A graph may be executed in its entirety, but more typically a set of values that are already known will be supplied. Execution of the graph will consist of nodes that consume these values leading
   * towards the root of the graph. For example this might be the market data for the cycle ({@link MarketDataSourcingFunction} nodes are never executed), or a more complete buffer of data if this is
   * a delta execution cycle.
   * <p>
   * The parameters allow execution to be modified from what is described in the initial graph. Any nodes producing outputs which are keys in the map will instead be executed with the given
   * parameters.
   * 
   * @param graph a dependency graph to be executed, not null
   * @param sharedValues values that are already calculated and available; nodes producing these will not be executed, not null and not containing null
   * @param parameters substitute parameters to adjust the execution, not null and not containing null
   * @return An object you can call get() on to wait for completion
   */
  DependencyGraphExecutionFuture execute(DependencyGraph graph, Set<ValueSpecification> sharedValues, Map<ValueSpecification, FunctionParameters> parameters);

}
