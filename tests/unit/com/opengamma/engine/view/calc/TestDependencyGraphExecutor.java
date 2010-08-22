/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.opengamma.engine.depgraph.DependencyGraph;

/**
 * 
 */
public class TestDependencyGraphExecutor<T> implements DependencyGraphExecutor<T> {
  
  private final T _result;
  
  public TestDependencyGraphExecutor(T result) {
    _result = result;
  }
  
  @Override
  public Future<T> execute(DependencyGraph graph) {
    FutureTask<T> future = new FutureTask<T>(new Runnable() {
      @Override
      public void run() {
      }
    }, _result);
    future.run();
    return future;
  }

}
