/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.client.merging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.junit.Test;

import com.opengamma.engine.view.ViewComputationResultModel;

/**
 * Tests RateLimitingMergingUpdateProvider
 */
public class RateLimitingMergingUpdateProviderTest {

  @Test
  public void testPassThrough() {
    RateLimitingMergingUpdateProvider<ViewComputationResultModel> provider = new RateLimitingMergingUpdateProvider<ViewComputationResultModel>(new ViewComputationResultModelMerger(), new Timer(
        "Custom timer"));

    TestMergingUpdateListener testListener = new TestMergingUpdateListener();
    provider.addUpdateListener(testListener);

    // OK, it doesn't really test the 'synchronous' bit, but it at least checks that no merging has happened.
    addResults(provider, 1000);
    assertEquals(1000, testListener.consumeResults().size());

    provider.setPaused(true);
    addResults(provider, 1000);
    assertEquals(0, testListener.consumeResults().size());
    provider.setPaused(false);
    assertEquals(1, testListener.consumeResults().size());

    provider.setPaused(false);
    addResults(provider, 1000);
    assertEquals(1000, testListener.consumeResults().size());

    provider.destroy();
  }

  @Test
  public void testMergingWhenRateLimiting() throws InterruptedException {
    RateLimitingMergingUpdateProvider<ViewComputationResultModel> provider = new RateLimitingMergingUpdateProvider<ViewComputationResultModel>(new ViewComputationResultModelMerger(), 500);

    TestMergingUpdateListener testListener = new TestMergingUpdateListener();
    provider.addUpdateListener(testListener);

    addResults(provider, 1000);
    Thread.sleep(500);
    assertEquals(1, testListener.consumeResults().size());

    provider.destroy();
  }

  @Test
  public void testModifiableUpdatePeriod() throws InterruptedException {
    RateLimitingMergingUpdateProvider<ViewComputationResultModel> provider = new RateLimitingMergingUpdateProvider<ViewComputationResultModel>(new ViewComputationResultModelMerger(), 500);

    TestMergingUpdateListener testListener = new TestMergingUpdateListener();
    provider.addUpdateListener(testListener);

    assertCorrectUpdateRate(provider, testListener, 100);
    assertCorrectUpdateRate(provider, testListener, 400);
    assertCorrectUpdateRate(provider, testListener, 50);

    provider.destroy();
  }

  private void assertCorrectUpdateRate(RateLimitingMergingUpdateProvider<ViewComputationResultModel> provider, TestMergingUpdateListener testListener, int period) throws InterruptedException {
    provider.setMinimumUpdatePeriodMillis(period);
    testUpdateRate(provider, testListener, period);

    // If the provider is paused then all updates should be merged regardless of the time elapsed or the rate
    provider.setPaused(true);
    for (int i = 0; i < 3; i++) {
      addResults(provider, 10);
      Thread.sleep(period);
    }
    assertEquals(0, testListener.consumeResults().size());
    provider.setPaused(false);
    Thread.sleep(2 * period);
    assertEquals(1, testListener.consumeResults().size());

    // Once unpaused, everything should be back to normal
    testUpdateRate(provider, testListener, period);
  }

  private void testUpdateRate(RateLimitingMergingUpdateProvider<ViewComputationResultModel> provider, TestMergingUpdateListener testListener, int period) throws InterruptedException {
    testListener.resetShortestDelay();
    for (int i = 0; i < 100; i++) {
      Thread.sleep(10);
      addResults(provider, 10);
    }
    // Wait a couple of periods for any stragglers
    Thread.sleep (2 * period);
    // Check that the results didn't come any faster than we asked for (give or take 10%), and not too slowly (allow up to twice)
    assertTrue ("Expecting results no faster than " + period + "ms, got " + testListener.getShortestDelay (), testListener.getShortestDelay () >= (period - period / 10));
    assertTrue ("Expecting results no slower than " + (period * 2) + "ms, got " + testListener.getShortestDelay (), testListener.getShortestDelay () <= (period * 2));
    System.out.println ("size = " + testListener.consumeResults ().size ());
  }

  private void addResults(RateLimitingMergingUpdateProvider<ViewComputationResultModel> provider, int count) {
    for (int i = 0; i < count; i++) {
      provider.newResult(mock(ViewComputationResultModel.class));
    }
  }

  private class TestMergingUpdateListener implements MergedUpdateListener<ViewComputationResultModel> {

    private long _lastResultReceived;
    private long _shortestDelay;
    private List<ViewComputationResultModel> _resultsReceived = new ArrayList<ViewComputationResultModel>();

    @Override
    public synchronized void handleResult(ViewComputationResultModel result) {
      long now = System.currentTimeMillis ();
      long delay = now - _lastResultReceived;
      _lastResultReceived = now;
      if (delay < _shortestDelay) {
        _shortestDelay = delay;
      }
      _resultsReceived.add(result);
    }

    public synchronized List<ViewComputationResultModel> consumeResults() {
      List<ViewComputationResultModel> results = _resultsReceived;
      _resultsReceived = new ArrayList<ViewComputationResultModel>();
      return results;
    }
    
    public synchronized long getShortestDelay () {
      return _shortestDelay;
    }
    
    public synchronized void resetShortestDelay () {
      _shortestDelay = Long.MAX_VALUE;
    }

  }

}
