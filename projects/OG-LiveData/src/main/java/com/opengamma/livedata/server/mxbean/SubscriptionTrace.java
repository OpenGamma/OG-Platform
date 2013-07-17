/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.mxbean;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class SubscriptionTrace {

  /**
   * The identifier for the subscription being traced.
   */
  private final String _identifier;

  /**
   * Creation time of the subscription. Held as a string for compatibility with the MX Bean.
   */
  private final String _created;

  private final Set<DistributorTrace> _distributors;

  public SubscriptionTrace(String identifier, String created) {
    this(identifier, created, ImmutableSet.<DistributorTrace>of());
  }

  public SubscriptionTrace(String identifier, String created, Set<DistributorTrace> distributors) {
    _identifier = identifier;
    _created = created;
    _distributors = distributors;
  }

  public String getIdentifier() {
    return _identifier;
  }

  public String getCreated() {
    return _created;
  }

  public Set<DistributorTrace> getDistributors() {
    return _distributors;
  }
}
