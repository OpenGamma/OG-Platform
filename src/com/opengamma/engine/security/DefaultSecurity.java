/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * A concrete, JavaBean-based implementation of {@link Security}. 
 *
 * @author kirk
 */
public class DefaultSecurity implements Security, Serializable {
  private IdentifierBundle _identifiers;
  private String _securityType;
  //private AnalyticValueDefinition<FudgeMsg> _marketDataDefinition;
  private Identifier _identityKey;
  private String _displayName;
  
  public DefaultSecurity() {
    _identifiers = new IdentifierBundle();
  }
  
  public DefaultSecurity(String securityType, Collection<? extends Identifier> identifiers) {
    setSecurityType(securityType);
    setIdentifiers(identifiers);
  }

  @Override
  public Collection<Identifier> getIdentifiers() {
    return _identifiers.getIdentifiers();
  }

  /**
   * This will create a <em>copy</em> of the provided collection.
   * @param identifiers the identifiers to set
   */
  public void setIdentifiers(Collection<? extends Identifier> identifiers) {
    setIdentifiers(new IdentifierBundle(identifiers));
  }
  
  public void setIdentifiers(IdentifierBundle identifierBundle) {
    _identifiers = identifierBundle;
  }
  
  @Override
  public Identifier getIdentityKey() {
    return _identityKey;
  }

  public void setIdentityKey(String identityKey) {
    _identityKey = new Identifier(SECURITY_IDENTITY_KEY_DOMAIN, identityKey);
  }
  
  public void setIdentityKey(Identifier identityKey) {
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
  public boolean equals (final Object o) {
    return EqualsBuilder.reflectionEquals(this, o);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
  
  public void setDisplayName (final String displayName) {
    _displayName = displayName;
  }
  
  /**
   * Override this to supply a "default" display name if one hasn't been explicitly set. The default
   * here constructs one from the identity key or identifiers.
   */
  protected String getDefaultDisplayName () {
    Identifier identifier = getIdentityKey ();
    if (identifier == null) {
      final Collection<Identifier> identifiers = getIdentifiers ();
      if ((identifiers == null) || identifiers.isEmpty ()) {
        return getClass ().getName ();
      }
      identifier = identifiers.iterator ().next ();
    }
    return identifier.getScheme ().getName () + "/" + identifier.getValue ();
  }
  
  @Override
  public String getDisplayName () {
    if (_displayName != null) {
      return _displayName;
    } else {
      return getDefaultDisplayName ();
    }
  }

}
