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
import com.opengamma.engine.view.calcnode.CalculationNode;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class TestDependencyGraphExecutor implements DependencyGraphExecutor {
  
  private final CalculationNode _node;
  
  public TestDependencyGraphExecutor(CalculationNode node) {
    ArgumentChecker.notNull(node, "Node");
    _node = node;    
  }

  @Override
  public ViewComputationCache getCache(CalculationJob job) {
    return _node.getCache(job.getSpecification());
  }

  @Override
  public ViewComputationCache getCache(CalculationJobResult result) {
    return _node.getCache(result.getSpecification());
  }

  @Override
  public Future<?> execute(DependencyGraph graph) {
    return null;
  }

}
