/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.fudgemsg.FudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * A record of a market data subscription currently active on a server. 
 *
 * @author pietari
 */
public class Subscription {
  
  private static final Logger s_logger = LoggerFactory.getLogger(Subscription.class);
  
  // Note how all mutator methods are package-private, whereas all getters are public.
  // The only class that should create/modify/delete Subscriptions is AbstractLiveDataServer.
  
  /** What was subscribed to. Bloomberg/Reuters/{your market data provider of choice} unique ID for a security. **/
  private final String _securityUniqueId;
  
  /** 
   * The data from this subscription can be distributed to clients in multiple formats,
   * therefore we need multiple market data distributors. 
   */
  private final Map<DistributionSpecification, MarketDataDistributor> _distributors = new ConcurrentHashMap<DistributionSpecification, MarketDataDistributor>();
  
  /** Handle to underlying (e.g., Bloomberg/Reuters) subscription */
  private volatile Object _handle;
  
  /** 
   * History of ticks received from the underlying market data source
   */
  private final FieldHistoryStore _history = new FieldHistoryStore();
  
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
  Subscription(
      String securityUniqueId,
      Object handle,
      boolean persistent) {
    ArgumentChecker.notNull(securityUniqueId, "Security unique ID");
    ArgumentChecker.notNull(handle, "Subscription handle");
    
    _securityUniqueId = securityUniqueId;
    _handle = handle;
    setPersistent(persistent);
    
    _creationTime = new Date();
  }
  
  
  void setHandle(Object handle) {
    _handle = handle;
  }

  public Object getHandle() {
    return _handle;
  }

  public Date getCreationTime() {
    return _creationTime;
  }

  public String getSecurityUniqueId() {
    return _securityUniqueId;
  }
  
  public Set<DistributionSpecification> getDistributionSpecifications() {
    return Collections.unmodifiableSet(_distributors.keySet());
  }
  
  public Collection<MarketDataDistributor> getDistributors() {
    return Collections.unmodifiableCollection(_distributors.values());
  }
  
  public MarketDataDistributor getMarketDataDistributor(DistributionSpecification distributionSpec) {
    return _distributors.get(distributionSpec);
  }
  
  /**
   * Tells this subscription to start distributing market data in the given format.
   * Only creates a new distribution if it doesn't already exist.
   * 
   * @param spec The format
   * @param marketDataSenders See {@link MarketDataDistributor#MarketDataDistributor(DistributionSpecification, Subscription, Collection)}
   */
  synchronized void createDistribution(DistributionSpecification spec, Collection<MarketDataSender> marketDataSenders) {
    
    if (getDistributionSpecifications().contains(spec)) {
      s_logger.info("Added distribution spec {} to {} (no-op)", spec, this);
      return;
    }
    
    MarketDataDistributor distributor = new MarketDataDistributor(
        spec,
        this,
        marketDataSenders);
    
    _distributors.put(spec, distributor);  
    
    s_logger.info("Added distribution spec {} to {}", spec, this);
  }
  
  synchronized void removeDistribution(DistributionSpecification spec) {
    MarketDataDistributor removed = _distributors.remove(spec);
    if (removed != null) {
      s_logger.info("Removed distribution spec {} from {}", spec, this);      
    } else {
      s_logger.info("Removed distribution spec {} from {} (no-op)", spec, this);
    }
  }
  
  FieldHistoryStore getLiveDataHistory() {
    return _history;
  }

  public void liveDataReceived(FudgeFieldContainer liveDataFields) {
    _history.liveDataReceived(liveDataFields);
    
    for (MarketDataDistributor distributionSpec : getDistributors()) {
      distributionSpec.liveDataReceived(liveDataFields);
    }
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
  synchronized void setPersistent(boolean persistent) {
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
  synchronized void setExpiry(Long expiry) {
    if (isPersistent() && expiry != null) {
      throw new IllegalStateException("A persistent subscription cannot expire");      
    }
    if (!isPersistent() && expiry == null) {
      throw new IllegalStateException("A non-persistent subscription must expire");
    }
    
    _expiry = expiry;
  }
  
  public String toString() {
    return "Subscription " + _securityUniqueId;
  }
  
}
