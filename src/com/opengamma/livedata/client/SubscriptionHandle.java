/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.SubscriptionType;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class SubscriptionHandle {
  private final String _userName;
  private final SubscriptionType _subscriptionType;
  private final LiveDataSpecification _requestedSpecification;
  private final LiveDataListener _listener;
  
  public SubscriptionHandle(
      String userName,
      SubscriptionType subscriptionType,
      LiveDataSpecification requestedSpecification,
      LiveDataListener listener) {
    ArgumentChecker.checkNotNull(userName, "User Name");
    ArgumentChecker.checkNotNull(subscriptionType, "Subscription type");
    ArgumentChecker.checkNotNull(requestedSpecification, "Requested Specification");
    ArgumentChecker.checkNotNull(listener, "Live Data Listener");
    _userName = userName;
    _subscriptionType = subscriptionType;
    _requestedSpecification = requestedSpecification;
    _listener = listener;
  }

  /**
   * @return the userName
   */
  public String getUserName() {
    return _userName;
  }
  
  public SubscriptionType getSubscriptionType() {
    return _subscriptionType;
  }

  /**
   * @return the requestedSpecification
   */
  public LiveDataSpecification getRequestedSpecification() {
    return _requestedSpecification;
  }

  /**
   * @return the listener
   */
  public LiveDataListener getListener() {
    return _listener;
  }

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
