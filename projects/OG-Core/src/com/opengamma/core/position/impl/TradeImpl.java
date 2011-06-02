/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.util.money.Currency;

/**
 * A simple mutable implementation of {@code Trade}.
 */
public class TradeImpl implements Trade, MutableUniqueIdentifiable, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The unique identifier of the trade.
   */
  private UniqueIdentifier _uniqueId;
  /**
   * The unique identifier of the parent position.
   */
  private UniqueIdentifier _parentPositionId;
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
   * The counterparty.
   */
  private Counterparty _counterparty;
  /**
   * The trade date.
   */
  private LocalDate _tradeDate;
  /**
   * The trade time with offset.
   */
  private OffsetTime _tradeTime;
  /**
   * Amount paid for trade at time of purchase
   */
  private Double _premium;
  /**
   * Currency of payment at time of purchase
   */
  private Currency _premiumCurrency;
  /**
   * Date of premium payment
   */
  private LocalDate _premiumDate;
  /**
   * Time of premium payment
   */
  private OffsetTime _premiumTime;
  

  /**
   * Creates a trade which must be initialized by calling methods.
   */
  public TradeImpl() {
  }

  /**
   * Creates a trade from a position, counterparty, trade instant, and an amount.
   * 
   * @param parentPositionId  the parent position id, not null
   * @param securityKey  the security identifier, not null
   * @param quantity  the amount of the trade, not null
   * @param counterparty  the counterparty, not null
   * @param tradeDate  the trade date, not null
   * @param tradeTime  the trade time with offset, may be null
   */
  public TradeImpl(UniqueIdentifier parentPositionId, Identifier securityKey, BigDecimal quantity, Counterparty counterparty, LocalDate tradeDate, OffsetTime tradeTime) {
    ArgumentChecker.notNull(parentPositionId, "parentPositionId");
    ArgumentChecker.notNull(securityKey, "securityKey");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(counterparty, "counterparty");
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    _quantity = quantity;
    _counterparty = counterparty;
    _tradeDate = tradeDate;
    _tradeTime = tradeTime;
    _parentPositionId = parentPositionId;
    _securityKey = IdentifierBundle.of(securityKey);
    _security = null;
  }

  /**
   * Creates a trade from a positionId, an amount of a security identified by key, counterparty and tradeinstant.
   * 
   * @param parentPositionId  the parent position id, not null
   * @param securityKey  the security identifier, not null
   * @param quantity  the amount of the trade, not null
   * @param counterparty  the counterparty, not null
   * @param tradeDate  the trade date, not null
   * @param tradeTime  the trade time with offset, may be null
   */
  public TradeImpl(UniqueIdentifier parentPositionId, IdentifierBundle securityKey, BigDecimal quantity, Counterparty counterparty, LocalDate tradeDate, OffsetTime tradeTime) {
    ArgumentChecker.notNull(parentPositionId, "parentPositionId");
    ArgumentChecker.notNull(securityKey, "securityKey");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(counterparty, "counterparty");
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    _quantity = quantity;
    _counterparty = counterparty;
    _tradeDate = tradeDate;
    _tradeTime = tradeTime;
    _parentPositionId = parentPositionId;
    _securityKey = securityKey;
    _security = null;
  }

  /**
   * Creates a trade from a positionId, an amount of a security, counterparty and tradeinstant.
   * 
   * @param parentPositionId  the parent position id, not null
   * @param security  the security, not null
   * @param quantity  the amount of the trade, not null
   * @param counterparty  the counterparty, not null
   * @param tradeDate  the trade date, not null
   * @param tradeTime  the trade time with offset, may be null
   */
  public TradeImpl(UniqueIdentifier parentPositionId, Security security, BigDecimal quantity, Counterparty counterparty, LocalDate tradeDate, OffsetTime tradeTime) {
    ArgumentChecker.notNull(parentPositionId, "parentPositionId");
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(counterparty, "counterparty");
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    _quantity = quantity;
    _counterparty = counterparty;
    _tradeDate = tradeDate;
    _tradeTime = tradeTime;
    _parentPositionId = parentPositionId;
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
    _uniqueId = copyFrom.getUniqueId();
    _quantity = copyFrom.getQuantity();
    _counterparty = copyFrom.getCounterparty();
    _tradeDate = copyFrom.getTradeDate();
    _tradeTime = copyFrom.getTradeTime();
    _parentPositionId = copyFrom.getParentPositionId();
    _securityKey = copyFrom.getSecurityKey();
    _security = copyFrom.getSecurity();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the unique identifier of the trade.
   * 
   * @return the identifier, not null
   */
  @Override
  public UniqueIdentifier getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of the trade.
   * 
   * @param identifier  the new identifier, not null
   */
  public void setUniqueId(UniqueIdentifier identifier) {
    ArgumentChecker.notNull(identifier, "identifier");
    _uniqueId = identifier;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the parent position unique identifier.
   * 
   * @return the parent position unique identifier, not null
   */
  @Override
  public UniqueIdentifier getParentPositionId() {
    return _parentPositionId;
  }

  /**
   * Sets the parent position unique identifier.
   * 
   * @param parentPositionId  the parent position unique identifier, not null
   */
  public void setParentPositionId(UniqueIdentifier parentPositionId) {
    ArgumentChecker.notNull(parentPositionId, "parentPositionId");
    _parentPositionId = parentPositionId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the amount of the position held in terms of the security.
   * 
   * @return the amount of the position, not null
   */
  @Override
  public BigDecimal getQuantity() {
    return _quantity;
  }

  /**
   * Sets the amount of the position held in terms of the security.
   * 
   * @param quantity  the amount of the position, not null
   */
  public void setQuantity(BigDecimal quantity) {
    ArgumentChecker.notNull(quantity, "quantity");
    _quantity = quantity;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a key to the security being held.
   * <p>
   * This allows the security to be referenced without actually loading the security itself.
   * 
   * @return the security key
   */
  @Override
  public IdentifierBundle getSecurityKey() {
    return _securityKey;
  }

  /**
   * Sets the key to the security being held.
   * 
   * @param securityKey  the security key, may be null
   */
  public void setSecurityKey(IdentifierBundle securityKey) {
    _securityKey = securityKey;
  }

  /**
   * Adds an identifier to the security key.
   * 
   * @param securityKeyIdentifier  the identifier to add, not null
   */
  public void addSecurityKey(final Identifier securityKeyIdentifier) {
    ArgumentChecker.notNull(securityKeyIdentifier, "securityKeyIdentifier");
    setSecurityKey(getSecurityKey().withIdentifier(securityKeyIdentifier));
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security being held, returning {@code null} if it has not been loaded.
   * <p>
   * This method is guaranteed to return a security within an analytic function.
   * 
   * @return the security
   */
  @Override
  public Security getSecurity() {
    return _security;
  }

  /**
   * Sets the security being held.
   * 
   * @param security  the security, may be null
   */
  public void setSecurity(Security security) {
    _security = security;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the counterparty.
   * 
   * @return the counterparty, may be null
   */
  @Override
  public Counterparty getCounterparty() {
    return _counterparty;
  }

  /**
   * Sets the counterparty.
   * 
   * @param counterparty  the counterparty, may be null
   */
  public void setCounterparty(Counterparty counterparty) {
    _counterparty = counterparty;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the trade date.
   * 
   * @return the trade date, may be null
   */
  @Override
  public LocalDate getTradeDate() {
    return _tradeDate;
  }

  /**
   * Sets the trade date.
   * 
   * @param tradeDate  the trade date, may be null
   */
  public void setTradeDate(LocalDate tradeDate) {
    _tradeDate = tradeDate;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the trade time.
   * 
   * @return the trade time, may be null
   */
  @Override
  public OffsetTime getTradeTime() {
    return _tradeTime;
  }

  /**
   * Sets the trade time.
   * 
   * @param tradeTime  the trade time, may be null
   */
  public void setTradeTime(OffsetTime tradeTime) {
    _tradeTime = tradeTime;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public Double getPremium() {
    return _premium;
  }
  
  public void setPremuim(final Double premium) {
    _premium = premium;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public Currency getPremiumCurrency() {
    return _premiumCurrency;
  }
  
  public void setPremiumCurrency(Currency premiumCurrency) {
    _premiumCurrency = premiumCurrency;
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDate getPremiumDate() {
    return _premiumDate;
  }
  
  public void setPremiumDate(LocalDate premiumDate) {
    _premiumDate = premiumDate;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public OffsetTime getPremiumTime() {
    return _premiumTime;
  }
  
  public void setPremiumTime(OffsetTime premiumTime) {
    _premiumTime = premiumTime;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof TradeImpl) {
      TradeImpl other = (TradeImpl) obj;
      return CompareUtils.compareWithNull(getQuantity(), other.getQuantity()) == 0 &&
          ObjectUtils.equals(getCounterparty(), other.getCounterparty()) &&
          ObjectUtils.equals(getTradeDate(), other.getTradeDate()) &&
          ObjectUtils.equals(getTradeTime(), other.getTradeTime()) &&
          ObjectUtils.equals(getSecurityKey(), other.getSecurityKey()) &&
          ObjectUtils.equals(getSecurity(), other.getSecurity()) &&
          ObjectUtils.equals(getPremium(), other.getPremium()) && 
          ObjectUtils.equals(getPremiumCurrency(), other.getPremiumCurrency()) &&
          ObjectUtils.equals(getPremiumDate(), other.getPremiumDate()) &&
          ObjectUtils.equals(getPremiumTime(), other.getPremiumTime());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hashCode = 65;
    hashCode += getQuantity().hashCode();
    hashCode += hashCode ^ ObjectUtils.hashCode(getCounterparty()) ^ ObjectUtils.hashCode(getTradeDate()) ^ ObjectUtils.hashCode(getTradeTime());
    if (getSecurity() != null) {
      hashCode *= 31;
      hashCode += getSecurity().hashCode();
    }
    hashCode *= 31;
    hashCode += ObjectUtils.hashCode(getSecurityKey());
    return hashCode;
  }

  @Override
  public String toString() {
    return new StrBuilder(256)
        .append("Trade[")
        .append(getUniqueId())
        .append(", ")
        .append(getQuantity())
        .append(' ')
        .append(getSecurity() != null ? getSecurity() : getSecurityKey())
        .append(" PositionID:")
        .append(getParentPositionId())
        .append(" ")
        .append(getCounterparty())
        .append(" ")
        .append(getTradeDate())
        .append(" ")
        .append(getTradeTime())
        .append(']')
        .toString();
  }
  
}
