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
public class Subscription {
  
  /** What was subscribed to **/
  private final String _securityUniqueId;
  
  /** Handle to underlying (e.g., Bloomberg/Reuters) subscription */
  private final Object _handle;
  
  /** JMS topic to which server will send updates */
  private final String _distributionSpecification;
  
  private final Date _creationTime;
  
  public Subscription(
      String securityUniqueId,
      Object handle,
      String distributionSpecification) {
    ArgumentChecker.checkNotNull(securityUniqueId, "Security unique ID");
    ArgumentChecker.checkNotNull(handle, "Subscription handle");
    ArgumentChecker.checkNotNull(distributionSpecification, "Distribution specification");
    
    _securityUniqueId = securityUniqueId;
    _handle = handle;
    _distributionSpecification = distributionSpecification;
    
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
  
}
