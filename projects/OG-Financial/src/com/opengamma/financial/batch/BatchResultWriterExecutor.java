/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.view.calc.DependencyGraphExecutor;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.NamedThreadPoolFactory;

/**
 * This dependency graph executor intercepts results coming back from a delegate
 * executor and writes them to batch DB.
 */
public class BatchResultWriterExecutor implements DependencyGraphExecutor<Object> {
  
  private final BatchResultWriter _writer;
  private final DependencyGraphExecutor<CalculationJobResult> _delegate;
  private final ExecutorService _executor;
  
  public BatchResultWriterExecutor(
      BatchResultWriter writer,
      DependencyGraphExecutor<CalculationJobResult> delegate) {
    this(writer,
        delegate,
        Executors.newSingleThreadExecutor(new NamedThreadPoolFactory("BatchResultWriterExecutor")));
  }
  
  public BatchResultWriterExecutor(
      BatchResultWriter writer,
      DependencyGraphExecutor<CalculationJobResult> delegate,
      ExecutorService executor) {
    
    ArgumentChecker.notNull(writer, "Batch result writer");
    ArgumentChecker.notNull(delegate, "Dep graph executor");
    ArgumentChecker.notNull(executor, "Task executor");
    
    _writer = writer;
    _delegate = delegate;
    _executor = executor;
  }
  
  @Override
  public Future<Object> execute(DependencyGraph graph, final GraphExecutorStatisticsGatherer statistics) {
    DependencyGraph subGraph = _writer.getGraphToExecute(graph);
    
    Future<CalculationJobResult> future = _delegate.execute(subGraph, statistics);
    
    BatchResultWriterCallable callable = new BatchResultWriterCallable(future, subGraph);
    
    return _executor.submit(callable);
  }
  
  private class BatchResultWriterCallable implements Callable<Object> {
    
    private final Future<CalculationJobResult> _future;
    private final DependencyGraph _subGraph;
    
    public BatchResultWriterCallable(Future<CalculationJobResult> future,
        DependencyGraph subGraph) {
      _future = future;
      _subGraph = subGraph;
    }
    
    @Override
    public Object call() {
      CalculationJobResult result;
      try {
        result = _future.get();
      } catch (InterruptedException e) {
        Thread.interrupted();
        throw new RuntimeException("Should not have been interrupted");
      } catch (ExecutionException e) {
        throw new RuntimeException("Execution of dependent job failed", e);
      }

      jobExecuted(result, _subGraph);
      return null;
    }
  }
  
  private void jobExecuted(CalculationJobResult result, DependencyGraph depGraph) {
    _writer.write(result, depGraph);
  }
  
}
