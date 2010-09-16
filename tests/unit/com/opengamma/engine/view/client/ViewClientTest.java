/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
import java.util.List;
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
import com.opengamma.engine.view.ViewComputationResultModelImpl;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewImpl;
import com.opengamma.engine.view.ViewProcessorImpl;
import com.opengamma.engine.view.ViewProcessorTestEnvironment;
import com.opengamma.engine.view.calc.ViewRecalculationJob;
import com.opengamma.livedata.msg.UserPrincipal;


/**
 * Tests ViewClient
 */
public class ViewClientTest {
  
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
    assertNotNull(client1.getUniqueIdentifier());
    
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
    assertEquals(0, resultListener.popResults().size());
    
    snapshotProvider.addValue(env.getPrimitive1(), 1);
    snapshotProvider.addValue(env.getPrimitive2(), 2);
    
    resultListener.setExpectedResultCount(1);
    assertEquals(0, resultListener.popResults().size());
    client.startLive();  // Performs an initial cycle
    
    resultListener.awaitExpectedResults();
    List<ViewComputationResultModel> results = resultListener.popResults();
    assertEquals(1, results.size());
    Map<ValueRequirement, Object> expected = new HashMap<ValueRequirement, Object>();
    expected.put(env.getPrimitive1(), (byte) 1);
    expected.put(env.getPrimitive2(), (byte) 2);
    ViewComputationResultModel result = results.get(0);
    assertComputationResult(expected, env.getCalculationResult(result));
    assertTrue(client.isResultAvailable());
    assertEquals(result, client.getLatestResult());
    
    client.pauseLive();
    
    snapshotProvider.addValue(env.getPrimitive1(), 3);
    snapshotProvider.addValue(env.getPrimitive2(), 4);
    
    resultListener.setExpectedResultCount(1);
    assertEquals(0, resultListener.popResults().size());
    env.getCurrentRecalcJob(view).liveDataChanged();  // Need to get it to perform another cycle
    
    // Should have been merging results received in the meantime
    client.startLive();
    resultListener.awaitExpectedResults();
    
    results = resultListener.popResults();
    assertEquals(1, results.size());
    expected = new HashMap<ValueRequirement, Object>();
    expected.put(env.getPrimitive1(), (byte) 3);
    expected.put(env.getPrimitive2(), (byte) 4);
    assertComputationResult(expected, env.getCalculationResult(results.get(0)));    
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
    view.recalculationPerformed(new ViewComputationResultModelImpl());
    
    ViewClient client = view.createClient(ViewProcessorTestEnvironment.TEST_USER);
    
    TestDeltaResultListener deltaListener = new TestDeltaResultListener();
    client.setDeltaResultListener(deltaListener);
    
    // Client not started - should not have been listening to anything that might have been going on
    assertEquals(0, deltaListener.popResults().size());
    
    snapshotProvider.addValue(env.getPrimitive1(), 1);
    snapshotProvider.addValue(env.getPrimitive2(), 2);
    
    deltaListener.setExpectedResultCount(1);
    assertEquals(0, deltaListener.popResults().size());
    client.startLive();  // Performs an initial cycle
    
    deltaListener.awaitExpectedResults();
    
    List<ViewDeltaResultModel> results = deltaListener.popResults();
    assertEquals(1, results.size());
    Map<ValueRequirement, Object> expected = new HashMap<ValueRequirement, Object>();
    expected.put(env.getPrimitive1(), (byte) 1);
    expected.put(env.getPrimitive2(), (byte) 2);
    assertComputationResult(expected, env.getCalculationResult(results.get(0)));
    
    client.pauseLive();
    
    // Just update one live data value, and only this one value should end up in the delta
    snapshotProvider.addValue(env.getPrimitive1(), 3);
    
    deltaListener.setExpectedResultCount(1);
    assertEquals(0, deltaListener.popResults().size());
    env.getCurrentRecalcJob(view).liveDataChanged();  // Need to get it to perform another cycle
    
    // Should have been merging results received in the meantime
    client.startLive();
    deltaListener.awaitExpectedResults();
    
    results = deltaListener.popResults();
    assertEquals(1, results.size());
    expected = new HashMap<ValueRequirement, Object>();
    expected.put(env.getPrimitive1(), (byte) 3);
    assertComputationResult(expected, env.getCalculationResult(results.get(0)));    
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
    
    // Start off paused. This should kick off an initial live computation cycle, which we'll consume
    viewResultListener.setExpectedResultCount(1);
    client.pauseLive();
    viewResultListener.awaitExpectedResults();
    assertEquals(1, viewResultListener.popResults().size());
    
    // Now we're paused, so any changes should be batched.
    snapshotProvider.addValue(env.getPrimitive1(), 1);
    viewResultListener.setExpectedResultCount(1);
    env.getCurrentRecalcJob(view).liveDataChanged();
    viewResultListener.awaitExpectedResults();
    assertEquals(1, viewResultListener.popResults().size());
    assertEquals(0, clientResultListener.popResults().size());
    
    snapshotProvider.addValue(env.getPrimitive1(), 2);
    viewResultListener.setExpectedResultCount(1);
    env.getCurrentRecalcJob(view).liveDataChanged();
    viewResultListener.awaitExpectedResults();
    assertEquals(1, viewResultListener.popResults().size());
    assertEquals(0, clientResultListener.popResults().size());
    
    // Resuming should release the most recent result to the client
    clientResultListener.setExpectedResultCount(1);
    client.startLive();
    clientResultListener.awaitExpectedResults();
    assertEquals(0, viewResultListener.popResults().size());
    List<ViewComputationResultModel> clientResults = clientResultListener.popResults();
    assertEquals(1, clientResults.size());
    Map<ValueRequirement, Object> expected = new HashMap<ValueRequirement, Object>();
    expected.put(env.getPrimitive1(), (byte) 2);
    expected.put(env.getPrimitive2(), (byte) 0);
    assertComputationResult(expected, env.getCalculationResult(clientResults.get(0)));
    
    // Changes should now propagate straight away to both listeners
    viewResultListener.setExpectedResultCount(1);
    clientResultListener.setExpectedResultCount(1);
    snapshotProvider.addValue(env.getPrimitive1(), 3);
    env.getCurrentRecalcJob(view).liveDataChanged();
    viewResultListener.awaitExpectedResults();
    clientResultListener.awaitExpectedResults();
    clientResults = clientResultListener.popResults();
    assertEquals(1, clientResults.size());
    assertEquals(1, viewResultListener.popResults().size());
    expected = new HashMap<ValueRequirement, Object>();
    expected.put(env.getPrimitive1(), (byte) 3);
    expected.put(env.getPrimitive2(), (byte) 0);
    assertComputationResult(expected, env.getCalculationResult(clientResults.get(0)));

    // Pause results again and we should be back to merging
    client.pauseLive();

    snapshotProvider.addValue(env.getPrimitive2(), 1);
    viewResultListener.setExpectedResultCount(1);
    env.getCurrentRecalcJob(view).liveDataChanged();
    viewResultListener.awaitExpectedResults();
    assertEquals(1, viewResultListener.popResults().size());
    assertEquals(0, clientResultListener.popResults().size());

    snapshotProvider.addValue(env.getPrimitive2(), 2);
    viewResultListener.setExpectedResultCount(1);
    env.getCurrentRecalcJob(view).liveDataChanged();
    viewResultListener.awaitExpectedResults();
    assertEquals(1, viewResultListener.popResults().size());
    assertEquals(0, clientResultListener.popResults().size());
    
    clientResultListener.setExpectedResultCount(1);
    client.startLive();
    clientResultListener.awaitExpectedResults();
    assertEquals(0, viewResultListener.popResults().size());
    clientResults = clientResultListener.popResults();
    assertEquals(1, clientResults.size());
    expected = new HashMap<ValueRequirement, Object>();
    expected.put(env.getPrimitive1(), (byte) 3);
    expected.put(env.getPrimitive2(), (byte) 2);
    assertComputationResult(expected, env.getCalculationResult(clientResults.get(0)));
    
    client.stopLive();
    assertEquals(0, clientResultListener.popResults().size());
    assertEquals(0, viewResultListener.popResults().size());
    
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
    computationListener1.setExpectedResultCount(1);
    client.startLive();
    ViewRecalculationJob recalcJob = env.getCurrentRecalcJob(view);
    assertTrue(view.isRunning());
    computationListener1.awaitExpectedResults();
    assertEquals(1, computationListener1.popResults().size());
    assertEquals(0, deltaListener1.popResults().size());
    
    // Push through a second result - should have a delta this time
    snapshotProvider.addValue(env.getPrimitive1(), 3);
    computationListener1.setExpectedResultCount(1);
    deltaListener1.setExpectedResultCount(1);
    recalcJob.liveDataChanged();
    computationListener1.awaitExpectedResults();
    deltaListener1.awaitExpectedResults();
    assertEquals(1, computationListener1.popResults().size());
    assertEquals(1, deltaListener1.popResults().size());

    // Change both listeners
    TestDeltaResultListener deltaListener2 = new TestDeltaResultListener();
    client.setDeltaResultListener(deltaListener2);
    TestComputationResultListener computationListener2 = new TestComputationResultListener();
    client.setResultListener(computationListener2);
    assertTrue(view.isLiveComputationRunning());

    // Push through a result which should arrive at the new listeners
    computationListener2.setExpectedResultCount(1);
    deltaListener2.setExpectedResultCount(1);
    recalcJob.liveDataChanged();
    computationListener2.awaitExpectedResults();
    deltaListener2.awaitExpectedResults();
    assertEquals(0, computationListener1.popResults().size());
    assertEquals(0, deltaListener1.popResults().size());
    assertEquals(1, computationListener2.popResults().size());
    assertEquals(1, deltaListener2.popResults().size());

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
        ValueRequirement requirement = new ValueRequirement(value.getKey(), target.getType(), target.getUniqueIdentifier());
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
