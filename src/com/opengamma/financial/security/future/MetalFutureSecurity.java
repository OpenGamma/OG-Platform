/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.future;

import com.opengamma.financial.Currency;
import com.opengamma.id.Identifier;
import com.opengamma.util.time.Expiry;

/**
 * A metal commodity future.
 */
public class MetalFutureSecurity extends CommodityFutureSecurity {

  /**
   *
   */
  protected static final String UNDERLYINGIDENTIFIER_KEY = "underlyingIdentifier";

  /** The underlying identifier. */
  private Identifier _underlyingIdentifier;

  /**
   * Creates a metal commodity future.
   * @param expiry  the expiry of the future
   * @param tradingExchange  the exchange that the future is trading on
   * @param settlementExchange  the exchange where the future is settled
   * @param currency  the currency
   * @param type  the type
   * @param unitNumber  the unit number
   * @param unitName  the unit name
   * @param underlyingIdentifier  the underlying identifier
   */
  public MetalFutureSecurity(
      final Expiry expiry, final String tradingExchange, final String settlementExchange,
      final Currency currency, final String type, final Double unitNumber, final String unitName, final Identifier underlyingIdentifier) {
    super(expiry, tradingExchange, settlementExchange, currency, type, unitNumber, unitName);
    _underlyingIdentifier = underlyingIdentifier;
  }

  /**
   * Creates a metal commodity future with no amount.
   * @param expiry  the expiry of the future
   * @param tradingExchange  the exchange that the future is trading on
   * @param settlementExchange  the exchange where the future is settled
   * @param currency  the currency
   * @param type  the type
   * @param underlyingIdentifier  the underlying identifier
   */
  public MetalFutureSecurity(
      final Expiry expiry, final String tradingExchange, final String settlementExchange,
      final Currency currency, final String type, final Identifier underlyingIdentifier) {
    super(expiry, tradingExchange, settlementExchange, currency, type);
    _underlyingIdentifier = underlyingIdentifier;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying identifier.
   * @return the identifier
   */
  public Identifier getUnderlyingIdentityKey() {
    return _underlyingIdentifier;
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(FutureSecurityVisitor<T> visitor) {
    return visitor.visitMetalFutureSecurity(this);
  }

}
