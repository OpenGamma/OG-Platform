/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.mxbean;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Data of a subscription trace.
 */
public class SubscriptionTrace {

  /**
   * The identifier for the subscription being traced.
   */
  private final String _identifier;
  /**
   * Creation time of the subscription. Held as a string for compatibility with the MX Bean.
   */
  private final String _created;
  /**
   * The distributor traces.
   */
  private final Set<DistributorTrace> _distributors;
  /**
   * The last value.
   */
  private final String _lastValues;

  /**
   * Creates an instance.
   * 
   * @param identifier  the identifier
   */
  public SubscriptionTrace(String identifier) {
    this(identifier, "N/A", ImmutableSet.<DistributorTrace>of(), "N/A");
  }

  /**
   * Creates an instance.
   * 
   * @param identifier  the identifier
   * @param created  the subscription creation time as a string
   * @param distributors  the distributors
   * @param lastValues  the last value
   */
  public SubscriptionTrace(String identifier, String created, Set<DistributorTrace> distributors, String lastValues) {
    _identifier = identifier;
    _created = created;
    _distributors = distributors;
    _lastValues = lastValues;
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

  public String getLastValues() {
    return _lastValues;
  }

}
