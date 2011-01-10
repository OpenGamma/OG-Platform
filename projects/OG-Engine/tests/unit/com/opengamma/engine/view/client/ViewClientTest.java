/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.livedata.AbstractLiveDataSnapshotProvider;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.LiveDataInjector;
import com.opengamma.engine.test.TestComputationResultListener;
import com.opengamma.engine.test.TestDeltaResultListener;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewImpl;
import com.opengamma.engine.view.ViewProcessorImpl;
import com.opengamma.engine.view.ViewProcessorTestEnvironment;
import com.opengamma.engine.view.calc.ViewRecalculationJob;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.test.Timeout;


/**
 * Tests ViewClient
 */
public class ViewClientTest {
  
  private static final long TIMEOUT = Timeout.standardTimeoutMillis();
  
  @Test
  public void testSingleViewMultipleClients() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewImpl view = (ViewImpl) vp.getView(env.getViewDefinition().getName(), ViewProcessorTestEnvironment.TEST_USER);
    view.start();
    view.init();
    
    ViewClient client1 = view.createClient(ViewProcessorTestEnvironment.TEST_USER);
    
    assertEquals(view, client1.getView());
    assertNotNull(client1.getUniqueId());
    
    client1.startLive();
    assertTrue(view.isLiveComputationRunning());
    
    client1.stopLive();
    assertFalse(view.isLiveComputationRunning());
    
    ViewClient client2 = view.createClient(ViewProcessorTestEnvironment.TEST_USER);
    client2.startLive();
    assertTrue(view.isLiveComputationRunning());
    
    client1.startLive();
    assertTrue(view.isLiveComputationRunning());
    
    client1.stopLive();
    client2.stopLive();
    assertFalse(view.isLiveComputationRunning());
    
    client1.startLive();
    assertTrue(view.isLiveComputationRunning());
    client2.startLive();
    assertTrue(view.isLiveComputationRunning());
    
    client1.shutdown();
    assertTrue(view.isLiveComputationRunning());
    client2.shutdown();
    assertFalse(view.isLiveComputationRunning());
  }
  
  @Test
  public void testCascadingShutdown() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewImpl view = (ViewImpl) vp.getView(env.getViewDefinition().getName(), ViewProcessorTestEnvironment.TEST_USER);
    view.start();
    view.init();
    
    ViewClient client1 = view.createClient(ViewProcessorTestEnvironment.TEST_USER);
    client1.startLive();
    ViewClient client2 = view.createClient(ViewProcessorTestEnvironment.TEST_USER);
    client2.startLive();
    
    vp.stop();
    
    assertFalse(vp.isRunning());
    assertFalse(view.isRunning());
    assertFalse(view.isLiveComputationRunning());
    assertEquals(ViewClientState.TERMINATED, client1.getState());
    assertEquals(ViewClientState.TERMINATED, client2.getState());
  }
  
  @Test
  public void testComputationResultsFlow() throws InterruptedException {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    SynchronousInMemoryLKVSnapshotProvider snapshotProvider = new SynchronousInMemoryLKVSnapshotProvider();
    snapshotProvider.addValue(env.getPrimitive1(), 0);
    snapshotProvider.addValue(env.getPrimitive2(), 0);
    env.setUserProviders(snapshotProvider, snapshotProvider);
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewImpl view = (ViewImpl) vp.getView(env.getViewDefinition().getName(), ViewProcessorTestEnvironment.TEST_USER);
    view.start();
    view.init();
    
    ViewClient client = view.createClient(ViewProcessorTestEnvironment.TEST_USER);
    assertFalse(client.isResultAvailable());
    assertNull(client.getLatestResult());
    
    TestComputationResultListener resultListener = new TestComputationResultListener();
    client.setResultListener(resultListener);
    
    // Client not started - should not have been listening to anything that might have been going on
    assertEquals(0, resultListener.getQueueSize());
    
    snapshotProvider.addValue(env.getPrimitive1(), 1);
    snapshotProvider.addValue(env.getPrimitive2(), 2);
    
    assertEquals(0, resultListener.getQueueSize());
    client.startLive();  // Performs an initial cycle
    
    ViewComputationResultModel result1 = resultListener.getResult(TIMEOUT);
    Map<ValueRequirement, Object> expected = new HashMap<ValueRequirement, Object>();
    expected.put(env.getPrimitive1(), (byte) 1);
    expected.put(env.getPrimitive2(), (byte) 2);
    assertComputationResult(expected, env.getCalculationResult(result1));
    assertTrue(client.isResultAvailable());
    assertEquals(result1, client.getLatestResult());
    
    client.pauseLive();
    
    snapshotProvider.addValue(env.getPrimitive1(), 3);
    snapshotProvider.addValue(env.getPrimitive2(), 4);
    
    env.getCurrentRecalcJob(view).liveDataChanged();  // Need to get it to perform another cycle
    
    // Should have been merging results received in the meantime
    client.startLive();
    ViewComputationResultModel result2 = resultListener.getResult(TIMEOUT);
    expected = new HashMap<ValueRequirement, Object>();
    expected.put(env.getPrimitive1(), (byte) 3);
    expected.put(env.getPrimitive2(), (byte) 4);
    assertComputationResult(expected, env.getCalculationResult(result2));    
  }

  @Test
  public void testSubscriptionToDeltaResults() throws InterruptedException {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    SynchronousInMemoryLKVSnapshotProvider snapshotProvider = new SynchronousInMemoryLKVSnapshotProvider();
    snapshotProvider.addValue(env.getPrimitive1(), 0);
    snapshotProvider.addValue(env.getPrimitive2(), 0);
    env.setUserProviders(snapshotProvider, snapshotProvider);
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewImpl view = (ViewImpl) vp.getView(env.getViewDefinition().getName(), ViewProcessorTestEnvironment.TEST_USER);
    view.start();
    view.init();
    
    // Push in an empty result so that we start with something to generate deltas against
    view.recalculationPerformed(new InMemoryViewComputationResultModel());
    
    ViewClient client = view.createClient(ViewProcessorTestEnvironment.TEST_USER);
    
    TestDeltaResultListener deltaListener = new TestDeltaResultListener();
    client.setDeltaResultListener(deltaListener);
    
    // Client not started - should not have been listening to anything that might have been going on
    assertEquals(0, deltaListener.getQueueSize());
    
    snapshotProvider.addValue(env.getPrimitive1(), 1);
    snapshotProvider.addValue(env.getPrimitive2(), 2);
    
    assertEquals(0, deltaListener.getQueueSize());
    client.startLive();  // Performs an initial cycle
    
    ViewDeltaResultModel result1 = deltaListener.getResult(TIMEOUT);
    Map<ValueRequirement, Object> expected = new HashMap<ValueRequirement, Object>();
    expected.put(env.getPrimitive1(), (byte) 1);
    expected.put(env.getPrimitive2(), (byte) 2);
    assertComputationResult(expected, env.getCalculationResult(result1));
    
    client.pauseLive();
    
    // Just update one live data value, and only this one value should end up in the delta
    snapshotProvider.addValue(env.getPrimitive1(), 3);
    
    assertEquals(0, deltaListener.getQueueSize());
    env.getCurrentRecalcJob(view).liveDataChanged();  // Need to get it to perform another cycle
    
    // Should have been merging results received in the meantime
    client.startLive();
    ViewDeltaResultModel result2 = deltaListener.getResult(TIMEOUT);
    
    expected = new HashMap<ValueRequirement, Object>();
    expected.put(env.getPrimitive1(), (byte) 3);
    assertComputationResult(expected, env.getCalculationResult(result2));    
  }
  
  @Test
  public void testStates() throws InterruptedException {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    SynchronousInMemoryLKVSnapshotProvider snapshotProvider = new SynchronousInMemoryLKVSnapshotProvider();
    snapshotProvider.addValue(env.getPrimitive1(), 0);
    snapshotProvider.addValue(env.getPrimitive2(), 0);
    env.setUserProviders(snapshotProvider, snapshotProvider);
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewImpl view = (ViewImpl) vp.getView(env.getViewDefinition().getName(), ViewProcessorTestEnvironment.TEST_USER);
    view.start();
    view.init();
    
    ViewClient client = view.createClient(ViewProcessorTestEnvironment.TEST_USER);
    
    TestComputationResultListener clientResultListener = new TestComputationResultListener();
    client.setResultListener(clientResultListener);
    
    // Cheekily hook in to all view results. This will allow us to test that the client is in fact holding back
    // results.
    TestComputationResultListener viewResultListener = new TestComputationResultListener();
    view.addResultListener(viewResultListener);
    
    assertEquals(0, viewResultListener.getQueueSize());
    
    // Start off paused. This should kick off an initial live computation cycle, which we'll consume
    client.pauseLive();
    viewResultListener.getResult(TIMEOUT);
    clientResultListener.assertNoResult(TIMEOUT);
    
    // Now we're paused, so any changes should be batched.
    snapshotProvider.addValue(env.getPrimitive1(), 1);
    env.getCurrentRecalcJob(view).liveDataChanged();
    viewResultListener.getResult(TIMEOUT);
    assertEquals(0, viewResultListener.getQueueSize());
    clientResultListener.assertNoResult(TIMEOUT);
    
    snapshotProvider.addValue(env.getPrimitive1(), 2);
    env.getCurrentRecalcJob(view).liveDataChanged();
    viewResultListener.getResult(TIMEOUT);
    assertEquals(0, viewResultListener.getQueueSize());
    clientResultListener.assertNoResult(TIMEOUT);
    
    // Resuming should release the most recent result to the client
    client.startLive();
    viewResultListener.assertNoResult(TIMEOUT);
    ViewComputationResultModel result2 = clientResultListener.getResult(TIMEOUT);
    assertEquals(0, clientResultListener.getQueueSize());
    Map<ValueRequirement, Object> expected = new HashMap<ValueRequirement, Object>();
    expected.put(env.getPrimitive1(), (byte) 2);
    expected.put(env.getPrimitive2(), (byte) 0);
    assertComputationResult(expected, env.getCalculationResult(result2));
    
    // Changes should now propagate straight away to both listeners
    snapshotProvider.addValue(env.getPrimitive1(), 3);
    env.getCurrentRecalcJob(view).liveDataChanged();
    viewResultListener.getResult(TIMEOUT);
    ViewComputationResultModel result3 = clientResultListener.getResult(TIMEOUT);
    expected = new HashMap<ValueRequirement, Object>();
    expected.put(env.getPrimitive1(), (byte) 3);
    expected.put(env.getPrimitive2(), (byte) 0);
    assertComputationResult(expected, env.getCalculationResult(result3));

    // Pause results again and we should be back to merging
    client.pauseLive();
    viewResultListener.assertNoResult(TIMEOUT);
    clientResultListener.assertNoResult(TIMEOUT);

    snapshotProvider.addValue(env.getPrimitive2(), 1);
    env.getCurrentRecalcJob(view).liveDataChanged();
    viewResultListener.getResult(TIMEOUT);
    assertEquals(0, viewResultListener.getQueueSize());
    clientResultListener.assertNoResult(TIMEOUT);

    snapshotProvider.addValue(env.getPrimitive2(), 2);
    env.getCurrentRecalcJob(view).liveDataChanged();
    viewResultListener.getResult(TIMEOUT);
    assertEquals(0, viewResultListener.getQueueSize());
    clientResultListener.assertNoResult(TIMEOUT);
    
    // Start results again
    client.startLive();
    ViewComputationResultModel result4 = clientResultListener.getResult(TIMEOUT);
    assertEquals(0, clientResultListener.getQueueSize());
    viewResultListener.assertNoResult(TIMEOUT);
    expected = new HashMap<ValueRequirement, Object>();
    expected.put(env.getPrimitive1(), (byte) 3);
    expected.put(env.getPrimitive2(), (byte) 2);
    assertComputationResult(expected, env.getCalculationResult(result4));
    
    client.stopLive();
    viewResultListener.assertNoResult(TIMEOUT);
    clientResultListener.assertNoResult(TIMEOUT);
    
    client.shutdown();
  }

  @Test(expected = IllegalStateException.class)
  public void testUseTerminatedClient() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewImpl view = (ViewImpl) vp.getView(env.getViewDefinition().getName(), ViewProcessorTestEnvironment.TEST_USER);
    view.start();
    view.init();
    
    ViewClient client = view.createClient(ViewProcessorTestEnvironment.TEST_USER);
    
    client.shutdown();
    
    client.startLive();
  }

  @Test
  public void testChangeOfListeners() throws InterruptedException {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    SynchronousInMemoryLKVSnapshotProvider snapshotProvider = new SynchronousInMemoryLKVSnapshotProvider();
    snapshotProvider.addValue(env.getPrimitive1(), 0);
    snapshotProvider.addValue(env.getPrimitive2(), 0);
    env.setUserProviders(snapshotProvider, snapshotProvider);
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewImpl view = (ViewImpl) vp.getView(env.getViewDefinition().getName(), ViewProcessorTestEnvironment.TEST_USER);
    view.start();
    view.init();
    
    ViewClient client = view.createClient(ViewProcessorTestEnvironment.TEST_USER);
    assertFalse(view.isLiveComputationRunning());
    
    TestDeltaResultListener deltaListener1 = new TestDeltaResultListener();
    client.setDeltaResultListener(deltaListener1);
    TestComputationResultListener computationListener1 = new TestComputationResultListener();
    client.setResultListener(computationListener1);
    
    // Start live computation and collect the initial result
    snapshotProvider.addValue(env.getPrimitive1(), 2);
    client.startLive();
    ViewRecalculationJob recalcJob = env.getCurrentRecalcJob(view);
    assertTrue(view.isRunning());
    computationListener1.getResult(TIMEOUT);
    deltaListener1.getResult(TIMEOUT);
    assertEquals(0, computationListener1.getQueueSize());
    assertEquals(0, deltaListener1.getQueueSize());
    
    // Push through a second result
    snapshotProvider.addValue(env.getPrimitive1(), 3);
    recalcJob.liveDataChanged();
    computationListener1.getResult(TIMEOUT);
    deltaListener1.getResult(TIMEOUT);
    assertEquals(0, computationListener1.getQueueSize());
    assertEquals(0, deltaListener1.getQueueSize());

    // Change both listeners
    TestDeltaResultListener deltaListener2 = new TestDeltaResultListener();
    client.setDeltaResultListener(deltaListener2);
    TestComputationResultListener computationListener2 = new TestComputationResultListener();
    client.setResultListener(computationListener2);
    assertTrue(view.isLiveComputationRunning());

    // Push through a result which should arrive at the new listeners
    recalcJob.liveDataChanged();
    computationListener2.getResult(TIMEOUT);
    deltaListener2.getResult(TIMEOUT);
    assertEquals(0, computationListener1.getQueueSize());
    assertEquals(0, computationListener2.getQueueSize());
    assertEquals(0, deltaListener1.getQueueSize());
    assertEquals(0, deltaListener2.getQueueSize());

    client.setResultListener(null);
    client.setDeltaResultListener(null);
    client.stopLive();
    assertFalse(view.isLiveComputationRunning());
    
    vp.stop();
  }
  
  @Test
  public void testOldRecalculationThreadDies() throws InterruptedException {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    SynchronousInMemoryLKVSnapshotProvider snapshotProvider = new SynchronousInMemoryLKVSnapshotProvider();
    snapshotProvider.addValue(env.getPrimitive1(), 0);
    snapshotProvider.addValue(env.getPrimitive2(), 0);
    env.setUserProviders(snapshotProvider, snapshotProvider);
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewImpl view = (ViewImpl) vp.getView(env.getViewDefinition().getName(), ViewProcessorTestEnvironment.TEST_USER);
    view.start();
    view.init();
    
    ViewClient client = view.createClient(ViewProcessorTestEnvironment.TEST_USER);
    
    client.startLive();
    
    ViewRecalculationJob recalcJob1 = env.getCurrentRecalcJob(view);
    Thread recalcThread1 = env.getCurrentRecalcThread(view);
    assertFalse(recalcJob1.isTerminated());
    assertTrue(recalcThread1.isAlive());
    
    client.stopLive();
    client.startLive();
    
    ViewRecalculationJob recalcJob2 = env.getCurrentRecalcJob(view);
    Thread recalcThread2 = env.getCurrentRecalcThread(view);
    
    assertTrue(recalcJob1.isTerminated());
    assertFalse(recalcJob2.isTerminated());

    recalcThread1.join(5000);
    assertFalse(recalcThread1.isAlive());
    assertTrue(recalcThread2.isAlive());
    
    vp.stop();
  }
 
  private void assertComputationResult(Map<ValueRequirement, Object> expected, ViewCalculationResultModel result) {
    Set<ValueRequirement> remaining = new HashSet<ValueRequirement>(expected.keySet());
    Collection<ComputationTargetSpecification> targets = result.getAllTargets();
    for (ComputationTargetSpecification target : targets) {
      Map<String, ComputedValue> values = result.getValues(target);
      for (Map.Entry<String, ComputedValue> value : values.entrySet()) {
        ValueRequirement requirement = new ValueRequirement(value.getKey(), target.getType(), target.getUniqueId());
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
  private static class SynchronousInMemoryLKVSnapshotProvider extends AbstractLiveDataSnapshotProvider implements LiveDataInjector, 
      LiveDataAvailabilityProvider {
    
    private static final Logger s_logger = LoggerFactory.getLogger(SynchronousInMemoryLKVSnapshotProvider.class);
    
    private final Map<ValueRequirement, Object> _lastKnownValues = new HashMap<ValueRequirement, Object>();
    private final Map<Long, Map<ValueRequirement, Object>> _snapshots = new ConcurrentHashMap<Long, Map<ValueRequirement, Object>>();

    @Override
    public void addSubscription(UserPrincipal user, ValueRequirement valueRequirement) {
      addSubscription(user, Collections.singleton(valueRequirement));
    }

    @Override
    public void addSubscription(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
      // No actual subscription to make, but we still need to acknowledge it.
      subscriptionSucceeded(valueRequirements);
    }

    @Override
    public Object querySnapshot(long snapshot, ValueRequirement requirement) {
      Map<ValueRequirement, Object> snapshotValues;
      snapshotValues = _snapshots.get(snapshot);
      if (snapshotValues == null) {
        return null;
      }
      Object value = snapshotValues.get(requirement);
      return value;
    }

    @Override
    public long snapshot() {
      long snapshotTime = System.currentTimeMillis();
      snapshot(snapshotTime);
      return snapshotTime;
    }

    @Override
    public long snapshot(long snapshotTime) {
      synchronized (_lastKnownValues) {
        Map<ValueRequirement, Object> snapshotValues = new HashMap<ValueRequirement, Object>(_lastKnownValues);
        _snapshots.put(snapshotTime, snapshotValues);
      }
      return snapshotTime;
    }

    @Override
    public void releaseSnapshot(long snapshot) {
      _snapshots.remove(snapshot);
    }

    @Override
    public void addValue(ValueRequirement requirement, Object value) {
      s_logger.debug("Setting {} = {}", requirement, value);
      synchronized (_lastKnownValues) {
        _lastKnownValues.put(requirement, value);
      }
      // Don't notify listeners of the change - we'll kick off a computation cycle manually in the tests
    }

    @Override
    public void removeValue(ValueRequirement valueRequirement) {
      synchronized(_lastKnownValues) {
        _lastKnownValues.remove(valueRequirement);
      }
      // Don't notify listeners of the change - we'll kick off a computation cycle manually in the tests
    }

    @Override
    public boolean isAvailable(ValueRequirement requirement) {
      synchronized (_lastKnownValues) {
        return _lastKnownValues.containsKey(requirement);        
      }
    }

  }
  
}
