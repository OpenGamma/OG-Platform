/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.db;

import java.math.BigDecimal;

import org.apache.commons.lang.ObjectUtils;

/**
 * A Hibernate bean for positions.
 */
public class PositionBean extends DateIdentifiableBean {

  private BigDecimal _quantity;
  private String _counterparty;
  private String _trader;

  /**
   * Creates an instance.
   */
  public PositionBean() {
  }

  /**
   * Creates an instance based on another.
   * @param other  the instance to copy, not null
   */
  public PositionBean(final PositionBean other) {
    super(other);
    setQuantity(other.getQuantity());
    setCounterparty(other.getCounterparty());
    setTrader(other.getTrader());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the quantity.
   * @return the quantity
   */
  public BigDecimal getQuantity() {
    return _quantity;
  }

  /**
   * Sets the quantity.
   * @param quantity  the quantity to set
   */
  public void setQuantity(BigDecimal quantity) {
    _quantity = quantity;
  }

  /**
   * Gets the counter party.
   * @return the counterParty
   */
  public String getCounterparty() {
    return _counterparty;
  }

  /**
   * Sets the counter party.
   * @param counterparty  the counterParty to set
   */
  public void setCounterparty(String counterparty) {
    _counterparty = counterparty;
  }

  /**
   * Gets the trader.
   * @return the trader
   */
  public String getTrader() {
    return _trader;
  }

  /**
   * Sets the trader.
   * @param trader  the trader to set
   */
  public void setTrader(String trader) {
    _trader = trader;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (super.equals(obj)) {
      final PositionBean other = (PositionBean) obj;
      return ObjectUtils.equals(getQuantity(), other.getQuantity())
          && ObjectUtils.equals(getCounterparty(), other.getCounterparty())
          && ObjectUtils.equals(getTrader(), other.getTrader());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hc = super.hashCode();
    hc = hc * 17 + ObjectUtils.hashCode(getQuantity());
    hc = hc * 17 + ObjectUtils.hashCode(getCounterparty());
    hc = hc * 17 + ObjectUtils.hashCode(getTrader());
    return hc;
  }

}
