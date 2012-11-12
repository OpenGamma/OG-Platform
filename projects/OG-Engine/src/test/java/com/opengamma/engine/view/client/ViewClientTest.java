/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.marketdata.AbstractMarketDataProvider;
import com.opengamma.engine.marketdata.AbstractMarketDataSnapshot;
import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.MarketDataUtils;
import com.opengamma.engine.marketdata.PermissiveMarketDataPermissionProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.test.TestViewResultListener;
import com.opengamma.engine.test.ViewProcessorTestEnvironment;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLog;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.ViewProcessImpl;
import com.opengamma.engine.view.ViewProcessState;
import com.opengamma.engine.view.ViewProcessorImpl;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.calc.ViewComputationJob;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.listener.CycleStartedCall;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.log.LogBridge;
import com.opengamma.util.log.LogEvent;
import com.opengamma.util.log.LogLevel;
import com.opengamma.util.log.SimpleLogEvent;
import com.opengamma.util.test.Timeout;
import com.opengamma.util.tuple.Pair;

/**
 * Tests ViewClient
 */
@Test
public class ViewClientTest {
  
  private static final long TIMEOUT = Timeout.standardTimeoutMillis();
  
  @Test
  public void testSingleViewMultipleClients() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client1 = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    assertNotNull(client1.getUniqueId());
    
    client1.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    ViewProcessImpl client1Process = env.getViewProcess(vp, client1.getUniqueId());
    assertTrue(client1Process.getState() == ViewProcessState.RUNNING);
    
    ViewClient client2 = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    assertNotNull(client2.getUniqueId());
    assertFalse(client1.getUniqueId().equals(client2.getUniqueId()));
    
    client2.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    ViewProcessImpl client2Process = env.getViewProcess(vp, client2.getUniqueId());
    assertEquals(client1Process, client2Process);
    assertTrue(client2Process.getState() == ViewProcessState.RUNNING);

    client1.detachFromViewProcess();
    assertTrue(client2Process.getState() == ViewProcessState.RUNNING);
    
    client2.detachFromViewProcess();
    assertTrue(client2Process.getState() == ViewProcessState.TERMINATED);

    client1.shutdown();
    client2.shutdown();
  }
  
  @Test
  public void testCascadingShutdown() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client1 = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client1.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    
    ViewClient client2 = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client2.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    
    ViewProcessImpl view = env.getViewProcess(vp, client1.getUniqueId());
    
    vp.stop();
    
    assertFalse(vp.isRunning());
    assertFalse(view.isRunning());
    assertTrue(view.getState() == ViewProcessState.TERMINATED);
    
    assertFalse(client1.isAttached());
    assertFalse(client2.isAttached());
    
    client1.shutdown();
    client2.shutdown();
  }
  
  @Test
  public void testComputationResultsFlow() throws InterruptedException {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    SynchronousInMemoryLKVSnapshotProvider marketDataProvider = new SynchronousInMemoryLKVSnapshotProvider();
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 0);
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), (byte) 0);
    env.setMarketDataProvider(marketDataProvider);
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client.setFragmentResultMode(ViewResultMode.FULL_ONLY);
    TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);
    
    // Client not attached - should not have been listening to anything that might have been going on
    assertEquals(0, resultListener.getQueueSize());

    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 1);
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), (byte) 2);
    
    assertEquals(0, resultListener.getQueueSize());
    
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    
    ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    assertTrue(viewProcess.getState() == ViewProcessState.RUNNING);
    
    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    resultListener.assertCycleStarted(TIMEOUT);
    ViewResultModel result1Fragment = resultListener.getCycleFragmentCompleted(TIMEOUT).getFullFragment();
    assertNotNull(result1Fragment);
    ViewComputationResultModel result1 = resultListener.getCycleCompleted(TIMEOUT).getFullResult();
    assertNotNull(result1);

    Map<ValueRequirement, Object> expected = new HashMap<ValueRequirement, Object>();
    expected.put(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 1);
    expected.put(ViewProcessorTestEnvironment.getPrimitive2(), (byte) 2);
    assertComputationResult(expected, env.getCalculationResult(result1));
    assertComputationResult(expected, env.getCalculationResult(result1Fragment));
    assertTrue(client.isResultAvailable());
    assertEquals(result1, client.getLatestResult());
    
    client.pause();
    
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 3);
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), (byte) 4);
    
    env.getCurrentComputationJob(viewProcess).marketDataChanged();  // Need to get it to perform another cycle
    
    // Should have been merging results received in the meantime
    client.resume();
    UniqueId cycleStartedId = resultListener.getCycleStarted(TIMEOUT).getCycleMetadata().getViewCycleId();
    resultListener.assertCycleFragmentCompleted(TIMEOUT);
    ViewComputationResultModel result2 = resultListener.getCycleCompleted(TIMEOUT).getFullResult();
    assertEquals(result2.getViewCycleId(), cycleStartedId);

    expected = new HashMap<ValueRequirement, Object>();
    expected.put(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 3);
    expected.put(ViewProcessorTestEnvironment.getPrimitive2(), (byte) 4);
    assertComputationResult(expected, env.getCalculationResult(result2));
  }

  @Test
  public void testDeltaResults() throws InterruptedException {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    SynchronousInMemoryLKVSnapshotProvider marketDataProvider = new SynchronousInMemoryLKVSnapshotProvider();
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), 0);
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), 0);
    env.setMarketDataProvider(marketDataProvider);
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client.setResultMode(ViewResultMode.DELTA_ONLY);
    client.setFragmentResultMode(ViewResultMode.FULL_ONLY);
    
    TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);
    
    // Client not attached - should not have been listening to anything that might have been going on
    assertEquals(0, resultListener.getQueueSize());
    
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), 1);
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), 2);
    
    assertEquals(0, resultListener.getQueueSize());
    
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    resultListener.assertCycleStarted(TIMEOUT);
    resultListener.assertCycleFragmentCompleted(TIMEOUT);
    ViewDeltaResultModel result1 = resultListener.getCycleCompleted(TIMEOUT).getDeltaResult();
    assertNotNull(result1);

    Map<ValueRequirement, Object> expected = new HashMap<ValueRequirement, Object>();
    expected.put(ViewProcessorTestEnvironment.getPrimitive1(), 1);
    expected.put(ViewProcessorTestEnvironment.getPrimitive2(), 2);
    assertComputationResult(expected, env.getCalculationResult(result1));
    
    client.pause();
    
    // Just update one live data value, and only this one value should end up in the delta
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), 3);
    
    assertEquals(0, resultListener.getQueueSize());
    ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    env.getCurrentComputationJob(viewProcess).marketDataChanged();  // Need to get it to perform another cycle
    
    // Should have been merging results received in the meantime
    client.resume();
    resultListener.assertCycleStarted(TIMEOUT);
    resultListener.assertCycleFragmentCompleted(TIMEOUT);
    ViewDeltaResultModel result2 = resultListener.getCycleCompleted(TIMEOUT).getDeltaResult();

    
    expected = new HashMap<ValueRequirement, Object>();
    expected.put(ViewProcessorTestEnvironment.getPrimitive1(), 3);
    assertComputationResult(expected, env.getCalculationResult(result2));
  }
  
  @Test
  public void testStates() throws InterruptedException {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    SynchronousInMemoryLKVSnapshotProvider marketDataProvider = new SynchronousInMemoryLKVSnapshotProvider();
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 0);
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), (byte) 0);
    env.setMarketDataProvider(marketDataProvider);
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client1 = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client1.setFragmentResultMode(ViewResultMode.FULL_ONLY);
    TestViewResultListener client1ResultListener = new TestViewResultListener();
    client1.setResultListener(client1ResultListener);
    
    assertEquals(0, client1ResultListener.getQueueSize());
    
    client1.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    
    // Wait for first computation cycle
    client1ResultListener.assertViewDefinitionCompiled(TIMEOUT);
    client1ResultListener.expectNextCall(CycleStartedCall.class, TIMEOUT);
    client1ResultListener.assertCycleFragmentCompleted(TIMEOUT);
    client1ResultListener.assertCycleCompleted(TIMEOUT);
    
    ViewClient client2 = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client2.setFragmentResultMode(ViewResultMode.FULL_ONLY);
    TestViewResultListener client2ResultListener = new TestViewResultListener();
    client2.setResultListener(client2ResultListener);
    
    assertEquals(0, client2ResultListener.getQueueSize());
    client2.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    
    // Initial result should be pushed through
    client2ResultListener.assertViewDefinitionCompiled(TIMEOUT);
    client2ResultListener.assertCycleCompleted(TIMEOUT);
    
    ViewProcessImpl viewProcess1 = env.getViewProcess(vp, client1.getUniqueId());
    ViewProcessImpl viewProcess2 = env.getViewProcess(vp, client2.getUniqueId());
    assertEquals(viewProcess1, viewProcess2);
    
    client1.pause();
    client1ResultListener.assertNoCalls(TIMEOUT);
    
    // Now client 1 is paused, so any changes should be batched.
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 1);
    env.getCurrentComputationJob(viewProcess1).marketDataChanged();
    client2ResultListener.assertCycleStarted(TIMEOUT);
    client2ResultListener.assertCycleFragmentCompleted(TIMEOUT);
    client2ResultListener.assertCycleCompleted(TIMEOUT);
    assertEquals(0, client2ResultListener.getQueueSize());
    client1ResultListener.assertNoCalls(TIMEOUT);
    
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 2);
    env.getCurrentComputationJob(viewProcess1).marketDataChanged();
    client2ResultListener.assertCycleStarted(TIMEOUT);
    client2ResultListener.assertCycleFragmentCompleted(TIMEOUT);
    client2ResultListener.assertCycleCompleted(TIMEOUT);
    assertEquals(0, client2ResultListener.getQueueSize());
    client1ResultListener.assertNoCalls(TIMEOUT);
    
    // Resuming should release the most recent result to the client
    client1.resume();
    client1ResultListener.getCycleStarted(TIMEOUT).getCycleMetadata();
    ViewComputationResultModel result2Fragment = client1ResultListener.getCycleFragmentCompleted(TIMEOUT).getFullFragment();
    ViewComputationResultModel result2 = client1ResultListener.getCycleCompleted(TIMEOUT).getFullResult();
    Map<ValueRequirement, Object> expected = new HashMap<ValueRequirement, Object>();
    expected.put(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 2);
    expected.put(ViewProcessorTestEnvironment.getPrimitive2(), (byte) 0);
    assertComputationResult(expected, env.getCalculationResult(result2Fragment));
    assertComputationResult(expected, env.getCalculationResult(result2));
    
    // Changes should now propagate straight away to both listeners
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 3);
    env.getCurrentComputationJob(viewProcess1).marketDataChanged();
    client1ResultListener.assertCycleStarted(TIMEOUT);
    client2ResultListener.assertCycleStarted(TIMEOUT);
    client2ResultListener.assertCycleFragmentCompleted(TIMEOUT);
    client2ResultListener.assertCycleCompleted(TIMEOUT);
    ViewComputationResultModel result3Fragment = client1ResultListener.getCycleFragmentCompleted(TIMEOUT).getFullFragment();
    ViewComputationResultModel result3 = client1ResultListener.getCycleCompleted(TIMEOUT).getFullResult();
    expected = new HashMap<ValueRequirement, Object>();
    expected.put(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 3);
    expected.put(ViewProcessorTestEnvironment.getPrimitive2(), (byte) 0);
    assertComputationResult(expected, env.getCalculationResult(result3Fragment));
    assertComputationResult(expected, env.getCalculationResult(result3));

    // Pause results again and we should be back to merging both whole cycle results and fragments
    client1.pause();
    client2ResultListener.assertNoCalls(TIMEOUT);
    client1ResultListener.assertNoCalls(TIMEOUT);

    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), (byte) 1);
    env.getCurrentComputationJob(viewProcess1).marketDataChanged();
    client2ResultListener.assertCycleStarted(TIMEOUT);
    client2ResultListener.assertCycleFragmentCompleted(TIMEOUT);
    client2ResultListener.assertCycleCompleted(TIMEOUT);
    assertEquals(0, client2ResultListener.getQueueSize());
    client1ResultListener.assertNoCalls(TIMEOUT);

    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), (byte) 2);
    env.getCurrentComputationJob(viewProcess1).marketDataChanged();
    client2ResultListener.assertCycleStarted(TIMEOUT);
    client2ResultListener.assertCycleFragmentCompleted(TIMEOUT);
    client2ResultListener.assertCycleCompleted(TIMEOUT);
    assertEquals(0, client2ResultListener.getQueueSize());
    client1ResultListener.assertNoCalls(TIMEOUT);
    
    // Start results again
    client1.resume();
    client1ResultListener.assertCycleStarted(TIMEOUT);
    ViewComputationResultModel result4Fragment = client1ResultListener.getCycleFragmentCompleted(TIMEOUT).getFullFragment();
    ViewComputationResultModel result4 = client1ResultListener.getCycleCompleted(TIMEOUT).getFullResult();
    assertEquals(0, client1ResultListener.getQueueSize());
    client2ResultListener.assertNoCalls(TIMEOUT);
    expected = new HashMap<ValueRequirement, Object>();
    expected.put(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 3);
    expected.put(ViewProcessorTestEnvironment.getPrimitive2(), (byte) 2);
    assertComputationResult(expected, env.getCalculationResult(result4Fragment));
    assertComputationResult(expected, env.getCalculationResult(result4));
    
    client1.detachFromViewProcess();
    client2ResultListener.assertNoCalls(TIMEOUT);
    client1ResultListener.assertNoCalls(TIMEOUT);
    
    client1.shutdown();
    client1ResultListener.assertClientShutdown(TIMEOUT);
    
    client2.setResultListener(null);
    client2.shutdown();
    client2ResultListener.assertNoCalls(TIMEOUT);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testUseTerminatedClient() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    
    ViewProcess viewProcess = env.getViewProcess(vp, client.getUniqueId());
    
    client.shutdown();
    
    assertEquals(ViewProcessState.TERMINATED, viewProcess.getState());
    
    client.pause();
  }

  @Test
  public void testChangeOfListeners() throws InterruptedException {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    SynchronousInMemoryLKVSnapshotProvider marketDataProvider = new SynchronousInMemoryLKVSnapshotProvider();
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), 0);
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), 0);
    env.setMarketDataProvider(marketDataProvider);
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client.setFragmentResultMode(ViewResultMode.FULL_ONLY);
    TestViewResultListener resultListener1 = new TestViewResultListener();
    client.setResultListener(resultListener1);
    
    // Start live computation and collect the initial result
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), 2);

    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    assertEquals(ViewProcessState.RUNNING, viewProcess.getState());
    
    ViewComputationJob recalcJob = env.getCurrentComputationJob(viewProcess);
    resultListener1.assertViewDefinitionCompiled(TIMEOUT);
    resultListener1.assertCycleStarted(TIMEOUT);
    resultListener1.assertCycleFragmentCompleted(TIMEOUT);
    resultListener1.assertCycleCompleted(TIMEOUT);
    assertEquals(0, resultListener1.getQueueSize());
    
    // Push through a second result
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), 3);
    recalcJob.marketDataChanged();
    resultListener1.assertCycleStarted(TIMEOUT);
    resultListener1.assertCycleFragmentCompleted(TIMEOUT);
    resultListener1.assertCycleCompleted(TIMEOUT);
    assertEquals(0, resultListener1.getQueueSize());

    // Change listener
    TestViewResultListener resultListener2 = new TestViewResultListener();
    client.setResultListener(resultListener2);

    // Push through a result which should arrive at the new listeners
    recalcJob.marketDataChanged();
    resultListener2.assertCycleStarted(TIMEOUT);
    resultListener2.assertCycleFragmentCompleted(TIMEOUT);
    resultListener2.assertCycleCompleted(TIMEOUT);
    assertEquals(0, resultListener1.getQueueSize());
    assertEquals(0, resultListener2.getQueueSize());

    client.setResultListener(null);
    client.shutdown();
    assertEquals(ViewProcessState.TERMINATED, viewProcess.getState());
    
    vp.stop();
  }
  
  @Test
  public void testOldRecalculationThreadDies() throws InterruptedException {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    SynchronousInMemoryLKVSnapshotProvider marketDataProvider = new SynchronousInMemoryLKVSnapshotProvider();
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), 0);
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), 0);
    env.setMarketDataProvider(marketDataProvider);
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    ViewProcessImpl viewProcess1 = env.getViewProcess(vp, client.getUniqueId());
    
    ViewComputationJob recalcJob1 = env.getCurrentComputationJob(viewProcess1);
    Thread recalcThread1 = env.getCurrentComputationThread(viewProcess1);
    assertFalse(recalcJob1.isTerminated());
    assertTrue(recalcThread1.isAlive());
    
    client.detachFromViewProcess();
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    ViewProcessImpl viewProcess2 = env.getViewProcess(vp, client.getUniqueId());
    ViewComputationJob recalcJob2 = env.getCurrentComputationJob(viewProcess2);
    Thread recalcThread2 = env.getCurrentComputationThread(viewProcess2);
    
    assertFalse(viewProcess1 == viewProcess2);
    assertTrue(recalcJob1.isTerminated());
    assertFalse(recalcJob2.isTerminated());

    recalcThread1.join(TIMEOUT);
    assertFalse(recalcThread1.isAlive());
    assertTrue(recalcThread2.isAlive());
    
    vp.stop();
    
    assertTrue(recalcJob2.isTerminated());
  }
  
  //-------------------------------------------------------------------------
  @Test
  public void testSetMinimumLogMode() throws InterruptedException {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    SynchronousInMemoryLKVSnapshotProvider marketDataProvider = new SynchronousInMemoryLKVSnapshotProvider();
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), 0);
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), 0);
    env.setMarketDataProvider(marketDataProvider);
    InMemoryFunctionRepository functionRepository = new InMemoryFunctionRepository();
    
    ComputationTarget target = new ComputationTarget(ComputationTargetType.PRIMITIVE, "USD");
    MockFunction fn = new MockFunction(MockFunction.UNIQUE_ID, target) {
      
      @Override
      public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
        LogBridge.getInstance().log(new SimpleLogEvent(LogLevel.WARN, "Warning during execution"));
        LogBridge.getInstance().log(new SimpleLogEvent(LogLevel.ERROR, "Error during execution"));
        return super.execute(executionContext, inputs, target, desiredValues);
      }
      
    };
    ValueRequirement requirement = new ValueRequirement("OUTPUT", target.toSpecification());
    fn.addResult(requirement, "Result");
    functionRepository.addFunction(fn);
    env.setFunctionRepository(functionRepository);
    
    ViewDefinition vd = new ViewDefinition(UniqueId.of("test", "vd1"), "Test view", UserPrincipal.getLocalUser());
    ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(vd, "Default");
    calcConfig.addSpecificRequirement(requirement);
    vd.addViewCalculationConfiguration(calcConfig);
    vd.setMinFullCalculationPeriod(Long.MAX_VALUE);  // Never force a full calculation
    vd.setMaxFullCalculationPeriod(Long.MAX_VALUE);  // Never force a full calculation
    env.setViewDefinition(vd);
    
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);
    
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    
    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    ViewComputationResultModel result1 = resultListener.getCycleCompleted(TIMEOUT).getFullResult();
    assertEquals(0, resultListener.getQueueSize());
    
    assertEquals(1, result1.getAllResults().size());
    ComputedValueResult result1Value = Iterables.getOnlyElement(result1.getAllResults()).getComputedValue();
    assertEquals("Result", result1Value.getValue());
    
    ExecutionLog log1 = result1Value.getExecutionLog();
    assertNotNull(log1);
    assertTrue(log1.hasError());
    assertTrue(log1.hasWarn());
    assertFalse(log1.hasInfo());
    assertNull(log1.getEvents());
    assertNull(log1.getExceptionClass());
    assertNull(log1.getExceptionMessage());
    assertNull(log1.getExceptionStackTrace());
    
    ValueSpecification resultSpec = Iterables.getOnlyElement(client.getLatestCompiledViewDefinition().getTerminalValuesRequirements().keySet());
    client.setMinimumLogMode(ExecutionLogMode.FULL, ImmutableSet.of(resultSpec));
    
    ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    ViewComputationJob recalcJob = env.getCurrentComputationJob(viewProcess);
    recalcJob.triggerCycle();
    
    ViewComputationResultModel result2 = resultListener.getCycleCompleted(TIMEOUT).getFullResult();
    assertEquals(0, resultListener.getQueueSize());
    
    assertEquals(1, result2.getAllResults().size());
    ComputedValueResult result2Value = Iterables.getOnlyElement(result2.getAllResults()).getComputedValue();
    assertEquals("Result", result2Value.getValue());
    
    ExecutionLog log2 = result2Value.getExecutionLog();
    assertNotNull(log2);
    assertTrue(log2.hasError());
    assertTrue(log2.hasWarn());
    assertFalse(log2.hasInfo());
    assertNotNull(log2.getEvents());
    assertEquals(2, log2.getEvents().size());
    LogEvent log2Event1 = log2.getEvents().get(0);
    assertEquals(LogLevel.WARN, log2Event1.getLevel());
    assertEquals("Warning during execution", log2Event1.getMessage());
    LogEvent log2Event2 = log2.getEvents().get(1);
    assertEquals(LogLevel.ERROR, log2Event2.getLevel());
    assertEquals("Error during execution", log2Event2.getMessage());
    assertNull(log2.getExceptionClass());
    assertNull(log2.getExceptionMessage());
    assertNull(log2.getExceptionStackTrace());
    
    client.setMinimumLogMode(ExecutionLogMode.INDICATORS, ImmutableSet.of(resultSpec));
    recalcJob.triggerCycle();
    
    ViewComputationResultModel result3 = resultListener.getCycleCompleted(TIMEOUT).getFullResult();
    assertEquals(0, resultListener.getQueueSize());
    
    assertEquals(1, result3.getAllResults().size());
    ComputedValueResult result3Value = Iterables.getOnlyElement(result3.getAllResults()).getComputedValue();
    assertEquals("Result", result3Value.getValue());
    
    ExecutionLog log3 = result3Value.getExecutionLog();
    assertNotNull(log3);
    // Delta cycle - should reuse the previous result which *does* include logs. 
    assertNotNull(log3.getEvents());
    
    // Force a full cycle - should *not* reuse any previous result, so back to indicators only
    recalcJob.dirtyViewDefinition();
    recalcJob.triggerCycle();
    
    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    ViewComputationResultModel result4 = resultListener.getCycleCompleted(TIMEOUT).getFullResult();
    assertEquals(0, resultListener.getQueueSize());
    
    assertEquals(1, result4.getAllResults().size());
    ComputedValueResult result4Value = Iterables.getOnlyElement(result4.getAllResults()).getComputedValue();
    assertEquals("Result", result4Value.getValue());
    
    ExecutionLog log4 = result4Value.getExecutionLog();
    assertNotNull(log4); 
    assertNull(log4.getEvents());
  }

  //-------------------------------------------------------------------------
  private void assertComputationResult(Map<ValueRequirement, Object> expected, ViewCalculationResultModel result) {
    assertNotNull(result);
    Set<ValueRequirement> remaining = new HashSet<ValueRequirement>(expected.keySet());
    Collection<ComputationTargetSpecification> targets = result.getAllTargets();
    for (ComputationTargetSpecification target : targets) {
      Map<Pair<String, ValueProperties>, ComputedValueResult> values = result.getValues(target);
      for (Map.Entry<Pair<String, ValueProperties>, ComputedValueResult> value : values.entrySet()) {
        String valueName = value.getKey().getFirst();
        ValueRequirement requirement = new ValueRequirement(valueName, target.getType(), target.getUniqueId());
        assertTrue(expected.containsKey(requirement));
        
        assertEquals(expected.get(requirement), value.getValue().getValue());
        remaining.remove(requirement);
      }
    }
    assertEquals(Collections.emptySet(), remaining);
  }
  
  /**
   * Avoids the ConcurrentHashMap-based implementation of InMemoryLKVSnapshotProvider, where the LKV map can appear to
   * lag behind if accessed from a different thread immediately after a change.
   */
  private static class SynchronousInMemoryLKVSnapshotProvider extends AbstractMarketDataProvider implements MarketDataInjector, 
      MarketDataAvailabilityProvider {
    
    private static final Logger s_logger = LoggerFactory.getLogger(SynchronousInMemoryLKVSnapshotProvider.class);
    
    private final Map<ValueRequirement, ComputedValue> _lastKnownValues = new HashMap<ValueRequirement, ComputedValue>();
    private final MarketDataPermissionProvider _permissionProvider = new PermissiveMarketDataPermissionProvider();

    @Override
    public void subscribe(ValueRequirement valueRequirement) {
      subscribe(Collections.singleton(valueRequirement));
    }

    @Override
    public void subscribe(Set<ValueRequirement> valueRequirements) {
      // No actual subscription to make, but we still need to acknowledge it.
      subscriptionSucceeded(valueRequirements);
    }
    
    @Override
    public void unsubscribe(ValueRequirement valueRequirement) {
    }

    @Override
    public void unsubscribe(Set<ValueRequirement> valueRequirements) {
    }

    //-----------------------------------------------------------------------
    @Override
    public MarketDataAvailabilityProvider getAvailabilityProvider() {
      return this;
    }

    @Override
    public MarketDataPermissionProvider getPermissionProvider() {
      return _permissionProvider;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isCompatible(MarketDataSpecification marketDataSpec) {
      return true;
    }
    
    @Override
    public MarketDataSnapshot snapshot(MarketDataSpecification marketDataSpec) {
      synchronized (_lastKnownValues) {
        Map<ValueRequirement, ComputedValue> snapshotValues = new HashMap<ValueRequirement, ComputedValue>(_lastKnownValues);
        return new SynchronousInMemoryLKVSnapshot(snapshotValues);
      }
    }

    //-----------------------------------------------------------------------
    @Override
    public void addValue(ValueRequirement requirement, Object value) {
      s_logger.debug("Setting {} = {}", requirement, value);
      synchronized (_lastKnownValues) {
        _lastKnownValues.put(requirement, new ComputedValue(MarketDataUtils.createMarketDataValue(requirement), value));
      }
      // Don't notify listeners of the change - we'll kick off a computation cycle manually in the tests
    }
    
    @Override
    public void addValue(ExternalId identifier, String valueName, Object value) {
    }

    @Override
    public void removeValue(ValueRequirement valueRequirement) {
      synchronized(_lastKnownValues) {
        _lastKnownValues.remove(valueRequirement);
      }
      // Don't notify listeners of the change - we'll kick off a computation cycle manually in the tests
    }
    
    @Override
    public void removeValue(ExternalId identifier, String valueName) {
    }

    //-----------------------------------------------------------------------
    @Override
    public ValueSpecification getAvailability(final ValueRequirement requirement) {
      synchronized (_lastKnownValues) {
        return _lastKnownValues.containsKey(requirement) ? MarketDataUtils.createMarketDataValue(requirement) : null;
      }
    }

  }
  
  private static class SynchronousInMemoryLKVSnapshot extends AbstractMarketDataSnapshot {

    private final Map<ValueRequirement, ComputedValue> _snapshot;
    private final Instant _snapshotTime = Instant.now();
    
    public SynchronousInMemoryLKVSnapshot(Map<ValueRequirement, ComputedValue> snapshot) {
      _snapshot = snapshot;
    }

    @Override
    public UniqueId getUniqueId() {
      return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "SynchronousInMemoryLKVSnapshot:"+getSnapshotTime());
    }
    
    @Override
    public Instant getSnapshotTimeIndication() {
      return _snapshotTime;
    }

    @Override
    public Instant getSnapshotTime() {
      return _snapshotTime;
    }
    
    @Override
    public ComputedValue query(ValueRequirement requirement) {
      return _snapshot.get(requirement);
    }
    
  }
  
}
