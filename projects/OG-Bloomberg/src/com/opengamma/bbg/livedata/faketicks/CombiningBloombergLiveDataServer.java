/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata.faketicks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Lists;
import com.opengamma.bbg.livedata.BloombergLiveDataServer;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.server.AbstractLiveDataServer;
import com.opengamma.livedata.server.CombiningLiveDataServer;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Combines the {@link FakeSubscriptionBloombergLiveDataServer} with the real {@link BloombergLiveDataServer} 
 */
public class CombiningBloombergLiveDataServer extends CombiningLiveDataServer {

  private final FakeSubscriptionBloombergLiveDataServer _fakeServer;
  private final BloombergLiveDataServer _realServer;
  private final FakeSubscriptionSelector _selector;
  /**
   * @param otherUnderlyings
   * @param fakeServer The server to use for fake requests
   * @param realServer The server to use for real requests
   * @param selector The selector to use to route requests
   */
  public CombiningBloombergLiveDataServer(FakeSubscriptionBloombergLiveDataServer fakeServer,
      BloombergLiveDataServer realServer, FakeSubscriptionSelector selector) {
    super(Lists.newArrayList(realServer, fakeServer));
    _fakeServer = fakeServer;
    _realServer = realServer;
    _selector = selector;
  }
  
  

  
  @Override
  protected Map<AbstractLiveDataServer, Collection<LiveDataSpecification>> groupByServer(Collection<LiveDataSpecification> specs) {
    ObjectsPair<Collection<LiveDataSpecification>, Collection<LiveDataSpecification>> split = _selector.splitShouldFake(_fakeServer, specs);

    Map<AbstractLiveDataServer, Collection<LiveDataSpecification>> mapped = new HashMap<AbstractLiveDataServer, Collection<LiveDataSpecification>>();
    mapped.put(_realServer, split.first);
    mapped.put(_fakeServer, split.second);
        
    return mapped;
  }

  /**
   * Gets the fakeServer field.
   * @return the fakeServer
   */
  public FakeSubscriptionBloombergLiveDataServer getFakeServer() {
    return _fakeServer;
  }


  /**
   * Gets the realServer field.
   * @return the realServer
   */
  public BloombergLiveDataServer getRealServer() {
    return _realServer;
  }
  
  
}
