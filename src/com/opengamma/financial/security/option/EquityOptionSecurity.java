/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

import com.opengamma.financial.Currency;
import com.opengamma.id.Identifier;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 */
public abstract class EquityOptionSecurity extends ExchangeTradedOptionSecurity {
  public static final String EQUITY_OPTION_TYPE = "EQUITY_OPTION";

  // TODO: jim 23-Sep-2009 -- Add support for regions/countries

  public EquityOptionSecurity(final OptionType optionType, final double strike, final Expiry expiry, final Identifier underlyingIdentityKey, final Currency currency,
      final double pointValue, final String exchange) {
    super(optionType, strike, expiry, underlyingIdentityKey, currency, pointValue, exchange);
    setSecurityType(EQUITY_OPTION_TYPE);
  }

  public abstract <T> T accept(EquityOptionSecurityVisitor<T> visitor);

  @Override
  public final <T> T accept(final ExchangeTradedOptionSecurityVisitor<T> visitor) {
    return accept((EquityOptionSecurityVisitor<T>) visitor);
  }
}
