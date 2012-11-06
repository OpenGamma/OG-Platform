/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.List;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Represents the result of an execution.
 */
public class ExecutionResult {

  private final List<DependencyNode> _nodes;

  private final CalculationJobResult _result;

  /**
   * Constructs an instance.
   * 
   * @param nodes  the dependency nodes executed, in execution order, not null
   * @param result  the result of the calculation job, not null
   */
  public ExecutionResult(final List<DependencyNode> nodes, final CalculationJobResult result) {
    ArgumentChecker.notNull(nodes, "nodes");
    ArgumentChecker.notNull(result, "result");
    _nodes = nodes;
    _result = result;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the nodes from the dependency graph that were executed, in execution order.
   *  
   * @return a list of nodes from the dependency graph that were executed, not null
   */
  public List<DependencyNode> getNodes() {
    return _nodes;
  }

  /**
   * Gets the result of the calculation job, not null
   * 
   * @return the result of the calcualtion job, not null
   */
  public CalculationJobResult getResult() {
    return _result;
  }

}
