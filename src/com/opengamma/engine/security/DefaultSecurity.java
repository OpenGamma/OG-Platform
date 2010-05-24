/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple mutable implementation of {@code Security}.
 */
public class DefaultSecurity implements Security, MutableUniqueIdentifiable, Serializable {

  /**
   * The unique identifier.
   */
  private UniqueIdentifier _identifier;
  /**
   * The name for display.
   */
  private String _name;
  /**
   * The type of security.
   */
  private String _securityType;
  /**
   * The bundle of underlying identifiers.
   */
  private IdentifierBundle _identifiers;

  /**
   * Creates a new security.
   */
  public DefaultSecurity() {
    _name = "";
    _identifiers = new IdentifierBundle();
    _securityType = "";
  }

  /**
   * Creates a new security.
   * @param securityType  the type, not null
   */
  public DefaultSecurity(String securityType) {
    _name = "";
    _identifiers = new IdentifierBundle();
    _securityType = securityType;
  }

  /**
   * Creates a new security.
   * @param securityType  the type, not null
   * @param identifierBundle  the identifiers, not null
   */
  public DefaultSecurity(String securityType, IdentifierBundle identifierBundle) {
    ArgumentChecker.notNull(securityType, "security type");
    ArgumentChecker.notNull(identifierBundle, "identifier bundle");
    _name = "";
    _securityType = securityType;
    _identifiers = identifierBundle;
  }

  /**
   * Creates a new security.
   * @param identifier the unique identifier for the security in the underlying store
   * @param securityType  the type, not null
   * @param identifierBundle  the identifiers, not null
   */
  public DefaultSecurity(UniqueIdentifier identifier, String securityType, IdentifierBundle identifierBundle) {
    ArgumentChecker.notNull(identifier, "identifier");
    ArgumentChecker.notNull(securityType, "security type");
    ArgumentChecker.notNull(identifierBundle, "identifier bundle");
    _identifier = identifier;
    _name = "";
    _securityType = securityType;
    _identifiers = identifierBundle;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the unique identifier of the security.
   * @return the identifier, not null
   */
  @Override
  public UniqueIdentifier getUniqueIdentifier() {
    return _identifier;
  }

  /**
   * Sets the unique identifier of the security.
   * @param identifier  the new identifier, not null
   */
  public void setUniqueIdentifier(UniqueIdentifier identifier) {
    ArgumentChecker.notNull(identifier, "identifier");
    _identifier = identifier;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name of the node intended for display purposes.
   * @return the name, not null
   */
  @Override
  public String getName() {
    String name = _name;  // assign for thread-safety
    if (name != null) {
      return name;
    } else {
      return buildDefaultDisplayName();
    }
  }

  /**
   * Dynamically determines a 'default' display name if one hasn't been explicitly set.
   * This implementation constructs one from the identity key or identifiers.
   * 
   * @return a default display name
   */
  protected String buildDefaultDisplayName() {
    final UniqueIdentifier identifier = getUniqueIdentifier();  // assign for thread-safety
    if (identifier != null) {
      return identifier.toString();
    }
    final IdentifierBundle bundle = getIdentifiers();  // assign for thread-safety
    final Identifier first = (bundle.size() == 0 ? null : bundle.getIdentifiers().iterator().next());
    return ObjectUtils.toString(first);
  }

  /**
   * Sets the name of the node intended for display purposes.
   * @param name  the name, not null
   */
  public void setName(String name) {
    ArgumentChecker.notNull(name, "name");
    _name = name;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security type.
   * @return the security type, not null
   */
  @Override
  public String getSecurityType() {
    return _securityType;
  }

  /**
   * Sets the security type.
   * @param securityType  the security type, not null
   */
  public void setSecurityType(String securityType) {
    ArgumentChecker.notNull(securityType, "security type");
    _securityType = securityType;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the bundle of identifiers that define the security.
   * Changing this bundle will change the security.
   * @return the bundle of identifiers, not null
   */
  @Override
  public IdentifierBundle getIdentifiers() {
    return _identifiers;
  }

  /**
   * Sets the bundle of identifiers that define the security.
   * The specified bundle will be directly stored, thus later changes will affect the security.
   * @param identifierBundle  the bundle of identifiers, not null
   */
  public void setIdentifiers(IdentifierBundle identifierBundle) {
    ArgumentChecker.notNull(identifierBundle, "identifier bundle");
    _identifiers = identifierBundle;
  }

  /**
   * Adds to the bundle of identifiers that define the security.
   * @param identifier  the identifier to add, not null
   */
  public void addIdentifier(Identifier identifier) {
    ArgumentChecker.notNull(identifier, "identifier");
    _identifiers = _identifiers.withIdentifier(identifier);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof DefaultSecurity) {
      final DefaultSecurity other = (DefaultSecurity) obj;
      return ObjectUtils.equals(_identifiers, other._identifiers) &&
              ObjectUtils.equals(_securityType, other._securityType);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _identifiers.hashCode() ^ _securityType.hashCode();
  }

  @Override
  public String toString() {
    return new StrBuilder()
      .append("Security[")
      .append(getUniqueIdentifier())
      .append(", ")
      .append(getSecurityType())
      .append(", ")
      .append(getIdentifiers())
      .append(']')
      .toString();
  }

}
