/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.server.distribution.MarketDataDistributor;
import com.opengamma.util.ArgumentChecker;

/**
 * A market data subscription that should survive server restarts.
 */
public class PersistentSubscription implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * What data should be subscribed to and how it should be distributed.
   */
  private final LiveDataSpecification _fullyQualifiedSpec;

  /**
   * Creates an instance.
   * 
   * @param fullyQualifiedSpec  the specification, not null
   */
  protected PersistentSubscription(LiveDataSpecification fullyQualifiedSpec) {
    ArgumentChecker.notNull(fullyQualifiedSpec, "Fully qualified spec");
    _fullyQualifiedSpec = fullyQualifiedSpec;
  }

  /**
   * Creates an instance.
   * 
   * @param distributor  the distributor, not null
   */
  protected PersistentSubscription(MarketDataDistributor distributor) {
    _fullyQualifiedSpec = distributor.getDistributionSpec().getFullyQualifiedLiveDataSpecification();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the specification.
   * 
   * @return the specification, not null
   */
  public LiveDataSpecification getFullyQualifiedSpec() {
    return _fullyQualifiedSpec;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
