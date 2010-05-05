/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.id.IdentificationScheme;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class MockLiveDataServer extends AbstractLiveDataServer {
  
  private final IdentificationScheme _domain;
  private final List<String> _subscriptions = new ArrayList<String>();
  private final List<String> _unsubscriptions = new ArrayList<String>();
  private volatile int _numConnections = 0;
  private volatile int _numDisconnections = 0;
  private final Map<String, FudgeFieldContainer> _uniqueId2MarketData;
  
  public MockLiveDataServer(IdentificationScheme domain) {
    this(domain, Collections.<String, FudgeFieldContainer>emptyMap());
  }
  
  public MockLiveDataServer(IdentificationScheme domain,
      Map<String, FudgeFieldContainer> uniqueId2Snapshot) {
    ArgumentChecker.notNull(domain, "Identification domain");
    ArgumentChecker.notNull(uniqueId2Snapshot, "Snapshot map");
    _domain = domain;
    _uniqueId2MarketData = uniqueId2Snapshot;
  }
  
  @Override
  protected IdentificationScheme getUniqueIdDomain() {
    return _domain;
  }

  @Override
  protected Object doSubscribe(String uniqueId) {
    _subscriptions.add(uniqueId);
    return uniqueId;
  }

  @Override
  protected void doUnsubscribe(Object subscriptionHandle) {
    _unsubscriptions.add((String) subscriptionHandle);
  }
  
  @Override
  protected FudgeFieldContainer doSnapshot(String uniqueId) {
    FudgeFieldContainer snapshot = _uniqueId2MarketData.get(uniqueId);
    if (snapshot == null) {
      snapshot = FudgeContext.GLOBAL_DEFAULT.newMessage();
    }
    return snapshot;
  }
  
  public void sendLiveDataToClient() {
    for (Subscription subscription : getSubscriptions()) {
      FudgeFieldContainer marketData = doSnapshot(subscription.getSecurityUniqueId());
      liveDataReceived(subscription.getSecurityUniqueId(), marketData);
    }
  }

  public List<String> getActualSubscriptions() {
    return _subscriptions;
  }

  public List<String> getActualUnsubscriptions() {
    return _unsubscriptions;
  }

  @Override
  protected void doConnect() {
    _numConnections++;
  }

  @Override
  protected void doDisconnect() {
    _numDisconnections++;
  }
  
  @Override
  protected boolean snapshotOnSubscriptionStartRequired(
      Subscription subscription) {
    return false;
  }

  public int getNumConnections() {
    return _numConnections;
  }

  public int getNumDisconnections() {
    return _numDisconnections;
  }

}
