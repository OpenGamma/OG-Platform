/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.io.IOException;
import java.util.Timer;

import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.server.LiveDataHeartbeat;
import com.opengamma.livedata.test.CollectingLiveDataListener;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION)
// test fails spuriously
public class HeartbeaterTest {

  private Timer _timer = null;

  @BeforeMethod
  public void startTimer() {
    _timer = new Timer("HeartbeatSenderTest Timer");
  }

  @AfterMethod
  public void shutdownTimer() {
    _timer.cancel();
    _timer = null;
  }

  //-------------------------------------------------------------------------
  @Test(invocationCount = 5, successPercentage = 19)
  public void basicOperation() throws InterruptedException, IOException {
    LiveDataHeartbeat heartbeatService = Mockito.mock(LiveDataHeartbeat.class);
    ValueDistributor valueDistributor = new ValueDistributor();
    CollectingLiveDataListener listener1 = new CollectingLiveDataListener();
    LiveDataSpecification spec1 = new LiveDataSpecification(
        "Test",
        ExternalId.of("foo", "bar"));
    LiveDataSpecification spec2 = new LiveDataSpecification(
        "Test",
        ExternalId.of("foo", "baz"));
    valueDistributor.addListener(spec1, listener1);
    valueDistributor.addListener(spec2, listener1);

    @SuppressWarnings("unused")
    Heartbeater heartbeatSender = new Heartbeater(valueDistributor, heartbeatService, _timer, 100L);
    // Wait 250ms to make sure we get two ticks.
    Thread.sleep(250L);

    Mockito.verify(heartbeatService, Mockito.atLeast(2)).heartbeat(Sets.newHashSet(spec1, spec2));

  }

  // TODO: Test the JmsLiveDataHeartbeatClient which was previously tested by the code above

}
