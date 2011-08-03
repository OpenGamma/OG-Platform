/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.text.StrBuilder;

import com.google.common.collect.Maps;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
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
   * The link to the security.
   */
  private SecurityLink _securityLink;
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
   * The trade attributes
   */
  private Map<String, String> _attributes = Maps.newHashMap();

  /**
   * Creates a trade which must be initialized by calling methods.
   */
  public TradeImpl() {
    _securityLink = new SecurityLink();
  }

  /**
   * Creates a trade from a positionId, an amount of a security identified by key, counterparty and tradeinstant.
   * 
   * @param parentPositionId  the parent position id, not null
   * @param securityLink  the security identifier, not null
   * @param quantity  the amount of the trade, not null
   * @param counterparty  the counterparty, not null
   * @param tradeDate  the trade date, not null
   * @param tradeTime  the trade time with offset, may be null
   */
  public TradeImpl(UniqueIdentifier parentPositionId, SecurityLink securityLink, BigDecimal quantity, Counterparty counterparty, LocalDate tradeDate, OffsetTime tradeTime) {
    ArgumentChecker.notNull(parentPositionId, "parentPositionId");
    ArgumentChecker.notNull(securityLink, "securityLink");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(counterparty, "counterparty");
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    _quantity = quantity;
    _counterparty = counterparty;
    _tradeDate = tradeDate;
    _tradeTime = tradeTime;
    _parentPositionId = parentPositionId;
    _securityLink = securityLink;
  }

  /**
   * Creates a trade from a positionId, an amount of a security, counterparty and trade instant.
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
    _securityLink = SecurityLink.of(security);
  }

  /**
   * Creates a deep copy of the specified position.
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
    _securityLink = copyFrom.getSecurityLink().clone();
    setAttributes(copyFrom.getAttributes());
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
   * @return the parent position unique identifier, null if no parent
   */
  @Override
  public UniqueIdentifier getParentPositionId() {
    return _parentPositionId;
  }

  /**
   * Sets the parent position unique identifier.
   * 
   * @param parentPositionId  the parent position unique identifier, null if no parent
   */
  public void setParentPositionId(UniqueIdentifier parentPositionId) {
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
   * Gets the link to the security being held.
   * <p>
   * This allows the security to be referenced without actually loading the security itself.
   * 
   * @return the security link, not null
   */
  @Override
  public SecurityLink getSecurityLink() {
    return _securityLink;
  }

  /**
   * Sets the link to the security being held.
   * 
   * @param securityLink  the security link, not null
   */
  public void setSecurityLink(SecurityLink securityLink) {
    ArgumentChecker.notNull(securityLink, "securityLink");
    _securityLink = securityLink;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the target security from the link.
   * <p>
   * This convenience method gets the target security from the link.
   * This is guaranteed to return a security within an analytic function.
   * 
   * @return the security link, null if target not resolved in the link
   */
  @Override
  public Security getSecurity() {
    return _securityLink.getTarget();
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
  
  public void setPremium(final Double premium) {
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
  
  @Override
  public Map<String, String> getAttributes() {
    return Collections.unmodifiableMap(_attributes);
  }
  
  /**
   * Add a trade attribute
   * 
   * @param key attribute key, not null
   * @param value attribute value, not null
   */
  public void addAttribute(String key, String value) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(value, "value");
    _attributes.put(key, value);
  }
  
  /**
   * Add all attributes from a given Map
   *  
   * @param attributes the attributes map, not null
   */
  public void setAttributes(Map<String, String> attributes) {
    ArgumentChecker.notNull(attributes, "attributes");
    clearAttributes();
    for (Entry<String, String> entry : attributes.entrySet()) {
      addAttribute(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Remove all attributes
   */
  public void clearAttributes() {
    _attributes.clear();
  }

  /**
   * Removes an attribute with given key
   * 
   * @param key the attribute key to remove, not null
   */
  public void removeAttribute(final String key) {
    ArgumentChecker.notNull(key, "key");
    _attributes.remove(key);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof TradeImpl) {
      TradeImpl other = (TradeImpl) obj;
      return new EqualsBuilder()
        .append(getQuantity(), other.getQuantity())
        .append(getCounterparty(), other.getCounterparty())
        .append(getTradeDate(), other.getTradeDate())
        .append(getSecurityLink(), other.getSecurityLink())
        .append(getPremium(), other.getPremium())
        .append(getPremiumCurrency(), other.getPremiumCurrency())
        .append(getPremiumDate(), other.getPremiumDate())
        .append(getPremiumTime(), other.getPremiumTime())
        .append(getAttributes(), other.getAttributes())
        .isEquals();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append(getQuantity())
      .append(getCounterparty())
      .append(getTradeDate())
      .append(getSecurityLink())
      .append(getPremium())
      .append(getPremiumCurrency())
      .append(getPremiumCurrency())
      .append(getPremiumDate())
      .append(getPremiumTime())
      .append(getAttributes())
      .toHashCode();
  }

  @Override
  public String toString() {
    return new StrBuilder(256)
        .append("Trade[")
        .append(getUniqueId())
        .append(", ")
        .append(getQuantity())
        .append(' ')
        .append(getSecurityLink().getBest())
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
