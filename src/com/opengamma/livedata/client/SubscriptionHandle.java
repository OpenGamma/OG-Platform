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

/**
 * 
 *
 * @author kirk
 */
public class SubscriptionHandle {
  private final String _userName;
  private final LiveDataSpecification _fullyQualifiedSpecification;
  private final LiveDataListener _listener;
  
  public SubscriptionHandle(
      String userName,
      LiveDataSpecification fullyQualifiedSpecification,
      LiveDataListener listener) {
    // TODO kirk 2009-09-29 -- Check Inputs.
    _userName = userName;
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
