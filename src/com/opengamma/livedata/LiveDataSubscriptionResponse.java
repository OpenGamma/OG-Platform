/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * A holder of data relating to a request for a subscription.
 *
 * @author kirk
 */
public class LiveDataSubscriptionResponse implements Serializable, Cloneable {
  private final String _requestingUserName;
  private final LiveDataSpecification _requestedSpecification;
  private final LiveDataSpecification _fullyQualifiedSpecification;
  private final LiveDataSubscriptionResult _subscriptionResult;
  
  public LiveDataSubscriptionResponse(
      String requestingUserName,
      LiveDataSpecification requestedSpecification,
      LiveDataSpecification fullyQualifiedSpecification,
      LiveDataSubscriptionResult subscriptionResult) {
    // TODO kirk 2009-09-29 -- Check inputs.
    _requestingUserName = requestingUserName;
    _requestedSpecification = requestedSpecification;
    _fullyQualifiedSpecification = fullyQualifiedSpecification;
    _subscriptionResult = subscriptionResult;
  }

  /**
   * @return the requestingUserName
   */
  public String getRequestingUserName() {
    return _requestingUserName;
  }

  /**
   * @return the requestedSpecification
   */
  public LiveDataSpecification getRequestedSpecification() {
    return _requestedSpecification;
  }

  /**
   * @return the fullyQualifiedSpecification
   */
  public LiveDataSpecification getFullyQualifiedSpecification() {
    return _fullyQualifiedSpecification;
  }

  /**
   * @return the subscriptionResult
   */
  public LiveDataSubscriptionResult getSubscriptionResult() {
    return _subscriptionResult;
  }

  @Override
  public LiveDataSubscriptionResponse clone() {
    try {
      return (LiveDataSubscriptionResponse) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Yes, it is supported.");
    }
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
