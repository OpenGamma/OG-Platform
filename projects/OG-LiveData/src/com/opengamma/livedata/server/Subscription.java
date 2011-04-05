/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.server.distribution.MarketDataDistributor;
import com.opengamma.livedata.server.distribution.MarketDataSenderFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * A record of a market data subscription currently active on a server. 
 *
 * @author pietari
 */
public class Subscription {
  
  private static final Logger s_logger = LoggerFactory.getLogger(Subscription.class);
  
  /** What was subscribed to. Bloomberg/Reuters/{your market data provider of choice} unique ID for a security. **/
  private final String _securityUniqueId;
  
  /** Controls how the data from this subscription will be sent */
  private final MarketDataSenderFactory _marketDataSenderFactory;
  
  /** 
   * The data from this subscription can be distributed to clients in multiple formats,
   * therefore we need multiple market data distributors.
   * <p>
   * Since an ordinary HashMap is used, access to the map must be via synchronized methods. 
   */
  private final Map<DistributionSpecification, MarketDataDistributor> _distributors = new HashMap<DistributionSpecification, MarketDataDistributor>();
  
  /** 
   * Handle to underlying (e.g., Bloomberg/Reuters) subscription.
   * May be null if the subscription is not currently active.
   */
  private volatile Object _handle;
  
  /** 
   * History of ticks received from the underlying market data API, in its native format.
   */
  private final FieldHistoryStore _history = new FieldHistoryStore();
  
  private final Date _creationTime;
  
  /**
   * @param marketDataSenderFactory Will create market data distributors for this subscription
   * @param securityUniqueId Security unique ID
   */
  public Subscription(String securityUniqueId, MarketDataSenderFactory marketDataSenderFactory) {
    ArgumentChecker.notNull(securityUniqueId, "Security unique ID");
    ArgumentChecker.notNull(marketDataSenderFactory, "Market data sender factory");
    
    _securityUniqueId = securityUniqueId;
    _marketDataSenderFactory = marketDataSenderFactory;
    _creationTime = new Date();
  }
  
  
  public void setHandle(Object handle) {
    _handle = handle;
  }

  /**
   * @return May return null if the subscription is not currently active. 
   */
  public Object getHandle() {
    return _handle;
  }

  public Date getCreationTime() {
    return _creationTime;
  }

  public String getSecurityUniqueId() {
    return _securityUniqueId;
  }
  
  public MarketDataSenderFactory getMarketDataSenderFactory() {
    return _marketDataSenderFactory;
  }
  
  public synchronized Set<DistributionSpecification> getDistributionSpecifications() {
    return new HashSet<DistributionSpecification>(_distributors.keySet());
  }
  
  public synchronized Collection<MarketDataDistributor> getDistributors() {
    return new ArrayList<MarketDataDistributor>(_distributors.values());
  }
  
  public synchronized MarketDataDistributor getMarketDataDistributor(DistributionSpecification distributionSpec) {
    return _distributors.get(distributionSpec);
  }
  
  public MarketDataDistributor getMarketDataDistributor(LiveDataSpecification fullyQualifiedSpec) {
    for (MarketDataDistributor distributor : getDistributors()) {
      if (distributor.getDistributionSpec().getFullyQualifiedLiveDataSpecification().equals(fullyQualifiedSpec)) {
        return distributor;
      }
    }
    return null;
  }
  
  /**
   * Tells this subscription to start distributing market data in the given format.
   * Only creates a new distribution if it doesn't already exist.
   * 
   * @param spec The format
   * @param persistent Whether the distributor should be persistent (i.e., survive
   * a server restart)
   * @return The created/modified {@code MarketDataDistributor}
   */
  /*package*/ synchronized MarketDataDistributor createDistributor(DistributionSpecification spec, 
      boolean persistent) {
    
    MarketDataDistributor distributor = getMarketDataDistributor(spec);
    if (distributor == null) {
      distributor = new MarketDataDistributor(
          spec,
          this,
          getMarketDataSenderFactory(),
          persistent);

      _distributors.put(spec, distributor);
      s_logger.info("Added {} to {}", distributor, this);
    }
    
    // Might be necessary to make the distributor persistent. We
    // never turn it back from persistent to non-persistent, however.
    if (!distributor.isPersistent() && persistent) {
      distributor.setPersistent(persistent);
      s_logger.info("Made {} persistent", distributor);
    }
    
    return distributor;
  }
  
  /*package*/ synchronized void removeDistributor(MarketDataDistributor distributor) {
    removeDistributor(distributor.getDistributionSpec());
  }
  
  /*package*/ synchronized void removeDistributor(DistributionSpecification spec) {
    MarketDataDistributor removed = _distributors.remove(spec);
    if (removed != null) {
      s_logger.info("Removed {} from {}", removed, this);      
    } else {
      s_logger.info("Removed distribution spec {} from {} (no-op)", spec, this);
    }
  }
  
  /*package*/ synchronized void removeAllDistributors() {
    s_logger.info("Removed {} from {}", _distributors, this);
    _distributors.clear();
  }
  
  /*package*/ synchronized void initialSnapshotReceived(FudgeMsg liveDataFields) {
    _history.liveDataReceived(liveDataFields);
    
    for (MarketDataDistributor distributor : getDistributors()) {
      distributor.updateFieldHistory(liveDataFields);
    }
  }

  /*package*/ synchronized void liveDataReceived(FudgeMsg liveDataFields) {
    _history.liveDataReceived(liveDataFields);
    
    for (MarketDataDistributor distributor : getDistributors()) {
      distributor.distributeLiveData(liveDataFields);
    }
  }
  
  public synchronized FieldHistoryStore getLiveDataHistory() {
    return new FieldHistoryStore(_history);
  }
  
  public boolean isActive() {
    return getHandle() != null;
  }
  
  @Override
  public String toString() {
    return "Subscription[" + _securityUniqueId + "]";
  }
  
}
