/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.id.Identifier;
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
  private UniqueIdentifier _positionId;
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
   * The trade date.
   */
  private LocalDate _tradeDate;
  /**
   * The trade time with timezone if available
   */
  private OffsetTime _tradeTime;

  /**
   * Creates a trade which must be initialized by calling methods.
   */
  public TradeImpl() {
  }

  /**
   * Creates a trade from a position, counterparty, tradeinstant, and an amount.
   * 
   * @param positionUid the parent positionid, not null
   * @param securityKey  the security identifier, not null
   * @param quantity  the amount of the trade, not null
   * @param counterparty the counterparty, not null
   * @param tradeDate the trade date, not null
   * @param tradeTime the trade time with timezone, may be null
   */
  public TradeImpl(UniqueIdentifier positionUid, Identifier securityKey, BigDecimal quantity, Counterparty counterparty, LocalDate tradeDate, OffsetTime tradeTime) {
    ArgumentChecker.notNull(positionUid, "position uid");
    ArgumentChecker.notNull(securityKey, "security key");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(counterparty, "counterparty");
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    _quantity = quantity;
    _counterparty = counterparty;
    _tradeDate = tradeDate;
    _tradeTime = tradeTime;
    _positionId = positionUid;
    _securityKey = IdentifierBundle.of(securityKey);
    _security = null;
  }

  /**
   * Creates a trade from a positionId, an amount of a security identified by key, counterparty and tradeinstant.
   * 
   * @param positionUid the parent positionid, not null
   * @param securityKey  the security identifier, not null
   * @param quantity  the amount of the trade, not null
   * @param counterparty the counterparty, not null
   * @param tradeDate the trade date, not null
   * @param tradeTime the trade time with timezone, may be null
   */
  public TradeImpl(UniqueIdentifier positionUid, IdentifierBundle securityKey, BigDecimal quantity, Counterparty counterparty, LocalDate tradeDate, OffsetTime tradeTime) {
    ArgumentChecker.notNull(positionUid, "position uid");
    ArgumentChecker.notNull(securityKey, "securityKey");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(counterparty, "counterparty");
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    _quantity = quantity;
    _counterparty = counterparty;
    _tradeDate = tradeDate;
    _tradeTime = tradeTime;
    _positionId = positionUid;
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
   * @param tradeDate the trade date, not null
   * @param tradeTime the trade time with timezone, may be null
   */
  public TradeImpl(UniqueIdentifier positionUid, Security security, BigDecimal quantity, Counterparty counterparty, LocalDate tradeDate, OffsetTime tradeTime) {
    ArgumentChecker.notNull(positionUid, "position uid");
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(counterparty, "counterparty");
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    _quantity = quantity;
    _counterparty = counterparty;
    _tradeDate = tradeDate;
    _tradeTime = tradeTime;
    _positionId = positionUid;
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
    _identifier = copyFrom.getUniqueId();
    _quantity = copyFrom.getQuantity();
    _counterparty = copyFrom.getCounterparty();
    _tradeDate = copyFrom.getTradeDate();
    _tradeTime = copyFrom.getTradeTime();
    _positionId = copyFrom.getPositionId();
    _securityKey = copyFrom.getSecurityKey();
    _security = copyFrom.getSecurity();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the unique identifier of the trade.
   * @return the identifier, not null
   */
  @Override
  public UniqueIdentifier getUniqueId() {
    return _identifier;
  }

  /**
   * Sets the unique identifier of the trade.
   * @param identifier  the new identifier, not null
   */
  public void setUniqueId(UniqueIdentifier identifier) {
    ArgumentChecker.notNull(identifier, "identifier");
    _identifier = identifier;
  }

  /**
   * Gets the parent position identifier.
   * @return the parent position, not null
   */
  @Override
  public UniqueIdentifier getPositionId() {
    return _positionId;
  }

  /**
   * Sets the parent position identifier.
   * @param positionUid  the position uid, not null
   */
  public void setPositionId(UniqueIdentifier positionUid) {
    ArgumentChecker.notNull(positionUid, "position uid");
    _positionId = positionUid;
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
   * Adds an identifier to the security key.
   * @param securityKeyIdentifier  the identifier to add, not null
   */
  public void addSecurityKey(final Identifier securityKeyIdentifier) {
    ArgumentChecker.notNull(securityKeyIdentifier, "securityKeyIdentifier");
    setSecurityKey(getSecurityKey().withIdentifier(securityKeyIdentifier));
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

  @Override
  public Counterparty getCounterparty() {
    return _counterparty;
  }

  /**
   * Sets the counterparty.
   * @param counterparty  the counterparty, may be null
   */
  public void setCounterparty(Counterparty counterparty) {
    _counterparty = counterparty;
  }

  @Override
  public LocalDate getTradeDate() {
    return _tradeDate;
  }

  /**
   * Sets the trade date.
   * @param tradeDate  the trade date, may be null
   */
  public void setTradeDate(LocalDate tradeDate) {
    _tradeDate = tradeDate;
  }

  @Override
  public OffsetTime getTradeTime() {
    return _tradeTime;
  }

  /**
   * Sets the trade time.
   * @param tradeTime  the trade time, may be null
   */
  public void setTradeTime(OffsetTime tradeTime) {
    _tradeTime = tradeTime;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof TradeImpl) {
      TradeImpl other = (TradeImpl) obj;
      return (CompareUtils.compareWithNull(_quantity, other._quantity) == 0) && ObjectUtils.equals(_counterparty, other._counterparty)
          && ObjectUtils.equals(_tradeDate, other._tradeDate) && ObjectUtils.equals(_tradeTime, _tradeTime) 
          && ObjectUtils.equals(_securityKey, other._securityKey) && ObjectUtils.equals(_security, other._security);
      
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hashCode = 65;
    hashCode += _quantity.hashCode();
    hashCode += hashCode ^ ObjectUtils.hashCode(_counterparty) ^ ObjectUtils.hashCode(_tradeDate) ^ ObjectUtils.hashCode(_tradeTime);
    if (getSecurity() != null) {
      hashCode *= 31;
      hashCode += _security.hashCode();
    }
    hashCode *= 31;
    hashCode += ObjectUtils.hashCode(_securityKey);
    return hashCode;
  }
  
  @Override
  public String toString() {
    return new StrBuilder().append("Trade[").append(getUniqueId()).append(", ").append(getQuantity()).append(' ')
      .append(getSecurity() != null ? getSecurity() : getSecurityKey()).append(" PositionID:").append(_positionId)
      .append(" ").append(_counterparty).append(" ").append(_tradeDate).append(" ").append(_tradeTime).append(']').toString();
  }
  
}
