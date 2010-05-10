/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

import com.opengamma.financial.Currency;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.time.Expiry;

/**
 * An equity option security.
 */
public abstract class EquityOptionSecurity extends ExchangeTradedOptionSecurity {

  /**
   * The security type.
   */
  public static final String EQUITY_OPTION_TYPE = "EQUITY_OPTION";

  // TODO: jim 23-Sep-2009 -- Add support for regions/countries

  /**
   * Creates the security.
   * @param optionType
   * @param strike
   * @param expiry
   * @param underlyingIdentifier
   * @param currency
   * @param exchange
   */
  public EquityOptionSecurity(final OptionType optionType,
      final double strike, final Expiry expiry,
      final UniqueIdentifier underlyingIdentifier, final Currency currency, final String exchange) {
    super(EQUITY_OPTION_TYPE, optionType, strike, expiry, underlyingIdentifier, currency, exchange);
  }

  //-------------------------------------------------------------------------
  public abstract <T> T accept(EquityOptionSecurityVisitor<T> visitor);

  @Override
  public final <T> T accept(final ExchangeTradedOptionSecurityVisitor<T> visitor) {
    return accept((EquityOptionSecurityVisitor<T>) visitor);
  }

}
