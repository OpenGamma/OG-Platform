/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdate;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;

/**
 * Just collects events for use in a test environment.
 * 
 * @author kirk
 */
public class CollectingLiveDataListener implements LiveDataListener {

  private final Semaphore _responses;
  private final Semaphore _updates;

  private final List<LiveDataSubscriptionResponse> _subscriptionResponses = new ArrayList<LiveDataSubscriptionResponse>();
  private final List<LiveDataSpecification> _stoppedSubscriptions = new ArrayList<LiveDataSpecification>();
  private final Map<LiveDataSpecification, LiveDataSpecification> _client2ServerSpec = new HashMap<LiveDataSpecification, LiveDataSpecification>();
  private final Map<LiveDataSpecification, List<LiveDataValueUpdate>> _valueUpdates = new HashMap<LiveDataSpecification, List<LiveDataValueUpdate>>();

  public CollectingLiveDataListener() {
    this(1, 1);
  }

  public CollectingLiveDataListener(int numResponsesToExpect, int numUpdatesToWaitFor) {
    _responses = new Semaphore(1 - numResponsesToExpect);
    _updates = new Semaphore(1 - numUpdatesToWaitFor);
  }

  public synchronized void clear() {
    _subscriptionResponses.clear();
    _stoppedSubscriptions.clear();
    _valueUpdates.clear();
  }

  @Override
  public synchronized void subscriptionResultReceived(LiveDataSubscriptionResponse subscriptionResult) {
    _subscriptionResponses.add(subscriptionResult);
    if (subscriptionResult.getSubscriptionResult() == LiveDataSubscriptionResult.SUCCESS) {
      _client2ServerSpec.put(subscriptionResult.getRequestedSpecification(), subscriptionResult.getFullyQualifiedSpecification());
    }
    _responses.release();
  }

  @Override
  public synchronized void subscriptionResultsReceived(Collection<LiveDataSubscriptionResponse> subscriptionResults) {
    _subscriptionResponses.addAll(subscriptionResults);
    for (LiveDataSubscriptionResponse subscriptionResult : subscriptionResults) {
      if (subscriptionResult.getSubscriptionResult() == LiveDataSubscriptionResult.SUCCESS) {
        _client2ServerSpec.put(subscriptionResult.getRequestedSpecification(), subscriptionResult.getFullyQualifiedSpecification());
      }
      _responses.release();
    }
  }

  @Override
  public synchronized void subscriptionStopped(LiveDataSpecification fullyQualifiedSpecification) {
    _stoppedSubscriptions.add(fullyQualifiedSpecification);
  }

  @Override
  public synchronized void valueUpdate(LiveDataValueUpdate valueUpdate) {
    List<LiveDataValueUpdate> updates = _valueUpdates.get(valueUpdate.getSpecification());
    if (updates == null) {
      updates = new ArrayList<LiveDataValueUpdate>();
      _valueUpdates.put(valueUpdate.getSpecification(), updates);
    }
    updates.add(valueUpdate);
    _updates.release();
  }

  public synchronized List<LiveDataSubscriptionResponse> getSubscriptionResponses() {
    return new ArrayList<LiveDataSubscriptionResponse>(_subscriptionResponses);
  }

  public synchronized List<LiveDataSpecification> getStoppedSubscriptions() {
    return new ArrayList<LiveDataSpecification>(_stoppedSubscriptions);
  }

  public synchronized List<LiveDataValueUpdate> getValueUpdates() {
    ArrayList<LiveDataValueUpdate> returnValue = new ArrayList<LiveDataValueUpdate>();
    for (List<LiveDataValueUpdate> updates : _valueUpdates.values()) {
      returnValue.addAll(updates);
    }
    return returnValue;
  }

  public synchronized List<LiveDataValueUpdate> getValueUpdates(LiveDataSpecification specFromClient) {
    LiveDataSpecification fullyQualifiedSpec = _client2ServerSpec.get(specFromClient);
    if (fullyQualifiedSpec == null) {
      return Collections.emptyList();
    }

    List<LiveDataValueUpdate> updates = _valueUpdates.get(fullyQualifiedSpec);
    if (updates == null) {
      return Collections.emptyList();
    }

    return new ArrayList<LiveDataValueUpdate>(updates);
  }

  public boolean waitForResponses(final int count, final long timeoutMs) {
    try {
      return _responses.tryAcquire(count, timeoutMs, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.interrupted();
      throw new RuntimeException("Interrupted");
    }
  }

  public boolean waitUntilAllResponsesReceived(final long timeoutMs) {
    return waitForResponses(1, timeoutMs);
  }

  public boolean waitForUpdates(final int count, final long timeoutMs) {
    try {
      return _updates.tryAcquire(count, timeoutMs, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.interrupted();
      throw new RuntimeException("Interrupted");
    }
  }

  public boolean waitUntilEnoughUpdatesReceived(final long timeoutMs) {
    return waitForUpdates(1, timeoutMs);
  }

}
