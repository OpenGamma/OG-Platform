/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.livedata.LiveDataSpecification;

/**
 * 
 *
 * @author kirk
 */
public class MockLiveDataServer extends AbstractLiveDataServer {
  
  private List<LiveDataSpecification> _subscriptions = new ArrayList<LiveDataSpecification>();
  private List<LiveDataSpecification> _unsubscriptions = new ArrayList<LiveDataSpecification>();

  @Override
  public synchronized void subscribe(LiveDataSpecification fullyQualifiedSpec) {
    _subscriptions.add(fullyQualifiedSpec);    
  }
  
  @Override
  public synchronized void unsubscribe(LiveDataSpecification spec) {
    _unsubscriptions.add(spec);
  }

  public List<LiveDataSpecification> getSubscriptions() {
    return _subscriptions;
  }

  public List<LiveDataSpecification> getUnsubscriptions() {
    return _unsubscriptions;
  }

}
