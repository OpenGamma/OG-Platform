/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import java.io.Serializable;

import com.google.common.base.Objects;
import com.opengamma.core.security.Security;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueId;

/**
 * Mock Security.
 */
public class MockSecurity implements Security, MutableUniqueIdentifiable, Serializable {

  private static final long serialVersionUID = 1L;

  private UniqueId _uniqueId;
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

  public MockSecurity(UniqueId uniqueId, String name, String securityType, IdentifierBundle identifiers) {
    _uniqueId = uniqueId;
    _name = name;
    _securityType = securityType;
    _identifiers = identifiers;
  }

  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  public void setUniqueId(UniqueId uniqueId) {
    _uniqueId = uniqueId;
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
      return Objects.equal(_uniqueId, other._uniqueId) &&
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
    result = prime * result + ((_uniqueId == null) ? 0 : _uniqueId.hashCode());
    return result;
  }

}
