/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security;

import java.io.Serializable;

import com.google.common.base.Objects;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;

/**
 * Mock Security.
 */
public class MockSecurity implements Security, MutableUniqueIdentifiable, Serializable {

  private static final long serialVersionUID = 1L;

  private UniqueIdentifier _uniqueIdentifier;
  private String _securityType;
  private String _name;
  private IdentifierBundle _identifiers;

  /**
   * Creates an instance.
   * @param securityType  the security type
   */
  public MockSecurity(String securityType) {
    _securityType = securityType;
    _identifiers = IdentifierBundle.EMPTY;
  }

  public UniqueIdentifier getUniqueIdentifier() {
    return _uniqueIdentifier;
  }

  public void setUniqueIdentifier(UniqueIdentifier uniqueIdentifier) {
    _uniqueIdentifier = uniqueIdentifier;
  }

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public String getSecurityType() {
    return _securityType;
  }

  public void setSecurityType(String securityType) {
    _securityType = securityType;
  }

  public IdentifierBundle getIdentifiers() {
    return _identifiers;
  }

  public void setIdentifiers(IdentifierBundle identifiers) {
    _identifiers = identifiers;
  }

  public void addIdentifier(final Identifier identifier) {
    setIdentifiers(getIdentifiers().withIdentifier(identifier));
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof MockSecurity) {
      MockSecurity other = (MockSecurity) obj;
      return Objects.equal(_uniqueIdentifier, other._uniqueIdentifier) &&
        Objects.equal(_securityType, other._securityType) &&
        Objects.equal(_name, other._name) &&
        Objects.equal(_identifiers, other._identifiers);
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_identifiers == null) ? 0 : _identifiers.hashCode());
    result = prime * result + ((_name == null) ? 0 : _name.hashCode());
    result = prime * result + ((_securityType == null) ? 0 : _securityType.hashCode());
    result = prime * result + ((_uniqueIdentifier == null) ? 0 : _uniqueIdentifier.hashCode());
    return result;
  }

}
