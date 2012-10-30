/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata.faketicks;

import java.util.Collection;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Selects which subscriptions are faked and which are real tickers.
 */
public interface FakeSubscriptionSelector {

  /**
   * Chooses whether to use the fake or real server.
   * 
   * @param server  the server to use to decide, not null
   * @param specs  the specifications that are required, not null
   * @return the two disjoint subsets of the specifications, (specs which shouldn't be faked, specs which should be faked)
   */
  ObjectsPair<Collection<LiveDataSpecification>, Collection<LiveDataSpecification>> splitShouldFake(
      FakeSubscriptionBloombergLiveDataServer server, Collection<LiveDataSpecification> specs);

}
