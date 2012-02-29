/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.opengamma.engine.view.calc.ViewResultListenerFactory;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.test.ViewProcessorTestEnvironment;
import com.opengamma.engine.view.calc.EngineResourceReference;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.InfiniteViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.test.Timeout;

/**
 * Tests ViewProcessor
 */
@Test
public class ViewProcessorTest {
  
  @Mock
  private ViewResultListenerFactory viewResultListenerFactoryStub;
  @Mock
  private ViewResultListener viewResultListenerMock;

  @BeforeMethod
  public void setUp() throws Exception {
    initMocks(this);
    when(viewResultListenerFactoryStub.createViewResultListener()).thenReturn(viewResultListenerMock);
  }

  public void testCreateViewProcessor() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();

    assertFalse(vp.isRunning());

    vp.start();

    assertTrue(vp.isRunning());
    assertTrue(vp.getViewProcesses().isEmpty());
    vp.stop();
  }

  @Test
  public void testAttachToView() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    
    vp.stop();
  }

  @Test
  public void testSuspend_viewExists() throws InterruptedException, ExecutionException {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();

    Runnable resume = vp.suspend(Executors.newCachedThreadPool()).get();
    assertNotNull(resume);
    
    final CountDownLatch latch = new CountDownLatch(1);
    final ViewClient client2 = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    Thread tryAttach = new Thread() {
      @Override
      public void run() {
        client2.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
        client2.shutdown();
        latch.countDown();
      }
    };
    tryAttach.start();
    assertFalse(latch.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS));
    resume.run();
    assertTrue(latch.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS));
    vp.stop();
  }

  @Test
  public void testSuspend_viewNotExists() throws InterruptedException, ExecutionException {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    Runnable resume = vp.suspend(Executors.newCachedThreadPool()).get();
    assertNotNull(resume);
    
    final ViewClient client2 = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    final CountDownLatch latch = new CountDownLatch(1);
    Thread tryAttach = new Thread() {
      @Override
      public void run() {
        client2.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
        client2.shutdown();
        latch.countDown();
      }
    };
    tryAttach.start();
    assertFalse(latch.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS));
    resume.run();
    assertTrue(latch.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS));
    vp.stop();
  }
  
  @Test
  public void testCycleManagement_realTimeInterrupted() throws InterruptedException {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    CycleCountingViewResultListener listener = new CycleCountingViewResultListener(10);
    client.setResultListener(listener);
    ViewExecutionOptions executionOptions = ExecutionOptions.of(new InfiniteViewCycleExecutionSequence(), new ViewCycleExecutionOptions(MarketData.live()), ExecutionFlags.none().runAsFastAsPossible().get());
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), executionOptions);
    listener.awaitCycles(10 * Timeout.standardTimeoutMillis());
    
    ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    Thread computationThread = env.getCurrentComputationThread(viewProcess);
    
    client.shutdown();
    computationThread.join();
    
    assertEquals(0, vp.getViewCycleManager().getResourceCount());
  }
  
  @Test
  public void testCycleManagement_processCompletes() throws InterruptedException {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.setViewResultListenerFactory(viewResultListenerFactoryStub);
    env.init();    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    ViewExecutionOptions executionOptions = ExecutionOptions.batch(generateExecutionSequence(10), new ViewCycleExecutionOptions(MarketData.live()));
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), executionOptions);
    client.waitForCompletion();
    client.shutdown();
    
    assertEquals(0, vp.getViewCycleManager().getResourceCount());
  }
  
  public void testCycleManagement_processCompletesWithReferences() throws InterruptedException {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    
    env.setViewResultListenerFactory(viewResultListenerFactoryStub);
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client.setViewCycleAccessSupported(true);
    final List<EngineResourceReference<? extends ViewCycle>> references = new ArrayList<EngineResourceReference<? extends ViewCycle>>();
    ViewResultListener resultListener = new AbstractViewResultListener() {
      
      @Override
      public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
        EngineResourceReference<? extends ViewCycle> reference = client.createLatestCycleReference();
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
    ViewExecutionOptions executionOptions = ExecutionOptions.batch(generateExecutionSequence(10), new ViewCycleExecutionOptions(MarketData.live()));
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), executionOptions);
    
    ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    UniqueId viewProcessId = viewProcess.getUniqueId();
    
    client.waitForCompletion();
    client.shutdown();
    
    assertEquals(10, references.size());
    assertEquals(10, vp.getViewCycleManager().getResourceCount());
    
    Set<UniqueId> cycleIds = new HashSet<UniqueId>();
    for (EngineResourceReference<? extends ViewCycle> reference : references) {
      assertEquals(viewProcessId, reference.get().getViewProcessId());
      cycleIds.add(reference.get().getUniqueId());
      reference.release();
    }
    
    // Expect distinct cycles
    assertEquals(10, cycleIds.size());
    assertEquals(0, vp.getViewCycleManager().getResourceCount());
  }
  
  private ViewCycleExecutionSequence generateExecutionSequence(int cycleCount) {
    Collection<InstantProvider> valuationTimes = new ArrayList<InstantProvider>(cycleCount);
    Instant now = Instant.now();
    for (int i = 0; i < cycleCount; i++) {
      valuationTimes.add(now.plus(i, TimeUnit.MINUTES));
    }
    return ArbitraryViewCycleExecutionSequence.of(valuationTimes);
  }
  
  private class CycleCountingViewResultListener extends AbstractViewResultListener {
    
    private final CountDownLatch _cycleLatch;

    public CycleCountingViewResultListener(int requiredCycleCount) {
      _cycleLatch = new CountDownLatch(requiredCycleCount);
    }
    
    @Override
    public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
      _cycleLatch.countDown();
    }
    
    public void awaitCycles(long timeoutMillis) throws InterruptedException {
      _cycleLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public UserPrincipal getUser() {
      return UserPrincipal.getTestUser();
    }
    
  }

}
