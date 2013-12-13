/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.test.TestViewResultListener;
import com.opengamma.engine.test.ViewProcessorTestEnvironment;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.ViewProcessState;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewClientState;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfigurationImpl;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.CycleCompletedCall;
import com.opengamma.engine.view.listener.CycleFragmentCompletedCall;
import com.opengamma.engine.view.listener.CycleStartedCall;
import com.opengamma.engine.view.listener.ViewDefinitionCompiledCall;
import com.opengamma.engine.view.worker.SingleThreadViewProcessWorker;
import com.opengamma.engine.view.worker.ViewProcessWorker;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestLifecycle;
import com.opengamma.util.test.Timeout;

/**
 * Tests {@link ViewProcess}
 */
@Test(groups = TestGroup.INTEGRATION)
public class ViewProcessTest {

  public void testLifecycle() {
    TestLifecycle.begin();
    try {
      final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
      env.init();
      final ViewProcessorImpl vp = env.getViewProcessor();
      vp.start();

      final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
      client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));

      final ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());

      assertEquals(env.getViewDefinition().getUniqueId(), viewProcess.getDefinitionId());

      viewProcess.stop();
      assertFalse(client.isAttached());
      vp.stop();
    } finally {
      TestLifecycle.end();
    }
  }

  public void testViewAccessors() {
    TestLifecycle.begin();
    try {
      final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
      env.init();
      final ViewProcessorImpl vp = env.getViewProcessor();
      vp.start();

      final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
      client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));

      final ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());

      assertNull(client.getLatestResult());
      assertEquals(env.getViewDefinition(), viewProcess.getLatestViewDefinition());

      vp.stop();
    } finally {
      TestLifecycle.end();
    }
  }

  public void testCreateClient() {
    TestLifecycle.begin();
    try {
      final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
      env.init();
      final ViewProcessorImpl vp = env.getViewProcessor();
      vp.start();

      final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
      assertNotNull(client.getUniqueId());

      assertEquals(ViewClientState.STARTED, client.getState());
      client.pause();
      assertEquals(ViewClientState.PAUSED, client.getState());
      client.resume();
      assertEquals(ViewClientState.STARTED, client.getState());

      assertEquals(client, vp.getViewClient(client.getUniqueId()));

      client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
      final ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
      viewProcess.stop();

      // Should automatically detach the client
      assertFalse(client.isAttached());
      assertEquals(ViewClientState.STARTED, client.getState());

      vp.stop();
    } finally {
      TestLifecycle.end();
    }
  }

  public void testGraphRebuild() throws InterruptedException {
    TestLifecycle.begin();
    try {
      final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
      env.init();
      final ViewProcessorImpl vp = env.getViewProcessor();
      vp.start();

      final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);

      final TestViewResultListener resultListener = new TestViewResultListener();
      client.setResultListener(resultListener);

      final Instant time0 = Instant.now();
      final ViewCycleExecutionOptions defaultCycleOptions = ViewCycleExecutionOptions.builder().setMarketDataSpecification(MarketData.live()).create();
      final ViewExecutionOptions executionOptions = new ExecutionOptions(ArbitraryViewCycleExecutionSequence.of(time0, time0.plusMillis(10), time0.plusMillis(20), time0.plusMillis(30)),
          ExecutionFlags.none().get(), defaultCycleOptions);

      client.attachToViewProcess(env.getViewDefinition().getUniqueId(), executionOptions);

      final ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
      final ViewProcessWorker worker = env.getCurrentWorker(viewProcess);

      final CompiledViewDefinitionWithGraphsImpl compilationModel1 = (CompiledViewDefinitionWithGraphsImpl) resultListener.getViewDefinitionCompiled(Timeout.standardTimeoutMillis())
          .getCompiledViewDefinition();

      assertEquals(time0, resultListener.getCycleCompleted(10 * Timeout.standardTimeoutMillis()).getFullResult().getViewCycleExecutionOptions().getValuationTime());

      worker.requestCycle();
      assertEquals(time0.plusMillis(10), resultListener.getCycleCompleted(10 * Timeout.standardTimeoutMillis()).getFullResult().getViewCycleExecutionOptions().getValuationTime());
      resultListener.assertNoCalls(Timeout.standardTimeoutMillis());

      // TODO: This test doesn't belong here; it is specific to the SingleThreadViewComputationJob.

      // Trick the compilation job into thinking it needs to rebuilt after time0 + 20
      final Collection<DependencyGraph> graphs = CompiledViewDefinitionWithGraphsImpl.getDependencyGraphs(compilationModel1);
      final Collection<CompiledViewCalculationConfiguration> configs = new ArrayList<CompiledViewCalculationConfiguration>(graphs.size());
      for (DependencyGraph graph : graphs) {
        configs.add(CompiledViewCalculationConfigurationImpl.of(graph));
      }
      final CompiledViewDefinitionWithGraphsImpl compiledViewDefinition = new CompiledViewDefinitionWithGraphsImpl(VersionCorrection.of(time0, time0), "",
          compilationModel1.getViewDefinition(), graphs, Collections.<ComputationTargetReference, UniqueId>emptyMap(), compilationModel1.getPortfolio(),
          compilationModel1.getFunctionInitId(), configs, null, null) {
        @Override
        public Instant getValidTo() {
          return time0.plusMillis(20);
        }
      };
      ((SingleThreadViewProcessWorker) worker).cacheCompiledViewDefinition(compiledViewDefinition);

      // Running at time0 + 20 doesn't require a rebuild - should still use our dummy
      worker.requestCycle();
      assertEquals(time0.plusMillis(20), resultListener.getCycleCompleted(10 * Timeout.standardTimeoutMillis()).getFullResult().getViewCycleExecutionOptions().getValuationTime());
      resultListener.assertNoCalls();

      // time0 + 30 requires a rebuild
      worker.requestCycle();
      final CompiledViewDefinition compilationModel2 = resultListener.getViewDefinitionCompiled(Timeout.standardTimeoutMillis()).getCompiledViewDefinition();
      assertNotSame(compilationModel1, compilationModel2);
      assertNotSame(compiledViewDefinition, compilationModel2);
      assertEquals(time0.plusMillis(30), resultListener.getCycleCompleted(Timeout.standardTimeoutMillis()).getFullResult().getViewCycleExecutionOptions().getValuationTime());
      resultListener.assertProcessCompleted(Timeout.standardTimeoutMillis());

      resultListener.assertNoCalls(Timeout.standardTimeoutMillis());

      assertTrue(executionOptions.getExecutionSequence().isEmpty());

      // Job should have terminated automatically with no further evaluation times
      assertEquals(ViewProcessState.FINISHED, viewProcess.getState());
      assertTrue(worker.isTerminated());

      vp.stop();
    } finally {
      TestLifecycle.end();
    }
  }

  public void testStreamingMode() throws InterruptedException {
    TestLifecycle.begin();
    try {
      final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
      env.init();
      final ViewProcessorImpl vp = env.getViewProcessor();
      vp.start();

      final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
      client.setFragmentResultMode(ViewResultMode.FULL_ONLY);

      final TestViewResultListener resultListener = new TestViewResultListener();
      client.setResultListener(resultListener);

      final Instant time0 = Instant.now();
      final ViewCycleExecutionOptions defaultCycleOptions = ViewCycleExecutionOptions.builder().setMarketDataSpecification(MarketData.live()).create();
      final ViewExecutionOptions executionOptions = new ExecutionOptions(ArbitraryViewCycleExecutionSequence.of(time0), ExecutionFlags.none().get(), defaultCycleOptions);

      client.attachToViewProcess(env.getViewDefinition().getUniqueId(), executionOptions);

      final ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
      final ViewProcessWorker worker = env.getCurrentWorker(viewProcess);

      resultListener.expectNextCall(ViewDefinitionCompiledCall.class, 10 * Timeout.standardTimeoutMillis());
      resultListener.expectNextCall(CycleStartedCall.class, 10 * Timeout.standardTimeoutMillis());

      // We expect 2 calls - 1 for initial fragment, 1 for final fragment on completion
      resultListener.expectNextCall(CycleFragmentCompletedCall.class, 10 * Timeout.standardTimeoutMillis());
      resultListener.expectNextCall(CycleFragmentCompletedCall.class, 10 * Timeout.standardTimeoutMillis());

      assertEquals(time0, resultListener.getCycleCompleted(10 * Timeout.standardTimeoutMillis()).getFullResult().getViewCycleExecutionOptions().getValuationTime());

      resultListener.assertProcessCompleted(Timeout.standardTimeoutMillis());
      resultListener.assertNoCalls(Timeout.standardTimeoutMillis());

      assertTrue(executionOptions.getExecutionSequence().isEmpty());

      // Job should have terminated automatically with no further evaluation times
      assertEquals(ViewProcessState.FINISHED, viewProcess.getState());
      assertTrue(worker.isTerminated());

      vp.stop();
    } finally {
      TestLifecycle.end();
    }
  }

  @Test
  public void testPersistentViewDefinition() throws InterruptedException {
    TestLifecycle.begin();
    try {
      final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
      env.init();
      env.getViewDefinition().setPersistent(true);
      final ViewProcessorImpl vp = env.getViewProcessor();
      vp.start();

      final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);

      final TestViewResultListener resultListener = new TestViewResultListener();
      client.setResultListener(resultListener);

      client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live(), ExecutionFlags.none().get()));

      final ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
      assertEquals(ViewProcessState.RUNNING, viewProcess.getState());

      resultListener.expectNextCall(ViewDefinitionCompiledCall.class, Timeout.standardTimeoutMillis());
      resultListener.expectNextCall(CycleCompletedCall.class, Timeout.standardTimeoutMillis());
      resultListener.assertNoCalls(Timeout.standardTimeoutMillis());

      client.detachFromViewProcess();
      assertEquals(ViewProcessState.RUNNING, viewProcess.getState());

      resultListener.assertNoCalls(Timeout.standardTimeoutMillis());

      client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live(), ExecutionFlags.none().get()));
      final ViewProcessImpl viewProcess2 = env.getViewProcess(vp, client.getUniqueId());
      assertEquals(viewProcess, viewProcess2);

      resultListener.expectNextCall(ViewDefinitionCompiledCall.class, Timeout.standardTimeoutMillis());
      resultListener.expectNextCall(CycleCompletedCall.class, Timeout.standardTimeoutMillis());

      client.detachFromViewProcess();

      // Still want to be able to shut down manually, e.g. through JMX
      assertEquals(1, vp.getViewProcesses().size());
      viewProcess.shutdown();
      assertEquals(ViewProcessState.TERMINATED, viewProcess.getState());
      assertEquals(0, vp.getViewProcesses().size());

      vp.stop();
    } finally {
      TestLifecycle.end();
    }
  }

}
