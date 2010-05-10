/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

import com.opengamma.financial.Currency;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.time.Expiry;

/**
 * A security traded on an exchange.
 */
public abstract class ExchangeTradedOptionSecurity extends OptionSecurity {

  /**
   * The exchange that the security is traded on.
   */
  private final String _exchange;

  public ExchangeTradedOptionSecurity(final String securityType, final OptionType optionType,
      final double strike, final Expiry expiry, final UniqueIdentifier underlyingIdentifier,
      final Currency currency, final String exchange) {
    super(securityType, optionType, strike, expiry, underlyingIdentifier, currency);
    _exchange = exchange;
  }

  /**
   * Gets the exchange that the security is traded on.
   * @return the exchange
   */
  public String getExchange() {
    return _exchange;
  }

  //-------------------------------------------------------------------------
  public abstract <T> T accept (ExchangeTradedOptionSecurityVisitor<T> visitor);

  @Override
  public final <T> T accept (OptionSecurityVisitor<T> visitor) {
    return accept ((ExchangeTradedOptionSecurityVisitor<T>)visitor);
  }

}
