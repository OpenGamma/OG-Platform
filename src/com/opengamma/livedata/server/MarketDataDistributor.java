/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.Collection;
import java.util.Collections;

import org.fudgemsg.FudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Distributes market data to clients and keeps a history of what has been distributed.
 *
 * @author pietari
 */
public class MarketDataDistributor {
  
  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataDistributor.class);
  
  /**
   * What data should be distributed, how and where.
   */
  private final DistributionSpecification _distributionSpec;
  
  /**
   * Which subscription this distributor belongs to.
   */
  private final Subscription _subscription;
  
  /** These listener(s) actually publish the data */ 
  private final Collection<MarketDataSender> _marketDataSenders;
  
  /**
   * The last (normalized) message that was sent to clients.
   */
  private volatile FudgeFieldContainer _lastKnownValue = null;
  
  /** 
   * A history store to be used by the FieldHistoryUpdater normalization rule.
   * What fields exactly are stored in this history is dependent on 
   * that rule.
   */
  private final FieldHistoryStore _history = new FieldHistoryStore();
  
  
  /**
   * @param marketDataSenders It is recommended that a thread-safe collection 
   * with iterators that do not throw <code>ConcurrentModificationException</code> is used,
   * unless you are sure that this <code>DistributionSpecification</code> will 
   * only be used within a single thread. No copy of the collection is made,
   * so any subsequent changes to the collection will be reflected in this object.
   */
  public MarketDataDistributor(DistributionSpecification distributionSpec,
      Subscription subscription,
      Collection<MarketDataSender> marketDataSenders) {
    
    ArgumentChecker.notNull(distributionSpec, "Distribution spec");
    ArgumentChecker.notNull(subscription, "Subscription");
    ArgumentChecker.notNull(marketDataSenders, "Market data senders");
    
    _distributionSpec = distributionSpec;
    _subscription = subscription;
    _marketDataSenders = marketDataSenders;
  }
  
  public DistributionSpecification getDistributionSpec() {
    return _distributionSpec;
  }

  public FudgeFieldContainer getLastKnownValue() {
    return _lastKnownValue;
  }

  private void setLastKnownValue(FudgeFieldContainer lastKnownValue) {
    _lastKnownValue = lastKnownValue;
  }
  
  public Collection<MarketDataSender> getMarketDataSenders() {
    return Collections.unmodifiableCollection(_marketDataSenders);
  }
  
  /**
   * @return Could be null if this distribution spec is a "temporary" distribution specification,
   * created for a snapshot request.
   */
  public Subscription getSubscription() {
    return _subscription;
  }
  
  /**
   * @param msg Message received from underlying market data API in its native format.
   * @return The normalized message. Null if in the process of normalization,
   * the message became empty and therefore should not be sent.
   */
  private FudgeFieldContainer normalize(FudgeFieldContainer msg) {
    FudgeFieldContainer normalizedMsg = _distributionSpec.getNormalizedMessage(msg, _history);
    if (normalizedMsg != null) {
      setLastKnownValue(normalizedMsg);
    }
    return normalizedMsg;
  }
  
  /**
   * Updates field history without sending any market data to field receivers. 
   * 
   * @param liveDataFields Unnormalized market data from underlying market data API.
   */
  public void updateFieldHistory(FudgeFieldContainer msg) {
    normalize(msg);    
  }
  
  /**
   * Sends normalized market data to field receivers. 
   * 
   * @param liveDataFields Unnormalized market data from underlying market data API.
   */
  public void distributeLiveData(FudgeFieldContainer liveDataFields) {
    FudgeFieldContainer normalizedMsg;
    try {
      normalizedMsg = normalize(liveDataFields);
    } catch (RuntimeException e) {
      s_logger.error("Normalizing " + liveDataFields + " to " + this + " failed.", e);
      return;
    }
    
    if (normalizedMsg != null) {
      setLastKnownValue(normalizedMsg);
      
      if (getSubscription().isActive()) {
        for (MarketDataSender sender : _marketDataSenders) {
          try {
            sender.sendMarketData(_distributionSpec, normalizedMsg);
          } catch (RuntimeException e) {
            s_logger.error("MarketDataSender " + sender + " failed", e);
          }
        }
      }
    } else {
      s_logger.debug("Not sending Live Data update (empty message).");
    }
  }
  

  
}
