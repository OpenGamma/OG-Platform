/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.Date;

import com.opengamma.util.ArgumentChecker;

/**
 * A record of a market data subscription currently active on a server. 
 *
 * @author pietari
 */
class Subscription {
  
  /** What was subscribed to. Bloomberg/Reuters/{your market data provider of choice} unique ID for a security. **/
  private final String _securityUniqueId;
  
  /** Handle to underlying (e.g., Bloomberg/Reuters) subscription */
  private final Object _handle;
  
  /** JMS topic to which server will send updates */
  private final String _distributionSpecification;
  
  /**
   * Whether this subscription should expire automatically if
   * no heartbeats are received from clients.
   * True = a persistent subscription, should never expire.
   * False = a non-persistent subscription, should expire.
   */
  private boolean _persistent;
  
  /** 
   * When this subscription should expire (milliseconds from UTC epoch).
   * Null means it does not expire (is persistent).
   */
  private Long _expiry = null;
  
  private final Date _creationTime;
  
  /**
   * 
   * @param securityUniqueId
   * @param handle
   * @param distributionSpecification
   * @param persistent If false, creates a subscription that will expire, but only
   * far, far in the future (Long.MAX_VALUE milliseconds from the epoch). You can use
   * {@link #setExpiry(Long)} to set a more sensible expiration date. 
   */
  public Subscription(
      String securityUniqueId,
      Object handle,
      String distributionSpecification,
      boolean persistent) {
    ArgumentChecker.checkNotNull(securityUniqueId, "Security unique ID");
    ArgumentChecker.checkNotNull(handle, "Subscription handle");
    ArgumentChecker.checkNotNull(distributionSpecification, "Distribution specification");
    
    _securityUniqueId = securityUniqueId;
    _handle = handle;
    _distributionSpecification = distributionSpecification;
    setPersistent(persistent);
    
    _creationTime = new Date();
  }
  
  public Object getHandle() {
    return _handle;
  }

  public String getDistributionSpecification() {
    return _distributionSpecification;
  }

  public Date getCreationTime() {
    return _creationTime;
  }

  public String getSecurityUniqueId() {
    return _securityUniqueId;
  }

  public synchronized boolean isPersistent() {
    return _persistent;
  }
  
  /**
   * 
   * @param persistent If false, the subscription will expire, but only
   * far, far in the future (Long.MAX_VALUE milliseconds from the epoch). You can use
   * {@link #setExpiry(Long)} to set a more sensible expiration date.
   */
  public synchronized void setPersistent(boolean persistent) {
    _persistent = persistent;
    if (_persistent) {
      _expiry = null;
    } else {
      _expiry = Long.MAX_VALUE;
    }
  }

  public synchronized Long getExpiry() {
    return _expiry;
  }

  /**
   * @param expiry If this subscription is persistent, you can only supply a null expiry.
   * If this subscription is non-persistent, you must supply a non-null expiry. 
   * @throws IllegalStateException If the above rules for expiry are violated.
   */
  public synchronized void setExpiry(Long expiry) {
    if (isPersistent() && expiry != null) {
      throw new IllegalStateException("A persistent subscription cannot expire");      
    }
    if (!isPersistent() && expiry == null) {
      throw new IllegalStateException("A non-persistent subscription must expire");
    }
    
    _expiry = expiry;
  }
  
  public String toString() {
    return _distributionSpecification;
  }
  
}
