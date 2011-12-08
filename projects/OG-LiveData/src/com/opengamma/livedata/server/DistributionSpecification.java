/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import org.fudgemsg.FudgeMsg;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.normalization.NormalizationRuleSet;
import com.opengamma.util.ArgumentChecker;

/**
 * Describes how market data should be distributed to clients.
 * <p>
 * This includes:
 * <ul>
 * <li>The format of the data (normalization)
 * <li>The destination of the data (JMS topic name)
 * </ul>
 */
public class DistributionSpecification {

  /**
   * The market data that is being distributed, such as AAPL equity.
   */
  private final ExternalId _marketDataId;
  /**
   * The JMS topic it's published to.
   */
  private final String _jmsTopic;
  /**
   * The format it's distributed in.
   */
  private final NormalizationRuleSet _normalizationRuleSet;

  /**
   * Creates a specification.
   * 
   * @param marketDataId  the external identifier, such as the ticker, not null
   * @param normalizationRuleSet  the rule set describing the data format, not null
   * @param jmsTopic  the JMS topic, not null
   */
  public DistributionSpecification(ExternalId marketDataId, NormalizationRuleSet normalizationRuleSet, String jmsTopic) {
    ArgumentChecker.notNull(marketDataId, "marketDataId");
    ArgumentChecker.notNull(normalizationRuleSet, "normalizationRuleSet");
    ArgumentChecker.notNull(jmsTopic, "jmsTopic");
    _marketDataId = marketDataId;
    _normalizationRuleSet = normalizationRuleSet;
    _jmsTopic = jmsTopic;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the external identifier of the data, such as the ticker.
   * 
   * @return the external identifier, not null
   */
  public ExternalId getMarketDataId() {
    return _marketDataId;
  }

  /**
   * Gets the format that the data should be sent to the client.
   * 
   * @return the data format, not null
   */
  public NormalizationRuleSet getNormalizationRuleSet() {
    return _normalizationRuleSet;
  }

  /**
   * Gets the JMS topic.
   * 
   * @return the JMS topic, not null
   */
  public String getJmsTopic() {
    return _jmsTopic;
  }

  //-------------------------------------------------------------------------
  /**
   * Converts this specification to a {@code LiveDataSpecification}.
   * 
   * @return the live data specification, not null
   */
  public LiveDataSpecification getFullyQualifiedLiveDataSpecification() {
    return new LiveDataSpecification(_normalizationRuleSet.getId(), _marketDataId);
  }

  /**
   * Checks if the specified live data specification matches this specification.
   * 
   * @param liveDataSpec  the specification to compare to, not null
   * @return true if equal
   */
  public boolean matches(LiveDataSpecification liveDataSpec) {
    return getFullyQualifiedLiveDataSpecification().equals(liveDataSpec);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the normalized message using an empty history store.
   * 
   * @param msg  the message received from underlying market data API in its native format
   * @param securityUniqueId  the data provider's unique ID of the security, not null
   * @return the normalized message, calculated assuming there is no market data history
   */
  public FudgeMsg getNormalizedMessage(FudgeMsg msg, String securityUniqueId) {
    FieldHistoryStore history = new FieldHistoryStore();
    return getNormalizedMessage(msg, securityUniqueId, history);  
  }

  /**
   * Gets a normalized message.
   * 
   * @param msg  the message received from underlying market data API in its native format.
   * @param securityUniqueId  the data provider's unique security ID 
   * @param history  the history of field values  
   * @return the normalized message, null if in the process of normalization,
   *  the message became empty and therefore should not be sent.
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

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof DistributionSpecification) {
      DistributionSpecification other = (DistributionSpecification) obj;
      return _marketDataId.equals(other._marketDataId) &&
          _normalizationRuleSet.equals(other._normalizationRuleSet) &&
          _jmsTopic.equals(other._jmsTopic);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _marketDataId.hashCode() ^ _normalizationRuleSet.hashCode();
  }

  @Override
  public String toString() {
    return _jmsTopic;
  }

}
