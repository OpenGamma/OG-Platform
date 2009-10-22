/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.io.Serializable;
import java.util.Collection;

import org.fudgemsg.FudgeMsg;

import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.DomainSpecificIdentifiersImpl;

/**
 * A concrete, JavaBean-based implementation of {@link Security}. 
 *
 * @author kirk
 */
public class DefaultSecurity implements Security, Serializable {
  private DomainSpecificIdentifiersImpl _identifiers;
  private String _securityType;
  private AnalyticValueDefinition<FudgeMsg> _marketDataDefinition;
  
  public DefaultSecurity() {
  }
  
  public DefaultSecurity(String securityType, Collection<? extends DomainSpecificIdentifier> identifiers) {
    setSecurityType(securityType);
    setIdentifiers(identifiers);
  }

  @Override
  public Collection<DomainSpecificIdentifier> getIdentifiers() {
    return _identifiers.getIdentifiers();
  }

  /**
   * This will create a <em>copy</em> of the provided collection.
   * @param identifiers the identifiers to set
   */
  public void setIdentifiers(Collection<? extends DomainSpecificIdentifier> identifiers) {
    _identifiers = new DomainSpecificIdentifiersImpl(identifiers);
  }
  
  // REVIEW jim 23-Sep-2009 -- maybe this should be separate from the identifiers
  // REVIEW kirk 2009-10-16 -- Almost certainly.
  @Override
  public SecurityKey getIdentityKey() {
    return new SecurityKeyImpl(_identifiers.getIdentifiers());
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
}
