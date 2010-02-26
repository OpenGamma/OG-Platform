/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import com.opengamma.livedata.LiveDataSpecification;

/**
 * 
 *
 * @author pietari
 */
public interface SubscriptionListener {
  
  public void subscribed(LiveDataSpecification fullyQualifiedSpec);
  public void unsubscribed(LiveDataSpecification fullyQualifiedSpec);

}
