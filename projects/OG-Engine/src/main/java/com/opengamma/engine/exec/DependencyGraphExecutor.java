/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import com.opengamma.engine.depgraph.DependencyGraph;

/**
 * Evaluates a dependency graph.
 */
public interface DependencyGraphExecutor {

  /**
   * Evaluates a dependency graph.
   * 
   * @param graph a full graph or a subgraph. A subgraph may have some nodes whose child nodes are NOT part of that graph. The assumption is that such nodes have already been evaluated and their
   *          values can already be found in the shared computation cache.
   * @return An object you can call get() on to wait for completion
   */
  DependencyGraphExecutionFuture execute(DependencyGraph graph);

}
