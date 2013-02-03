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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.test.TestViewResultListener;
import com.opengamma.engine.test.ViewProcessorTestEnvironment;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.ExecutionLog;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.ExecutionLogWithContext;
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
import com.opengamma.util.money.Currency;
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
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    final ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();

    final ViewClient client1 = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    assertNotNull(client1.getUniqueId());

    client1.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    final ViewProcessImpl client1Process = env.getViewProcess(vp, client1.getUniqueId());
    assertTrue(client1Process.getState() == ViewProcessState.RUNNING);

    final ViewClient client2 = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    assertNotNull(client2.getUniqueId());
    assertFalse(client1.getUniqueId().equals(client2.getUniqueId()));

    client2.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    final ViewProcessImpl client2Process = env.getViewProcess(vp, client2.getUniqueId());
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
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    final ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();

    final ViewClient client1 = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client1.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));

    final ViewClient client2 = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client2.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));

    final ViewProcessImpl view = env.getViewProcess(vp, client1.getUniqueId());

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
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    final SynchronousInMemoryLKVSnapshotProvider marketDataProvider = new SynchronousInMemoryLKVSnapshotProvider();
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 0);
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), (byte) 0);
    env.setMarketDataProvider(marketDataProvider);
    env.init();

    final ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();

    final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client.setFragmentResultMode(ViewResultMode.FULL_ONLY);
    final TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);

    // Client not attached - should not have been listening to anything that might have been going on
    assertEquals(0, resultListener.getQueueSize());

    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 1);
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), (byte) 2);

    assertEquals(0, resultListener.getQueueSize());

    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));

    final ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    assertTrue(viewProcess.getState() == ViewProcessState.RUNNING);

    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    resultListener.assertCycleStarted(TIMEOUT);
    final ViewResultModel result1Fragment = resultListener.getCycleFragmentCompleted(TIMEOUT).getFullFragment();
    assertNotNull(result1Fragment);
    final ViewComputationResultModel result1 = resultListener.getCycleCompleted(TIMEOUT).getFullResult();
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

    env.getCurrentComputationJob(viewProcess).requestCycle();

    // Should have been merging results received in the meantime
    client.resume();
    final UniqueId cycleStartedId = resultListener.getCycleStarted(TIMEOUT).getCycleMetadata().getViewCycleId();
    resultListener.assertCycleFragmentCompleted(TIMEOUT);
    final ViewComputationResultModel result2 = resultListener.getCycleCompleted(TIMEOUT).getFullResult();
    assertEquals(result2.getViewCycleId(), cycleStartedId);

    expected = new HashMap<ValueRequirement, Object>();
    expected.put(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 3);
    expected.put(ViewProcessorTestEnvironment.getPrimitive2(), (byte) 4);
    assertComputationResult(expected, env.getCalculationResult(result2));
  }

  @Test
  public void testDeltaResults() throws InterruptedException {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    final SynchronousInMemoryLKVSnapshotProvider marketDataProvider = new SynchronousInMemoryLKVSnapshotProvider();
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), 0);
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), 0);
    env.setMarketDataProvider(marketDataProvider);
    env.init();

    final ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();

    final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client.setResultMode(ViewResultMode.DELTA_ONLY);
    client.setFragmentResultMode(ViewResultMode.FULL_ONLY);

    final TestViewResultListener resultListener = new TestViewResultListener();
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
    final ViewDeltaResultModel result1 = resultListener.getCycleCompleted(TIMEOUT).getDeltaResult();
    assertNotNull(result1);

    Map<ValueRequirement, Object> expected = new HashMap<ValueRequirement, Object>();
    expected.put(ViewProcessorTestEnvironment.getPrimitive1(), 1);
    expected.put(ViewProcessorTestEnvironment.getPrimitive2(), 2);
    assertComputationResult(expected, env.getCalculationResult(result1));

    client.pause();

    // Just update one live data value, and only this one value should end up in the delta
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), 3);

    assertEquals(0, resultListener.getQueueSize());
    final ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    env.getCurrentComputationJob(viewProcess).requestCycle();

    // Should have been merging results received in the meantime
    client.resume();
    resultListener.assertCycleStarted(TIMEOUT);
    resultListener.assertCycleFragmentCompleted(TIMEOUT);
    final ViewDeltaResultModel result2 = resultListener.getCycleCompleted(TIMEOUT).getDeltaResult();


    expected = new HashMap<ValueRequirement, Object>();
    expected.put(ViewProcessorTestEnvironment.getPrimitive1(), 3);
    assertComputationResult(expected, env.getCalculationResult(result2));
  }

  @Test
  public void testStates() throws InterruptedException {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    final SynchronousInMemoryLKVSnapshotProvider marketDataProvider = new SynchronousInMemoryLKVSnapshotProvider();
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 0);
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), (byte) 0);
    env.setMarketDataProvider(marketDataProvider);
    env.init();

    final ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();

    final ViewClient client1 = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client1.setFragmentResultMode(ViewResultMode.FULL_ONLY);
    final TestViewResultListener client1ResultListener = new TestViewResultListener();
    client1.setResultListener(client1ResultListener);

    assertEquals(0, client1ResultListener.getQueueSize());

    client1.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));

    // Wait for first computation cycle
    client1ResultListener.assertViewDefinitionCompiled(TIMEOUT);
    client1ResultListener.expectNextCall(CycleStartedCall.class, TIMEOUT);
    client1ResultListener.assertCycleFragmentCompleted(TIMEOUT);
    client1ResultListener.assertCycleCompleted(TIMEOUT);

    final ViewClient client2 = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client2.setFragmentResultMode(ViewResultMode.FULL_ONLY);
    final TestViewResultListener client2ResultListener = new TestViewResultListener();
    client2.setResultListener(client2ResultListener);

    assertEquals(0, client2ResultListener.getQueueSize());
    client2.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));

    // Initial result should be pushed through
    client2ResultListener.assertViewDefinitionCompiled(TIMEOUT);
    client2ResultListener.assertCycleCompleted(TIMEOUT);

    final ViewProcessImpl viewProcess1 = env.getViewProcess(vp, client1.getUniqueId());
    final ViewProcessImpl viewProcess2 = env.getViewProcess(vp, client2.getUniqueId());
    assertEquals(viewProcess1, viewProcess2);

    client1.pause();
    client1ResultListener.assertNoCalls(TIMEOUT);

    // Now client 1 is paused, so any changes should be batched.
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 1);
    env.getCurrentComputationJob(viewProcess1).requestCycle();
    client2ResultListener.assertCycleStarted(TIMEOUT);
    client2ResultListener.assertCycleFragmentCompleted(TIMEOUT);
    client2ResultListener.assertCycleCompleted(TIMEOUT);
    assertEquals(0, client2ResultListener.getQueueSize());
    client1ResultListener.assertNoCalls(TIMEOUT);

    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 2);
    env.getCurrentComputationJob(viewProcess1).requestCycle();
    client2ResultListener.assertCycleStarted(TIMEOUT);
    client2ResultListener.assertCycleFragmentCompleted(TIMEOUT);
    client2ResultListener.assertCycleCompleted(TIMEOUT);
    assertEquals(0, client2ResultListener.getQueueSize());
    client1ResultListener.assertNoCalls(TIMEOUT);

    // Resuming should release the most recent result to the client
    client1.resume();
    client1ResultListener.getCycleStarted(TIMEOUT).getCycleMetadata();
    final ViewComputationResultModel result2Fragment = client1ResultListener.getCycleFragmentCompleted(TIMEOUT).getFullFragment();
    final ViewComputationResultModel result2 = client1ResultListener.getCycleCompleted(TIMEOUT).getFullResult();
    Map<ValueRequirement, Object> expected = new HashMap<ValueRequirement, Object>();
    expected.put(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 2);
    expected.put(ViewProcessorTestEnvironment.getPrimitive2(), (byte) 0);
    assertComputationResult(expected, env.getCalculationResult(result2Fragment));
    assertComputationResult(expected, env.getCalculationResult(result2));

    // Changes should now propagate straight away to both listeners
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 3);
    env.getCurrentComputationJob(viewProcess1).requestCycle();
    client1ResultListener.assertCycleStarted(TIMEOUT);
    client2ResultListener.assertCycleStarted(TIMEOUT);
    client2ResultListener.assertCycleFragmentCompleted(TIMEOUT);
    client2ResultListener.assertCycleCompleted(TIMEOUT);
    final ViewComputationResultModel result3Fragment = client1ResultListener.getCycleFragmentCompleted(TIMEOUT).getFullFragment();
    final ViewComputationResultModel result3 = client1ResultListener.getCycleCompleted(TIMEOUT).getFullResult();
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
    env.getCurrentComputationJob(viewProcess1).requestCycle();
    client2ResultListener.assertCycleStarted(TIMEOUT);
    client2ResultListener.assertCycleFragmentCompleted(TIMEOUT);
    client2ResultListener.assertCycleCompleted(TIMEOUT);
    assertEquals(0, client2ResultListener.getQueueSize());
    client1ResultListener.assertNoCalls(TIMEOUT);

    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), (byte) 2);
    env.getCurrentComputationJob(viewProcess1).requestCycle();
    client2ResultListener.assertCycleStarted(TIMEOUT);
    client2ResultListener.assertCycleFragmentCompleted(TIMEOUT);
    client2ResultListener.assertCycleCompleted(TIMEOUT);
    assertEquals(0, client2ResultListener.getQueueSize());
    client1ResultListener.assertNoCalls(TIMEOUT);

    // Start results again
    client1.resume();
    client1ResultListener.assertCycleStarted(TIMEOUT);
    final ViewComputationResultModel result4Fragment = client1ResultListener.getCycleFragmentCompleted(TIMEOUT).getFullFragment();
    final ViewComputationResultModel result4 = client1ResultListener.getCycleCompleted(TIMEOUT).getFullResult();
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
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();

    final ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();

    final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));

    final ViewProcess viewProcess = env.getViewProcess(vp, client.getUniqueId());

    client.shutdown();

    assertEquals(ViewProcessState.TERMINATED, viewProcess.getState());

    client.pause();
  }

  @Test
  public void testChangeOfListeners() throws InterruptedException {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    final SynchronousInMemoryLKVSnapshotProvider marketDataProvider = new SynchronousInMemoryLKVSnapshotProvider();
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), 0);
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), 0);
    env.setMarketDataProvider(marketDataProvider);
    env.init();

    final ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();

    final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    client.setFragmentResultMode(ViewResultMode.FULL_ONLY);
    final TestViewResultListener resultListener1 = new TestViewResultListener();
    client.setResultListener(resultListener1);

    // Start live computation and collect the initial result
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), 2);

    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    final ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    assertEquals(ViewProcessState.RUNNING, viewProcess.getState());

    final ViewComputationJob recalcJob = env.getCurrentComputationJob(viewProcess);
    resultListener1.assertViewDefinitionCompiled(TIMEOUT);
    resultListener1.assertCycleStarted(TIMEOUT);
    resultListener1.assertCycleFragmentCompleted(TIMEOUT);
    resultListener1.assertCycleCompleted(TIMEOUT);
    assertEquals(0, resultListener1.getQueueSize());

    // Push through a second result
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), 3);
    recalcJob.requestCycle();
    resultListener1.assertCycleStarted(TIMEOUT);
    resultListener1.assertCycleFragmentCompleted(TIMEOUT);
    resultListener1.assertCycleCompleted(TIMEOUT);
    assertEquals(0, resultListener1.getQueueSize());

    // Change listener
    final TestViewResultListener resultListener2 = new TestViewResultListener();
    client.setResultListener(resultListener2);

    // Push through a result which should arrive at the new listeners
    recalcJob.requestCycle();
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
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    final SynchronousInMemoryLKVSnapshotProvider marketDataProvider = new SynchronousInMemoryLKVSnapshotProvider();
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), 0);
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), 0);
    env.setMarketDataProvider(marketDataProvider);
    env.init();

    final ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();

    final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);

    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    final ViewProcessImpl viewProcess1 = env.getViewProcess(vp, client.getUniqueId());

    final ViewComputationJob recalcJob1 = env.getCurrentComputationJob(viewProcess1);
    final Thread recalcThread1 = env.getCurrentComputationThread(viewProcess1);
    assertFalse(recalcJob1.isTerminated());
    assertTrue(recalcThread1.isAlive());

    client.detachFromViewProcess();
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    final ViewProcessImpl viewProcess2 = env.getViewProcess(vp, client.getUniqueId());
    final ViewComputationJob recalcJob2 = env.getCurrentComputationJob(viewProcess2);
    final Thread recalcThread2 = env.getCurrentComputationThread(viewProcess2);

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
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    final SynchronousInMemoryLKVSnapshotProvider marketDataProvider = new SynchronousInMemoryLKVSnapshotProvider();
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), 0);
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), 0);
    env.setMarketDataProvider(marketDataProvider);
    final InMemoryFunctionRepository functionRepository = new InMemoryFunctionRepository();

    final ComputationTarget target = new ComputationTarget(ComputationTargetType.CURRENCY, Currency.USD);
    final MockFunction fn1 = new MockFunction(MockFunction.UNIQUE_ID + "1", target) {

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
        LogBridge.getInstance().log(new SimpleLogEvent(LogLevel.WARN, "Warning during execution"));
        LogBridge.getInstance().log(new SimpleLogEvent(LogLevel.ERROR, "Error during execution"));
        return super.execute(executionContext, inputs, target, desiredValues);
      }

    };
    final ValueRequirement requirement1 = new ValueRequirement("value1", target.toSpecification());
    fn1.addResult(new ValueSpecification(requirement1.getValueName(), target.toSpecification(), ValueProperties.with(ValuePropertyNames.FUNCTION, "fn1").get()), "result1");
    functionRepository.addFunction(fn1);

    final MockFunction fn2 = new MockFunction(MockFunction.UNIQUE_ID + "2", target) {

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
        LogBridge.getInstance().log(new SimpleLogEvent(LogLevel.WARN, "Warning during execution"));
        return super.execute(executionContext, inputs, target, desiredValues);
      }

    };
    fn2.addRequirement(requirement1);
    final ValueRequirement requirement2 = new ValueRequirement("value2", target.toSpecification());
    fn2.addResult(new ValueSpecification(requirement2.getValueName(), target.toSpecification(), ValueProperties.with(ValuePropertyNames.FUNCTION, "fn2").get()), "result2");
    functionRepository.addFunction(fn2);

    env.setFunctionRepository(functionRepository);

    final ViewDefinition vd = new ViewDefinition(UniqueId.of("test", "vd1"), "Test view", UserPrincipal.getLocalUser());
    final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(vd, "Default");
    calcConfig.addSpecificRequirement(requirement2);
    vd.addViewCalculationConfiguration(calcConfig);
    vd.setMinFullCalculationPeriod(Long.MAX_VALUE);  // Never force a full calculation
    vd.setMaxFullCalculationPeriod(Long.MAX_VALUE);  // Never force a full calculation
    env.setViewDefinition(vd);

    env.init();

    final ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();

    final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    final TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);

    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));

    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    final ViewComputationResultModel result1 = resultListener.getCycleCompleted(TIMEOUT).getFullResult();
    assertEquals(0, resultListener.getQueueSize());

    assertEquals(1, result1.getAllResults().size());
    final ComputedValueResult result1Value = Iterables.getOnlyElement(result1.getAllResults()).getComputedValue();
    assertEquals("result2", result1Value.getValue());

    final AggregatedExecutionLog log1 = result1Value.getAggregatedExecutionLog();
    assertNotNull(log1);
    assertTrue(log1.getLogLevels().contains(LogLevel.ERROR));
    assertTrue(log1.getLogLevels().contains(LogLevel.WARN));
    assertFalse(log1.getLogLevels().contains(LogLevel.INFO));
    assertNull(log1.getLogs());

    final Pair<String, ValueSpecification> resultSpec = Pair.of(calcConfig.getName(), Iterables.getOnlyElement(client.getLatestCompiledViewDefinition().getTerminalValuesRequirements().keySet()));
    client.setMinimumLogMode(ExecutionLogMode.FULL, ImmutableSet.of(resultSpec));

    final ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    final ViewComputationJob recalcJob = env.getCurrentComputationJob(viewProcess);
    recalcJob.triggerCycle();

    final ViewComputationResultModel result2 = resultListener.getCycleCompleted(TIMEOUT).getFullResult();
    assertEquals(0, resultListener.getQueueSize());

    assertEquals(1, result2.getAllResults().size());
    final ComputedValueResult result2Value = Iterables.getOnlyElement(result2.getAllResults()).getComputedValue();
    assertEquals("result2", result2Value.getValue());

    final AggregatedExecutionLog log2 = result2Value.getAggregatedExecutionLog();
    assertNotNull(log2);
    assertTrue(log2.getLogLevels().contains(LogLevel.ERROR));
    assertTrue(log2.getLogLevels().contains(LogLevel.WARN));
    assertFalse(log2.getLogLevels().contains(LogLevel.INFO));
    assertNotNull(log2.getLogs());
    assertEquals(2, log2.getLogs().size());

    final ExecutionLogWithContext result2LogContext = log2.getLogs().get(0);
    assertNotNull(result2LogContext);
    assertEquals(fn2.getFunctionDefinition().getShortName(), result2LogContext.getFunctionName());
    assertEquals(resultSpec.getSecond().getTargetSpecification(), result2LogContext.getTargetSpecification());
    final ExecutionLog result2Log = result2LogContext.getExecutionLog();
    assertEquals(1, result2Log.getEvents().size());
    final LogEvent result2Event1 = result2Log.getEvents().get(0);
    assertEquals(LogLevel.WARN, result2Event1.getLevel());
    assertEquals("Warning during execution", result2Event1.getMessage());
    assertNull(result2Log.getExceptionClass());
    assertNull(result2Log.getExceptionMessage());
    assertNull(result2Log.getExceptionStackTrace());

    final ExecutionLogWithContext result1LogContext = log2.getLogs().get(1);
    assertNotNull(result1LogContext);
    assertEquals(fn1.getFunctionDefinition().getShortName(), result1LogContext.getFunctionName());
    assertEquals(resultSpec.getSecond().getTargetSpecification(), result1LogContext.getTargetSpecification());
    final ExecutionLog result1Log = result1LogContext.getExecutionLog();
    assertEquals(2, result1Log.getEvents().size());
    final LogEvent result1Event1 = result1Log.getEvents().get(0);
    assertEquals(LogLevel.WARN, result1Event1.getLevel());
    assertEquals("Warning during execution", result1Event1.getMessage());
    final LogEvent result1Event2 = result1Log.getEvents().get(1);
    assertEquals(LogLevel.ERROR, result1Event2.getLevel());
    assertEquals("Error during execution", result1Event2.getMessage());
    assertNull(result1Log.getExceptionClass());
    assertNull(result1Log.getExceptionMessage());
    assertNull(result1Log.getExceptionStackTrace());

    client.setMinimumLogMode(ExecutionLogMode.INDICATORS, ImmutableSet.of(resultSpec));
    recalcJob.triggerCycle();

    final ViewComputationResultModel result3 = resultListener.getCycleCompleted(TIMEOUT).getFullResult();
    assertEquals(0, resultListener.getQueueSize());

    assertEquals(1, result3.getAllResults().size());
    final ComputedValueResult result3Value = Iterables.getOnlyElement(result3.getAllResults()).getComputedValue();
    assertEquals("result2", result3Value.getValue());

    final AggregatedExecutionLog log3 = result3Value.getAggregatedExecutionLog();
    assertNotNull(log3);
    // Delta cycle - should reuse the previous result which *does* include logs.
    assertNotNull(log3.getLogs());

    // Force a full cycle - should *not* reuse any previous result, so back to indicators only
    recalcJob.dirtyViewDefinition();
    recalcJob.triggerCycle();

    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    final ViewComputationResultModel result4 = resultListener.getCycleCompleted(TIMEOUT).getFullResult();
    assertEquals(0, resultListener.getQueueSize());

    assertEquals(1, result4.getAllResults().size());
    final ComputedValueResult result4Value = Iterables.getOnlyElement(result4.getAllResults()).getComputedValue();
    assertEquals("result2", result4Value.getValue());

    final AggregatedExecutionLog log4 = result4Value.getAggregatedExecutionLog();
    assertNotNull(log4);
    assertNull(log4.getLogs());
  }

  //-------------------------------------------------------------------------
  private void assertComputationResult(final Map<ValueRequirement, Object> expected, final ViewCalculationResultModel result) {
    assertNotNull(result);
    final Set<ValueRequirement> remaining = new HashSet<ValueRequirement>(expected.keySet());
    final Collection<ComputationTargetSpecification> targets = result.getAllTargets();
    for (final ComputationTargetSpecification target : targets) {
      final Map<Pair<String, ValueProperties>, ComputedValueResult> values = result.getValues(target);
      for (final Map.Entry<Pair<String, ValueProperties>, ComputedValueResult> value : values.entrySet()) {
        final String valueName = value.getKey().getFirst();
        final ValueRequirement requirement = new ValueRequirement(valueName, target.getType(), target.getUniqueId());
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
    public void subscribe(final ValueRequirement valueRequirement) {
      subscribe(Collections.singleton(valueRequirement));
    }

    @Override
    public void subscribe(final Set<ValueRequirement> valueRequirements) {
      // No actual subscription to make, but we still need to acknowledge it.
      subscriptionSucceeded(valueRequirements);
    }

    @Override
    public void unsubscribe(final ValueRequirement valueRequirement) {
    }

    @Override
    public void unsubscribe(final Set<ValueRequirement> valueRequirements) {
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
    public boolean isCompatible(final MarketDataSpecification marketDataSpec) {
      return true;
    }

    @Override
    public MarketDataSnapshot snapshot(final MarketDataSpecification marketDataSpec) {
      synchronized (_lastKnownValues) {
        final Map<ValueRequirement, ComputedValue> snapshotValues = new HashMap<ValueRequirement, ComputedValue>(_lastKnownValues);
        return new SynchronousInMemoryLKVSnapshot(snapshotValues);
      }
    }

    //-----------------------------------------------------------------------
    @Override
    public void addValue(final ValueRequirement requirement, final Object value) {
      s_logger.debug("Setting {} = {}", requirement, value);
      synchronized (_lastKnownValues) {
        _lastKnownValues.put(requirement, new ComputedValue(MarketDataUtils.createMarketDataValue(requirement, MarketDataUtils.DEFAULT_EXTERNAL_ID), value));
      }
      // Don't notify listeners of the change - we'll kick off a computation cycle manually in the tests
    }

    @Override
    public void addValue(final ExternalId identifier, final String valueName, final Object value) {
    }

    @Override
    public void removeValue(final ValueRequirement valueRequirement) {
      synchronized(_lastKnownValues) {
        _lastKnownValues.remove(valueRequirement);
      }
      // Don't notify listeners of the change - we'll kick off a computation cycle manually in the tests
    }

    @Override
    public void removeValue(final ExternalId identifier, final String valueName) {
    }

    //-----------------------------------------------------------------------
    @Override
    public ValueSpecification getAvailability(final ValueRequirement requirement) {
      synchronized (_lastKnownValues) {
        return _lastKnownValues.containsKey(requirement) ? MarketDataUtils.createMarketDataValue(requirement, MarketDataUtils.DEFAULT_EXTERNAL_ID) : null;
      }
    }

  }

  private static class SynchronousInMemoryLKVSnapshot extends AbstractMarketDataSnapshot {

    private final Map<ValueRequirement, ComputedValue> _snapshot;
    private final Instant _snapshotTime = Instant.now();

    public SynchronousInMemoryLKVSnapshot(final Map<ValueRequirement, ComputedValue> snapshot) {
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
    public ComputedValue query(final ValueRequirement requirement) {
      return _snapshot.get(requirement);
    }

  }

}
