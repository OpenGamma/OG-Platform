/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.List;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.view.calcnode.CalculationJobResult;

/**
 * Reports the result of an execution. The execution result describes the nodes from the dependency graph that were executed and the job results (in node execution order) reported from the calculation
 * nodes.
 */
public class ExecutionResult {

  private final List<DependencyNode> _nodes;

  private final CalculationJobResult _result;

  public ExecutionResult(final List<DependencyNode> nodes, final CalculationJobResult result) {
    _nodes = nodes;
    _result = result;
  }

  public List<DependencyNode> getNodes() {
    return _nodes;
  }

  public CalculationJobResult getResult() {
    return _result;
  }

}
