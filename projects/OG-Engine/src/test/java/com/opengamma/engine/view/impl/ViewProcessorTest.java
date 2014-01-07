/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.impl;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.threeten.bp.temporal.ChronoUnit.MINUTES;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.resource.EngineResourceReference;
import com.opengamma.engine.test.ViewProcessorTestEnvironment;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.InfiniteViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.engine.view.listener.ViewResultListenerFactory;
import com.opengamma.engine.view.worker.ViewProcessWorker;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestLifecycle;
import com.opengamma.util.test.Timeout;

/**
 * Tests ViewProcessor
 */
@Test(groups = TestGroup.UNIT)
public class ViewProcessorTest {

  @Mock
  private ViewResultListenerFactory viewResultListenerFactoryStub;
  @Mock
  private ViewResultListener viewResultListenerMock;

  @BeforeMethod
  public void setUp() throws Exception {
    initMocks(this);
    when(viewResultListenerFactoryStub.createViewResultListener(ViewProcessorTestEnvironment.TEST_USER)).thenReturn(viewResultListenerMock);
  }

  public void testCreateViewProcessor() {
    TestLifecycle.begin();
    try {
      final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
      env.init();
      final ViewProcessorImpl vp = env.getViewProcessor();
      assertTrue(vp.isRunning());
      assertTrue(vp.getViewProcesses().isEmpty());
      vp.stop();
      assertFalse(vp.isRunning());
    } finally {
      TestLifecycle.end();
    }
  }

  @Test
  public void testAttachToView() {
    TestLifecycle.begin();
    try {
      final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
      env.init();
      final ViewProcessorImpl vp = env.getViewProcessor();
      vp.start();

      final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
      client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));

      vp.stop();
    } finally {
      TestLifecycle.end();
    }
  }

  private void waitForCompletionAndShutdown(final ViewProcessorImpl vp, final ViewClient client, final ViewProcessorTestEnvironment env) throws InterruptedException {
    client.waitForCompletion();
    // Note: notification of client completion happens before the client computation thread terminates and performs its postRunCycle - must wait for this to happen
    final ViewProcessWorker worker = env.getCurrentWorker(env.getViewProcess(vp, client.getUniqueId()));
    client.shutdown();
    worker.join();
  }

  @Test
  public void testSuspend_viewExists() throws InterruptedException, ExecutionException {
    TestLifecycle.begin();
    final ExecutorService executor = Executors.newCachedThreadPool();
    try {
      final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
      env.init();
      final ViewProcessorImpl vp = env.getViewProcessor();
      vp.start();
      final Runnable resume;
      resume = vp.suspend(executor).get();
      assertNotNull(resume);
      final CountDownLatch latch = new CountDownLatch(1);
      final ViewClient client2 = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
      executor.submit(new Runnable() {
        @Override
        public void run() {
          client2.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
          client2.shutdown();
          latch.countDown();
        }
      });
      assertFalse(latch.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS));
      resume.run();
      assertTrue(latch.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS));
      vp.stop();
    } finally {
      executor.shutdown();
      TestLifecycle.end();
    }
  }

  @Test
  public void testSuspend_viewNotExists() throws InterruptedException, ExecutionException {
    TestLifecycle.begin();
    final ExecutorService executor = Executors.newCachedThreadPool();
    try {
      final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
      env.init();
      final ViewProcessorImpl vp = env.getViewProcessor();
      vp.start();
      final Runnable resume = vp.suspend(executor).get();
      assertNotNull(resume);
      final ViewClient client2 = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
      final CountDownLatch latch = new CountDownLatch(1);
      executor.submit(new Runnable() {
        @Override
        public void run() {
          client2.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
          client2.shutdown();
          latch.countDown();
        }
      });
      assertFalse(latch.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS));
      resume.run();
      assertTrue(latch.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS));
      vp.stop();
    } finally {
      executor.shutdown();
      TestLifecycle.end();
    }
  }

  @Test
  public void testCycleManagement_realTimeInterrupted() throws InterruptedException {
    TestLifecycle.begin();
    try {
      final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
      env.init();
      final ViewProcessorImpl vp = env.getViewProcessor();
      vp.start();

      final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
      final CycleCountingViewResultListener listener = new CycleCountingViewResultListener(10);
      client.setResultListener(listener);
      final ViewExecutionOptions executionOptions = ExecutionOptions.of(new InfiniteViewCycleExecutionSequence(),
          ViewCycleExecutionOptions.builder().setMarketDataSpecification(MarketData.live()).create(), ExecutionFlags.none().runAsFastAsPossible().get());
      client.attachToViewProcess(env.getViewDefinition().getUniqueId(), executionOptions);
      listener.awaitCycles(10 * Timeout.standardTimeoutMillis());

      final ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
      final ViewProcessWorker worker = env.getCurrentWorker(viewProcess);

      client.shutdown();
      worker.join();

      assertEquals(0, vp.getViewCycleManager().getResourceCount());
    } finally {
      TestLifecycle.end();
    }
  }

  @Test
  public void testCycleManagement_processCompletes() throws InterruptedException {
    TestLifecycle.begin();
    try {
      final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
      env.setViewResultListenerFactory(viewResultListenerFactoryStub);
      env.init();
      final ViewProcessorImpl vp = env.getViewProcessor();
      vp.start();

      final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
      final ViewExecutionOptions executionOptions = ExecutionOptions.batch(generateExecutionSequence(10), ViewCycleExecutionOptions.builder().setMarketDataSpecification(MarketData.live())
          .create());
      client.attachToViewProcess(env.getViewDefinition().getUniqueId(), executionOptions);
      waitForCompletionAndShutdown(vp, client, env);
      assertEquals(0, vp.getViewCycleManager().getResourceCount());
    } finally {
      TestLifecycle.end();
    }
  }

  public void testCycleManagement_processCompletesWithReferences() throws InterruptedException {
    TestLifecycle.begin();
    try {
      final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();

      env.setViewResultListenerFactory(viewResultListenerFactoryStub);
      env.init();
      final ViewProcessorImpl vp = env.getViewProcessor();
      vp.start();

      final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
      client.setViewCycleAccessSupported(true);
      final List<EngineResourceReference<? extends ViewCycle>> references = new ArrayList<EngineResourceReference<? extends ViewCycle>>();
      final ViewResultListener resultListener = new AbstractViewResultListener() {

        @Override
        public void cycleCompleted(final ViewComputationResultModel fullResult, final ViewDeltaResultModel deltaResult) {
          final EngineResourceReference<? extends ViewCycle> reference = client.createLatestCycleReference();
          if (reference != null) {
            references.add(reference);
          }
        }

        @Override
        public UserPrincipal getUser() {
          return UserPrincipal.getTestUser();
        }

      };
      client.setResultListener(resultListener);
      final ViewExecutionOptions executionOptions = ExecutionOptions.batch(generateExecutionSequence(10), ViewCycleExecutionOptions.builder().setMarketDataSpecification(MarketData.live())
          .create());
      client.attachToViewProcess(env.getViewDefinition().getUniqueId(), executionOptions);

      final ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
      final UniqueId viewProcessId = viewProcess.getUniqueId();

      waitForCompletionAndShutdown(vp, client, env);

      assertEquals(10, references.size());
      assertEquals(10, vp.getViewCycleManager().getResourceCount());

      final Set<UniqueId> cycleIds = new HashSet<UniqueId>();
      for (final EngineResourceReference<? extends ViewCycle> reference : references) {
        assertEquals(viewProcessId, reference.get().getViewProcessId());
        cycleIds.add(reference.get().getUniqueId());
        reference.release();
      }

      // Expect distinct cycles
      assertEquals(10, cycleIds.size());
      assertEquals(0, vp.getViewCycleManager().getResourceCount());
    } finally {
      TestLifecycle.end();
    }
  }

  private ViewCycleExecutionSequence generateExecutionSequence(final int cycleCount) {
    final Collection<Instant> valuationTimes = new ArrayList<Instant>(cycleCount);
    final Instant now = Instant.now();
    for (int i = 0; i < cycleCount; i++) {
      valuationTimes.add(now.plus(i, MINUTES));
    }
    return ArbitraryViewCycleExecutionSequence.of(valuationTimes);
  }

  private class CycleCountingViewResultListener extends AbstractViewResultListener {

    private final CountDownLatch _cycleLatch;

    public CycleCountingViewResultListener(final int requiredCycleCount) {
      _cycleLatch = new CountDownLatch(requiredCycleCount);
    }

    @Override
    public void cycleCompleted(final ViewComputationResultModel fullResult, final ViewDeltaResultModel deltaResult) {
      _cycleLatch.countDown();
    }

    public void awaitCycles(final long timeoutMillis) throws InterruptedException {
      _cycleLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public UserPrincipal getUser() {
      return UserPrincipal.getTestUser();
    }

  }

}
