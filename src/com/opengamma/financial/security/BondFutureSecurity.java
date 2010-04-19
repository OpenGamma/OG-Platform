/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Set;

import com.opengamma.financial.Currency;
import com.opengamma.id.Identifier;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author Andrew
 */
public class BondFutureSecurity extends FutureSecurity {
  
  private final Set<Identifier> _basket;
  private final String _type;
  
  /**
   * @param expiry
   * @param tradingExchange
   * @param settlementExchange
   */
  public BondFutureSecurity(Expiry expiry, String tradingExchange, String settlementExchange, Currency currency, String type, Set<Identifier> basket) {
    super(expiry, tradingExchange, settlementExchange, currency);
    _basket = basket;
    _type = type;
  }
  
  public Set<Identifier> getBasket () {
    return _basket;
  }
  
  public String getBondType () {
    return _type;
  }

  @Override
  public <T> T accept(FutureSecurityVisitor<T> visitor) {
    return visitor.visitBondFutureSecurity (this);
  }

}