/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import java.util.ArrayList;
import java.util.List;

/**
 * Just collects events for use in a test environment.
 *
 * @author kirk
 */
public class CollectingLiveDataListener implements LiveDataListener {
  private final List<LiveDataSubscriptionResponse> _subscriptionResponses =
    new ArrayList<LiveDataSubscriptionResponse>();
  private final List<LiveDataSpecification> _stoppedSubscriptions =
    new ArrayList<LiveDataSpecification>();
  private final List<LiveDataValueUpdate> _valueUpdates =
    new ArrayList<LiveDataValueUpdate>();
  
  public synchronized void clear() {
    _subscriptionResponses.clear();
    _stoppedSubscriptions.clear();
    _valueUpdates.clear();
  }

  @Override
  public synchronized void subscriptionResultReceived(
      LiveDataSubscriptionResponse subscriptionResult) {
    _subscriptionResponses.add(subscriptionResult);
  }

  @Override
  public synchronized void subscriptionStopped(
      LiveDataSpecification fullyQualifiedSpecification) {
    _stoppedSubscriptions.add(fullyQualifiedSpecification);
  }

  @Override
  public synchronized void valueUpdate(LiveDataValueUpdate valueUpdate) {
    _valueUpdates.add(valueUpdate);
  }
  
  public synchronized List<LiveDataSubscriptionResponse> getSubscriptionResponses() {
    return new ArrayList<LiveDataSubscriptionResponse>(_subscriptionResponses);
  }
  
  public synchronized List<LiveDataSpecification> getStoppedSubscriptions() {
    return new ArrayList<LiveDataSpecification>(_stoppedSubscriptions);
  }
  
  public synchronized List<LiveDataValueUpdate> getValueUpdates() {
    return new ArrayList<LiveDataValueUpdate>(_valueUpdates);
  }

}
