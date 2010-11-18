/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.time.Instant;

import org.apache.commons.lang.text.StrBuilder;

import com.opengamma.core.security.Security;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple mutable implementation of {@link Trade Trade}
 */
public class TradeImpl implements Trade, Serializable {
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
  public String toString() {
    return new StrBuilder().append("Trade[").append(getQuantity()).append(' ')
      .append(getSecurity() != null ? getSecurity() : getSecurityKey()).append(" PositionID: ").append(_position.getUniqueIdentifier()).append(']').toString();
  }

}
