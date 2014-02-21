/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.fudgemsg.FudgeMsg;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.client.AbstractLiveDataClient;
import com.opengamma.livedata.client.SubscriptionHandle;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;

/**
 * A {@code LiveDataClient} that works completely in memory
 * and actually does not contact any server.
 */
public class TestLiveDataClient extends AbstractLiveDataClient {
  
  private final List<LiveDataSpecification> _cancelRequests = new ArrayList<LiveDataSpecification>();
  private final List<Collection<SubscriptionHandle>> _subscriptionRequests = new ArrayList<Collection<SubscriptionHandle>>();
  private final AtomicLong _sequenceGenerator = new AtomicLong(0);
  
  @Override
  protected void cancelPublication(LiveDataSpecification fullyQualifiedSpecification) {
    _cancelRequests.add(fullyQualifiedSpecification);
  }

  @Override
  protected void handleSubscriptionRequest(Collection<SubscriptionHandle> subHandles) {
    _subscriptionRequests.add(subHandles);
  }

  public List<LiveDataSpecification> getCancelRequests() {
    return _cancelRequests;
  }

  public List<Collection<SubscriptionHandle>> getSubscriptionRequests() {
    return _subscriptionRequests;
  }

  public void marketDataReceived(LiveDataSpecification fullyQualifiedSpecification, FudgeMsg fields) {
    LiveDataValueUpdateBean bean = new LiveDataValueUpdateBean(_sequenceGenerator.incrementAndGet(), fullyQualifiedSpecification, fields);
    getValueDistributor().notifyListeners(bean);
  }

  @Override
  public Map<LiveDataSpecification, Boolean> isEntitled(UserPrincipal user,
      Collection<LiveDataSpecification> requestedSpecifications) {
    Map<LiveDataSpecification, Boolean> returnValue = new HashMap<LiveDataSpecification, Boolean>();
    for (LiveDataSpecification spec : requestedSpecifications) {
      returnValue.put(spec, isEntitled(user, spec));            
    }
    return returnValue;
  }

  @Override
  public boolean isEntitled(UserPrincipal user, LiveDataSpecification requestedSpecification) {
    return true;
  }
  
  @Override
  public void subscriptionRequestSatisfied(SubscriptionHandle subHandle, LiveDataSubscriptionResponse response) {
    super.subscriptionRequestSatisfied(subHandle, response);
  }
  
}
