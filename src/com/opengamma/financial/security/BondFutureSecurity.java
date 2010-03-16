/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Set;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author Andrew
 */
public class BondFutureSecurity extends FutureSecurity {
  
  private final Set<DomainSpecificIdentifier> _basket;
  private final String _type;
  
  /**
   * @param expiry
   * @param tradingExchange
   * @param settlementExchange
   */
  public BondFutureSecurity(Expiry expiry, String tradingExchange, String settlementExchange, String type, Set<DomainSpecificIdentifier> basket) {
    super(expiry, tradingExchange, settlementExchange);
    _basket = basket;
    _type = type;
  }
  
  public Set<DomainSpecificIdentifier> getBasket () {
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