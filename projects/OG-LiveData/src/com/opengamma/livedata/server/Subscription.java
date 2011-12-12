/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.server.distribution.MarketDataDistributor;
import com.opengamma.livedata.server.distribution.MarketDataSenderFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * A record of a market data subscription currently active on a server. 
 */
public class Subscription {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(Subscription.class);

  /**
   * The unique ID that was subscribed to, specific to the market data provider, such as Bloomberg/Reuters.
   */
  private final String _securityUniqueId;
  /**
   * Controls how the data from this subscription will be sent.
   */
  private final MarketDataSenderFactory _marketDataSenderFactory;
  
  /**
   * A lock to enforce that live data is handled in a serialized and thus safe & ordered fashion.
   */
  private final ReentrantLock _liveDataSerializationLock = new ReentrantLock();
  
  /** 
   * The data from this subscription can be distributed to clients in multiple formats,
   * therefore we need multiple market data distributors.
   * NOTE: this is a concurrent map for speedy reads
   * <p> 
   */
  private final ConcurrentHashMap<DistributionSpecification, MarketDataDistributor> _distributors = new ConcurrentHashMap<DistributionSpecification, MarketDataDistributor>();
  /** 
   * The handle to the underlying subscription, specific to the market data provider, such as Bloomberg/Reuters.
   * May be null if the subscription is not currently active.
   */
  private volatile Object _handle;
  /** 
   * History of ticks received from the underlying market data API, in its native format.
   */
  private final FieldHistoryStore _history = new FieldHistoryStore();
  /**
   * The creation instant.
   */
  private final Date _creationTime;

  /**
   * Creates an instance.
   * 
   * @param securityUniqueId  the security unique ID, specific to the market data provider, not null
   * @param marketDataSenderFactory  the factory that will create market data distributors for this subscription
   */
  public Subscription(String securityUniqueId, MarketDataSenderFactory marketDataSenderFactory) {
    ArgumentChecker.notNull(securityUniqueId, "Security unique ID");
    ArgumentChecker.notNull(marketDataSenderFactory, "Market data sender factory");
    _securityUniqueId = securityUniqueId;
    _marketDataSenderFactory = marketDataSenderFactory;
    _creationTime = new Date();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the opaque handle to the underlying subscription, specific to the market data provider.
   * 
   * @return the opaque handle, null if the subscription is not currently active
   */
  public Object getHandle() {
    return _handle;
  }

  /**
   * Sets the opaque handle to the underlying subscription, specific to the market data provider.
   * 
   * @param handle  the opaque handle, null if the subscription is not currently active
   */
  public void setHandle(Object handle) {
    _handle = handle;
  }

  /**
   * Gets the creation instant.
   * 
   * @return the creation instant, not null
   */
  public Date getCreationTime() {
    return _creationTime;
  }

  /**
   * Gets the unique ID that was subscribed to, specific to the market data provider.
   * 
   * @return the market data provider unique ID, not null
   */
  public String getSecurityUniqueId() {
    return _securityUniqueId;
  }

  /**
   * Gets the factory used to create distributors.
   * 
   * @return the factory, not null
   */
  public MarketDataSenderFactory getMarketDataSenderFactory() {
    return _marketDataSenderFactory;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the set of distribution specifications.
   * 
   * @return a modifiable copy of the specifications, not null
   */
  public Set<DistributionSpecification> getDistributionSpecifications() {
    return _distributors.keySet();
  }

  /**
   * Gets the set of distributors.
   * 
   * @return a modifiable copy of the distributors, not null
   */
  public Collection<MarketDataDistributor> getDistributors() {
    return _distributors.values();
  }

  /**
   * Gets a specific distributor by distribution specification.
   * 
   * @param distributionSpec  the specification to find
   * @return the distributor, null if not found
   */
  public MarketDataDistributor getMarketDataDistributor(DistributionSpecification distributionSpec) {
    return _distributors.get(distributionSpec);
  }

  /**
   * Gets a specific distributor by specification.
   * 
   * @param fullyQualifiedSpec  the specification to find
   * @return the distributor, null if not found
   */
  public MarketDataDistributor getMarketDataDistributor(LiveDataSpecification fullyQualifiedSpec) {
    for (MarketDataDistributor distributor : getDistributors()) {
      if (distributor.getDistributionSpec().getFullyQualifiedLiveDataSpecification().equals(fullyQualifiedSpec)) {
        return distributor;
      }
    }
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * Tells this subscription to start distributing market data in the given format.
   * Only creates a new distribution if it doesn't already exist.
   * 
   * @param spec  the format to use
   * @param persistent  whether the distributor should be persistent (survive a server restart)
   * @return the created/modified {@code MarketDataDistributor}
   */
  /*package*/ MarketDataDistributor createDistributor(DistributionSpecification spec, boolean persistent) {
    MarketDataDistributor distributor = getMarketDataDistributor(spec);
    if (distributor == null) {
      distributor = new MarketDataDistributor(spec, this, getMarketDataSenderFactory(), persistent);
      MarketDataDistributor previous = _distributors.putIfAbsent(spec, distributor);
      if (previous == null) {
        s_logger.info("Added {} to {}", distributor, this);
      } else {
        s_logger.debug("Lost race to create distributor {} to {}", previous, this);
        distributor = previous;
      }
    }
    // Might be necessary to make the distributor persistent. We
    // never turn it back from persistent to non-persistent, however.
    if (!distributor.isPersistent() && persistent) {
      distributor.setPersistent(persistent);
      s_logger.info("Made {} persistent", distributor);
    }
    return distributor;
  }

  /*package*/ void removeDistributor(MarketDataDistributor distributor) {
    removeDistributor(distributor.getDistributionSpec());
  }

  /*package*/ void removeDistributor(DistributionSpecification spec) {
    MarketDataDistributor removed = _distributors.remove(spec);
    if (removed != null) {
      s_logger.info("Removed {} from {}", removed, this);      
    } else {
      s_logger.info("Removed distribution spec {} from {} (no-op)", spec, this);
    }
  }

  /*package*/ void removeAllDistributors() {
    s_logger.info("Removed {} from {}", _distributors, this);
    _distributors.clear();
  }

  /*package*/ void initialSnapshotReceived(FudgeMsg liveDataFields) {
    _liveDataSerializationLock.lock();
    try {
      _history.liveDataReceived(liveDataFields);

      for (MarketDataDistributor distributor : getDistributors()) {
        distributor.updateFieldHistory(liveDataFields);
      }
    } finally {
      _liveDataSerializationLock.unlock();
    }
  }

  /*package*/ void liveDataReceived(FudgeMsg liveDataFields) {
    _liveDataSerializationLock.lock();
    try {
      _history.liveDataReceived(liveDataFields);

      for (MarketDataDistributor distributor : getDistributors()) {
        distributor.distributeLiveData(liveDataFields);
      }
    } finally {
      _liveDataSerializationLock.unlock();
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the history.
   * 
   * @return a modifiable copy of the history, not null
   */
  public FieldHistoryStore getLiveDataHistory() {
    _liveDataSerializationLock.lock();
    try {
      return new FieldHistoryStore(_history);
    } finally {
      _liveDataSerializationLock.unlock();
    }
  }

  /**
   * Checks if the subscription is active.
   * 
   * @return true if active
   */
  public boolean isActive() {
    return getHandle() != null;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "Subscription[" + _securityUniqueId + "]";
  }
}
