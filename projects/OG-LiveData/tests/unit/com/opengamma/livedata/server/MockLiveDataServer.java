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

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.id.IdentificationScheme;
import com.opengamma.livedata.normalization.StandardRules;
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
  private volatile int _numConnections; // = 0;
  private volatile int _numDisconnections; // = 0;
  private final Map<String, FudgeFieldContainer> _uniqueId2MarketData;
  
  public MockLiveDataServer(IdentificationScheme domain) {
    this(domain, new ConcurrentHashMap<String, FudgeFieldContainer>());
  }
  
  public MockLiveDataServer(IdentificationScheme domain,
      Map<String, FudgeFieldContainer> uniqueId2Snapshot) {
    ArgumentChecker.notNull(domain, "Identification domain");
    ArgumentChecker.notNull(uniqueId2Snapshot, "Snapshot map");
    _domain = domain;
    _uniqueId2MarketData = uniqueId2Snapshot;
  }
  
  public void addMarketDataMapping(String key, FudgeFieldContainer value) {
    _uniqueId2MarketData.put(key, value);        
  }
  
  @Override
  protected IdentificationScheme getUniqueIdDomain() {
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
  protected Map<String, FudgeFieldContainer> doSnapshot(Collection<String> uniqueIds) {
    Map<String, FudgeFieldContainer> returnValue = new HashMap<String, FudgeFieldContainer>();
    
    for (String uniqueId : uniqueIds) {
      FudgeFieldContainer snapshot = _uniqueId2MarketData.get(uniqueId);
      if (snapshot == null) {
        snapshot = FudgeContext.GLOBAL_DEFAULT.newMessage();
      }
      returnValue.put(uniqueId, snapshot);
    }
    
    return returnValue;
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
  
  @Override
  public String getDefaultNormalizationRuleSetId() {
    return StandardRules.getNoNormalization().getId();
  }

}
