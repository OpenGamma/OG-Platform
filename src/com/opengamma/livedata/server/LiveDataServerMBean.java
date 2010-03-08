/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.Set;

/**
 * LiveData server attributes and operations that can be managed via JMX. 
 *
 * @author pietari
 */
public interface LiveDataServerMBean {
  
  /**
   * @return How many different tickers the server subscribes to.
   */
  int getNumActiveSubscriptions();
  
  /**
   * @return Security IDs the server subscribes to.
   * The form of the IDs is dependent on the source system - 
   * Reuters RICs, Bloomberg unique IDs, etc. 
   */
  Set<String> getActiveSubscriptionIds();
  
  /**
   * @return JMS topics the server publishes to. 
   */
  Set<String> getActiveDistributionSpecs();
  
  /**
   * @return The number of market data updates the server has processed in its lifetime.
   */
  long getNumLiveDataUpdatesSent();

}
