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
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;

/**
 * A simple mutable implementation of {@link Trade Trade}
 */
public class TradeImpl implements Trade, Serializable {

  /** Serialization. */
  private static final long serialVersionUID = 1L;

  /**
   * The position a trade is part of.
   */
  private Position _position;
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
  
  public TradeImpl(Position position, BigDecimal quantity, Counterparty counterparty, Instant tradeInstant) {
    ArgumentChecker.notNull(position, "position");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(counterparty, "counterparty");
    ArgumentChecker.notNull(tradeInstant, "tradeInstant");
    
    _position = position;
    _quantity = quantity;
    _counterparty = counterparty;
    _tradeInstant = tradeInstant;
  }
  
  /**
   * Gets the position field.
   * @return the position
   */
  public Position getPosition() {
    return _position;
  }
  
  @Override
  public BigDecimal getQuantity() {
    return _quantity;
  }

  @Override
  public IdentifierBundle getSecurityKey() {
    return _position.getSecurityKey();
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
    return _position.getSecurity();
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
    return new StrBuilder().append("Trade[").append(getQuantity()).append(' ')
      .append(getSecurity() != null ? getSecurity() : getSecurityKey()).append(" PositionID: ").append(_position.getUniqueIdentifier())
      .append(" Counterparty: ").append(_counterparty).append(" Trade Instant: ").append(_tradeInstant).append(']').toString();
  }

}
