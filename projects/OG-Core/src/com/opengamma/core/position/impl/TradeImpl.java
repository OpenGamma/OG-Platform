/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.time.Instant;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;

/**
 * A simple mutable implementation of {@link Trade Trade}
 */
public class TradeImpl implements Trade, MutableUniqueIdentifiable, Serializable {

  /** Serialization. */
  private static final long serialVersionUID = 1L;
  /**
   * The identifier of the parent position.
   */
  private UniqueIdentifier _position;
  /**
   * The identifier of the trade.
   */
  private UniqueIdentifier _identifier;
  /**
   * The identifier specifying the security.
   */
  private IdentifierBundle _securityKey;
  /**
   * The security.
   */
  private Security _security;
  /**
   * The amount of the position.
   */
  private BigDecimal _quantity;
  /**
   * The counter party
   */
  private Counterparty _counterparty;
  /**
   * Instant that trade happened
   */
  private Instant _tradeInstant;
  
  /**
   * Creates a trade from a position, counterparty, tradeinstant, and an amount.
   * 
   * @param position the parent position, not null
   * @param quantity  the amount of the trade, not null
   * @param counterparty the counterparty, not null
   * @param tradeInstant the trade instant, not null
   */
  public TradeImpl(Position position, BigDecimal quantity, Counterparty counterparty, Instant tradeInstant) {
    ArgumentChecker.notNull(position, "position");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(counterparty, "counterparty");
    ArgumentChecker.notNull(tradeInstant, "tradeInstant");
    _quantity = quantity;
    _counterparty = counterparty;
    _tradeInstant = tradeInstant;
    _position = position.getUniqueIdentifier();
    _securityKey = position.getSecurityKey();
    _security = position.getSecurity();
  }
  
  /**
   * Creates a trade from a positionId, an amount of a security identified by key, counterparty and tradeinstant.
   * 
   * @param positionUid the parent positionid, not null
   * @param securityKey  the security identifier, not null
   * @param quantity  the amount of the trade, not null
   * @param counterparty the counterparty, not null
   * @param tradeInstant the trade instant, not null
   */
  public TradeImpl(UniqueIdentifier positionUid, IdentifierBundle securityKey, BigDecimal quantity, Counterparty counterparty, Instant tradeInstant) {
    ArgumentChecker.notNull(positionUid, "position uid");
    ArgumentChecker.notNull(securityKey, "securityKey");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(counterparty, "counterparty");
    ArgumentChecker.notNull(tradeInstant, "tradeInstant");
    _quantity = quantity;
    _counterparty = counterparty;
    _tradeInstant = tradeInstant;
    _position = positionUid;
    _securityKey = securityKey;
    _security = null;
  }

  /**
   * Creates a trade from a positionId, an amount of a security, counterparty and tradeinstant.
   * 
   * @param positionUid the parent positionid, not null
   * @param security the security, not null
   * @param quantity the amount of the trade, not null
   * @param counterparty the counterparty, not null
   * @param tradeInstant the trade instant, not null
   */
  public TradeImpl(UniqueIdentifier positionUid, Security security, BigDecimal quantity, Counterparty counterparty, Instant tradeInstant) {
    ArgumentChecker.notNull(positionUid, "position uid");
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(counterparty, "counterparty");
    ArgumentChecker.notNull(tradeInstant, "tradeInstant");
    _quantity = quantity;
    _counterparty = counterparty;
    _tradeInstant = tradeInstant;
    _position = positionUid;
    _security = security;
    _securityKey = security.getIdentifiers();
  }
  
  /**
   * Construct a mutable trade copying data from another, possibly immutable, {@link Trade} implementation.
   * 
   * @param copyFrom instance to copy fields from, not null
   */
  public TradeImpl(final Trade copyFrom) {
    ArgumentChecker.notNull(copyFrom, "copyFrom");
    _identifier = copyFrom.getUniqueIdentifier();
    _quantity = copyFrom.getQuantity();
    _counterparty = copyFrom.getCounterparty();
    _tradeInstant = copyFrom.getTradeInstant();
    _position = copyFrom.getPosition();
    _securityKey = copyFrom.getSecurityKey();
    _security = copyFrom.getSecurity();
  }

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
  
  /**
   * Sets the key to the security being held.
   * @param securityKey  the security key, may be null
   */
  public void setSecurityKey(IdentifierBundle securityKey) {
    _securityKey = securityKey;
  }

  @Override
  public Counterparty getCounterparty() {
    return _counterparty;
  }

  @Override
  public Instant getTradeInstant() {
    return _tradeInstant;
  }

  @Override
  public Security getSecurity() {
    return _security;
  }
  
  @Override
  public UniqueIdentifier getUniqueIdentifier() {
    return _identifier;
  }

  /**
   * Sets the unique identifier of the trade.
   * @param identifier  the new identifier, not null
   */
  @Override
  public void setUniqueIdentifier(UniqueIdentifier identifier) {
    ArgumentChecker.notNull(identifier, "identifier");
    _identifier = identifier;
  }
  
  /**
   * Sets the security being held.
   * @param security  the security, may be null
   */
  public void setSecurity(Security security) {
    _security = security;
  }
  
  @Override
  public UniqueIdentifier getPosition() {
    return _position;
  }
  
  /**
   * Sets the parent position identifier.
   * @param positionUid  the position uid, not null
   */
  public void setPosition(UniqueIdentifier positionUid) {
    ArgumentChecker.notNull(positionUid, "position uid");
    _position = positionUid;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof TradeImpl) {
      TradeImpl other = (TradeImpl) obj;
      return (CompareUtils.compareWithNull(_quantity, other._quantity) == 0) && ObjectUtils.equals(_counterparty, other._counterparty)
          && ObjectUtils.equals(_tradeInstant, other._tradeInstant);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hashCode = 65;
    hashCode += _quantity.hashCode();
    return hashCode ^ ObjectUtils.hashCode(_counterparty) ^ ObjectUtils.hashCode(_tradeInstant);
  }
  
  @Override
  public String toString() {
    return new StrBuilder().append("Trade[").append(getUniqueIdentifier()).append(", ").append(getQuantity()).append(' ')
      .append(getSecurity() != null ? getSecurity() : getSecurityKey()).append(" PositionID: ").append(_position)
      .append(" Counterparty: ").append(_counterparty).append(" Trade Instant: ").append(_tradeInstant).append(']').toString();
  }
  
}
