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
 * Implementors decide which subscriptions are faked out 
 */
public interface FakeSubscriptionSelector {

  /**
   * 
   * @param server the server to use to decide
   * @param specs The securities 
   * @return 2 disjoint subsets of the specs, (specs which shouldn't be faked, specs which should be faked)
   */
  ObjectsPair<Collection<LiveDataSpecification>, Collection<LiveDataSpecification>> splitShouldFake(FakeSubscriptionBloombergLiveDataServer server,
      Collection<LiveDataSpecification> specs);
}
