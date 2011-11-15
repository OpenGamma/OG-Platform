/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.engine.view.listener.JobResultReceivedCall;
import org.testng.annotations.Test;

import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.test.TestViewResultListener;
import com.opengamma.engine.test.ViewProcessorTestEnvironment;
import com.opengamma.engine.view.calc.ViewComputationJob;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewClientState;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.util.test.Timeout;

/**
 * Tests {@link ViewProcess}
 */
@Test
public class ViewProcessTest {

  public void testLifecycle() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    
    ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    
    assertEquals(env.getViewDefinition().getUniqueId(), viewProcess.getDefinitionId());
    
    viewProcess.stop();
    assertFalse(client.isAttached());
    vp.stop();
  }
  
  public void testViewAccessors() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    
    ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    
    assertNull(client.getLatestResult());
    assertEquals(env.getViewDefinition(), viewProcess.getLatestViewDefinition());
    
    vp.stop();
  }
  
  public void testCreateClient() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    assertNotNull(client.getUniqueId());
    
    assertEquals(ViewClientState.STARTED, client.getState());
    client.pause();
    assertEquals(ViewClientState.PAUSED, client.getState());
    client.resume();
    assertEquals(ViewClientState.STARTED, client.getState());
    
    assertEquals(client, vp.getViewClient(client.getUniqueId()));
    
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));    
    ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    viewProcess.stop();
    
    // Should automatically detach the client
    assertFalse(client.isAttached());
    assertEquals(ViewClientState.STARTED, client.getState());
    
    vp.stop();
  }
  
  public void testGraphRebuild() throws InterruptedException {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    final ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    
    TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);
    
    final Instant time0 = Instant.now();
    ViewCycleExecutionOptions defaultCycleOptions = new ViewCycleExecutionOptions(MarketData.live());
    final ViewExecutionOptions executionOptions = new ExecutionOptions(ArbitraryViewCycleExecutionSequence.of(time0, time0.plusMillis(10), time0.plusMillis(20), time0.plusMillis(30)), ExecutionFlags.none().get(), defaultCycleOptions);
        
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), executionOptions);
    
    ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    ViewComputationJob computationJob = env.getCurrentComputationJob(viewProcess);
    Thread computationThread = env.getCurrentComputationThread(viewProcess);
    
    CompiledViewDefinitionWithGraphsImpl compilationModel1 = (CompiledViewDefinitionWithGraphsImpl) resultListener.getViewDefinitionCompiled(Timeout.standardTimeoutMillis()).getCompiledViewDefinition();

    assertEquals(time0, resultListener.getCycleCompleted(10 * Timeout.standardTimeoutMillis()).getFullResult().getValuationTime());
    
    computationJob.marketDataChanged();
    assertEquals(time0.plusMillis(10), resultListener.getCycleCompleted(10 * Timeout.standardTimeoutMillis()).getFullResult().getValuationTime());
    resultListener.assertNoCalls(Timeout.standardTimeoutMillis());

    // Trick the compilation job into thinking it needs to rebuilt after time0 + 20
    CompiledViewDefinitionWithGraphsImpl compiledViewDefinition = new CompiledViewDefinitionWithGraphsImpl(compilationModel1.getViewDefinition(), compilationModel1.getDependencyGraphsByConfiguration(), compilationModel1.getPortfolio(), compilationModel1.getFunctionInitId()) {
      @Override
      public boolean isValidFor(final InstantProvider timestampProvider) {
        Instant timestamp = timestampProvider.toInstant();
        return (!timestamp.isAfter(time0.plusMillis(20)));
      }
    };
    computationJob.setCachedCompiledViewDefinition(compiledViewDefinition);
    
    // Running at time0 + 20 doesn't require a rebuild - should still use our dummy
    computationJob.marketDataChanged();
    assertEquals(time0.plusMillis(20), resultListener.getCycleCompleted(10 * Timeout.standardTimeoutMillis()).getFullResult().getValuationTime());
    resultListener.assertNoCalls();

    // time0 + 30 requires a rebuild
    computationJob.marketDataChanged();
    CompiledViewDefinition compilationModel2 = resultListener.getViewDefinitionCompiled(Timeout.standardTimeoutMillis()).getCompiledViewDefinition();
    assertNotSame(compilationModel1, compilationModel2);
    assertNotSame(compiledViewDefinition, compilationModel2);
    assertEquals(time0.plusMillis(30), resultListener.getCycleCompleted(Timeout.standardTimeoutMillis()).getFullResult().getValuationTime());
    resultListener.assertProcessCompleted(Timeout.standardTimeoutMillis());
    
    resultListener.assertNoCalls(Timeout.standardTimeoutMillis());
    
    assertTrue(executionOptions.getExecutionSequence().isEmpty());
    
    // Job should have terminated automatically with no further evaluation times
    assertEquals(ViewProcessState.FINISHED, viewProcess.getState());
    assertTrue(computationJob.isTerminated());
    assertFalse(computationThread.isAlive());
    
    vp.stop();
  }

  public void testGraphRebuildWithStreamingModeOn() throws InterruptedException {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    final ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();

    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client.setJobResultMode(ViewResultMode.FULL_ONLY);

    TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);

    final Instant time0 = Instant.now();
    ViewCycleExecutionOptions defaultCycleOptions = new ViewCycleExecutionOptions(MarketData.live());
    final ViewExecutionOptions executionOptions = new ExecutionOptions(ArbitraryViewCycleExecutionSequence.of(time0, time0.plusMillis(10), time0.plusMillis(20), time0.plusMillis(30)), ExecutionFlags.none().get(), defaultCycleOptions);

    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), executionOptions);

    ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    ViewComputationJob computationJob = env.getCurrentComputationJob(viewProcess);
    Thread computationThread = env.getCurrentComputationThread(viewProcess);

    CompiledViewDefinitionWithGraphsImpl compilationModel1 = (CompiledViewDefinitionWithGraphsImpl) resultListener.getViewDefinitionCompiled(Timeout.standardTimeoutMillis()).getCompiledViewDefinition();

    resultListener.expectNextCall(JobResultReceivedCall.class, 10 * Timeout.standardTimeoutMillis());
    assertEquals(time0, resultListener.getCycleCompleted(10 * Timeout.standardTimeoutMillis()).getFullResult().getValuationTime());

    computationJob.marketDataChanged();
    resultListener.expectNextCall(JobResultReceivedCall.class, 10 * Timeout.standardTimeoutMillis());
    assertEquals(time0.plusMillis(10), resultListener.getCycleCompleted(10 * Timeout.standardTimeoutMillis()).getFullResult().getValuationTime());
    resultListener.assertNoCalls(Timeout.standardTimeoutMillis());

    // Trick the compilation job into thinking it needs to rebuilt after time0 + 20
    CompiledViewDefinitionWithGraphsImpl compiledViewDefinition = new CompiledViewDefinitionWithGraphsImpl(compilationModel1.getViewDefinition(), compilationModel1.getDependencyGraphsByConfiguration(), compilationModel1.getPortfolio(), compilationModel1.getFunctionInitId()) {
      @Override
      public boolean isValidFor(final InstantProvider timestampProvider) {
        Instant timestamp = timestampProvider.toInstant();
        return (!timestamp.isAfter(time0.plusMillis(20)));
      }
    };
    computationJob.setCachedCompiledViewDefinition(compiledViewDefinition);

    // Running at time0 + 20 doesn't require a rebuild - should still use our dummy
    computationJob.marketDataChanged();
    resultListener.expectNextCall(JobResultReceivedCall.class, 10 * Timeout.standardTimeoutMillis());
    assertEquals(time0.plusMillis(20), resultListener.getCycleCompleted(10 * Timeout.standardTimeoutMillis()).getFullResult().getValuationTime());
    resultListener.assertNoCalls();

    // time0 + 30 requires a rebuild
    computationJob.marketDataChanged();
    CompiledViewDefinition compilationModel2 = resultListener.getViewDefinitionCompiled(Timeout.standardTimeoutMillis()).getCompiledViewDefinition();
    assertNotSame(compilationModel1, compilationModel2);
    assertNotSame(compiledViewDefinition, compilationModel2);
    resultListener.expectNextCall(JobResultReceivedCall.class, 10 * Timeout.standardTimeoutMillis());
    assertEquals(time0.plusMillis(30), resultListener.getCycleCompleted(Timeout.standardTimeoutMillis()).getFullResult().getValuationTime());
    resultListener.assertProcessCompleted(Timeout.standardTimeoutMillis());

    resultListener.assertNoCalls(Timeout.standardTimeoutMillis());

    assertTrue(executionOptions.getExecutionSequence().isEmpty());

    // Job should have terminated automatically with no further evaluation times
    assertEquals(ViewProcessState.FINISHED, viewProcess.getState());
    assertTrue(computationJob.isTerminated());
    assertFalse(computationThread.isAlive());

    vp.stop();
  }
  
}
