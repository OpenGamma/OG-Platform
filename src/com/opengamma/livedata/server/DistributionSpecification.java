/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import org.fudgemsg.FudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.DomainSpecificIdentifiers;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.normalization.NormalizationRuleSet;
import com.opengamma.util.ArgumentChecker;

/**
 * How market data should be distributed to clients. This includes:
 * <ul>
 * <li>The format of the data (normalization)
 * <li>The destination of the data (JMS topic name)
 * </ul>
 *
 * @author pietari
 */
public class DistributionSpecification implements Serializable {
  
  private static final Logger s_logger = LoggerFactory.getLogger(DistributionSpecification.class);
  
  // final, equals()/hashCode()-eligible fields
  
  /** What market data is being distributed (e.g., AAPL stock) */
  private final DomainSpecificIdentifiers _identifiers;
  
  /** Topic it's published to */
  private final String _jmsTopic;
  
  /** The format it's distributed in */
  private final NormalizationRuleSet _normalizationRuleSet;
  
  // non-final fields - do not use in equals()/hashCode()
  
  /**
   * Which subscription this distribution specification belongs to.
   * Note - this could be null if this distribution spec is a "temporary" distribution specification,
   * created for a snapshot request.
   */
  private Subscription _subscription = null;
  
  /** These listener(s) actually publish the data */ 
  private Collection<MarketDataReceiver> _fieldReceivers;
  
  /**
   * The last (normalized) message that was sent to clients.
   */
  private FudgeFieldContainer _lastKnownValue = null;
  
  public DistributionSpecification(DomainSpecificIdentifiers identifiers, 
      NormalizationRuleSet normalizationRuleSet,
      String jmsTopic) {
    ArgumentChecker.checkNotNull(identifiers, "Identifier(s) for the market data ticker this distribution spec relates to");
    ArgumentChecker.checkNotNull(normalizationRuleSet, "Normalization rules to apply before sending data to the JMS topic");
    ArgumentChecker.checkNotNull(jmsTopic, "JMS topic name");
    _identifiers = identifiers;
    _normalizationRuleSet = normalizationRuleSet;
    _jmsTopic = jmsTopic;
    setFieldReceivers(Collections.<MarketDataReceiver>emptyList());
  }
  
  public LiveDataSpecification getFullyQualifiedLiveDataSpecification() {
    return new LiveDataSpecification(
        _normalizationRuleSet.getId(),
        _identifiers);
  }
  
  public boolean matches(LiveDataSpecification liveDataSpec) {
    return getFullyQualifiedLiveDataSpecification().equals(liveDataSpec);
  }

  public NormalizationRuleSet getNormalizationRuleSet() {
    return _normalizationRuleSet;
  }
  
  public String getJmsTopic() {
    return _jmsTopic;
  }
  
  public String toString() {
    return _jmsTopic;
  }
  
  public synchronized FudgeFieldContainer getLastKnownValue() {
    return _lastKnownValue;
  }

  private synchronized void setLastKnownValue(FudgeFieldContainer lastKnownValue) {
    _lastKnownValue = lastKnownValue;
  }
  
  /**
   * @param fieldReceivers It is recommended that a thread-safe collection 
   * with iterators that do not throw <code>ConcurrentModificationException</code> is used,
   * unless you are sure that this <code>DistributionSpecification</code> will 
   * only be used within a single thread. No copy of the collection is made,
   * so any subsequent changes to the collection will be reflected in this object.
   */
  void setFieldReceivers(Collection<MarketDataReceiver> fieldReceivers) {
    _fieldReceivers = fieldReceivers;
  }

  public Collection<MarketDataReceiver> getFieldReceivers() {
    return Collections.unmodifiableCollection(_fieldReceivers);
  }
  
  /**
   * @return Could be null if this distribution spec is a "temporary" distribution specification,
   * created for a snapshot request.
   */
  public Subscription getSubscription() {
    return _subscription;
  }

  void setSubscription(Subscription subscription) {
    ArgumentChecker.checkNotNull(subscription, "Subscription");
    if (_subscription != null) {
      throw new IllegalStateException("Subscription already set");
    }
    _subscription = subscription;
  }
  
  /**
   * @param msg Message received from underlying market data API in its native format.
   * @return The normalized message. Null if in the process of normalization,
   * the message became empty and therefore should not be sent.
   */
  public FudgeFieldContainer getNormalizedMessage(FudgeFieldContainer msg) {
    FieldHistoryStore history;
    if (getSubscription() != null) {
      history = getSubscription().getLiveDataHistory();
    } else {
      // no history available.
      history = new FieldHistoryStore(msg);
    }
    
    FudgeFieldContainer normalizedMsg = _normalizationRuleSet.getNormalizedMessage(msg,
        history);
    
    if (normalizedMsg.getAllFields().size() == 0) {
      return null;
    }
    return normalizedMsg;
  }
  
  /**
   * Sends normalized market data to field receivers. 
   * 
   * @param liveDataFields Unnormalized market data from underlying market data API.
   * @see #setFieldReceivers
   */
  public void liveDataReceived(FudgeFieldContainer liveDataFields) {
    FudgeFieldContainer normalizedMsg;
    try {
      normalizedMsg = getNormalizedMessage(liveDataFields);
    } catch (RuntimeException e) {
      s_logger.error("Normalizing " + liveDataFields + " to " + this + " failed.", e);
      return;
    }
    setLastKnownValue(normalizedMsg);
    
    if (normalizedMsg != null) {
      for (MarketDataReceiver receiver : _fieldReceivers) {
        try {
          receiver.marketDataReceived(this, normalizedMsg);
        } catch (RuntimeException e) {
          s_logger.error("MarketDataReceiver " + receiver + " failed", e);
        }
      }
    } else {
      s_logger.debug("Not sending Live Data update (empty message).");
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((_identifiers == null) ? 0 : _identifiers.hashCode());
    result = prime * result + ((_jmsTopic == null) ? 0 : _jmsTopic.hashCode());
    result = prime
        * result
        + ((_normalizationRuleSet == null) ? 0 : _normalizationRuleSet
            .hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DistributionSpecification other = (DistributionSpecification) obj;
    if (_identifiers == null) {
      if (other._identifiers != null)
        return false;
    } else if (!_identifiers.equals(other._identifiers))
      return false;
    if (_jmsTopic == null) {
      if (other._jmsTopic != null)
        return false;
    } else if (!_jmsTopic.equals(other._jmsTopic))
      return false;
    if (_normalizationRuleSet == null) {
      if (other._normalizationRuleSet != null)
        return false;
    } else if (!_normalizationRuleSet.equals(other._normalizationRuleSet))
      return false;
    return true;
  }

}
