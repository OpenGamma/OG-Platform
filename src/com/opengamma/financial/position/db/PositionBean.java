/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.db;

import java.math.BigDecimal;

import org.apache.commons.lang.ObjectUtils;

/**
 * 
 * @author Andrew Griffin
 */
public class PositionBean extends DateIdentifiableBean {
  
  private BigDecimal _quantity;
  private String _counterparty;
  private String _trader;

  public PositionBean () {
  }
  
  public PositionBean (final PositionBean other) {
    super (other);
    setQuantity (other.getQuantity ());
    setCounterparty (other.getCounterparty ());
    setTrader (other.getTrader ());
  }

  /**
   * @return the quantity
   */
  public BigDecimal getQuantity() {
    return _quantity;
  }

  /**
   * @param quantity the quantity to set
   */
  public void setQuantity(BigDecimal quantity) {
    _quantity = quantity;
  }

  /**
   * @return the counterParty
   */
  public String getCounterparty() {
    return _counterparty;
  }

  /**
   * @param counterparty the counterParty to set
   */
  public void setCounterparty(String counterparty) {
    _counterparty = counterparty;
  }

  /**
   * @return the trader
   */
  public String getTrader() {
    return _trader;
  }

  /**
   * @param trader the trader to set
   */
  public void setTrader(String trader) {
    _trader = trader;
  }
  
  @Override
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!super.equals (o)) return false;
    final PositionBean other = (PositionBean)o;
    return ObjectUtils.equals (getQuantity (), other.getQuantity ()) && ObjectUtils.equals (getCounterparty (), other.getCounterparty ()) && ObjectUtils.equals (getTrader (), other.getTrader ());
  }
  
  @Override
  public int hashCode () {
    int hc = super.hashCode ();
    hc = hc * 17 + ObjectUtils.hashCode (getQuantity ());
    hc = hc * 17 + ObjectUtils.hashCode (getCounterparty ());
    hc = hc * 17 + ObjectUtils.hashCode (getTrader ());
    return hc;
  }
  
}