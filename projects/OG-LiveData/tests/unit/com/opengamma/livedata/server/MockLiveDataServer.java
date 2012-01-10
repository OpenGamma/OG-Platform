/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fudgemsg.FudgeMsg;

import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * 
 */
public class MockLiveDataServer extends AbstractLiveDataServer {
  
  private final ExternalScheme _domain;
  private final List<String> _subscriptions = new ArrayList<String>();
  private final List<String> _unsubscriptions = new ArrayList<String>();
  private volatile int _numConnections; // = 0;
  private volatile int _numDisconnections; // = 0;
  private final Map<String, FudgeMsg> _uniqueId2MarketData;
  
  public MockLiveDataServer(ExternalScheme domain) {
    this(domain, new ConcurrentHashMap<String, FudgeMsg>());
  }
  
  public MockLiveDataServer(ExternalScheme domain,
      Map<String, FudgeMsg> uniqueId2Snapshot) {
    ArgumentChecker.notNull(domain, "Identification domain");
    ArgumentChecker.notNull(uniqueId2Snapshot, "Snapshot map");
    _domain = domain;
    _uniqueId2MarketData = uniqueId2Snapshot;
  }
  
  public void addMarketDataMapping(String key, FudgeMsg value) {
    _uniqueId2MarketData.put(key, value);        
  }
  
  @Override
  public ExternalScheme getUniqueIdDomain() {
    return _domain;
  }

  @Override
  protected Map<String, Object> doSubscribe(Collection<String> uniqueIds) {
    Map<String, Object> returnValue = new HashMap<String, Object>();
    
    for (String uniqueId : uniqueIds) {
      _subscriptions.add(uniqueId);
      returnValue.put(uniqueId, uniqueId);
    }
    
    return returnValue;
  }

  @Override
  protected void doUnsubscribe(Collection<Object> subscriptionHandles) {
    for (Object subscriptionHandle : subscriptionHandles) {
      _unsubscriptions.add((String) subscriptionHandle);
    }
  }
  
  @Override
  protected Map<String, FudgeMsg> doSnapshot(Collection<String> uniqueIds) {
    Map<String, FudgeMsg> returnValue = new HashMap<String, FudgeMsg>();
    
    for (String uniqueId : uniqueIds) {
      FudgeMsg snapshot = _uniqueId2MarketData.get(uniqueId);
      if (snapshot == null) {
        snapshot = OpenGammaFudgeContext.getInstance().newMessage();
      }
      returnValue.put(uniqueId, snapshot);
    }
    
    return returnValue;
  }
  
  public void sendLiveDataToClient() {
    for (Subscription subscription : getSubscriptions()) {
      FudgeMsg marketData = doSnapshot(subscription.getSecurityUniqueId());
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
  
  @Override
  public String getDefaultNormalizationRuleSetId() {
    return StandardRules.getNoNormalization().getId();
  }

}
