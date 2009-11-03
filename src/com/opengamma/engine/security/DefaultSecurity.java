/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeMsg;

import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.id.DomainSpecificIdentifier;

/**
 * A concrete, JavaBean-based implementation of {@link Security}. 
 *
 * @author kirk
 */
public class DefaultSecurity implements Security, Serializable {
  private Collection<DomainSpecificIdentifier> _identifiers;
  private String _securityType;
  private AnalyticValueDefinition<FudgeMsg> _marketDataDefinition;
  private String _identityKey;
  
  public DefaultSecurity() {
  }
  
  public DefaultSecurity(String securityType, Collection<? extends DomainSpecificIdentifier> identifiers) {
    setSecurityType(securityType);
    setIdentifiers(identifiers);
  }

  @Override
  public Collection<DomainSpecificIdentifier> getIdentifiers() {
    return _identifiers;
  }

  /**
   * This will create a <em>copy</em> of the provided collection.
   * @param identifiers the identifiers to set
   */
  public void setIdentifiers(Collection<? extends DomainSpecificIdentifier> identifiers) {
    _identifiers = new ArrayList<DomainSpecificIdentifier>(identifiers);
  }
  
  @Override
  public String getIdentityKey() {
    return _identityKey;
  }

  /**
   * @param identityKey the identityKey to set
   */
  public void setIdentityKey(String identityKey) {
    _identityKey = identityKey;
  }

  @Override
  public String getSecurityType() {
    return _securityType;
  }

  /**
   * @param securityType the securityType to set
   */
  public void setSecurityType(String securityType) {
    _securityType = securityType;
  }

  @Override
  public AnalyticValueDefinition<FudgeMsg> getMarketDataDefinition() {
    return _marketDataDefinition;
  }

  /**
   * @param marketDataDefinition the marketDataDefinition to set
   */
  public void setMarketDataDefinition(
      AnalyticValueDefinition<FudgeMsg> marketDataDefinition) {
    _marketDataDefinition = marketDataDefinition;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
