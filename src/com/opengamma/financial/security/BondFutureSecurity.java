/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Set;

import com.opengamma.financial.Currency;
import com.opengamma.util.time.Expiry;

/**
 * A bond future
 */
public class BondFutureSecurity extends FutureSecurity {
  
  private final Set<BondFutureDeliverable> _basket;
  private final String _type;
  
  /**
   * @param expiry the expiry date/time of the future
   * @param tradingExchange the exchange that the future is traded on
   * @param settlementExchange the exchange that the future is settled on
   * @param currency the currency of the contract
   * @param type the type e.g. Bund, Long Bond, etc.
   * @param basket the basket of bonds that could be delivered to satisfy the contract
   */
  public BondFutureSecurity(Expiry expiry, String tradingExchange, String settlementExchange, Currency currency, 
                            String type, Set<BondFutureDeliverable> basket) {
    super(expiry, tradingExchange, settlementExchange, currency);
    _basket = basket;
    _type = type;
  }
  
  public Set<BondFutureDeliverable> getBasket() {
    return _basket;
  }
  
  public String getBondType() {
    return _type;
  }

  @Override
  public <T> T accept(FutureSecurityVisitor<T> visitor) {
    return visitor.visitBondFutureSecurity(this);
  }

}
