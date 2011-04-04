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

import org.testng.annotations.Test;

import com.opengamma.engine.test.TestComputationResultListener;
import com.opengamma.engine.test.TestViewCompilationListener;
import com.opengamma.engine.test.ViewProcessorTestEnvironment;
import com.opengamma.engine.view.calc.ViewComputationJob;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewClientState;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionImpl;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.util.test.Timeout;

/**
 * Tests View
 */
@Test
public class ViewTest {

  public void testLifecycle() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client.attachToViewProcess(env.getViewDefinition().getName(), ExecutionOptions.realTime());
    
    ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    
    assertEquals(env.getViewDefinition().getName(), viewProcess.getDefinitionName());
    
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
    client.attachToViewProcess(env.getViewDefinition().getName(), ExecutionOptions.realTime());
    
    ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    
    assertNull(client.getLatestResult());
    assertEquals(env.getViewDefinition(), viewProcess.getDefinition());
    
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
    
    client.attachToViewProcess(env.getViewDefinition().getName(), ExecutionOptions.realTime());    
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
    
    TestViewCompilationListener compilationListener = new TestViewCompilationListener();
    client.setCompilationListener(compilationListener);
    
    TestComputationResultListener resultListener = new TestComputationResultListener();
    client.setResultListener(resultListener);
    
    final long time0 = System.currentTimeMillis();
    final ViewExecutionOptions executionOptions = new ExecutionOptions(ArbitraryViewCycleExecutionSequence.of(time0, time0 + 10, time0 + 20, time0 + 30), false, false, null);
        
    client.attachToViewProcess(env.getViewDefinition().getName(), executionOptions);
    
    ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    ViewComputationJob computationJob = env.getCurrentComputationJob(viewProcess);
    Thread computationThread = env.getCurrentComputationThread(viewProcess);
    
    CompiledViewDefinitionImpl compilationModel1 = (CompiledViewDefinitionImpl) compilationListener.getResult(Timeout.standardTimeoutMillis());
    assertEquals(time0, resultListener.getResult(10 * Timeout.standardTimeoutMillis()).getValuationTime().toEpochMillisLong());
    
    computationJob.liveDataChanged();
    assertEquals(time0 + 10, resultListener.getResult(10 * Timeout.standardTimeoutMillis()).getValuationTime().toEpochMillisLong());
    compilationListener.assertNoResult(Timeout.standardTimeoutMillis());

    // Trick the compilation job into thinking it needs to rebuilt after time0 + 20
    CompiledViewDefinitionImpl compiledViewDefinition = new CompiledViewDefinitionImpl(compilationModel1.getViewDefinition(), compilationModel1.getDependencyGraphsByConfiguration(), compilationModel1.getPortfolio(), compilationModel1.getFunctionInitId()) {
      @Override
      public boolean isValidFor(final InstantProvider timestampProvider) {
        Instant timestamp = timestampProvider.toInstant();
        return (!timestamp.isAfter(Instant.ofEpochMillis(time0 + 20)));
      }
    };
    computationJob.setLatestCompiledViewDefinition(compiledViewDefinition);
    
    // Running at time0 + 20 doesn't require a rebuild - should still use our dummy
    computationJob.liveDataChanged();
    compilationListener.assertNoResult(Timeout.standardTimeoutMillis());
    assertEquals(time0 + 20, resultListener.getResult(10 * Timeout.standardTimeoutMillis()).getValuationTime().toEpochMillisLong());

    // time0 + 30 requires a rebuild
    computationJob.liveDataChanged();
    CompiledViewDefinition compilationModel2 = compilationListener.getResult(Timeout.standardTimeoutMillis());
    assertNotSame(compilationModel1, compilationModel2);
    assertNotSame(compiledViewDefinition, compilationModel2);
    assertEquals(time0 + 30, resultListener.getResult(Timeout.standardTimeoutMillis()).getValuationTime().toEpochMillisLong());
    
    compilationListener.assertNoResult(Timeout.standardTimeoutMillis());
    resultListener.assertNoResult(Timeout.standardTimeoutMillis());
    
    assertTrue(executionOptions.getExecutionSequence().isEmpty());
    
    // Job should have terminated automatically with no further evaluation times
    assertEquals(ViewProcessState.FINISHED, viewProcess.getState());
    assertTrue(computationJob.isTerminated());
    assertFalse(computationThread.isAlive());
    
    vp.stop();
  }
  
}
