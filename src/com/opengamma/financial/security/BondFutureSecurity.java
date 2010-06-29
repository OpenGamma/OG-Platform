/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Set;

import com.opengamma.financial.Currency;
import com.opengamma.util.time.Expiry;

/**
 * A bond future.
 */
public class BondFutureSecurity extends FutureSecurity {

  /** The basket of bonds that could be delivered to satisfy the contract. */
  private final Set<BondFutureDeliverable> _basket;
  /** The type, such as Bund, Long Bond. */
  private final String _type;

  /**
   * Creates a bond future.
   * @param expiry  the expiry date/time of the future
   * @param tradingExchange  the exchange that the future is traded on
   * @param settlementExchange  the exchange that the future is settled on
   * @param currency  the currency of the contract
   * @param type  the type, such as Bund, Long Bond
   * @param basket  the basket of bonds that could be delivered to satisfy the contract
   */
  public BondFutureSecurity(Expiry expiry, String tradingExchange, String settlementExchange, Currency currency, 
                            String type, Set<BondFutureDeliverable> basket) {
    super(expiry, tradingExchange, settlementExchange, currency);
    _basket = basket;
    _type = type;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the basket of bonds that could be delivered to satisfy the contract.
   * @return the basket of bonds
   */
  public Set<BondFutureDeliverable> getBasket() {
    return _basket;
  }

  /**
   * Gets the bond type, such as Bund, Long Bond.
   * @return the bond type
   */
  public String getBondType() {
    return _type;
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(FutureSecurityVisitor<T> visitor) {
    return visitor.visitBondFutureSecurity(this);
  }

}
