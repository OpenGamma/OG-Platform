/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cycle;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.testng.annotations.Test;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.exec.DependencyGraphExecutionFuture;
import com.opengamma.engine.exec.DependencyGraphExecutor;
import com.opengamma.engine.exec.DependencyGraphExecutorFactory;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.test.ViewProcessorTestEnvironment;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.impl.ViewProcessImpl;
import com.opengamma.engine.view.impl.ViewProcessorImpl;
import com.opengamma.engine.view.worker.ViewProcessWorker;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestLifecycle;
import com.opengamma.util.test.Timeout;

/**
 * Tests SingleComputationCycle
 */
@Test(groups = TestGroup.UNIT)
public class SingleComputationCycleTest {

  private static final long TIMEOUT = Timeout.standardTimeoutMillis();

  public void testInterruptCycle() throws InterruptedException {
    TestLifecycle.begin();
    try {
      ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
      BlockingDependencyGraphExecutorFactory dgef = new BlockingDependencyGraphExecutorFactory(TIMEOUT);
      env.setDependencyGraphExecutorFactory(dgef);
      env.init();

      ViewProcessorImpl vp = env.getViewProcessor();
      vp.start();

      ViewClient client = vp.createViewClient(UserPrincipal.getTestUser());
      client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));

      BlockingDependencyGraphExecutor executor = dgef.getExecutorInstance();
      assertTrue(executor.awaitFirstRun(TIMEOUT));

      // We're now blocked in the execution of the initial cycle
      assertFalse(executor.wasInterrupted());

      // Interrupting should cause everything to terminate gracefully
      ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
      ViewProcessWorker worker = env.getCurrentWorker(viewProcess);
      worker.terminate();
      worker.join(TIMEOUT);
      for (int i = 0; (i < TIMEOUT / 10) && !executor.wasInterrupted(); i++) {
        Thread.sleep(10);
      }
      assertTrue(executor.wasInterrupted());
    } finally {
      TestLifecycle.end();
    }
  }

  private class BlockingDependencyGraphExecutorFactory implements DependencyGraphExecutorFactory {

    private final BlockingDependencyGraphExecutor _instance;

    public BlockingDependencyGraphExecutorFactory(long timeoutMillis) {
      _instance = new BlockingDependencyGraphExecutor(timeoutMillis);
    }

    @Override
    public DependencyGraphExecutor createExecutor(SingleComputationCycle cycle) {
      return _instance;
    }

    public BlockingDependencyGraphExecutor getExecutorInstance() {
      return _instance;
    }

  }

  private class BlockingDependencyGraphExecutor implements DependencyGraphExecutor {

    private final long _timeout;
    private final CountDownLatch _firstRunLatch = new CountDownLatch(1);
    private final AtomicBoolean _wasInterrupted = new AtomicBoolean();

    public BlockingDependencyGraphExecutor(long timeoutMillis) {
      _timeout = timeoutMillis;
    }

    public boolean awaitFirstRun(long timeoutMillis) throws InterruptedException {
      return _firstRunLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    public boolean wasInterrupted() {
      return _wasInterrupted.get();
    }

    @Override
    public DependencyGraphExecutionFuture execute(DependencyGraph graph, Set<ValueSpecification> sharedValues, Map<ValueSpecification, FunctionParameters> parameters) {
      final FutureTask<String> future = new FutureTask<String>(new Runnable() {
        @Override
        public void run() {
          _firstRunLatch.countDown();
          try {
            Thread.sleep(_timeout);
          } catch (InterruptedException e) {
            _wasInterrupted.set(true);
          }
        }
      }, graph.getCalculationConfigurationName());
      // Cheat a bit - don't give the job to the dispatcher, etc, just run it.
      new Thread(future).start();
      return new DependencyGraphExecutionFuture() {

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
          return future.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
          return future.isCancelled();
        }

        @Override
        public boolean isDone() {
          return future.isDone();
        }

        @Override
        public String get() throws InterruptedException, ExecutionException {
          return future.get();
        }

        @Override
        public String get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
          return future.get(timeout, unit);
        }

        @Override
        public void setListener(Listener listener) {
          // No-op
        }

      };
    }

  }

}
