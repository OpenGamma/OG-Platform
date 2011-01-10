/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.view.calc.DependencyGraphExecutor;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;

/**
 * 
 */
public class TestDependencyGraphExecutor<T> implements DependencyGraphExecutor<T> {
  
  private final T _result;
  
  public TestDependencyGraphExecutor(T result) {
    _result = result;
  }
  
  @Override
  public Future<T> execute(DependencyGraph graph, final GraphExecutorStatisticsGatherer statistics) {
    FutureTask<T> future = new FutureTask<T>(new Runnable() {
      @Override
      public void run() {
      }
    }, _result);
    future.run();
    return future;
  }

}
