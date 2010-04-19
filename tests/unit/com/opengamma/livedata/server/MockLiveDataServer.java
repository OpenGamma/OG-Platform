/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.ArrayList;
import java.util.List;

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
  
  public MockLiveDataServer(IdentificationScheme domain) {
    ArgumentChecker.checkNotNull(domain, "Identification domain");
    _domain = domain;
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
    return FudgeContext.GLOBAL_DEFAULT.newMessage();
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

  public int getNumConnections() {
    return _numConnections;
  }

  public int getNumDisconnections() {
    return _numDisconnections;
  }

}
