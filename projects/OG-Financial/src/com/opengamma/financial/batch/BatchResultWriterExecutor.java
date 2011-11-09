/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.view.calc.ResultWriter;
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

  /**
   * The batch result writer.
   */
  private final ResultWriter _writer;
  /**
   * The dependency graph executor.
   */
  private final DependencyGraphExecutor<CalculationJobResult> _delegate;
  /**
   * The executor service.
   */
  private final ExecutorService _executor;

  /**
   * Creates an instance.
   * 
   * @param writer  the result writer, not null
   * @param delegate  the underlying graph executor, not null
   */
  public BatchResultWriterExecutor(
      ResultWriter writer,
      DependencyGraphExecutor<CalculationJobResult> delegate) {
    this(writer,
        delegate,
        Executors.newSingleThreadExecutor(new NamedThreadPoolFactory("BatchResultWriterExecutor")));
  }

  /**
   * Creates an instance.
   * 
   * @param writer  the result writer, not null
   * @param delegate  the underlying graph executor, not null
   * @param executor  the executor service to use, not null
   */
  public BatchResultWriterExecutor(
      ResultWriter writer,
      DependencyGraphExecutor<CalculationJobResult> delegate,
      ExecutorService executor) {
    ArgumentChecker.notNull(writer, "Batch result writer");
    ArgumentChecker.notNull(delegate, "Dep graph executor");
    ArgumentChecker.notNull(executor, "Task executor");
    _writer = writer;
    _delegate = delegate;
    _executor = executor;
  }

  //-------------------------------------------------------------------------
  @Override
  public Future<Object> execute(DependencyGraph graph, BlockingQueue<CalculationJobResult> calcJobResultQueue, final GraphExecutorStatisticsGatherer statistics) {
    DependencyGraph subGraph = _writer.getGraphToExecute(graph);
    Future<CalculationJobResult> future = _delegate.execute(subGraph, calcJobResultQueue, statistics);
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
      } catch (InterruptedException ex) {
        Thread.interrupted();
        throw new RuntimeException("Should not have been interrupted");
      } catch (ExecutionException ex) {
        throw new RuntimeException("Execution of dependent job failed", ex);
      }
      jobExecuted(result, _subGraph);
      return null;
    }
  }

  private void jobExecuted(CalculationJobResult result, DependencyGraph depGraph) {
    _writer.write(result, depGraph);
  }

}
