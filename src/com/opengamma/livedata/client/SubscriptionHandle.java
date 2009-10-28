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
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class SubscriptionHandle {
  private final String _userName;
  private final LiveDataSpecification _requestedSpecification;
  private final LiveDataSpecification _fullyQualifiedSpecification;
  private final LiveDataListener _listener;
  
  public SubscriptionHandle(
      String userName,
      LiveDataSpecification requestedSpecification,
      LiveDataSpecification fullyQualifiedSpecification,
      LiveDataListener listener) {
    ArgumentChecker.checkNotNull(userName, "User Name");
    // Intentionally don't check the requested specification.
    ArgumentChecker.checkNotNull(fullyQualifiedSpecification, "Fully Qualified Specification");
    ArgumentChecker.checkNotNull(listener, "Live Data Listener");
    _userName = userName;
    _requestedSpecification = requestedSpecification;
    _fullyQualifiedSpecification = fullyQualifiedSpecification;
    _listener = listener;
  }

  /**
   * @return the userName
   */
  public String getUserName() {
    return _userName;
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
   * @return the listener
   */
  public LiveDataListener getListener() {
    return _listener;
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj, new String[] {"_requestedSpecification"});
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, new String[] {"_requestedSpecification"});
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
  
  

}
