/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;

import com.google.common.collect.Sets;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;

/**
 * A simple mutable implementation of {@code Position}.
 */
public class PositionImpl implements Position, MutableUniqueIdentifiable, Serializable {

  /** Serialization. */
  private static final long serialVersionUID = 1L;

  /**
   * The identifier of the whole position.
   */
  private UniqueIdentifier _identifier;
  /**
   * The identifier of the parent node.
   */
  private UniqueIdentifier _parentNode;
  /**
   * The amount of the position.
   */
  private BigDecimal _quantity;
  /**
   * The identifier specifying the security.
   */
  private IdentifierBundle _securityKey;
  /**
   * The security.
   */
  private Security _security;
  /**
   * The collections of Trades that make up the position
   */
  private Set<Trade> _trades = Sets.newHashSet();

  /**
   * Construct a mutable position copying data from another, possibly immutable, {@link Position} implementation.
   * 
   * @param copyFrom instance to copy fields from, not null
   */
  public PositionImpl(final Position copyFrom) {
    ArgumentChecker.notNull(copyFrom, "copyFrom");
    _identifier = copyFrom.getUniqueIdentifier();
    _parentNode = copyFrom.getPortfolioNode();
    _quantity = copyFrom.getQuantity();
    _securityKey = copyFrom.getSecurityKey();
    _security = copyFrom.getSecurity();
  }

  /**
   * Creates a position from an amount of a security identified by key.
   * @param quantity  the amount of the position, not null
   * @param securityKey  the security identifier, not null
   */
  public PositionImpl(BigDecimal quantity, Identifier securityKey) {
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(securityKey, "security key");
    _quantity = quantity;
    _securityKey = IdentifierBundle.of(securityKey);
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
    _securityKey = IdentifierBundle.of(securityKey);
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

  @Override
  public UniqueIdentifier getPortfolioNode() {
    return _parentNode;
  }

  public void setPortfolioNode(final UniqueIdentifier parentNode) {
    _parentNode = parentNode;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the amount of the position held in terms of the security.
   * @return the amount of the position, not null
   */
  @Override
  public BigDecimal getQuantity() {
    return _quantity;
  }

  /**
   * Sets the amount of the position held in terms of the security.
   * @param quantity  the amount of the position, not null
   */
  public void setQuantity(BigDecimal quantity) {
    ArgumentChecker.notNull(quantity, "quantity");
    _quantity = quantity;
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

  /**
   * Sets the key to the security being held.
   * @param securityKey  the security key, may be null
   */
  public void setSecurityKey(IdentifierBundle securityKey) {
    _securityKey = securityKey;
  }

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
   
//  /**
//   * Add collections of trades to the position
//   * @param trades the trades that make up the position, not-null
//   */
//  public void addTrades(Collection<Trade> trades) {
//    ArgumentChecker.notNull(trades, "trades");
//    _trades.addAll(trades);
//  }
//  
//  /**
//   * Add a trade to the position
//   * @param trade the trade that make up the position, not-null
//   */
//  public void addTrade(Trade trade) {
//    ArgumentChecker.notNull(trade, "trade");
//    _trades.add(trade);
//  }
  
  /**
   * Gets the trades the makes up this position if available
   * @return the trades
   */
  @Override
  public Set<Trade> getTrades() {
    return _trades;
  }
  
 
  /**
   * Sets the _trades field.
   * @param trades  the trades
   */
  public void setTrades(Set<Trade> trades) {
    _trades = trades;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof PositionImpl) {
      PositionImpl other = (PositionImpl) obj;
      return CompareUtils.compareWithNull(_quantity, other._quantity) == 0 && ObjectUtils.equals(_securityKey, other._securityKey) && ObjectUtils.equals(_security, other._security)
          && ObjectUtils.equals(_trades, other._trades) && ObjectUtils.equals(_parentNode, other._parentNode);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hashCode = 65;
    hashCode += _quantity.hashCode();
    hashCode *= 31;
    hashCode += _securityKey.hashCode();
    hashCode *= 31;
    if (getSecurity() != null) {
      hashCode += _security.hashCode();
    }
    if (_trades != null) {
      hashCode *= 31;
      hashCode += _trades.hashCode();
    }
    hashCode *= 31;
    if (_parentNode != null) {
      hashCode += _parentNode.hashCode();
    }
    return hashCode;
  }

  @Override
  public String toString() {
    return new StrBuilder().append("Position[").append(getUniqueIdentifier()).append(", ").append(getQuantity()).append(' ').append(getSecurity() != null ? getSecurity() : getSecurityKey()).append(
        ']').toString();
  }
}
