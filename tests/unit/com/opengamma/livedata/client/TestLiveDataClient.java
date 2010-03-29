/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataSubscriptionResponse;
import com.opengamma.livedata.LiveDataSubscriptionResult;

/**
 * 
 *
 * @author pietari
 */
public class TestLiveDataClient extends AbstractLiveDataClient {
  
  private final List<LiveDataSpecification> _cancelRequests = new ArrayList<LiveDataSpecification>();
  private final List<Collection<SubscriptionHandle>> _subscriptionRequests = new ArrayList<Collection<SubscriptionHandle>>();
  
  @Override
  protected void cancelPublication(LiveDataSpecification fullyQualifiedSpecification) {
    _cancelRequests.add(fullyQualifiedSpecification);
  }

  @Override
  protected void handleSubscriptionRequest(Collection<SubscriptionHandle> subHandles) {
    _subscriptionRequests.add(subHandles);
    for (SubscriptionHandle subHandle : subHandles) {
      LiveDataSubscriptionResponse response = new LiveDataSubscriptionResponse(
          subHandle.getRequestedSpecification(),
          subHandle.getRequestedSpecification(),
          LiveDataSubscriptionResult.SUCCESS,
          "success message",
          "test distribution spec");        
      subscriptionRequestSatisfied(subHandle, response);
    }
  }

  public List<LiveDataSpecification> getCancelRequests() {
    return _cancelRequests;
  }

  public List<Collection<SubscriptionHandle>> getSubscriptionRequests() {
    return _subscriptionRequests;
  }
  
  public void marketDataReceived(LiveDataSpecification fullyQualifiedSpecification, FudgeFieldContainer fields) {
    getValueDistributor().notifyListeners(System.currentTimeMillis(), fullyQualifiedSpecification, fields);
  }
}
