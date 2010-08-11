/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.concurrent.Future;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobResult;

/**
 * Evaluates dependency graph.
 */
public interface DependencyGraphExecutor {
  
  /**
   * Gets the computation cache in which results for
   * the given calculation job are stored.
   * 
   * @param job Calculation job
   * @return Cache corresponding to the job
   */
  ViewComputationCache getCache(CalculationJob job);
  
  /**
   * Gets the computation cache in which results for
   * the given calculation job result are stored.
   * 
   * @param result Calculation job result
   * @return Cache corresponding to the job
   */
  ViewComputationCache getCache(CalculationJobResult result);
  
  /**
   * Evaluates a dependency graph. 
   * 
   * @param graph This may be a full graph or a subgraph. 
   * A subgraph may have some nodes whose child nodes are 
   * NOT part of that graph. The assumption is 
   * that such nodes have already been evaluated and their
   * values can already be found in the shared computation cache.
   * @return An object you can call get() on to wait for completion
   */
  Future<?> execute(DependencyGraph graph);

}
