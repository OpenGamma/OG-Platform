/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.testng.annotations.Test;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.test.ViewProcessorTestEnvironment;
import com.opengamma.engine.view.ViewProcessImpl;
import com.opengamma.engine.view.ViewProcessorImpl;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.test.Timeout;

/**
 * Tests SingleComputationCycle
 */
@Test
public class SingleComputationCycleTest {

  private static final long TIMEOUT = Timeout.standardTimeoutMillis();
  
  public void testInterruptCycle() throws InterruptedException {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    BlockingDependencyGraphExecutorFactory dgef = new BlockingDependencyGraphExecutorFactory(TIMEOUT);
    env.setDependencyGraphExecutorFactory(dgef);
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(UserPrincipal.getTestUser());
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    
    BlockingDependencyGraphExecutor executor = dgef.getExecutorInstance();
    assertTrue (executor.awaitFirstRun(TIMEOUT));
    
    // We're now blocked in the execution of the initial cycle
    assertFalse(executor.wasInterrupted());
    
    // Interrupting should cause everything to terminate gracefully
    ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    ViewComputationJob recalcJob = env.getCurrentComputationJob(viewProcess);
    Thread recalcThread = env.getCurrentComputationThread(viewProcess);
    recalcJob.terminate();
    recalcThread.interrupt();
    recalcThread.join(TIMEOUT);
    for (int i = 0; (i < TIMEOUT / 10) && !executor.wasInterrupted (); i++) {
      Thread.sleep (10);
    }
    assertTrue(executor.wasInterrupted());
  }
  
  private class BlockingDependencyGraphExecutorFactory implements DependencyGraphExecutorFactory<CalculationJobResult> {

    private final BlockingDependencyGraphExecutor _instance;
    
    public BlockingDependencyGraphExecutorFactory(long timeoutMillis) {
      _instance = new BlockingDependencyGraphExecutor(timeoutMillis);
    }
    
    @Override
    public DependencyGraphExecutor<CalculationJobResult> createExecutor(SingleComputationCycle cycle) {
      return _instance;
    }
    
    public BlockingDependencyGraphExecutor getExecutorInstance() {
      return _instance;
    }
    
  }
  
  private class BlockingDependencyGraphExecutor implements DependencyGraphExecutor<CalculationJobResult> {

    private final long _timeout;
    private final CountDownLatch _firstRunLatch = new CountDownLatch(1);
    private final AtomicBoolean _wasInterrupted = new AtomicBoolean ();
    
    public BlockingDependencyGraphExecutor(long timeoutMillis) {
      _timeout = timeoutMillis;
    }
    
    public boolean awaitFirstRun(long timeoutMillis) throws InterruptedException {
      return _firstRunLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
    }
    
    public boolean wasInterrupted() {
      return _wasInterrupted.get ();
    }
    
    @Override
    public Future<CalculationJobResult> execute(DependencyGraph graph, final BlockingQueue<CalculationJobResult> calcJobResultQueue, GraphExecutorStatisticsGatherer statistics) {
      FutureTask<CalculationJobResult> future = new FutureTask<CalculationJobResult>(new Runnable() {
        @Override
        public void run() {
          _firstRunLatch.countDown();
          try {
            Thread.sleep(_timeout);
          } catch (InterruptedException e) {
            _wasInterrupted.set (true);
          }
        }
      }, null);
      
      // Cheat a bit - don't give the job to the dispatcher, etc, just run it.
      new Thread(future).start();
      return future;
    }
    
  }
  
}
