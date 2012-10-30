/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata.faketicks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.CacheManager;

import com.google.common.collect.Lists;
import com.opengamma.bbg.livedata.BloombergLiveDataServer;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.server.CombiningLiveDataServer;
import com.opengamma.livedata.server.StandardLiveDataServer;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Combines the real and fake Bloomberg servers.
 * <p>
 * See {@link FakeSubscriptionBloombergLiveDataServer} and {@link BloombergLiveDataServer}.
 */
public class CombiningBloombergLiveDataServer extends CombiningLiveDataServer {

  /**
   * The fake server.
   */
  private final FakeSubscriptionBloombergLiveDataServer _fakeServer;
  /**
   * The real server.
   */
  private final BloombergLiveDataServer _realServer;
  /**
   * The selector.
   */
  private final FakeSubscriptionSelector _selector;

  /**
   * Creates an instance.
   * 
   * @param fakeServer  the server to use for fake requests, not null
   * @param realServer  the server to use for real requests, not null
   * @param selector  the selector to use to route requests, not null
   * @param cacheManager  the cache manager, not null
   */
  public CombiningBloombergLiveDataServer(
      FakeSubscriptionBloombergLiveDataServer fakeServer,
      BloombergLiveDataServer realServer,
      FakeSubscriptionSelector selector,
      CacheManager cacheManager) {
    super(Lists.newArrayList(realServer, fakeServer), cacheManager);
    _fakeServer = fakeServer;
    _realServer = realServer;
    _selector = selector;
  }

  //-------------------------------------------------------------------------
  @Override
  protected Map<StandardLiveDataServer, Collection<LiveDataSpecification>> groupByServer(Collection<LiveDataSpecification> specs) {
    ObjectsPair<Collection<LiveDataSpecification>, Collection<LiveDataSpecification>> split = _selector.splitShouldFake(_fakeServer, specs);
    Map<StandardLiveDataServer, Collection<LiveDataSpecification>> mapped = new HashMap<StandardLiveDataServer, Collection<LiveDataSpecification>>();
    mapped.put(_realServer, split.first);
    mapped.put(_fakeServer, split.second);
    return mapped;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the fake server.
   * 
   * @return the fake server, not null
   */
  public FakeSubscriptionBloombergLiveDataServer getFakeServer() {
    return _fakeServer;
  }

  /**
   * Gets the real server.
   * 
   * @return the real server, not null
   */
  public BloombergLiveDataServer getRealServer() {
    return _realServer;
  }

}
