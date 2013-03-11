/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.opengamma.engine.calcnode.CalculationJobResult;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.exec.DependencyGraphExecutor;
import com.opengamma.engine.exec.ExecutionResult;
import com.opengamma.engine.exec.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.view.impl.ExecutionLogModeSource;

/**
 * 
 */
public class TestDependencyGraphExecutor implements DependencyGraphExecutor<CalculationJobResult> {
  
  private final CalculationJobResult _result;
  
  public TestDependencyGraphExecutor(CalculationJobResult result) {
    _result = result;
  }
  
  @Override
  public Future<CalculationJobResult> execute(final DependencyGraph graph, final Queue<ExecutionResult> calcJobResultQueue,
      final GraphExecutorStatisticsGatherer statistics, final ExecutionLogModeSource logModeSource) {
    FutureTask<CalculationJobResult> future = new FutureTask<CalculationJobResult>(new Runnable() {
      @Override
      public void run() {
        calcJobResultQueue.add(new ExecutionResult(graph.getExecutionOrder(), _result));
      }
    }, _result);
    future.run();
    return future;
  }

}
