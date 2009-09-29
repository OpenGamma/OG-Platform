/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import com.opengamma.id.DomainSpecificIdentifier;

/**
 * A concrete, JavaBean-based implementation of {@link Security}. 
 *
 * @author kirk
 */
public class DefaultSecurity implements Security, Serializable {
  private Collection<DomainSpecificIdentifier> _identifiers;
  private String _securityType;

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
  
  // REVIEW jim 23-Sep-2009 -- maybe this should be separate from the identifiers
  @Override
  public SecurityKey getIdentityKey() {
    return new SecurityKeyImpl(_identifiers);
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




}
