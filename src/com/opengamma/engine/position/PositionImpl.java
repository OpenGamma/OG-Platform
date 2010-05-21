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
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;

/**
 * A simple mutable implementation of {@code Position}.
 */
public class PositionImpl implements Position, Serializable {

  /**
   * The identifier of the whole position.
   */
  private UniqueIdentifier _identifier;
  /**
   * The amount of the position.
   */
  private final BigDecimal _quantity;
  /**
   * The identifier specifying the security.
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
  public PositionImpl(BigDecimal quantity, Identifier securityKey) {
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(securityKey, "security key");
    _quantity = quantity;
    _securityKey = new IdentifierBundle(securityKey);
    _security = null;
  }

  /**
   * Creates a position from an amount of a security identified by key.
   * @param quantity  the amount of the position, not null
   * @param securityKey  the security identifier, not null
   */
  public PositionImpl(BigDecimal quantity, IdentifierBundle securityKey) {
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(securityKey, "security key");
    _quantity = quantity;
    _securityKey = securityKey;
    _security = null;
  }

  /**
   * Creates a position from an amount of a security identified by key.
   * @param identifier the unique identifier for the position in the underlying store
   * @param quantity  the amount of the position, not null
   * @param securityKey  the security identifier, not null
   */
  public PositionImpl(UniqueIdentifier identifier, BigDecimal quantity, Identifier securityKey) {
    ArgumentChecker.notNull(identifier, "identifier");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(securityKey, "security key");
    _identifier = identifier;
    _quantity = quantity;
    _securityKey = new IdentifierBundle(securityKey);
    _security = null;
  }

  /**
   * Creates a position from an amount of a security identified by key.
   * @param identifier the unique identifier for the position in the underlying store
   * @param quantity  the amount of the position, not null
   * @param securityKey  the security identifier, not null
   */
  public PositionImpl(UniqueIdentifier identifier, BigDecimal quantity, IdentifierBundle securityKey) {
    ArgumentChecker.notNull(identifier, "identifier");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(securityKey, "security key");
    _identifier = identifier;
    _quantity = quantity;
    _securityKey = securityKey;
    _security = null;
  }

  /**
   * Creates a position from an amount of a security.
   * @param identifier the unique identifier for the position in the underlying store
   * @param quantity  the amount of the position, not null
   * @param security  the security, not null
   */
  public PositionImpl(UniqueIdentifier identifier, BigDecimal quantity, Security security) {
    ArgumentChecker.notNull(identifier, "identifier");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(security, "security");
    _identifier = identifier;
    _quantity = quantity;
    _securityKey = security.getIdentifiers();
    _security = security;
  }

  /**
   * Creates a position from an amount of a security.
   * @param identifier the unique identifier for the position in the underlying store
   * @param quantity  the amount of the position, not null
   * @param securityKey  the security identifier, not null
   * @param security  the security, not null
   */
  public PositionImpl(UniqueIdentifier identifier, BigDecimal quantity, IdentifierBundle securityKey, Security security) {
    ArgumentChecker.notNull(identifier, "identifier");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(securityKey, "security key");
    ArgumentChecker.notNull(security, "security");
    _identifier = identifier;
    _quantity = quantity;
    _securityKey = securityKey;
    _security = security;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the unique identifier of the position.
   * @return the identifier, not null
   */
  @Override
  public UniqueIdentifier getUniqueIdentifier() {
    return _identifier;
  }

  /**
   * Sets the unique identifier of the position.
   * @param identifier  the new identifier, not null
   */
  public void setUniqueIdentifier(UniqueIdentifier identifier) {
    ArgumentChecker.notNull(identifier, "identifier");
    _identifier = identifier;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the amount of the position held in terms of the security.
   * @return the amount of the position
   */
  @Override
  public BigDecimal getQuantity() {
    return _quantity;
  }

  /**
   * Gets a key to the security being held.
   * <p>
   * This allows the security to be referenced without actually loading the security itself.
   * @return the security key
   */
  @Override
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
  @Override
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
    if (obj instanceof PositionImpl) {
      PositionImpl other = (PositionImpl) obj;
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
      .append(getUniqueIdentifier())
      .append(", ")
      .append(getQuantity())
      .append(' ')
      .append(getSecurity() != null ? getSecurity() : getSecurityKey())
      .append(']')
      .toString();
  }

}
