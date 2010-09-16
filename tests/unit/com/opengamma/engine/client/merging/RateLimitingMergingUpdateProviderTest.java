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
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < 100; i++) {
      Thread.sleep(10);
      addResults(provider, 10);
    }
    long endTime = System.currentTimeMillis();
    int timeTaken = (int) (endTime - startTime);
    int count = 0;
    // Allow up to a few more periods for stragglers to come through
    for (int i = 0; i < 10; i++) {
      count += testListener.consumeResults().size();
      //System.out.println("i=" + i + ", count=" + count + ", expected=" + timeTaken / period);
      if (assertAcceptableResultCount(timeTaken / period, count, false)) {
        return;
      }
      Thread.sleep(period);
    }
    assertAcceptableResultCount(timeTaken / period, count, true);
  }

  private void addResults(RateLimitingMergingUpdateProvider<ViewComputationResultModel> provider, int count) {
    for (int i = 0; i < count; i++) {
      provider.newResult(mock(ViewComputationResultModel.class));
    }
  }

  private boolean assertAcceptableResultCount(int periodCount, int numberOfResults, boolean raiseError) {
    // Timer delays could result in fewer results than the theoretical number of periods.
    int lowerLimit = periodCount - 1;

    // If we're unlucky, the timer could go off mid-burst, splitting a burst into two, and producing an extra result.
    // Seem to need a bit of flexibility here.
    int upperLimit = periodCount + 2;

    boolean isAcceptable = numberOfResults >= lowerLimit && numberOfResults <= upperLimit;
    if (raiseError) {
      assertTrue("Expecting " + lowerLimit + " to " + upperLimit + " results, got " + numberOfResults, isAcceptable);
    }
    return isAcceptable;
  }

  private class TestMergingUpdateListener implements MergedUpdateListener<ViewComputationResultModel> {

    private List<ViewComputationResultModel> _resultsReceived = new ArrayList<ViewComputationResultModel>();

    @Override
    public synchronized void handleResult(ViewComputationResultModel result) {
      _resultsReceived.add(result);
    }

    public synchronized List<ViewComputationResultModel> consumeResults() {
      List<ViewComputationResultModel> results = _resultsReceived;
      _resultsReceived = new ArrayList<ViewComputationResultModel>();
      return results;
    }

  }

}
