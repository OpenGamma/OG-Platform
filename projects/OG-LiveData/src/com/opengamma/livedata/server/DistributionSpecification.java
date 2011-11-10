/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.fudgemsg.FudgeMsg;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.normalization.NormalizationRuleSet;
import com.opengamma.util.ArgumentChecker;

/**
 * Describes how market data should be distributed to clients. This includes:
 * <ul>
 * <li>The format of the data (normalization)
 * <li>The destination of the data (JMS topic name)
 * </ul>
 */
public class DistributionSpecification {

  /** What market data is being distributed (e.g., AAPL stock) */
  private final ExternalId _marketDataUniqueId;

  /** Topic it's published to */
  private final String _jmsTopic;

  /** The format it's distributed in */
  private final NormalizationRuleSet _normalizationRuleSet;

  public DistributionSpecification(ExternalId marketDataUniqueId, 
      NormalizationRuleSet normalizationRuleSet,
      String jmsTopic) {
    ArgumentChecker.notNull(marketDataUniqueId, "Unique identifier for the market data ticker this distribution spec relates to");
    ArgumentChecker.notNull(normalizationRuleSet, "Normalization rules to apply before sending data to the JMS topic");
    ArgumentChecker.notNull(jmsTopic, "JMS topic name");
    _marketDataUniqueId = marketDataUniqueId;
    _normalizationRuleSet = normalizationRuleSet;
    _jmsTopic = jmsTopic;
  }

  public ExternalId getMarketDataId() {
    return _marketDataUniqueId;
  }

  public LiveDataSpecification getFullyQualifiedLiveDataSpecification() {
    return new LiveDataSpecification(
        _normalizationRuleSet.getId(),
        _marketDataUniqueId);
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
  
  /**
   * Gets a normalized message.
   * 
   * @param msg  message received from underlying market data API in its native format.
   * @param securityUniqueId  data provider's unique security ID 
   * @param history  history of field values  
   * @return the normalized message. Null if in the process of normalization,
   * the message became empty and therefore should not be sent.
   */
  public FudgeMsg getNormalizedMessage(FudgeMsg msg, String securityUniqueId, FieldHistoryStore history) {
    FudgeMsg normalizedMsg = _normalizationRuleSet.getNormalizedMessage(msg, securityUniqueId, history);
    
    if (normalizedMsg == null) {
      return null;
    }
    
    if (normalizedMsg.getNumFields() == 0) {
      return null;
    }
    return normalizedMsg;
  }
  
  /**
   * @param msg Message received from underlying market data API in its native format.
   * @param securityUniqueId  the data provider's unique ID of the security, not null
   * @return A normalized message, calculated assuming there is no market data history.
   */
  public FudgeMsg getNormalizedMessage(FudgeMsg msg, String securityUniqueId) {
    FieldHistoryStore history = new FieldHistoryStore();
    return getNormalizedMessage(msg, securityUniqueId, history);  
  }
  
  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }  

}
