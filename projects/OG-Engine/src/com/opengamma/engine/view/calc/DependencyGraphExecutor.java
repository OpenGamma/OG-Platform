/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.AbstractQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.view.calcnode.CalculationJobResult;

/**
 * Evaluates a dependency graph.
 * 
  * @param <T> Type of return information from the executor
 */
public interface DependencyGraphExecutor<T> {
  
  /**
   * Evaluates a dependency graph. 
   * 
   * @param graph This may be a full graph or a subgraph. 
   * A subgraph may have some nodes whose child nodes are 
   * NOT part of that graph. The assumption is 
   * that such nodes have already been evaluated and their
   * values can already be found in the shared computation cache.
   * @param statistics Details about the evaluation should be
   * reported to this callback object.
   * @param calcJobResultQueue queue in to which the executor enqueues individual calculation job results
   * @return An object you can call get() on to wait for completion
   */
  Future<T> execute(DependencyGraph graph, BlockingQueue<CalculationJobResult> calcJobResultQueue, GraphExecutorStatisticsGatherer statistics);

}
