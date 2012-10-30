/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.normalization.NormalizationRuleSet;

/**
 * 
 */
public class JmsTopicNameResolveRequest {
  
  /**
   * what market data the server is going to publish
   */
  private final ExternalId _marketDataUniqueId;
  
  /**
   * what normalization rule will be applied to the raw market data
   */
  private final NormalizationRuleSet _normalizationRule;
  
  public JmsTopicNameResolveRequest(
      ExternalId marketDataUniqueId,
      NormalizationRuleSet normalizationRule) {
    _marketDataUniqueId = marketDataUniqueId;
    _normalizationRule = normalizationRule;
    
  }

  /**
   * @return what market data the server is going to publish
   */
  public ExternalId getMarketDataUniqueId() {
    return _marketDataUniqueId;
  }

  /**
   * @return what normalization rule will be applied to the raw market data
   */
  public NormalizationRuleSet getNormalizationRule() {
    return _normalizationRule;
  }
  
  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this); 
  }
  
}
