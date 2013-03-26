/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.fudgemsg.FudgeContext;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.msg.SubscriptionType;
import com.opengamma.livedata.test.CollectingLiveDataListener;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SubscriptionHandleTest {

  private final UserPrincipal _user = new UserPrincipal("kirk", "127.0.0.1");

  public void equalsDifferentRequestedSpecification() {
    LiveDataSpecification requestedSpecification =
      new LiveDataSpecification(
          "NormalizationId1",
          ExternalId.of("Domain1", "Value1"));
    CollectingLiveDataListener listener = new CollectingLiveDataListener();
    SubscriptionHandle handle1 = new SubscriptionHandle(_user, SubscriptionType.NON_PERSISTENT, requestedSpecification, listener);
    SubscriptionHandle handle2 = new SubscriptionHandle(_user, SubscriptionType.NON_PERSISTENT, requestedSpecification,  listener);
    assertFalse(handle1.equals(handle2));
  }

  public void hashCodeDifferentRequestedSpecification() {
    LiveDataSpecification requestedSpecification =
      new LiveDataSpecification(
          "NormalizationId1",
          ExternalId.of("Domain1", "Value1"));
    CollectingLiveDataListener listener = new CollectingLiveDataListener();
    SubscriptionHandle handle1 = new SubscriptionHandle(_user, SubscriptionType.NON_PERSISTENT, requestedSpecification, listener);
    SubscriptionHandle handle2 = new SubscriptionHandle(_user, SubscriptionType.NON_PERSISTENT, requestedSpecification, listener);
    assertFalse(handle1.hashCode() == handle2.hashCode());
  }

  public void releaseTicks() {
    LiveDataSpecification spec =
      new LiveDataSpecification(
          "NormalizationId1",
          ExternalId.of("Domain1", "Value1"));
    CollectingLiveDataListener listener = new CollectingLiveDataListener();
    SubscriptionHandle handle = new SubscriptionHandle(_user, SubscriptionType.NON_PERSISTENT, spec, listener);
    
    handle.addTickOnHold(new LiveDataValueUpdateBean(500, spec, FudgeContext.EMPTY_MESSAGE));
    handle.addSnapshotOnHold(new LiveDataValueUpdateBean(501, spec, FudgeContext.EMPTY_MESSAGE));
    handle.addTickOnHold(new LiveDataValueUpdateBean(502, spec, FudgeContext.EMPTY_MESSAGE));
    handle.releaseTicksOnHold();
    
    assertEquals(2, listener.getValueUpdates().size());
    
    assertEquals(501, listener.getValueUpdates().get(0).getSequenceNumber());
    assertEquals(502, listener.getValueUpdates().get(1).getSequenceNumber());
  }

  public void releaseTicksServerRestart() {
    LiveDataSpecification spec =
      new LiveDataSpecification(
          "NormalizationId1",
          ExternalId.of("Domain1", "Value1"));
    CollectingLiveDataListener listener = new CollectingLiveDataListener();
    SubscriptionHandle handle = new SubscriptionHandle(_user, SubscriptionType.NON_PERSISTENT, spec, listener);
    
    handle.addTickOnHold(new LiveDataValueUpdateBean(500, spec, FudgeContext.EMPTY_MESSAGE));
    handle.addTickOnHold(new LiveDataValueUpdateBean(501, spec, FudgeContext.EMPTY_MESSAGE));
    handle.addSnapshotOnHold(new LiveDataValueUpdateBean(502, spec, FudgeContext.EMPTY_MESSAGE));
    handle.addTickOnHold(new LiveDataValueUpdateBean(0, spec, FudgeContext.EMPTY_MESSAGE));
    handle.addTickOnHold(new LiveDataValueUpdateBean(1, spec, FudgeContext.EMPTY_MESSAGE));
    handle.releaseTicksOnHold();
    
    assertEquals(2, listener.getValueUpdates().size());
    
    assertEquals(0, listener.getValueUpdates().get(0).getSequenceNumber());
    assertEquals(1, listener.getValueUpdates().get(1).getSequenceNumber());
  }

  public void releaseTicksMultipleServerRestarts() {
    LiveDataSpecification spec =
      new LiveDataSpecification(
          "NormalizationId1",
          ExternalId.of("Domain1", "Value1"));
    CollectingLiveDataListener listener = new CollectingLiveDataListener();
    SubscriptionHandle handle = new SubscriptionHandle(_user, SubscriptionType.NON_PERSISTENT, spec, listener);
    
    handle.addTickOnHold(new LiveDataValueUpdateBean(500, spec, FudgeContext.EMPTY_MESSAGE));
    handle.addSnapshotOnHold(new LiveDataValueUpdateBean(501, spec, FudgeContext.EMPTY_MESSAGE));
    handle.addTickOnHold(new LiveDataValueUpdateBean(502, spec, FudgeContext.EMPTY_MESSAGE));
    handle.addTickOnHold(new LiveDataValueUpdateBean(0, spec, FudgeContext.EMPTY_MESSAGE));
    handle.addTickOnHold(new LiveDataValueUpdateBean(1, spec, FudgeContext.EMPTY_MESSAGE));
    handle.addTickOnHold(new LiveDataValueUpdateBean(0, spec, FudgeContext.EMPTY_MESSAGE));
    handle.addTickOnHold(new LiveDataValueUpdateBean(1, spec, FudgeContext.EMPTY_MESSAGE));
    handle.releaseTicksOnHold();
    
    assertEquals(2, listener.getValueUpdates().size());
    
    assertEquals(0, listener.getValueUpdates().get(0).getSequenceNumber());
    assertEquals(1, listener.getValueUpdates().get(1).getSequenceNumber());
  }

}
