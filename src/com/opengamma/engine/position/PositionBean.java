/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;

import com.opengamma.engine.security.Security;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;

/**
 * A simple mutable implementation of {@code Position}.
 */
public class PositionBean implements Position, Serializable {

  /**
   * The identity key of the whole position.
   */
  private Identifier _identityKey;
  /**
   * The amount of the position.
   */
  private final BigDecimal _quantity;
  /**
   * The identity key specifying the security.
   */
  private final IdentifierBundle _securityKey;
  /**
   * The security.
   */
  private Security _security;

  /**
   * Creates a position from an amount of a security identified by key.
   * @param quantity  the amount of the position, not null
   * @param securityKey  the security identifier, not null
   */
  public PositionBean(BigDecimal quantity, Identifier securityKey) {
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(securityKey, "identity key");
    _quantity = quantity;
    _securityKey = new IdentifierBundle(securityKey);
    _security = null;
  }

  /**
   * Creates a position from an amount of a security identified by key.
   * @param quantity  the amount of the position, not null
   * @param securityKey  the security identifier, not null
   */
  public PositionBean(BigDecimal quantity, IdentifierBundle securityKey) {
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(securityKey, "identity key");
    _quantity = quantity;
    _securityKey = securityKey;
    _security = null;
  }

  /**
   * Creates a position from an amount of a security identified by key.
   * @param quantity  the amount of the position, not null
   * @param securityKey  the security identifier, not null
   * @param security  the security, not null
   */
  public PositionBean(BigDecimal quantity, IdentifierBundle securityKey, Security security) {
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(securityKey, "identity key");
    ArgumentChecker.notNull(security, "security");
    _quantity = quantity;
    _securityKey = securityKey;
    _security = security;
  }

  /**
   * Creates a position from an amount of a security identified by key.
   * @param quantity  the amount of the position, not null
   * @param security  the security, not null
   */
  public PositionBean(BigDecimal quantity, Security security) {
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(security, "security");
    _quantity = quantity;
    _securityKey = security.getIdentifiers() != null ? new IdentifierBundle(security.getIdentifiers()) : new IdentifierBundle();
    _security = security;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the identity key of the position.
   * @return the identity key, null if not uniquely identified
   */
  @Override
  public Identifier getIdentityKey() {
    return _identityKey;
  }

  /**
   * Sets the identity key of the node.
   * @param identityKey  the new identity key, not null
   */
  public void setIdentityKey(Identifier identityKey) {
    ArgumentChecker.notNull(identityKey, "Identity key");
    if (identityKey.isNotScheme(POSITION_IDENTITY_KEY_SCHEME)) {
      throw new IllegalArgumentException("Wrong scheme specified: " + identityKey.getScheme());
    }
    _identityKey = identityKey; 
  }

  /**
   * Sets the identity key identifier of the node.
   * @param identityKey  the new identity key identifier, not null
   */
  public void setIdentityKey(String identityKey) {
    _identityKey = new Identifier(POSITION_IDENTITY_KEY_SCHEME, identityKey);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the amount of the position held in terms of the security.
   * @return the amount of the position
   */
  public BigDecimal getQuantity() {
    return _quantity;
  }

  /**
   * Gets a key to the security being held.
   * <p>
   * This allows the security to be referenced without actually loading the security itself.
   * @return the security key
   */
  public IdentifierBundle getSecurityKey() {
    return _securityKey;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security being held, returning {@code null} if it has not been loaded.
   * <p>
   * This method is guaranteed to return a security within an analytic function.
   * @return the security
   */
  public Security getSecurity() {
    return _security;
  }

  /**
   * Sets the security being held.
   * @param security  the security, may be null
   */
  public void setSecurity(Security security) {
    _security = security;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof PositionBean) {
      PositionBean other = (PositionBean) obj;
      return CompareUtils.compareWithNull(_quantity, other._quantity) == 0 &&
              ObjectUtils.equals(_securityKey, other._securityKey) &&
              ObjectUtils.equals(_security, other._security);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hashCode = 65;
    hashCode += _quantity.hashCode();
    hashCode <<= 5;
    hashCode += _securityKey.hashCode();
    if (getSecurity() != null) {
      hashCode <<= 5;
      hashCode += _security.hashCode();
    }
    return hashCode;
  }

  @Override
  public String toString() {
    return new StrBuilder()
      .append("Position[")
      .append(_quantity)
      .append(' ')
      .append(_security != null ? _security : _securityKey)
      .toString();
  }

}
