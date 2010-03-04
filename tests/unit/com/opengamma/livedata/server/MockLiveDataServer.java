/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.id.IdentificationDomain;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class MockLiveDataServer extends AbstractLiveDataServer {
  
  private final IdentificationDomain _domain;
  private List<String> _subscriptions = new ArrayList<String>();
  private List<String> _unsubscriptions = new ArrayList<String>();
  
  public MockLiveDataServer(IdentificationDomain domain) {
    ArgumentChecker.checkNotNull(domain, "Identification domain");
    _domain = domain;
  }
  
  @Override
  protected IdentificationDomain getUniqueIdDomain() {
    return _domain;
  }

  @Override
  protected Object subscribe(String uniqueId) {
    _subscriptions.add(uniqueId);
    return uniqueId;
  }

  @Override
  protected void unsubscribe(Object subscriptionHandle) {
    _unsubscriptions.add((String) subscriptionHandle);
  }

  public List<String> getSubscriptions() {
    return _subscriptions;
  }

  public List<String> getUnsubscriptions() {
    return _unsubscriptions;
  }

}
