/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.AbstractMarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.MarketDataAvailability;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.permission.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.permission.PermissiveMarketDataPermissionProvider;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.test.TestViewResultListener;
import com.opengamma.engine.test.ViewProcessorTestEnvironment;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.ViewProcessImpl;
import com.opengamma.engine.view.ViewProcessState;
import com.opengamma.engine.view.ViewProcessorImpl;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.calc.ViewComputationJob;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.id.ExternalId;
import com.opengamma.livedata.UserPrincipal;
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
    resultListener.assertCycleFragmentCompleted(TIMEOUT);
    ViewComputationResultModel result2 = resultListener.getCycleCompleted(TIMEOUT).getFullResult();

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
    resultListener.assertCycleFragmentCompleted(TIMEOUT);
    ViewDeltaResultModel result1 = resultListener.getCycleCompleted(TIMEOUT).getDeltaResult();
    assertNotNull(result1);

    Map<ValueRequirement, Object> expected = new HashMap<ValueRequirement, Object>();
    expected.put(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 1);
    expected.put(ViewProcessorTestEnvironment.getPrimitive2(), (byte) 2);
    assertComputationResult(expected, env.getCalculationResult(result1));
    
    client.pause();
    
    // Just update one live data value, and only this one value should end up in the delta
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), 3);
    
    assertEquals(0, resultListener.getQueueSize());
    ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    env.getCurrentComputationJob(viewProcess).marketDataChanged();  // Need to get it to perform another cycle
    
    // Should have been merging results received in the meantime
    client.resume();
    resultListener.assertCycleFragmentCompleted(TIMEOUT);
    ViewDeltaResultModel result2 = resultListener.getCycleCompleted(TIMEOUT).getDeltaResult();

    
    expected = new HashMap<ValueRequirement, Object>();
    expected.put(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 3);
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
    client2ResultListener.assertCycleFragmentCompleted(TIMEOUT);
    client2ResultListener.assertCycleCompleted(TIMEOUT);
    assertEquals(0, client2ResultListener.getQueueSize());
    client1ResultListener.assertNoCalls(TIMEOUT);
    
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 2);
    env.getCurrentComputationJob(viewProcess1).marketDataChanged();
    client2ResultListener.assertCycleFragmentCompleted(TIMEOUT);
    client2ResultListener.assertCycleCompleted(TIMEOUT);
    assertEquals(0, client2ResultListener.getQueueSize());
    client1ResultListener.assertNoCalls(TIMEOUT);
    
    // Resuming should release the most recent result to the client
    client1.resume();
    assertEquals(0, client2ResultListener.getQueueSize());
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
    client2ResultListener.assertCycleFragmentCompleted(TIMEOUT);
    client2ResultListener.assertCycleCompleted(TIMEOUT);
    assertEquals(0, client2ResultListener.getQueueSize());
    client1ResultListener.assertNoCalls(TIMEOUT);

    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), (byte) 2);
    env.getCurrentComputationJob(viewProcess1).marketDataChanged();
    client2ResultListener.assertCycleFragmentCompleted(TIMEOUT);
    client2ResultListener.assertCycleCompleted(TIMEOUT);
    assertEquals(0, client2ResultListener.getQueueSize());
    client1ResultListener.assertNoCalls(TIMEOUT);
    
    // Start results again
    client1.resume();
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
    client2.shutdown();
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
    resultListener1.assertCycleFragmentCompleted(TIMEOUT);
    resultListener1.assertCycleCompleted(TIMEOUT);
    assertEquals(0, resultListener1.getQueueSize());
    
    // Push through a second result
    marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), 3);
    recalcJob.marketDataChanged();
    resultListener1.assertCycleFragmentCompleted(TIMEOUT);
    resultListener1.assertCycleCompleted(TIMEOUT);
    assertEquals(0, resultListener1.getQueueSize());

    // Change listener
    TestViewResultListener resultListener2 = new TestViewResultListener();
    client.setResultListener(resultListener2);

    // Push through a result which should arrive at the new listeners
    recalcJob.marketDataChanged();
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
 
  private void assertComputationResult(Map<ValueRequirement, Object> expected, ViewCalculationResultModel result) {
    assertNotNull(result);
    Set<ValueRequirement> remaining = new HashSet<ValueRequirement>(expected.keySet());
    Collection<ComputationTargetSpecification> targets = result.getAllTargets();
    for (ComputationTargetSpecification target : targets) {
      Map<Pair<String, ValueProperties>, ComputedValue> values = result.getValues(target);
      for (Map.Entry<Pair<String, ValueProperties>, ComputedValue> value : values.entrySet()) {
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
    
    private final Map<ValueRequirement, Object> _lastKnownValues = new HashMap<ValueRequirement, Object>();
    private final MarketDataPermissionProvider _permissionProvider = new PermissiveMarketDataPermissionProvider();

    @Override
    public void subscribe(UserPrincipal user, ValueRequirement valueRequirement) {
      subscribe(user, Collections.singleton(valueRequirement));
    }

    @Override
    public void subscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
      // No actual subscription to make, but we still need to acknowledge it.
      subscriptionSucceeded(valueRequirements);
    }
    
    @Override
    public void unsubscribe(UserPrincipal user, ValueRequirement valueRequirement) {
    }

    @Override
    public void unsubscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
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
        Map<ValueRequirement, Object> snapshotValues = new HashMap<ValueRequirement, Object>(_lastKnownValues);
        return new SynchronousInMemoryLKVSnapshot(snapshotValues);
      }
    }

    //-----------------------------------------------------------------------
    @Override
    public void addValue(ValueRequirement requirement, Object value) {
      s_logger.debug("Setting {} = {}", requirement, value);
      synchronized (_lastKnownValues) {
        _lastKnownValues.put(requirement, value);
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
    public MarketDataAvailability getAvailability(final ValueRequirement requirement) {
      synchronized (_lastKnownValues) {
        return _lastKnownValues.containsKey(requirement) ? MarketDataAvailability.AVAILABLE : MarketDataAvailability.NOT_AVAILABLE;
      }
    }

  }
  
  private static class SynchronousInMemoryLKVSnapshot implements MarketDataSnapshot {

    private final Map<ValueRequirement, Object> _snapshot;
    private final Instant _snapshotTime = Instant.now();
    
    public SynchronousInMemoryLKVSnapshot(Map<ValueRequirement, Object> snapshot) {
      _snapshot = snapshot;
    }
    
    @Override
    public Instant getSnapshotTimeIndication() {
      return _snapshotTime;
    }

    @Override
    public void init() {
    }
    
    @Override
    public void init(Set<ValueRequirement> valuesRequired, long timeout, TimeUnit unit) {
    }

    @Override
    public Instant getSnapshotTime() {
      return _snapshotTime;
    }
    
    @Override
    public Object query(ValueRequirement requirement) {
      return _snapshot.get(requirement);
    }
    
  }
  
}
