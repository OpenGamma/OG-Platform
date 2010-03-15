/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.id.IdentificationDomain;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class MockLiveDataServer extends AbstractLiveDataServer {
  
  private final IdentificationDomain _domain;
  private final List<String> _subscriptions = new ArrayList<String>();
  private final List<String> _unsubscriptions = new ArrayList<String>();
  
  public MockLiveDataServer(IdentificationDomain domain) {
    ArgumentChecker.checkNotNull(domain, "Identification domain");
    _domain = domain;
  }
  
  @Override
  protected IdentificationDomain getUniqueIdDomain() {
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

  public List<String> getActualSubscriptions() {
    return _subscriptions;
  }

  public List<String> getActualUnsubscriptions() {
    return _unsubscriptions;
  }

}
