/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.view.calcnode.CalculationJobResult;

/**
 * Writes results.
 */
public interface ResultWriter {

  /**
   * Writes results.
   * 
   * @param result the result to save
   * @param depGraph  the context information, useful for determining
   *  which results to write and which to skip
   */
  void write(CalculationJobResult result, DependencyGraph depGraph);

  /**
   * Gets the dependency graph to execute. 
   * <p>
   * This is useful when a batch is restarted. The
   * writer can analyze the contents of the batch database 
   * and the computation cache. If results are found
   * there, it is possible to skip many calculations
   * and evaluate only a sub-graph of the entire graph.
   *  
   * @param graph  the original graph
   * @return the original graph or, possibly, a sub-graph of the original
   * graph, if results are already found
   */
  DependencyGraph getGraphToExecute(DependencyGraph graph);

}
