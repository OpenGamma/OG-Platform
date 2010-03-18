/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.Set;

import org.springframework.jmx.export.annotation.ManagedOperation;

/**
 * 
 * 
 * @author pietari
 */
public interface PersistentSubscriptionManagerMBean {
  
  public Set<String> getPersistentSubscriptions();

  @ManagedOperation(description = "Reads persistent subscriptions from persistent storage."
      + " Subscribes to any entries to which we are not yet subscribed.")
  public void refresh();

  @ManagedOperation(description = "Saves all persistent subscriptions to persistent storage.")
  public void save();
  
  @ManagedOperation(description = "Adds a persistent subscription. If the subscription already exists, makes it persistent.")
  public void addPersistentSubscription(String securityUniqueId);
  
  @ManagedOperation(description = "Removes a persistent subscription by making the subscription non-persistent." +
  		" Returns true if a subscription was actually made non-persistent, false otherwise.")
  public boolean removePersistentSubscription(String securityUniqueId);

}
