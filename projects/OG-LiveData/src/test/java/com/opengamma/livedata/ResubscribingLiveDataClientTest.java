/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.Collection;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class ResubscribingLiveDataClientTest {

  private static final ExternalId ID1 = ExternalId.of("scheme", "1");
  private static final ExternalId ID2 = ExternalId.of("scheme", "2");
  private static final LiveDataSpecification SPEC1 = new LiveDataSpecification("rules", ID1);
  private static final LiveDataSpecification SPEC2 = new LiveDataSpecification("rules", ID2);

  @Test
  public void subscribe() {
    LiveDataClient delegate = mock(LiveDataClient.class);
    ResubscribingLiveDataClient client = new ResubscribingLiveDataClient(delegate);
    Listener listener = new Listener();
    client.subscribe(UserPrincipal.getTestUser(), SPEC1, listener);
    client.subscribe(UserPrincipal.getTestUser(), SPEC2, listener);
    verify(delegate).subscribe(UserPrincipal.getTestUser(), SPEC1, listener);
    verify(delegate).subscribe(UserPrincipal.getTestUser(), SPEC2, listener);
    reset(delegate);
    client.resubscribe();
    verify(delegate).subscribe(UserPrincipal.getTestUser(), SPEC1, listener);
    verify(delegate).subscribe(UserPrincipal.getTestUser(), SPEC2, listener);
  }

  @Test
  public void unsubscribe() {
    LiveDataClient delegate = mock(LiveDataClient.class);
    ResubscribingLiveDataClient client = new ResubscribingLiveDataClient(delegate);
    Listener listener = new Listener();
    client.subscribe(UserPrincipal.getTestUser(), SPEC1, listener);
    client.subscribe(UserPrincipal.getTestUser(), SPEC2, listener);
    client.unsubscribe(UserPrincipal.getTestUser(), SPEC1, listener);
    verify(delegate).subscribe(UserPrincipal.getTestUser(), SPEC1, listener);
    verify(delegate).subscribe(UserPrincipal.getTestUser(), SPEC2, listener);
    verify(delegate).unsubscribe(UserPrincipal.getTestUser(), SPEC1, listener);
    reset(delegate);
    client.resubscribe();
    verify(delegate).subscribe(UserPrincipal.getTestUser(), SPEC2, listener);
  }

  private static class Listener implements LiveDataListener {

    @Override
    public void subscriptionResultReceived(LiveDataSubscriptionResponse subscriptionResult) {
    }

    @Override
    public void subscriptionResultsReceived(Collection<LiveDataSubscriptionResponse> subscriptionResults) {
    }

    @Override
    public void subscriptionStopped(LiveDataSpecification fullyQualifiedSpecification) {
    }

    @Override
    public void valueUpdate(LiveDataValueUpdate valueUpdate) {
    }
  }
}
