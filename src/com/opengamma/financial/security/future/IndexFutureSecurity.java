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
 * An index future.
 */
public class IndexFutureSecurity extends FutureSecurity {

  /**
   * 
   */
  protected static final String UNDERLYINGIDENTIFIER_KEY = "underlyingIdentifier";

  /** The underlying identifier. */
  private final Identifier _underlyingIdentifier;

  /**
   * Creates an index future.
   * @param expiry  the expiry of the future
   * @param tradingExchange  the exchange that the future is trading on
   * @param settlementExchange  the exchange where the future is settled
   * @param currency  the currency
   * @param underlyingIdentifier  the underlying identifier
   */
  public IndexFutureSecurity(
      final Expiry expiry, final String tradingExchange, final String settlementExchange,
      final Currency currency, final Identifier underlyingIdentifier) {
    super(expiry, tradingExchange, settlementExchange, currency);
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
  public <T> T accept(final FutureSecurityVisitor<T> visitor) {
    return visitor.visitIndexFutureSecurity(this);
  }

}
