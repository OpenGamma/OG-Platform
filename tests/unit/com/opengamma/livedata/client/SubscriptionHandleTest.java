/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.livedata.CollectingLiveDataListener;
import com.opengamma.livedata.LiveDataSpecificationImpl;

/**
 * 
 *
 * @author kirk
 */
public class SubscriptionHandleTest {

  @Test
  public void equalsDifferentRequestedSpecification() {
    LiveDataSpecificationImpl requestedSpecification1 =
      new LiveDataSpecificationImpl(
          new DomainSpecificIdentifier("Domain1", "Value1"));
    LiveDataSpecificationImpl requestedSpecification2 =
      new LiveDataSpecificationImpl(
          new DomainSpecificIdentifier("Domain1", "Value2"));
    CollectingLiveDataListener listener = new CollectingLiveDataListener();
    SubscriptionHandle handle1 = new SubscriptionHandle("kirk", requestedSpecification1, requestedSpecification1, listener);
    SubscriptionHandle handle2 = new SubscriptionHandle("kirk", requestedSpecification1, requestedSpecification1, listener);
    assertTrue(handle1.equals(handle2));
    handle2 = new SubscriptionHandle("kirk", requestedSpecification2, requestedSpecification1, listener);
    assertTrue(handle1.equals(handle2));
    handle2 = new SubscriptionHandle("kirk", requestedSpecification1, requestedSpecification2, listener);
    assertFalse(handle1.equals(handle2));
  }

  @Test
  public void hashCodeDifferentRequestedSpecification() {
    LiveDataSpecificationImpl requestedSpecification1 =
      new LiveDataSpecificationImpl(
          new DomainSpecificIdentifier("Domain1", "Value1"));
    LiveDataSpecificationImpl requestedSpecification2 =
      new LiveDataSpecificationImpl(
          new DomainSpecificIdentifier("Domain1", "Value2"));
    CollectingLiveDataListener listener = new CollectingLiveDataListener();
    SubscriptionHandle handle1 = new SubscriptionHandle("kirk", requestedSpecification1, requestedSpecification1, listener);
    SubscriptionHandle handle2 = new SubscriptionHandle("kirk", requestedSpecification1, requestedSpecification1, listener);
    assertEquals(handle1.hashCode(), handle2.hashCode());
    handle2 = new SubscriptionHandle("kirk", requestedSpecification2, requestedSpecification1, listener);
    assertEquals(handle1.hashCode(), handle2.hashCode());
    handle2 = new SubscriptionHandle("kirk", requestedSpecification1, requestedSpecification2, listener);
    assertFalse(handle1.hashCode() == handle2.hashCode());
  }
}
