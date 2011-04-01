/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

import static org.mockito.Mockito.mock;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.InMemoryViewDeltaResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewProcessListener;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.engine.view.calc.ViewCycleManagerImpl;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionImpl;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.tuple.Pair;

/**
 * Tests RateLimitingMergingUpdateProvider
 */
@Test
public class RateLimitingMergingViewProcessListenerTest {

  private static final Logger s_logger = LoggerFactory.getLogger(RateLimitingMergingViewProcessListenerTest.class);

  @Test
  public void testPassThrough() {
    TestViewProcessListener testListener = new TestViewProcessListener();
    RateLimitingMergingViewProcessListener mergingListener = new RateLimitingMergingViewProcessListener(testListener, mock(ViewCycleManagerImpl.class), new Timer("Custom timer"));

    // OK, it doesn't really test the 'synchronous' bit, but it at least checks that no merging has happened.
    addCompile(mergingListener);
    addResults(mergingListener, 1000);
    testListener.assertCompiled();
    testListener.assertFullResults(1000);
    testListener.assertNothingMoreReceived();

    mergingListener.setPaused(true);
    addResults(mergingListener, 1000);
    testListener.assertNothingMoreReceived();
    mergingListener.setPaused(false);
    testListener.assertFullResult();
    testListener.assertNothingMoreReceived();

    mergingListener.setPaused(false);
    addResults(mergingListener, 1000);
    testListener.assertFullResults(1000);

    mergingListener.shutdown();
    testListener.assertShutdown();
    testListener.assertNothingMoreReceived();
    
    mergingListener.shutdown();
  }

  @Test
  public void testMergingWhenRateLimiting() throws InterruptedException {
    TestViewProcessListener testListener = new TestViewProcessListener();
    RateLimitingMergingViewProcessListener mergingListener = new RateLimitingMergingViewProcessListener(testListener, mock(ViewCycleManagerImpl.class), new Timer("Custom timer"));
    mergingListener.setMinimumUpdatePeriodMillis(500);

    addResults(mergingListener, 1000);
    Thread.sleep(500);
    testListener.assertFullResult();
    testListener.assertNothingMoreReceived();

    mergingListener.terminate();
  }

  @Test
  public void testModifiableUpdatePeriod() throws InterruptedException {
    TestViewProcessListener testListener = new TestViewProcessListener();
    RateLimitingMergingViewProcessListener mergingListener = new RateLimitingMergingViewProcessListener(testListener, mock(ViewCycleManagerImpl.class), new Timer("Custom timer"));

    assertCorrectUpdateRate(mergingListener, testListener, 100);
    assertCorrectUpdateRate(mergingListener, testListener, 400);
    assertCorrectUpdateRate(mergingListener, testListener, 50);

    mergingListener.terminate();
  }
  
  @Test
  public void testCallOrderingAndCollapsing() {
    TestViewProcessListener testListener = new TestViewProcessListener(true);
    RateLimitingMergingViewProcessListener mergingListener = new RateLimitingMergingViewProcessListener(testListener, mock(ViewCycleManagerImpl.class), new Timer("Custom timer"));
   
    mergingListener.setPaused(true);
    testListener.assertNothingMoreReceived();

    // Begin sequence while paused
    
    addCompile(mergingListener);
    addResults(mergingListener, 10);
    
    CompiledViewDefinitionImpl preCompilation = mock(CompiledViewDefinitionImpl.class);
    mergingListener.viewDefinitionCompiled(preCompilation);
    
    addResults(mergingListener, 10);
    mergingListener.result(mock(ViewComputationResultModel.class), getDeltaResult(1));
    ViewComputationResultModel latestResult = mock(ViewComputationResultModel.class);
    mergingListener.result(latestResult, getDeltaResult(2));

    CompiledViewDefinitionImpl postCompilation = mock(CompiledViewDefinitionImpl.class);
    mergingListener.viewDefinitionCompiled(postCompilation);
    
    mergingListener.processCompleted();
    mergingListener.shutdown();
    
    // End of sequence while paused
    
    mergingListener.setPaused(false);
    
    testListener.assertNextCall(preCompilation);
    testListener.assertNextCall(latestResult);
    
    ViewDeltaResultModel mergedDelta = (ViewDeltaResultModel) testListener.popNextCall();
    assertEquals(2, mergedDelta.getAllResults().size());
    Set<Pair<String, Integer>> results = new HashSet<Pair<String, Integer>>();
    for (ViewResultEntry deltaItem : mergedDelta.getAllResults()) {
      results.add(Pair.of(deltaItem.getComputedValue().getSpecification().getValueName(), (Integer) deltaItem.getComputedValue().getValue()));
    }
    assertTrue(results.contains(Pair.of("value1", 1)));
    assertTrue(results.contains(Pair.of("value2", 2)));
    
    testListener.assertNextCall(postCompilation);
    testListener.assertProcessCompleted();
    testListener.assertShutdown();
    testListener.assertNothingMoreReceived();
  }
  
  private ViewDeltaResultModel getDeltaResult(int value) {
    InMemoryViewDeltaResultModel deltaResult = new InMemoryViewDeltaResultModel();
    deltaResult.setCalculationConfigurationNames(Collections.singleton("DEFAULT"));
    deltaResult.addValue("DEFAULT", getComputedValue("value" + value, value));
    return deltaResult;
  }
  
  private ComputedValue getComputedValue(String valueName, Object value) {
    UniqueIdentifier uniqueId = UniqueIdentifier.of("Scheme", valueName);
    ValueRequirement valueRequirement = new ValueRequirement(valueName, ComputationTargetType.PRIMITIVE, uniqueId);
    return new ComputedValue(new ValueSpecification(valueRequirement, "FunctionId"), value);
  }

  private void assertCorrectUpdateRate(RateLimitingMergingViewProcessListener mergingListener, TestViewProcessListener testListener, int period) throws InterruptedException {
    mergingListener.setMinimumUpdatePeriodMillis(period);
    assertUpdateRate(mergingListener, testListener, period);

    // If the provider is paused then all updates should be merged regardless of the time elapsed or the rate
    mergingListener.setPaused(true);
    for (int i = 0; i < 3; i++) {
      addResults(mergingListener, 10);
      Thread.sleep(period);
    }
    testListener.assertNothingMoreReceived();
    mergingListener.setPaused(false);
    Thread.sleep(2 * period);
    testListener.assertFullResults(1);
    testListener.assertNothingMoreReceived();

    // Once unpaused, everything should be back to normal
    assertUpdateRate(mergingListener, testListener, period);
  }

  private void assertUpdateRate(RateLimitingMergingViewProcessListener mergingListener, TestViewProcessListener testListener, int period) throws InterruptedException {
    testListener.resetShortestDelay();
    for (int i = 0; i < 100; i++) {
      Thread.sleep(10);
      addResults(mergingListener, 10);
    }
    // Wait a couple of periods for any stragglers
    Thread.sleep (2 * period);
    // Check that the results didn't come any faster than we asked for (give or take 10%), and not too slowly (allow up to twice)
    assertTrue ("Expecting results no faster than " + period + "ms, got " + testListener.getShortestDelay (), testListener.getShortestDelay () >= (period - period / 10));
    assertTrue ("Expecting results no slower than " + (period * 2) + "ms, got " + testListener.getShortestDelay (), testListener.getShortestDelay () <= (period * 2));
    s_logger.info("Size = {}", testListener.clearCalls());
  }

  private void addResults(ViewProcessListener listener, int count) {
    for (int i = 0; i < count; i++) {
      listener.result(mock(ViewComputationResultModel.class), null);
    }
  }
  
  private void addCompile(ViewProcessListener listener) {
    listener.viewDefinitionCompiled(mock(CompiledViewDefinitionImpl.class));
  }

  private class TestViewProcessListener implements ViewProcessListener {

    private final boolean _deltaRequired;
    private long _lastResultReceived;
    private long _shortestDelay;
    private Queue<Object> _callsReceived = new LinkedList<Object>();
    
    public TestViewProcessListener() {
      this(false);
    }
    
    public TestViewProcessListener(boolean deltaRequired) {
      _deltaRequired = deltaRequired;
    }
    
    public synchronized long getShortestDelay () {
      return _shortestDelay;
    }
    
    public synchronized void resetShortestDelay () {
      _shortestDelay = Long.MAX_VALUE;
    }

    @Override
    public boolean isDeltaResultRequired() {
      return _deltaRequired;
    }

    @Override
    public void viewDefinitionCompiled(CompiledViewDefinitionImpl compiledView) {
      _callsReceived.add(compiledView);
    }
    
    public void assertCompiled() {
      assertReceived(CompiledViewDefinitionImpl.class);
    }

    @Override
    public void result(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
      long now = System.currentTimeMillis();
      long delay = now - _lastResultReceived;
      _lastResultReceived = now;
      if (delay < _shortestDelay) {
        _shortestDelay = delay;
      }
      _callsReceived.add(fullResult);
      if (deltaResult != null) {
        _callsReceived.add(deltaResult);
      }
    }
    
    public void assertFullResult() {
      assertReceived(ViewComputationResultModel.class);
    }
    
    public void assertFullResults(int resultCount) {
      for (int i = 0; i < resultCount; i++) {
        try {
          assertFullResult();
        } catch (Exception e) {
          throw new AssertionError("Expecting " + resultCount + " results but no more found after result " + i);
        }
      }
    }

    @Override
    public void processCompleted() {
      _callsReceived.add("ProcessCompleted");
    }
    
    public void assertProcessCompleted() {
      assertNextCall("ProcessCompleted");
    }
    
    @Override
    public void shutdown() {
      _callsReceived.add("Shutdown");
    }
    
    public void assertShutdown() {
      assertNextCall("Shutdown");
    }
    
    public void assertNothingMoreReceived() {
      assertTrue(_callsReceived.isEmpty());
    }
    
    public void assertNextCall(Object expected) {
      assertEquals(expected, _callsReceived.poll());
    }
    
    public Object popNextCall() {
      return _callsReceived.poll();
    }
    
    public int clearCalls() {
      int size = _callsReceived.size();
      _callsReceived.clear();
      return size;
    }
    
    private void assertReceived(Class<?> expectedResultType) {
      Object o = _callsReceived.poll();
      assertNotNull(o);
      assertTrue(expectedResultType.isInstance(o));
    }

  }

}
