/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.DomainSpecificIdentifiers;

/**
 * A concrete, JavaBean-based implementation of {@link Security}. 
 *
 * @author kirk
 */
public class DefaultSecurity implements Security, Serializable {
  private DomainSpecificIdentifiers _identifiers;
  private String _securityType;
  //private AnalyticValueDefinition<FudgeMsg> _marketDataDefinition;
  private DomainSpecificIdentifier _identityKey;
  private String _displayName;
  
  public DefaultSecurity() {
    _identifiers = new DomainSpecificIdentifiers();
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
    _identifiers = new DomainSpecificIdentifiers(identifiers);
  }
  
  @Override
  public DomainSpecificIdentifier getIdentityKey() {
    return _identityKey;
  }

  public void setIdentityKey(String identityKey) {
    _identityKey = new DomainSpecificIdentifier(SECURITY_IDENTITY_KEY_DOMAIN, identityKey);
  }
  
  public void setIdentityKey(DomainSpecificIdentifier identityKey) {
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
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
  
  public void setDisplayName (final String displayName) {
    _displayName = displayName;
  }
  
  @Override
  public String getDisplayName () {
    if (_displayName != null) {
      return _displayName;
    } else {
      DomainSpecificIdentifier identifier = getIdentityKey ();
      if (identifier == null) {
        final Collection<DomainSpecificIdentifier> identifiers = getIdentifiers ();
        if ((identifiers == null) || identifiers.isEmpty ()) {
          return getClass ().getName ();
        }
        identifier = identifiers.iterator ().next ();
      }
      return identifier.getDomain ().getDomainName () + "/" + identifier.getValue ();
    }
  }

}
